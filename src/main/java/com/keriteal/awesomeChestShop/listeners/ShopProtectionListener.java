package com.keriteal.awesomeChestShop.listeners;

import com.keriteal.awesomeChestShop.shop.ChestShop;
import com.keriteal.awesomeChestShop.shop.ShopManager;
import com.keriteal.awesomeChestShop.utils.ShopUtils;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.block.Block;
import org.bukkit.block.Container;
import org.bukkit.block.DoubleChest;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.minecart.HopperMinecart;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.entity.EntityExplodeEvent;
import org.bukkit.event.inventory.InventoryAction;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryMoveItemEvent;
import org.bukkit.event.inventory.InventoryOpenEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.plugin.java.JavaPlugin;
import org.slf4j.Logger;

import java.util.UUID;

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
            shop = shopManager.loadShopAt(container.getBlock());
        } else if (event.getInventory().getHolder() instanceof DoubleChest doubleChest) {
            logger.info("Double chest at {}", doubleChest.getLocation());
            shop = shopManager.loadShopAt(doubleChest.getLocation());
        }

        if (shop != null && !shop.getOwnerId().equals(event.getPlayer().getUniqueId())) {
            event.getPlayer().sendMessage(Component.text("你不能打开别人的商店", NamedTextColor.RED));
            event.setCancelled(true);
        }
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onItemDrag(final InventoryClickEvent event) {
        Inventory clickedInventory = event.getClickedInventory();
        if (!event.getAction().equals(InventoryAction.MOVE_TO_OTHER_INVENTORY)) {
            if (clickedInventory == null) return;

            logger.info("Item click event, holder: {}", clickedInventory.getHolder());
            if (!(clickedInventory.getHolder() instanceof ChestShop)) return;

            event.setCancelled(true);
        } else {
            if (!(event.getView().getTopInventory().getHolder() instanceof ChestShop)) return;

            event.setCancelled(true);
        }
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onItemTransfer(final InventoryMoveItemEvent event) {
        logger.info("Item transfer event, source: {}, dest: {}", event.getSource().getHolder(), event.getDestination().getHolder());
        InventoryHolder sourceHolder = event.getSource().getHolder();
        InventoryHolder destHolder = event.getDestination().getHolder();
        Block shopBlock;
        InventoryHolder anotherHolder;

        // 判断格子类型
        if (sourceHolder instanceof DoubleChest doubleChest) {
            shopBlock = doubleChest.getLocation().getBlock();
            anotherHolder = destHolder;
        } else if (sourceHolder instanceof Container container) {
            shopBlock = container.getLocation().getBlock();
            anotherHolder = destHolder;
        } else if (destHolder instanceof DoubleChest doubleChest) {
            shopBlock = doubleChest.getLocation().getBlock();
            anotherHolder = sourceHolder;
        } else if (destHolder instanceof Container container) {
            shopBlock = container.getLocation().getBlock();
            anotherHolder = sourceHolder;
        } else {
            return;
        }

        if (!ShopUtils.isShopBlock(shopBlock)) return;
        if (anotherHolder instanceof HumanEntity entity) {
            UUID id = entity.getUniqueId();
            ChestShop shop = shopManager.loadShopAt(shopBlock);
            if (shop == null) return;

            if (id.equals(shop.getOwnerId())) return;
            event.setCancelled(true);
        } else if (anotherHolder instanceof HopperMinecart) {
            event.setCancelled(true);
            return;
        }
    }

    @EventHandler
    public void onShopBlockBreak(BlockBreakEvent event) {
        Block block = event.getBlock();
        if (!ShopUtils.isShopBlock(block)) return;

        ChestShop shop = shopManager.loadShopAt(block);
        if (shop == null) return;

        logger.info("Deleting chest shop at {}", shop.getChestBlockLocation());
        if (shop.getOwnerId().equals(event.getPlayer().getUniqueId())) {
            logger.info("Player {} is deleting his own shop", event.getPlayer().getName());
            if (ShopUtils.isShopMainBlock(block)) {
                shopManager.deleteShop(shop.getShopUuid());
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
