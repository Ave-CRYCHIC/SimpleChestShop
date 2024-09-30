package com.keriteal.awesomeChestShop;

import com.keriteal.awesomeChestShop.utils.BlockUtils;
import com.keriteal.awesomeChestShop.utils.ItemUtils;
import com.keriteal.awesomeChestShop.utils.ShopUtils;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.minimessage.tag.resolver.Formatter;
import org.apache.commons.lang3.ArrayUtils;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.*;
import org.bukkit.block.data.Directional;
import org.bukkit.block.sign.Side;
import org.bukkit.block.sign.SignSide;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.UUID;

import static com.keriteal.awesomeChestShop.Messages.*;

public final class ChestShop {
    private final MiniMessage miniMessage = MiniMessage.miniMessage();
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
        Player owner = Bukkit.getServer().getPlayer(getOwnerId());
        if (owner == null) {
            signSide.line(0, Component.text("Unknown", getShopType().getTextColor()));
        } else {
            signSide.line(0, owner.displayName().color(getShopType().getTextColor()));
        }

        // Line 2: Sold item
        if (getShopType() == ShopType.GOTCHA_MODE) {
            signSide.line(1, Component.text("一番くじ", NamedTextColor.WHITE));
        } else {
            signSide.line(1, ItemUtils.getItemName(itemStack));
        }

        // Line 3: Price
        String priceTemplate = "<color:#EE578C>" + AwesomeChestShop.getEconomy().currencyNamePlural() + " </color><color:#F3C64B><price:'#.0'></color>";
        signSide.line(2, miniMessage.deserialize(priceTemplate, Formatter.number("price", price)));

        // Line 4: Amount
        Component numberComponent;
        long amount = getStock();
        if (amount == 0) {
            numberComponent = Component.text(amount, NamedTextColor.RED);
        } else {
            numberComponent = Component.text(amount, NamedTextColor.GREEN);
        }

        switch (getShopType()) {
            case SALE_MODE -> {
                signSide.line(3, TEMPLATE_AMOUNT_STOCK.append(numberComponent));
            }
            case BUY_MODE -> {
                signSide.line(3, TEMPLATE_AMOUNT_FREE_SPACE.append(numberComponent));
            }
            case GOTCHA_MODE -> {
                signSide.line(3, Component.empty());
            }
        }

        // Prepare to save data
        PersistentDataContainer signDataContainer = sign.getPersistentDataContainer();
        PersistentDataContainer containerDataContainer = container.getPersistentDataContainer();

        // Save data to main chest
        containerDataContainer.set(AwesomeChestShop.getKeys().shopIdKey, PersistentDataType.STRING, shopUuid.toString());
        containerDataContainer.set(AwesomeChestShop.getKeys().signAttachedFace, PersistentDataType.INTEGER, getSignFace().ordinal());

        // Save data to shop sign
        signDataContainer.set(AwesomeChestShop.getKeys().shopIdKey, PersistentDataType.STRING, shopUuid.toString());
        signDataContainer.set(AwesomeChestShop.getKeys().shopOwnerKey, PersistentDataType.STRING, playerId.toString());
        signDataContainer.set(AwesomeChestShop.getKeys().shopItemKey, PersistentDataType.BYTE_ARRAY, getItemStack().serializeAsBytes());
        signDataContainer.set(AwesomeChestShop.getKeys().shopPriceKey, PersistentDataType.DOUBLE, this.price);
        signDataContainer.set(AwesomeChestShop.getKeys().shopTypeKey, PersistentDataType.INTEGER, this.getShopType().ordinal());

