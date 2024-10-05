package com.keriteal.awesomeChestShop.listeners;

import com.keriteal.awesomeChestShop.shop.ChestShop;
import com.keriteal.awesomeChestShop.shop.ShopManager;
import org.bukkit.block.BlockState;
import org.bukkit.block.Container;
import org.bukkit.block.DoubleChest;
import org.bukkit.block.Sign;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.world.ChunkLoadEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.plugin.java.JavaPlugin;
import org.slf4j.Logger;

public class ShopUpdateListener implements Listener {
    private final ShopManager shopManager;
    private final Logger logger;

    public ShopUpdateListener(JavaPlugin plugin, ShopManager shopManager) {
        this.shopManager = shopManager;
        this.logger = plugin.getSLF4JLogger();
    }

    /**
     * 关闭箱子时更新商店
     */
    @EventHandler
    public void onCloseInventory(InventoryCloseEvent event) {
        Inventory inventory = event.getInventory();
        ChestShop shop;
        if (inventory.getHolder() instanceof Container container) {
            shop = shopManager.loadShopAt(container.getBlock());
        } else if (inventory.getHolder() instanceof DoubleChest doubleChest) {
            shop = shopManager.loadShopAt(doubleChest);
        } else {
            return;
        }
        if (shop == null) return;

        shop.updateWorld();
    }

    /**
     * 区块加载时加载区块内商店
     */
    @EventHandler
    public void onChunkLoaded(ChunkLoadEvent event) {
        int shopCount = 0;
        long start = System.currentTimeMillis();
        for (BlockState state : event.getChunk().getTileEntities()) {
            if (!(state instanceof Sign)) continue;

            ChestShop shop = shopManager.loadShopAt(state.getBlock());
            if (shop != null) {
                shopCount++;
                shop.updateTopPreviewItem();
            }
        }
        if (shopCount > 0) {
//            logger.info("加载位于 {}, {} 区块的商店，共 {} 个，用时：{} ms", event.getChunk().getX(), event.getChunk().getZ(), shopCount, System.currentTimeMillis() - start);
        }
    }

    /**
     * 玩家加入时发送商店预览物品数据包
     */
    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        for (ChestShop shop : shopManager.getShops()) {
            shop.updateTopPreviewItem(event.getPlayer());
        }
    }
}
