package com.keriteal.awesomeChestShop.shop;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.ProtocolManager;
import com.comphenix.protocol.events.PacketContainer;
import com.keriteal.awesomeChestShop.*;
import com.keriteal.awesomeChestShop.utils.BlockUtils;
import com.keriteal.awesomeChestShop.utils.EntityUtils;
import com.keriteal.awesomeChestShop.utils.ItemUtils;
import com.keriteal.awesomeChestShop.utils.ShopUtils;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.event.HoverEvent;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextColor;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.minimessage.tag.resolver.Formatter;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;
import org.apache.commons.lang3.ArrayUtils;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.block.*;
import org.bukkit.block.data.Directional;
import org.bukkit.block.sign.Side;
import org.bukkit.block.sign.SignSide;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Arrays;
import java.util.Collections;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;

public final class ChestShop implements InventoryHolder {
    private static final AtomicInteger shopEntityId = new AtomicInteger(Integer.MAX_VALUE);
    private static final MiniMessage miniMessage = MiniMessage.miniMessage();
    private static final ItemStack GOTCHA_ITEM;

    static {
        GOTCHA_ITEM = ItemUtils.createGotchaItem();
    }

    @NotNull
    private final Location chestBlockLocation;
    @NotNull
    private final BlockFace signFace;
    @NotNull
    private final UUID playerId;
    @NotNull
    private ItemStack itemStack;
    @NotNull
    private final UUID shopUuid;
    @Nullable
    private ShopType shopType;
    private double price;
    private boolean shopProtected = true;
    private boolean created;
    private final int shopItemEntityId = shopEntityId.getAndDecrement();

    private ChestShop(@NotNull UUID shopUuid,
                      @NotNull Location chestBlockLocation,
                      @NotNull BlockFace signFace,
                      @NotNull UUID playerId,
                      @NotNull ShopType shopType,
                      @NotNull ItemStack itemStack,
                      double price,
                      boolean created) {
        this.shopUuid = shopUuid;
        this.chestBlockLocation = chestBlockLocation;
        this.signFace = signFace;
        this.playerId = playerId;
        this.price = price;
        this.itemStack = itemStack.clone();
        this.created = created;
        this.shopType = shopType;
    }

    public ChestShop(Location chestBlockLocation, BlockFace signFace, UUID playerId, ItemStack itemStack) {
        this(UUID.randomUUID(), chestBlockLocation, signFace, playerId, ShopType.SALE_MODE, itemStack, Double.MAX_VALUE, false);
    }

