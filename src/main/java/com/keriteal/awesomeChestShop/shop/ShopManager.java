package com.keriteal.awesomeChestShop.shop;

import com.keriteal.awesomeChestShop.AwesomeChestShop;
import com.keriteal.awesomeChestShop.ShopType;
import com.keriteal.awesomeChestShop.shop.operations.ShopCreationOperation;
import com.keriteal.awesomeChestShop.shop.operations.ShopOperation;
import com.keriteal.awesomeChestShop.shop.operations.ShopTradingOperation;
import com.keriteal.awesomeChestShop.utils.ShopUtils;
import io.papermc.paper.threadedregions.scheduler.RegionScheduler;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.Bukkit;
import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;

import java.util.*;

import static com.keriteal.awesomeChestShop.Messages.MESSAGE_CREATE_SUCCESS;

public class ShopManager {
    private final MiniMessage miniMessage = MiniMessage.miniMessage();
    private final JavaPlugin plugin;
    private final Map<UUID, ChestShop> existingShops = new HashMap<>();
    private final Logger logger;
    private final List<UUID> unProtectedShop = new LinkedList<>();
    private boolean isProtectionEnabled = true;
    private final Map<UUID, ShopOperation> playerOperations = new HashMap<>();

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

    public void prepareCreate(Player player, ShopOperation operation) {
        playerOperations.put(player.getUniqueId(), operation);
    }

