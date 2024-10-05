package com.keriteal.awesomeChestShop.shop;

import com.google.common.collect.ImmutableList;
import com.keriteal.awesomeChestShop.AwesomeChestShop;
import com.keriteal.awesomeChestShop.Messages;
import com.keriteal.awesomeChestShop.ShopType;
import com.keriteal.awesomeChestShop.shop.operations.IShopOperation;
import com.keriteal.awesomeChestShop.utils.ShopUtils;
import io.papermc.paper.threadedregions.scheduler.RegionScheduler;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.minimessage.tag.resolver.Formatter;
import net.wesjd.anvilgui.AnvilGUI;
import org.apache.commons.lang3.math.NumberUtils;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.Chest;
import org.bukkit.block.DoubleChest;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;

import java.util.*;

public class ShopManager {
    private static final ItemStack EMPTY_ITEM = new ItemStack(Material.GRAY_STAINED_GLASS_PANE);
    private static final List<Component> EMPTY_ITEM_LORE = ImmutableList.of(Component.text("点击输出物品确认"));
    private final MiniMessage miniMessage = MiniMessage.miniMessage();
    private final JavaPlugin plugin;
    private final Map<UUID, ChestShop> existingShops = new HashMap<>();
    private final Logger logger;
    private final List<UUID> unProtectedShop = new LinkedList<>();
    private boolean isProtectionEnabled = true;
    private final Map<UUID, IShopOperation> playerOperations = new HashMap<>();

    static {
        ItemMeta meta = EMPTY_ITEM.getItemMeta();
        meta.displayName(Component.empty());
        meta.lore(EMPTY_ITEM_LORE);
        EMPTY_ITEM.setItemMeta(meta);
    }

    public ShopManager(JavaPlugin javaPlugin) {
        this.plugin = javaPlugin;
        this.logger = plugin.getSLF4JLogger();
    }

    public boolean hasPendingShop(Player player) {
        return playerOperations.containsKey(player.getUniqueId());
    }

    public void cancelPendingShop(Player player) {
        playerOperations.remove(player.getUniqueId());
    }

    @Nullable
    public ChestShop getShop(@NotNull UUID shopId) {
        return existingShops.get(shopId);
    }

    public ChestShop loadShopAt(DoubleChest doubleChest) {
        if (doubleChest.getLeftSide() instanceof Chest left) {
            return loadShopAt(left.getBlock());
        }
        return null;
    }

    /**
     * Get the shop related to the block
     *
     * @param block the shop block, could be a container or a sign
     * @return The shop related to the block
     */
    @Nullable
    public ChestShop loadShopAt(@NotNull Block block) {
        UUID shopId = ShopUtils.getShopId(block);
        if (shopId == null) return null;

        if (existingShops.containsKey(shopId)) {
            return existingShops.get(shopId);
        }

        ChestShop shop = ChestShop.ofBlock(block);
        if (shop != null) {
            shop.updateWorld();
            existingShops.put(shopId, shop);
        }

//        logger.info("Load finished, shop data: {}", shop);
        return shop;
    }

    @Nullable
    public ChestShop loadShopAt(@NotNull Location location) {
        return loadShopAt(location.getBlock());
    }

    public void setProtectionEnabled(boolean enabled) {
        isProtectionEnabled = enabled;
    }

    public boolean isShopProtected(UUID shopId) {
        if (isProtectionEnabled) {
            return !unProtectedShop.contains(shopId);
        } else {
            return false;
        }
    }

    public boolean isTrading(@NotNull UUID player) {
        return playerOperations.containsKey(player) && playerOperations.get(player).getOperationType() == ShopOperationType.TRADING;
    }

    public boolean isCreatingShop(@NotNull UUID player) {
        return playerOperations.containsKey(player) && playerOperations.get(player).getOperationType() == ShopOperationType.CREATING;
    }

    @Contract(pure = true)
    public boolean hasOperation(@NotNull UUID player) {
        return playerOperations.containsKey(player);
    }

    public void cancelCreation(@NotNull UUID playerId) {
        if (isCreatingShop(playerId)) playerOperations.remove(playerId);
        if (Bukkit.getOfflinePlayer(playerId) instanceof Player player) {
            player.sendMessage(Component.text("商店创建操作已取消", NamedTextColor.GOLD));
        }
    }