    public void updateWorld() {
        if (!Bukkit.isPrimaryThread()) throw new IllegalStateException("updateWorld must be called on main thread");
        Block containerBlock = getChestBlockLocation().getBlock();
        Block signBlock = containerBlock.getRelative(signFace);

        signBlock.setType(Material.OAK_WALL_SIGN);
        Container container = (Container) containerBlock.getState();
        Sign sign = (Sign) signBlock.getState();

        Directional signData = (Directional) sign.getBlockData();
        signData.setFacing(signFace);
        sign.setBlockData(signData);
        sign.setWaxed(true);

        SignSide signSide = sign.getSide(Side.FRONT);

        // Line 1: Shop owner
        OfflinePlayer owner = Bukkit.getServer().getOfflinePlayer(getOwnerId());
        signSide.line(0, Component.text(Optional.ofNullable(owner.getName()).orElse("Unknown"), getShopType().getTextColor()));

        // Line 2: Sold item
        signSide.line(1, ItemUtils.getItemName(getItemStack()));

        // Line 3: Price
        String priceTemplate = "<color:#EE578C>" + AwesomeChestShop.getEconomy().currencyNamePlural() + " </color><color:#F3C64B><price:'#.0'></color>";
        signSide.line(2, miniMessage.deserialize(priceTemplate, Formatter.number("price", price)));

        // Line 4: Amount
        final Component numberComponent;
        long amount = getStock();
        if (amount == 0) {
            numberComponent = Component.text(amount, NamedTextColor.RED);
        } else {
            numberComponent = Component.text(amount, NamedTextColor.GREEN);
        }

        switch (getShopType()) {
            case SALE_MODE, GOTCHA_MODE -> signSide.line(3, Messages.TEMPLATE_AMOUNT_STOCK.append(numberComponent));
            case BUY_MODE -> signSide.line(3, Messages.TEMPLATE_AMOUNT_FREE_SPACE.append(numberComponent));
        }

        // Prepare to save data
        PersistentDataContainer signDataContainer = sign.getPersistentDataContainer();
        PersistentDataContainer containerDataContainer = container.getPersistentDataContainer();

        // Save data to main chest
        NamespacedKeys.SHOP_ID.setValueTo(containerDataContainer, this.getShopUuid());
        NamespacedKeys.SIGN_ATTACHED_FACE.setValueTo(containerDataContainer, this.getSignFace().ordinal());

        // Save data to shop sign
        NamespacedKeys.SHOP_OWNER_ID.setValueTo(signDataContainer, this.getOwnerId());
        NamespacedKeys.SHOP_ID.setValueTo(signDataContainer, this.getShopUuid());
        NamespacedKeys.SHOP_ITEM.setValueTo(signDataContainer, this.getItemStack());
        NamespacedKeys.SHOP_TYPE.setValueTo(signDataContainer, this.getShopType().ordinal());
        NamespacedKeys.SHOP_PRICE.setValueTo(signDataContainer, this.getPrice());

        // Save shop location to chunk
        final PersistentDataContainer chunkContainer = chestBlockLocation.getChunk().getPersistentDataContainer();
        if (chunkContainer.has(AwesomeChestShop.getKeys().shopListKey)) {
            int[] shopLists = chunkContainer.getOrDefault(AwesomeChestShop.getKeys().shopListKey, PersistentDataType.INTEGER_ARRAY, new int[]{});
            int[] newShopList = ArrayUtils.addAll(shopLists, chestBlockLocation.getBlockX(), chestBlockLocation.getBlockY(), chestBlockLocation.getBlockZ());
            chunkContainer.set(AwesomeChestShop.getKeys().shopListKey, PersistentDataType.INTEGER_ARRAY, newShopList);
        } else {
            chunkContainer.set(AwesomeChestShop.getKeys().shopListKey, PersistentDataType.INTEGER_ARRAY, new int[]{chestBlockLocation.getBlockX(), chestBlockLocation.getBlockY(), chestBlockLocation.getBlockZ()});
        }
        // update blocks
        sign.update();
        container.update();
        updateTopPreviewItem();
    }

    public void updateTopPreviewItem() {
        for (Player player : Bukkit.getOnlinePlayers()) {
            EntityUtils.spawnFakeItemAt(player, getChestBlockLocation().add(0.5, 1, 0.5), getTopPreviewItem(), shopItemEntityId);
        }
    }

    public void updateTopPreviewItem(Player player) {
        EntityUtils.spawnFakeItemAt(player, getChestBlockLocation().add(0.5, 1, 0.5), getTopPreviewItem(), shopItemEntityId);
    }

    public void deleteShownItem() {
        final ProtocolManager manager = ProtocolLibrary.getProtocolManager();
        final PacketContainer packetContainer = manager.createPacket(PacketType.Play.Server.ENTITY_DESTROY);
        packetContainer.getIntLists().write(0, Collections.singletonList(shopItemEntityId));
        for (Player player : Bukkit.getOnlinePlayers()) {
            manager.sendServerPacket(player, packetContainer);
        }
    }