    public void doCreate(Player player, double price) {
        if (!(playerOperations.getOrDefault(player.getUniqueId(), null) instanceof ShopCreationOperation operation))
            return;

        ChestShop shop = new ChestShop(operation.getShopLocation(), operation.getSignRelativeFace(), player.getUniqueId(), operation.getItem());

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
                player.sendMessage(MESSAGE_CREATE_SUCCESS
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

    public ChestShop getShop(UUID shopId) {
        return existingShops.get(shopId);
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
        logger.info("Getting shop at ({}, {}, {}), shopId: {}", block.getLocation().getBlockX(), block.getLocation().getBlockY(), block.getLocation().getBlockZ(), shopId);
        if (shopId == null) {
            return null;
        }

        if (existingShops.containsKey(shopId)) {
            return existingShops.get(shopId);
        }

        logger.info("Shop {} at ({}, {}, {}) not loaded, loading from world...", shopId, block.getLocation().getBlockX(), block.getLocation().getBlockY(), block.getLocation().getBlockZ());
        ChestShop shop = ChestShop.ofBlock(block);
        if (shop != null) {
            existingShops.put(shopId, shop);
        }

        logger.info("Load finished, shop data: {}", shop);
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

    public boolean isTrading(Player player) {
        return playerOperations.containsKey(player.getUniqueId()) && playerOperations.get(player.getUniqueId()).getOperationType() == ShopOperationType.TRADING;
    }

    public boolean isCreatingShop(Player player) {
        return playerOperations.containsKey(player.getUniqueId()) && playerOperations.get(player.getUniqueId()).getOperationType() == ShopOperationType.CREATING;
    }

    public boolean hasOperation(Player player) {
        return playerOperations.containsKey(player.getUniqueId());
    }

    public void cancelCreation(Player player) {
        if (isCreatingShop(player)) playerOperations.remove(player.getUniqueId());
        if (player.isOnline()) {
            player.sendMessage(Component.text("商店创建操作已取消", NamedTextColor.GOLD));
        }
    }

    public void cancelTrading(Player player) {
        if (isTrading(player)) playerOperations.remove(player.getUniqueId());
        if (player.isOnline()) {
            player.sendMessage(Component.text("交易已取消", NamedTextColor.GOLD));
        }
    }

    public void cancelOperation(Player player) {
        if (!hasOperation(player)) return;
        ShopOperation operation = playerOperations.get(player.getUniqueId());
        if (operation.getOperationType() == ShopOperationType.CREATING) {
            cancelCreation(player);
        } else if (operation.getOperationType() == ShopOperationType.TRADING) {
            cancelTrading(player);
        }
    }

    public ShopOperation getOperation(Player player) {
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

    public List<ChestShop> getShopsInChunk(Chunk chunk) {
        List<ChestShop> shops = new LinkedList<>();
        for (ChestShop shop : existingShops.values()) {
            if (shop.getChestBlockLocation().getChunk().equals(chunk)) {
                shops.add(shop);
            }
        }
        return shops;
    }

    public List<ChestShop> getShops() {
        return existingShops.values().parallelStream().toList();
    }

    public void deleteShop(UUID uuid) {
        if (!existingShops.containsKey(uuid)) return;
        ChestShop shop = existingShops.get(uuid);
        shop.delete();
        existingShops.remove(uuid);
    }

    public void prepareTrade(Player player, UUID shopId) {
        ChestShop shop = existingShops.get(shopId);
        if (shop == null) {
            player.sendMessage(Component.text("商店不存在", NamedTextColor.RED));
            return;
        }

        logger.info("Player {} is preparing to trade with shop {}", player.getName(), shopId);
        playerOperations.put(player.getUniqueId(), new ShopTradingOperation(shop, player));
    }

    public void doTrade(Player player, int amount) {
        if (!hasOperation(player) || !isTrading(player)) {
            logger.warn("Player {} tried to trade without operation", player.getName());
            playerOperations.remove(player.getUniqueId());
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

            if (!(shop.getChestBlockLocation().getBlock().getState() instanceof InventoryHolder inventoryHolder)) {
                logger.warn("Player {} tried to trade with a non-inventory block", player.getName());
                playerOperations.remove(player.getUniqueId());
                return;
            }

            if (shop.getStock() < amount) {
                player.sendMessage(Component.text("商店库存不足", NamedTextColor.RED));
                playerOperations.remove(player.getUniqueId());
                return;
            }

            Inventory shopInventory = inventoryHolder.getInventory();
            Inventory playerInventory = player.getInventory();

            int successAmount = 0;
            if (shop.getShopType() == ShopType.SALE_MODE) {
                successAmount = transferItem(shopInventory, playerInventory, shop.getShopUuid(), amount);
            } else if (shop.getShopType() == ShopType.BUY_MODE) {
                successAmount = transferItem(playerInventory, shopInventory, shop.getShopUuid(), amount);
            }

            if (successAmount == 0) {
                player.sendMessage(miniMessage.deserialize("<red>交易失败，物品数量不足</red>"));
                playerOperations.remove(player.getUniqueId());
                return;
            }

            AwesomeChestShop.getEconomy().withdrawPlayer(player, shop.getPrice() * successAmount);
            AwesomeChestShop.getEconomy().depositPlayer(player, shop.getPrice() * successAmount);
            shop.updateWorld();
            player.sendMessage(miniMessage.deserialize("<green>交易成功，花费<price></green>", shop.getTagResolver()));
            playerOperations.remove(player.getUniqueId());
        });
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

        for (ItemStack shopItem : fromInventory.getContents()) {
            if (shopItem == null || !shopItem.isSimilar(shop.getItemStack())) continue;

            if (shopItem.getAmount() <= remainedAmount) {
                tradingItems.add(shopItem);
                // Transfer full stack
            } else {
                // Transfer part of stack
                remainedAmount -= shopItem.getAmount();
                ItemStack transferItem = shopItem.clone();
                transferItem.setAmount(remainedAmount);
                shopItem.setAmount(shopItem.getAmount() - remainedAmount);
                tradingItems.add(transferItem);
            }
        }

        HashMap<Integer, ItemStack> overflow = toInventory.addItem(tradingItems.toArray(new ItemStack[0]));
        int overflowAmount = 0;
        for (ItemStack item : overflow.values()) {
            overflowAmount += item.getAmount();
        }

        logger.info("OverflowAmount: {}", overflowAmount);
        return amount - overflowAmount;
    }
}
