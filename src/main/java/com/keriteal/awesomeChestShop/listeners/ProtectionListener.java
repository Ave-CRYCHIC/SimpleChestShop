package com.keriteal.awesomeChestShop.listeners;

import com.keriteal.awesomeChestShop.ChestShop;
import com.keriteal.awesomeChestShop.ShopManager;
import com.keriteal.awesomeChestShop.utils.ShopUtils;
import io.papermc.paper.event.block.BlockBreakProgressUpdateEvent;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.Chest;
import org.bukkit.block.Container;
import org.bukkit.block.DoubleChest;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockExplodeEvent;
import org.bukkit.event.entity.EntityExplodeEvent;
import org.bukkit.event.inventory.InventoryOpenEvent;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.plugin.java.JavaPlugin;
import org.slf4j.Logger;

public class ProtectionListener implements Listener {
    private final ShopManager shopManager;
    private final Logger logger;

    public ProtectionListener(JavaPlugin plugin, ShopManager shopManager) {
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
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onShopBlockBreak(BlockBreakEvent event) {
        Block block = event.getBlock();
        if (!ShopUtils.isShopBlock(block)) return;

        ChestShop shop = shopManager.getShopAt(block);
        if (shop == null) return;

        if (shop.getOwnerId().equals(event.getPlayer().getUniqueId())) {
            shop.delete();
        } else {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onShopExplode(EntityExplodeEvent event) {
        event.blockList().removeIf(ShopUtils::isShopBlock);
    }
}