    void delete() {
        OfflinePlayer owner = Bukkit.getOfflinePlayer(getOwnerId());

        Block containerBlock = getChestBlockLocation().getBlock();
        Block signBlock = containerBlock.getRelative(signFace);
        signBlock.setType(Material.AIR);

        if (containerBlock.getState() instanceof TileState tileState) {
            PersistentDataContainer dataContainer = tileState.getPersistentDataContainer();
            NamespacedKeys.SHOP_ID.removeValueFrom(dataContainer);
            NamespacedKeys.SIGN_ATTACHED_FACE.removeValueFrom(dataContainer);
            tileState.update();
        }

        if (owner instanceof Player player) {
            player.sendMessage(Messages.DELETING_SHOP_LEFT
                    .append(Component.text(getChestBlockLocation().getBlockX() + ", " + getChestBlockLocation().getBlockY() + ", " + getChestBlockLocation().getBlockZ(), NamedTextColor.BLUE))
                    .append(Messages.DELETING_SHOP_RIGHT));
        }

        deleteShownItem();
    }

    boolean create(double price) {
        Player player = AwesomeChestShop.getPlugin().getServer().getPlayer(playerId);
        if (player == null) {
            return false;
        }

        // Check container
        Block containerBlock = getChestBlockLocation().getBlock();
        if (!ShopUtils.isValidContainer(containerBlock)) {
            player.sendMessage(Messages.MESSAGE_NOT_CHEST_PART_LOCATION
                    .hoverEvent(Component.text(getChestBlockLocation().getBlockX() + ", " + getChestBlockLocation().getBlockY() + ", " + getChestBlockLocation().getBlockZ()))
                    .append(Messages.MESSAGE_NOT_CONTAINER));
            return false;
        }
        // Already has a shop
        if (ShopUtils.getShopIdAt(getChestBlockLocation()) != null) {
            player.sendMessage(Messages.MESSAGE_SHOP_EXISTS);
            return false;
        }

        // Validate has space to generate a sign
        Block targetSignBlock = containerBlock.getRelative(signFace);
        if (!targetSignBlock.isEmpty() && !targetSignBlock.isLiquid()) {
            player.sendMessage(Messages.MESSAGE_SIGN_NO_SPACE);
            return false;
        }

        this.price = price;
        this.updateWorld();
        this.created = true;
        return true;
    }

    public @NotNull Location getChestBlockLocation() {
        return chestBlockLocation.clone();
    }

    public @NotNull BlockFace getSignFace() {
        return signFace;
    }

    public @NotNull UUID getOwnerId() {
        return playerId;
    }

    public void setItemStack(@NotNull ItemStack itemStack) {
        this.itemStack = itemStack.clone();
    }

    public @NotNull ItemStack getItemStack() {
        return itemStack.clone();
    }

    public @NotNull ItemStack getTopPreviewItem() {
        return this.getShopType() == ShopType.GOTCHA_MODE ? GOTCHA_ITEM : this.getItemStack();
    }

    public @NotNull UUID getShopUuid() {
        return this.shopUuid;
    }

    public @NotNull ShopType getShopType() {
        if (this.shopType == null) return ShopType.SALE_MODE;
        return this.shopType;
    }

    public double getPrice() {
        return price;
    }

    public int getStock() {
        Block containerBlock = chestBlockLocation.getBlock();
        if (!(containerBlock.getState() instanceof Container container)) return 0;

        if (getShopType() == ShopType.GOTCHA_MODE) {
            return Math.toIntExact(Arrays.stream(container.getInventory().getContents()).filter(ItemUtils::isNotEmpty).count());
        }

        InventorySpace space = BlockUtils.countInventorySpace(container.getInventory(), getItemStack());
        return getShopType() == ShopType.BUY_MODE ? space.space() : space.amount();
    }

    public boolean isProtected() {
        return shopProtected;
    }

    public void setShopType(@NotNull ShopType shopType) {
        this.shopType = shopType;
        if (created && Bukkit.isPrimaryThread()) {
            updateWorld();
        }
    }

    public void setProtected(boolean shopProtected) {
        this.shopProtected = shopProtected;
    }