    public void cancelTrading(@NotNull UUID playerId) {
        if (isTrading(playerId)) playerOperations.remove(playerId);
        if (Bukkit.getOfflinePlayer(playerId) instanceof Player player) {
            player.sendMessage(Component.text("交易结束", NamedTextColor.GOLD));
        }
    }

    public void cancelOperation(@NotNull UUID playerId) {
        if (!hasOperation(playerId)) return;
        IShopOperation operation = playerOperations.get(playerId);
        if (operation.getOperationType() == ShopOperationType.CREATING) {
            cancelCreation(playerId);
        } else if (operation.getOperationType() == ShopOperationType.TRADING) {
            cancelTrading(playerId);
        }
    }

    @Contract(pure = true)
    public IShopOperation getOperation(@NotNull Player player) {
        return playerOperations.get(player.getUniqueId());
    }

    public void changeShopType(@NotNull UUID shopId, @NotNull ShopType type) {
        if (!existingShops.containsKey(shopId)) {
            logger.warn("Changing shop {}, but shop not loaded or created", shopId);
            return;
        }
        ChestShop shop = existingShops.get(shopId);
        logger.info("Changing shop {} type from {} to {}", shopId, shop.getShopType(), type);
        shop.setShopType(type);
    }

    @Contract(pure = true)
    @NotNull
    public List<ChestShop> getShopsInChunk(@NotNull Chunk chunk) {
        List<ChestShop> shops = new LinkedList<>();
        for (ChestShop shop : existingShops.values()) {
            if (shop.getChestBlockLocation().getChunk().equals(chunk)) {
                shops.add(shop);
            }
        }
        return shops;
    }

    @Contract(pure = true)
    @NotNull
    public List<ChestShop> getShops() {
        return existingShops.values().parallelStream().toList();
    }

    public void deleteShop(@NotNull UUID uuid) {
        if (!existingShops.containsKey(uuid)) return;
        ChestShop shop = existingShops.get(uuid);
        shop.delete();
        existingShops.remove(uuid);
    }

    public void prepareCreate(Player player, Location shopLocation, ItemStack item, BlockFace signRelativeFace) {
        ShopCreationOperation operation = new ShopCreationOperation(this, shopLocation, player.getUniqueId(), item, signRelativeFace);
        playerOperations.put(player.getUniqueId(), operation);
        new AnvilGUI.Builder()
                .mainThreadExecutor(task -> Bukkit.getRegionScheduler().execute(plugin, shopLocation, task))
                .itemLeft(EMPTY_ITEM)
                .plugin(this.plugin)
                .onClick((slot, stateSnapshot) -> {
                    if (slot != AnvilGUI.Slot.OUTPUT) {
                        return Collections.emptyList();
                    }

                    double price = NumberUtils.toDouble(stateSnapshot.getText(), Double.NaN);
                    if (Double.isNaN(price)) {
                        return Collections.emptyList();
                    }

                    doCreate(player.getUniqueId(), price);

                    return List.of(AnvilGUI.ResponseAction.close());
                })
                .open(player);
    }

    public void doCreate(UUID playerId, double price) {
        if (!(playerOperations.getOrDefault(playerId, null) instanceof ShopCreationOperation operation))
            return;

        OfflinePlayer offlinePlayer = Bukkit.getOfflinePlayer(playerId);
        if (!(offlinePlayer instanceof Player player)) return;

        ChestShop shop = new ChestShop(operation.getShopLocation(), operation.getSignRelativeFace(), playerId, operation.getItem());

        if (price <= 0) {
            player.sendMessage(Component.text("价格不能小于等于0，重新输入价格", NamedTextColor.RED));
            return;
        }

        RegionScheduler scheduler = Bukkit.getServer().getRegionScheduler();
        scheduler.execute(this.plugin, shop.getChestBlockLocation(), () -> {
            logger.info("Creating shop for player {} at {}, {}, {}",
                    player.getName(),
                    shop.getChestBlockLocation().getBlockX(),
                    shop.getChestBlockLocation().getBlockY(),
                    shop.getChestBlockLocation().getBlockZ());
            boolean result = shop.create(price);
            if (result) {
                Location location = shop.getChestBlockLocation();

                logger.info("Shop created at {}, {}, {}", location.getBlockX(), location.getBlockY(), location.getBlockZ());
                player.sendMessage(Messages.MESSAGE_CREATE_SUCCESS
                        .appendNewline()
                        .append(Component.text("物品: "))
                        .append(Component.translatable(shop.getItemStack().translationKey()))
                        .append(Component.text("[预览]", NamedTextColor.GREEN).hoverEvent(shop.getItemStack()))
                        .appendNewline());
                existingShops.put(shop.getShopUuid(), shop);
                playerOperations.remove(player.getUniqueId());
            }
        });
    }


