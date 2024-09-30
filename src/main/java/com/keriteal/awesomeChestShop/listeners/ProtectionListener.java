package com.keriteal.awesomeChestShop.listeners;

import com.keriteal.awesomeChestShop.ChestShop;
import com.keriteal.awesomeChestShop.ShopManager;
import com.keriteal.awesomeChestShop.utils.ShopUtils;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
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
    public void onShopBlockBreak(BlockBreakEvent event) {
        Block block = event.getBlock();
        if (block.getType() != Material.OAK_WALL_SIGN && !ShopUtils.isValidContainer(block)) return;
        if (!ShopUtils.isShopBlock(block)) return;

        ChestShop shop = shopManager.getShop(ShopUtils.getShopId(block));
        if (shop == null) return;

        if (shop.getOwnerId() == event.getPlayer().getUniqueId()) {
            shop.delete();
            return;
        }

        event.setCancelled(true);
    }
}