    public static ChestShop ofSign(Block signBlock) {
        if (signBlock.getType() != Material.OAK_WALL_SIGN) return null;
        if (!(signBlock.getState() instanceof Sign sign)) return null;
        if (!(signBlock.getBlockData() instanceof Directional signBlockData)) return null;
        Block containerBlock = signBlock.getRelative(signBlockData.getFacing().getOppositeFace());

        if (!(containerBlock.getState() instanceof Container container)) return null;
        return ofContainerAndSign(container, sign, signBlockData.getFacing());
    }

    public static ChestShop ofContainer(Block containerBlock) {
        if (!ShopUtils.isValidContainer(containerBlock)) {
            return null;
        }

        if (!(containerBlock.getState() instanceof Container container)) return null;

        PersistentDataContainer chestContainer = container.getPersistentDataContainer();
        int signFace = NamespacedKeys.SIGN_ATTACHED_FACE.getValueFrom(chestContainer);
        if (signFace == -1) {
            return null;
        }

        BlockFace signRelativeFace = BlockFace.values()[signFace];
        Block signBlock = containerBlock.getRelative(signRelativeFace);
        if (!(signBlock.getState() instanceof Sign sign)) return null;

        return ofContainerAndSign(container, sign, signRelativeFace);
    }

    private static ChestShop ofContainerAndSign(Container container, Sign sign, BlockFace face) {
        PersistentDataContainer chestDataContainer = container.getPersistentDataContainer();
        PersistentDataContainer signContainer = sign.getPersistentDataContainer();

        UUID shopUuid = NamespacedKeys.SHOP_ID.getValueFrom(chestDataContainer);
        if (shopUuid == null) return null;

        int signFace = NamespacedKeys.SIGN_ATTACHED_FACE.getValueFrom(chestDataContainer);
        if (signFace == -1) throw new IllegalStateException("商店" + shopUuid + "数据损坏: 告示牌方位");

        UUID ownerId = NamespacedKeys.SHOP_OWNER_ID.getValueFrom(signContainer);
        if (ownerId == null) throw new IllegalStateException("商店" + shopUuid + "数据损坏: 创建者");

        ItemStack item = NamespacedKeys.SHOP_ITEM.getValueFrom(signContainer);
        if (item == null) throw new IllegalStateException("商店" + shopUuid + "数据损坏: 物品");

        Double price = NamespacedKeys.SHOP_PRICE.getValueFrom(signContainer);
        if (price == null) throw new IllegalStateException("商店" + shopUuid + "数据损坏: 价格");

        Integer shopTypeIndex = NamespacedKeys.SHOP_TYPE.getValueFrom(signContainer);
        ShopType shopType = ShopType.values()[shopTypeIndex == null ? 0 : shopTypeIndex];

        return new ChestShop(shopUuid, container.getLocation(), face, ownerId, shopType, item, price, true);
    }