    public void prepareTrade(@NotNull Player player, @NotNull UUID shopId) {
        ChestShop shop = existingShops.get(shopId);
        if (shop == null) {
            player.sendMessage(Component.text("商店不存在", NamedTextColor.RED));
            return;
        }

        logger.info("Player {} is preparing to trade with shop {}", player.getName(), shopId);
        final Location shopLocation = shop.getChestBlockLocation();
        ShopTradingOperation operation = new ShopTradingOperation(this, player.getUniqueId(), shopLocation);
        playerOperations.put(player.getUniqueId(), operation);
        new AnvilGUI.Builder()
                .mainThreadExecutor(task -> Bukkit.getRegionScheduler().execute(plugin, shopLocation, task))
                .itemLeft(EMPTY_ITEM)
                .jsonTitle("<green>输入价格</green")
                .plugin(this.plugin)
                .onClick((slot, stateSnapshot) -> {
                    if (slot != AnvilGUI.Slot.OUTPUT) {
                        return Collections.emptyList();
                    }

                    int amount = NumberUtils.toInt(stateSnapshot.getText(), -1);
                    if (amount == -1) {
                        return Collections.emptyList();
                    }

                    doTrade(player.getUniqueId(), amount);

                    return List.of(AnvilGUI.ResponseAction.close());
                })
                .open(player);
    }

    public void doTrade(@NotNull final UUID playerId, int amount) {
        if (!(Bukkit.getOfflinePlayer(playerId) instanceof Player player)) return;

        if (!isTrading(playerId)) {
            logger.warn("Player {} tried to trade without operation", player.getName());
            return;
        }

        Location tradingShop = getOperation(player).getShopLocation();

        Bukkit.getRegionScheduler().execute(plugin, tradingShop, () -> {
            logger.info("Doing trading runnable");
            ChestShop shop = loadShopAt(tradingShop);

            if (shop == null) {
                logger.warn("Player {} tried to trade with a non-existing shop at {}, {}, {}", player.getName(), tradingShop.getBlockX(), tradingShop.getBlockY(), tradingShop.getBlockZ());
                playerOperations.remove(player.getUniqueId());
                return;
            }

            OfflinePlayer owner = Bukkit.getOfflinePlayer(shop.getOwnerId());
            OfflinePlayer trader = Bukkit.getOfflinePlayer(player.getUniqueId());
            int maxTradeAmount;
            if (shop.getShopType() == ShopType.BUY_MODE) {
                maxTradeAmount = (int) (AwesomeChestShop.getEconomy().getBalance(owner) / shop.getPrice());
            } else {
                maxTradeAmount = (int) (AwesomeChestShop.getEconomy().getBalance(trader) / shop.getPrice());
            }

            int realTradeAmount = Math.min(amount, maxTradeAmount);

            if (!(shop.getChestBlockLocation().getBlock().getState() instanceof InventoryHolder inventoryHolder)) {
                logger.warn("Player {} tried to trade with a non-inventory block", player.getName());
                playerOperations.remove(player.getUniqueId());
                return;
            }
            if (realTradeAmount == 0) {
                if (shop.getShopType() == ShopType.BUY_MODE) {
                    player.sendMessage(miniMessage.deserialize("<red>交易失败，店主余额不足</red>", Messages.buildBalanceComponent(trader)));
                } else {
                    player.sendMessage(miniMessage.deserialize("<red>交易失败，余额不足，你的余额：<balance></red>", Messages.buildBalanceComponent(trader)));
                }
                return;
            }

            realTradeAmount = Math.min(realTradeAmount, shop.getStock());

            Inventory shopInventory = inventoryHolder.getInventory();
            Inventory playerInventory = player.getInventory();

            int successAmount = 0;
            if (shop.getShopType() == ShopType.SALE_MODE) {
                successAmount = transferItem(shopInventory, playerInventory, shop.getShopUuid(), realTradeAmount);
            } else if (shop.getShopType() == ShopType.BUY_MODE) {
                successAmount = transferItem(playerInventory, shopInventory, shop.getShopUuid(), realTradeAmount);
            } else {
                successAmount = gotcha(shopInventory, playerInventory, realTradeAmount);
            }

            if (successAmount == 0) {
                player.sendMessage(miniMessage.deserialize("<red>交易失败，物品数量不足</red>"));
                playerOperations.remove(player.getUniqueId());
                return;
            }

            if (shop.getShopType() == ShopType.BUY_MODE) {
                AwesomeChestShop.getEconomy().withdrawPlayer(owner, shop.getPrice() * successAmount);
                AwesomeChestShop.getEconomy().depositPlayer(trader, shop.getPrice() * successAmount);
                player.sendMessage(miniMessage.deserialize("<green>交易成功，数量: <amount>，获得<price>，余额：<balance></green>",
                        shop.getTagResolver(successAmount),
                        Messages.buildBalanceComponent(trader),
                        Formatter.number("amount", successAmount)));
            } else {
                AwesomeChestShop.getEconomy().withdrawPlayer(trader, shop.getPrice() * successAmount);
                AwesomeChestShop.getEconomy().depositPlayer(owner, shop.getPrice() * successAmount);
                player.sendMessage(miniMessage.deserialize("<green>交易成功，数量: <amount>，花费<price>，余额：<balance></green>",
                        shop.getTagResolver(successAmount),
                        Messages.buildBalanceComponent(trader),
                        Formatter.number("amount", successAmount)));
            }
            shop.updateWorld();
        });
    }

