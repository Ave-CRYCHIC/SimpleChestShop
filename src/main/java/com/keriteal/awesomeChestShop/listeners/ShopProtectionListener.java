package com.keriteal.awesomeChestShop.listeners;

import com.keriteal.awesomeChestShop.ChestShop;
import com.keriteal.awesomeChestShop.ShopManager;
import com.keriteal.awesomeChestShop.utils.ShopUtils;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.block.Block;
import org.bukkit.block.Container;
import org.bukkit.block.DoubleChest;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.entity.EntityExplodeEvent;
import org.bukkit.event.inventory.InventoryOpenEvent;
import org.bukkit.plugin.java.JavaPlugin;
import org.slf4j.Logger;

public class ShopProtectionListener implements Listener {
    private final ShopManager shopManager;
    private final Logger logger;

    public ShopProtectionListener(JavaPlugin plugin, ShopManager shopManager) {
        this.shopManager = shopManager;
        this.logger = plugin.getSLF4JLogger();
    }

    @EventHandler
    public void onShopInventoryOpen(InventoryOpenEvent event) {
        ChestShop shop = null;
        if (event.getInventory().getHolder() instanceof Container container) {
            shop = shopManager.getShopAt(container.getBlock());
        } else if (event.getInventory().getHolder() instanceof DoubleChest doubleChest) {
            logger.info("Double chest at {}", doubleChest.getLocation());
            shop = shopManager.getShopAt(doubleChest.getLocation());
        }

        if (shop != null && !shop.getOwnerId().equals(event.getPlayer().getUniqueId())) {
            event.getPlayer().sendMessage(Component.text("你不能打开别人的商店", NamedTextColor.RED));
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onShopBlockBreak(BlockBreakEvent event) {
        Block block = event.getBlock();
        if (!ShopUtils.isShopBlock(block)) return;

        ChestShop shop = shopManager.getShopAt(block);
        if (shop == null) return;

        logger.info("Deleting chest shop at {}", shop.getChestBlockLocation());
        if (shop.getOwnerId().equals(event.getPlayer().getUniqueId())) {
            logger.info("Player {} is deleting his own shop", event.getPlayer().getName());
            if (ShopUtils.isShopMainBlock(block)) {
                shop.delete();
            }
        } else {
            logger.info("Deleting rejected");
            event.getPlayer().sendMessage(Component.text("你不能破坏别人的商店", NamedTextColor.RED));
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onShopExplode(EntityExplodeEvent event) {
        event.blockList().removeIf(ShopUtils::isShopBlock);
    }
}