        // Save shop location to chunk
        PersistentDataContainer chunkContainer = chestBlockLocation.getChunk().getPersistentDataContainer();
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
    }

    public void delete() {
    }

    public boolean create(double price) {
        Player player = AwesomeChestShop.getPlugin().getServer().getPlayer(playerId);
        if (player == null) {
            return false;
        }

        // Check container
        Block containerBlock = getChestBlockLocation().getBlock();
        if (!ShopUtils.isValidContainer(containerBlock)) {
            player.sendMessage(MESSAGE_NOT_CHEST_PART_LOCATION
                    .hoverEvent(Component.text(getChestBlockLocation().getBlockX() + ", " + getChestBlockLocation().getBlockY() + ", " + getChestBlockLocation().getBlockZ()))
                    .append(MESSAGE_NOT_CONTAINER));
            return false;
        }
        // Already has a shop
        if (ShopUtils.getShopIdAt(getChestBlockLocation()) != null) {
            player.sendMessage(MESSAGE_SHOP_EXISTS);
            return false;
        }

        // Validate has space to generate a sign
        Block targetSignBlock = containerBlock.getRelative(signFace);
        if (!targetSignBlock.isEmpty() && !targetSignBlock.isLiquid()) {
            player.sendMessage(MESSAGE_SIGN_NO_SPACE);
            return false;
        }

        this.price = price;
        this.updateWorld();
        this.created = true;
        return true;
    }

    public @NotNull Location getChestBlockLocation() {
        return chestBlockLocation;
    }

    public @NotNull BlockFace getSignFace() {
        return signFace;
    }

    public @NotNull UUID getOwnerId() {
        return playerId;
    }

    public void setItemStack(@NotNull ItemStack itemStack) {
        this.itemStack = itemStack;
    }

    public @NotNull ItemStack getItemStack() {
        return itemStack;
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

    public long getStock() {
        Block containerBlock = chestBlockLocation.getBlock();
        if (!(containerBlock.getState() instanceof Container container)) return 0;
        InventorySpace space = BlockUtils.countInventorySpace(container.getInventory(), itemStack);
        return getShopType() == ShopType.BUY_MODE ? space.space() : space.amount();
    }

    public boolean isProtected() {
        return shopProtected;
    }

    public void setShopType(@NotNull ShopType shopType) {
        this.shopType = shopType;
        if (created) {
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
        if (ShopUtils.isValidContainer(containerBlock)) return null;

        if (!(containerBlock.getState() instanceof Container container)) return null;

        PersistentDataContainer chestContainer = container.getPersistentDataContainer();
        int signFace = chestContainer.getOrDefault(AwesomeChestShop.getKeys().signAttachedFace, PersistentDataType.INTEGER, -1);
        if (signFace == -1) return null;

        BlockFace signRelativeFace = BlockFace.values()[signFace];
        Block signBlock = containerBlock.getRelative(signRelativeFace);
        if (!(signBlock.getState() instanceof Sign sign)) return null;

        return ofContainerAndSign(container, sign, signRelativeFace);
    }

    private static ChestShop ofContainerAndSign(Container container, Sign sign, BlockFace face) {
        PersistentDataContainer chestDataContainer = container.getPersistentDataContainer();
        PersistentDataContainer signContainer = sign.getPersistentDataContainer();

        String shopUuid = chestDataContainer.getOrDefault(AwesomeChestShop.getKeys().shopIdKey, PersistentDataType.STRING, "");
        if (shopUuid.isEmpty()) return null;

        int signFace = chestDataContainer.getOrDefault(AwesomeChestShop.getKeys().signAttachedFace, PersistentDataType.INTEGER, -1);
        if (signFace == -1) return null;

        String ownerId = signContainer.getOrDefault(AwesomeChestShop.getKeys().shopOwnerKey, PersistentDataType.STRING, "");
        if (ownerId.isEmpty()) return null;

        ItemStack item = ItemStack.deserializeBytes(signContainer.get(AwesomeChestShop.getKeys().shopItemKey, PersistentDataType.BYTE_ARRAY));

        double price = signContainer.getOrDefault(AwesomeChestShop.getKeys().shopPriceKey, PersistentDataType.DOUBLE, Double.MAX_VALUE);

        int shopTypeIndex = signContainer.getOrDefault(AwesomeChestShop.getKeys().shopTypeKey, PersistentDataType.INTEGER, ShopType.SALE_MODE.ordinal());
        ShopType shopType = ShopType.values()[shopTypeIndex];

        return new ChestShop(UUID.fromString(shopUuid), container.getLocation(), face, UUID.fromString(ownerId), shopType, item, price, true);
    }

    @Nullable
    public static ChestShop ofBlock(@Nullable Block block) {
        if (block == null) return null;
        if (block.getType() == Material.OAK_WALL_SIGN) {
            return ofSign(block);
        }
        if (block.getState() instanceof Container) {
            return ofContainer(block);
        }
        return null;
    }

    public static ChestShop fromLocation(Location location) {
        Block block = location.getBlock();
        if (block.getType() == Material.OAK_WALL_SIGN) {
            return ofSign(block);
        }
        if (block.getState() instanceof Container) {
            return ofContainer(block);
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
}