    private int gotcha(Inventory shopInventory, Inventory playerInventory, int ticketAmount) {
        List<Integer> slots = new LinkedList<>();
        int successItems = 0;

        for (int i = 0; i < shopInventory.getSize(); i++) {
            ItemStack item = shopInventory.getItem(i);
            if (item == null) continue;
            slots.add(i);
        }

        Collections.shuffle(slots, new Random(System.currentTimeMillis()));

        for (int i = 0; i < ticketAmount; i++) {
            int chosenIndex = slots.get(i);
            ItemStack item = shopInventory.getItem(chosenIndex);
            if (item == null) throw new IllegalStateException("Item is null");
            shopInventory.setItem(chosenIndex, null);
            HashMap<Integer, ItemStack> overflow = playerInventory.addItem(item);
            if (!overflow.isEmpty()) shopInventory.setItem(chosenIndex, overflow.get(0));
            successItems++;
        }

        return successItems;
    }

    /**
     * Transfer item from one inventory to another
     *
     * @param fromInventory
     * @param toInventory
     * @param shopUuid
     * @param amount
     * @return Amount of successfully transferred items
     */
    private int transferItem(Inventory fromInventory, Inventory toInventory, UUID shopUuid, int amount) {
        logger.info("Transferring {} item from {} to {}", amount, fromInventory.getHolder(), toInventory.getHolder());
        int remainedAmount = amount;
        List<ItemStack> tradingItems = new LinkedList<>();
        ChestShop shop = getShop(shopUuid);
        if (shop == null) return 0;

        for (ItemStack shopItem : fromInventory.getContents()) {
            if (shopItem == null || !shopItem.isSimilar(shop.getItemStack())) continue;

            if (remainedAmount == 0) break;

            int transferredAmount = Math.min(shopItem.getAmount(), remainedAmount);

            ItemStack transferItem = shopItem.clone();

            transferItem.setAmount(transferredAmount);
            shopItem.setAmount(shopItem.getAmount() - transferredAmount);

            tradingItems.add(transferItem);
            remainedAmount -= transferredAmount;
        }

        HashMap<Integer, ItemStack> overflow = toInventory.addItem(tradingItems.toArray(new ItemStack[0]));
        int overflowAmount = 0;
        for (ItemStack item : overflow.values()) {
            overflowAmount += item.getAmount();
        }

        return amount - overflowAmount;
    }
}
