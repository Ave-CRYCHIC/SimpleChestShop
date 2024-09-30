package com.keriteal.awesomeChestShop;

import com.keriteal.awesomeChestShop.utils.ShopUtils;
import io.papermc.paper.threadedregions.scheduler.RegionScheduler;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.OfflinePlayer;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;

import java.util.*;

import static com.keriteal.awesomeChestShop.Messages.MESSAGE_CREATE_SUCCESS;

public class ShopManager {
    private final JavaPlugin plugin;
    private final Map<OfflinePlayer, ChestShop> pendingShops = new HashMap<>();
    private final Map<UUID, ChestShop> existingShops = new HashMap<>();
    private final Logger logger;
    private final List<UUID> unProtectedShop = new LinkedList<>();
    private boolean isProtectionEnabled = true;

    public ShopManager(JavaPlugin javaPlugin) {
        this.plugin = javaPlugin;
        this.logger = plugin.getSLF4JLogger();
    }

    public boolean hasPendingShop(Player player) {
        return pendingShops.containsKey(player);
    }

    public void cancelPendingShop(Player player) {
        pendingShops.remove(player);
    }

    public void requestShop(ChestShop shop) {
        pendingShops.put(plugin.getServer().getPlayer(shop.getOwnerId()), shop);
    }

    public void createShop(Player player, double price) {
        if (!pendingShops.containsKey(player)) return;
        ChestShop shop = pendingShops.get(player);
        if (shop == null) return;

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
                pendingShops.remove(player);
            }
        });
    }

    public void cancelShopCreation(Player player) {
        pendingShops.remove(player);
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
    public ChestShop getShopAt(@NotNull Block block) {
        UUID shopId = ShopUtils.getShopId(block);
        logger.info("Getting shop at ({}, {}, {}), shopId: {}", block.getLocation().getBlockX(), block.getLocation().getBlockY(), block.getLocation().getBlockZ(), shopId);
        if (shopId == null) {
            return null;
        }

        if (existingShops.containsKey(shopId)) {
            logger.info("Shop {} at ({}, {}, {}) already loaded", shopId, block.getLocation().getBlockX(), block.getLocation().getBlockY(), block.getLocation().getBlockZ());
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
    public ChestShop getShopAt(@NotNull Location location) {
        return getShopAt(location.getBlock());
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

    public void changeShopType(@NotNull UUID shopId, @NotNull ShopType type) {
        if (!existingShops.containsKey(shopId)) {
            logger.warn("Changing shop {}, but shop not loaded or created", shopId);
            return;
        }
        ChestShop shop = existingShops.get(shopId);
        logger.info("Changing shop {} type from {} to {}", shopId, shop.getShopType(), type);
        shop.setShopType(type);
    }
}