    public TagResolver getTagResolver() {
        TagResolver locationResolver = TagResolver.resolver(
                Formatter.number("x", getChestBlockLocation().getBlockX()),
                Formatter.number("y", getChestBlockLocation().getBlockY()),
                Formatter.number("z", getChestBlockLocation().getBlockZ())
        );

        TagResolver itemPreviewResolver;
        if (getShopType() == ShopType.GOTCHA_MODE) {
            itemPreviewResolver = Placeholder.styling("item_preview",
                    HoverEvent.showText(Component.text("点击预览奖池")),
                    ClickEvent.runCommand(String.format("/shop view %d %d %d", chestBlockLocation.getBlockX(), chestBlockLocation.getBlockY(), chestBlockLocation.getBlockZ())));
        } else {
            itemPreviewResolver = Placeholder.styling("item_preview", getItemStack().asHoverEvent());
        }

        OfflinePlayer owner = Bukkit.getOfflinePlayer(getOwnerId());
        TagResolver ownerResolver;
        if (owner.isOnline()) {
            ownerResolver = Placeholder.component("owner", Component.text(owner.getName())
                    .hoverEvent(HoverEvent.showText(miniMessage.deserialize("<green>点击私聊</green>")))
                    .clickEvent(ClickEvent.suggestCommand("/tell " + owner.getName() + " ")));
        } else {
            ownerResolver = Placeholder.component("owner", Component.text(owner.getName())
                    .hoverEvent(HoverEvent.showText(miniMessage.deserialize("<green>上次在线：</green><last_online>",
                            Formatter.date("last_online", LocalDateTime.ofInstant(Instant.ofEpochMilli(owner.getLastSeen()), ZoneId.of("Asia/Shanghai")))
                    ))));
        }
        TagResolver priceRawResolver = Formatter.number("price_raw", getPrice());
        TagResolver currencyRawResolver = Placeholder.component("currency_raw", Component.text(AwesomeChestShop.getEconomy().currencyNamePlural()));

        return TagResolver.resolver(
                locationResolver,
                itemPreviewResolver,
                ownerResolver,
                priceRawResolver,
                currencyRawResolver,
                Formatter.booleanChoice("is_buy_mode", getShopType() == ShopType.BUY_MODE),
                Formatter.number("stock", getStock()),
                Placeholder.component("currency", miniMessage.deserialize("<#EE578C><currency_raw></#EE578C>", currencyRawResolver)),
                Placeholder.component("price", miniMessage.deserialize("<#F3C64B><price_raw></#F3C64B>", priceRawResolver)),
                Placeholder.component("chest_location", miniMessage.deserialize("<hover:show_text:点击复制><aqua><x>, <y>, <z></aqua></hover>", locationResolver)),
                Placeholder.component("shop_mode", getShopType().getColoredName())
        );
    }

    public TagResolver getTagResolver(int amount) {
        return TagResolver.resolver(Placeholder.component("price", Component.text(AwesomeChestShop.getEconomy().currencyNamePlural(), TextColor.color(0xEE578C))
                .appendSpace().append(Component.text(getPrice() * amount, TextColor.color(0xF3C64B)))));
    }

    @Nullable
    public static ChestShop ofBlock(@Nullable Block block) {
        if (block == null) return null;
        if (block.getState() instanceof Sign) {
            return ofSign(block);
        }
        if (block.getState() instanceof Container container) {
            // 不是大箱子
            if (!(container.getInventory().getHolder() instanceof DoubleChest doubleChest)) {
                System.out.println("不是大箱子");
                return ofContainer(block);
            }

            if (doubleChest.getLeftSide() instanceof Chest leftChest && NamespacedKeys.SHOP_ID.hasValue(leftChest)) {
                System.out.println("左边");
                return ofContainer(leftChest.getBlock());
            } else if (doubleChest.getRightSide() instanceof Chest rightChest && NamespacedKeys.SHOP_ID.hasValue(rightChest)) {
                System.out.println("右边");
                return ofContainer(rightChest.getBlock());
            }
        }
        return null;
    }

    @Override
    public String toString() {
        return "ChestShop{" +
                "chestBlockLocation=" + chestBlockLocation +
                ", signFace=" + signFace +
                ", playerId=" + playerId +
                ", itemStack=" + itemStack +
                ", shopUuid=" + shopUuid +
                ", shopType=" + shopType +
                ", price=" + price +
                ", shopProtected=" + shopProtected +
                ", created=" + created +
                '}';
    }

    @Override
    public @NotNull Inventory getInventory() {
        final Inventory inventory;
        if (getChestBlockLocation().getBlock().getState() instanceof InventoryHolder originalInventoryHolder) {
            inventory = Bukkit.createInventory(this, originalInventoryHolder.getInventory().getSize(), Messages.SHOP_PREVIEW);
            inventory.setContents(originalInventoryHolder.getInventory().getContents());
        } else {
            inventory = Bukkit.createInventory(this, 27, Component.text("商店预览"));
        }
        return inventory;
    }
}
