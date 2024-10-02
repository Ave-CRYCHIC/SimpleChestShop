package com.keriteal.awesomeChestShop.listeners;

import com.keriteal.awesomeChestShop.*;
import com.keriteal.awesomeChestShop.shop.ChestShop;
import com.keriteal.awesomeChestShop.shop.ShopManager;
import com.keriteal.awesomeChestShop.utils.ItemUtils;
import com.keriteal.awesomeChestShop.utils.ShopUtils;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.event.HoverEvent;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.minimessage.tag.resolver.Formatter;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.OfflinePlayer;
import org.bukkit.block.*;
import org.bukkit.block.Container;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.world.ChunkLoadEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.plugin.java.JavaPlugin;
import org.slf4j.Logger;

import java.util.UUID;

public class ShopUpdateListener implements Listener {
    private final ShopManager shopManager;
    private final JavaPlugin plugin;
    private final Logger logger;
    private final MiniMessage miniMessage = MiniMessage.miniMessage();

    public ShopUpdateListener(AwesomeChestShop plugin, ShopManager shopManager) {
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
    public void onChunkLoaded(ChunkLoadEvent event) {
        for (BlockState state : event.getChunk().getTileEntities()) {
            if (!(state instanceof Sign)) continue;

            ChestShop shop = shopManager.loadShopAt(state.getBlock());
            if (shop != null) {
                shop.updateShownItem();
            }
        }
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        for (ChestShop shop : shopManager.getShops()) {
            shop.updateShownItem(event.getPlayer());
        }
    }
}
