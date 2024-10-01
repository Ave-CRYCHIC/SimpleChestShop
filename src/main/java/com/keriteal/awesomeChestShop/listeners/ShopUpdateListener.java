package com.keriteal.awesomeChestShop.listeners;

import com.keriteal.awesomeChestShop.ChestShop;
import com.keriteal.awesomeChestShop.NamespacedKeys;
import com.keriteal.awesomeChestShop.ShopManager;
import com.keriteal.awesomeChestShop.utils.ShopUtils;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.block.Container;
import org.bukkit.block.Sign;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.plugin.java.JavaPlugin;
import org.slf4j.Logger;

import java.util.UUID;

public class ShopUpdateListener implements Listener {
    private final ShopManager shopManager;
    private final JavaPlugin plugin;
    private final Logger logger;

    public ShopUpdateListener(JavaPlugin plugin, ShopManager shopManager) {
        this.shopManager = shopManager;
        this.plugin = plugin;
        this.logger = plugin.getSLF4JLogger();
    }

    /**
     * 关闭箱子时更新商店
     */
    @EventHandler
    public void onCloseInventory(InventoryCloseEvent event) {
        Inventory inventory = event.getInventory();
        if (!(inventory.getHolder() instanceof Container container)) return;

        UUID shopId = NamespacedKeys.SHOP_ID.getValueFrom(container);
        if (shopId == null) return;

        ChestShop shop = shopManager.getShop(shopId);
        if (shop == null) return;

        shop.updateWorld();
    }

    @EventHandler
    public void onShopInteracted(PlayerInteractEvent event) {
        Block clickedBlock = event.getClickedBlock();
        if (clickedBlock == null || !(clickedBlock.getState() instanceof Sign sign)) return;
        if (event.getAction() != Action.RIGHT_CLICK_BLOCK) return;

        ChestShop shop = shopManager.getShopAt(clickedBlock);
        if (shop == null) return;

        Location location = clickedBlock.getLocation();
        plugin.getSLF4JLogger().debug("Player {}[{}] interacting a shop at {}, {}, {}, owner: {}, shop id: {}",
                event.getPlayer().getName(),
                event.getPlayer().getUniqueId(),
                location.getBlockX(), location.getBlockY(), location.getBlockZ(),
                shop.getOwnerId(),
                shop.getShopUuid());

        if (!shop.getOwnerId().equals(event.getPlayer().getUniqueId())) {
            handleShopping();
            return;
        }

        if (event.getPlayer().isSneaking()) {
            handleManagement();
            return;
        }

        logger.info("Changing shop type at {}, {}, {}", location.getBlockX(), location.getBlockY(), location.getBlockZ());
        shop.setShopType(shop.getShopType().nextType());
    }

    @EventHandler
    public void onShopChestExpanded(BlockPlaceEvent event) {
        Block eventBlock = event.getBlockPlaced();
        if (ShopUtils.isValidContainer(eventBlock)) return;

        // TODO 当放置一个箱子时，检测是否会变成一个大箱子
    }

    private void handleShopping() {
        // TODO 处理购物
        logger.info("Shopping in shop");
    }

    private void handleManagement() {
        // TODO 处理管理
        logger.info("Managing shop");
    }
}
