package com.keriteal.awesomeChestShop.listeners;

import com.keriteal.awesomeChestShop.AwesomeChestShop;
import com.keriteal.awesomeChestShop.ChestShop;
import com.keriteal.awesomeChestShop.ShopManager;
import com.keriteal.awesomeChestShop.utils.ShopUtils;
import org.bukkit.block.Block;
import org.bukkit.block.Container;
import org.bukkit.block.Sign;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.persistence.PersistentDataType;

import java.util.UUID;

public class ShopUpdateListener implements Listener {
    private final ShopManager shopManager;

    public ShopUpdateListener(ShopManager shopManager) {
        this.shopManager = shopManager;
    }

    @EventHandler
    public void onCloseInventory(InventoryCloseEvent event) {
        Inventory inventory = event.getInventory();
        if (!(inventory.getHolder() instanceof Container container)) return;

        String shopId = container.getPersistentDataContainer().getOrDefault(AwesomeChestShop.getKeys().shopIdKey, PersistentDataType.STRING, "");
        if (shopId.isEmpty()) return;

        ChestShop shop = shopManager.getShop(UUID.fromString(shopId));
        if (shop == null) return;

        shop.updateWorld();
    }

    @EventHandler
    public void onShopInteracted(PlayerInteractEvent event) {
        Block clickedBlock = event.getClickedBlock();
        if (clickedBlock == null) return;
        if (!(clickedBlock.getState() instanceof Sign sign)) return;

        ChestShop shop = shopManager.getShopAt(clickedBlock);
        if (shop == null) return;

        if (shop.getOwnerId() == event.getPlayer().getUniqueId()) {
            handleManagement();
        } else {
            handleShopping();
        }
    }

    @EventHandler
    public void onShopChestExpanded(BlockPlaceEvent event) {
        Block eventBlock = event.getBlockPlaced();
        if (ShopUtils.isValidContainer(eventBlock)) return;

        // TODO 当放置一个箱子时，检测是否会变成一个大箱子
    }

    private void handleShopping() {
        // TODO 处理购物
    }

    private void handleManagement() {
        // TODO 处理管理
    }
}
