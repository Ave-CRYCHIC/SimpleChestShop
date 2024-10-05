package com.keriteal.awesomeChestShop.listeners;

import com.keriteal.awesomeChestShop.Messages;
import com.keriteal.awesomeChestShop.ShopType;
import com.keriteal.awesomeChestShop.shop.ChestShop;
import com.keriteal.awesomeChestShop.shop.ShopManager;
import com.keriteal.awesomeChestShop.shop.operations.AbstractShopOperation;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.block.Sign;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;

import java.util.UUID;

public class ShopOperationListener implements Listener {
    private final ShopManager shopManager;
    private final Logger logger;
    private final MiniMessage miniMessage = MiniMessage.miniMessage();

    public ShopOperationListener(JavaPlugin plugin, ShopManager shopManager) {
        this.shopManager = shopManager;
        this.logger = plugin.getSLF4JLogger();
    }

    @EventHandler
    public void onPlayerCancelOperation(InventoryCloseEvent event) {
        if (event.getView().getTopInventory().getHolder() instanceof AbstractShopOperation<?> operation) {
            operation.onClose();
        }
    }

    @EventHandler
    public void onShopInteracted(PlayerInteractEvent event) {
        Block clickedBlock = event.getClickedBlock();
        if (clickedBlock == null || !(clickedBlock.getState() instanceof Sign)) return;
        if (event.getAction() != Action.RIGHT_CLICK_BLOCK) return;

        ChestShop shop = shopManager.loadShopAt(clickedBlock);
        if (shop == null) return;

        final Player player = event.getPlayer();
        final Location location = clickedBlock.getLocation();

        if (!shop.getOwnerId().equals(player.getUniqueId())) {
            if (player.isSneaking()) {
                shopManager.prepareTrade(player, shop.getShopUuid());
                event.setCancelled(true);
            } else {
                sendTradeMessage(player, shop.getShopUuid());
            }
            return;
        }

        if (event.getPlayer().isSneaking()) {
            sendManagementMessage(event.getPlayer(), shop.getShopUuid());
            return;
        }

        //logger.info("Changing shop type at {}, {}, {}", location.getBlockX(), location.getBlockY(), location.getBlockZ());
        shop.setShopType(shop.getShopType().nextType());
    }

    private void sendManagementMessage(Player player, UUID shopId) {
        ChestShop shop = shopManager.getShop(shopId);
        if (shop == null) {
            player.sendMessage(Messages.MISSING_SHOP);
            return;
        }
        player.sendMessage(miniMessage.deserialize(
                "<#e3a7bc><currency_raw></#e3a7bc><#edeed8>======== <#e3a7bc><b>管理</b></#e3a7bc> ========</#edeed8><#e3a7bc><currency_raw></#e3a7bc><br>" +
                        "<#95d064>店主：</#95d064><#ca7534><owner></#ca7534><br>" +
                        "<#95d064>类型：</#95d064><shop_mode><br>" +
                        "<#95d064>物品：</#95d064><item_preview><#8348bf>[预览]</#8348bf></item_preview><br>" +
                        "<#95d064>单价：</#95d064><currency><price><br>" +
                        "<#95d064>库存：</#95d064><stock><br>" +
                        "<#95d064>位置：</#95d064><#b59f7b><chest_location></#b59f7b><br>" +
                        "<#e3a7bc><currency_raw></#e3a7bc><#edeed8>=====================</#edeed8><#e3a7bc><currency_raw></#e3a7bc>",
                shop.getTagResolver()));
    }

    private Component buildChangeModeText(@NotNull Player player, @NotNull ChestShop shop, @NotNull ShopType shopType) {
        return shopType.getColoredName()
                .hoverEvent(Component.text("点击切换商店类型为", NamedTextColor.WHITE).append(shopType.getColoredName()))
                .clickEvent(ClickEvent.runCommand(String.format("/shop modify type %s %d %d %d", shopType.getCommandName(), shop.getChestBlockLocation().getBlockX(), shop.getChestBlockLocation().getBlockY(), shop.getChestBlockLocation().getBlockZ())));
    }

    private void sendTradeMessage(Player player, UUID shopId) {
        ChestShop shop = shopManager.getShop(shopId);
        if (shop == null) {
            logger.info("Shop {} not found", shopId);
            return;
        }

        final Component generatedMessage = miniMessage.deserialize(
                "<#e3a7bc><currency_raw></#e3a7bc><#edeed8>======== <#e3a7bc><b>交易</b></#e3a7bc> ========</#edeed8><#e3a7bc><currency_raw></#e3a7bc><br>" +
                        "<#95d064>店主：</#95d064><#ca7534><owner></#ca7534><br>" +
                        "<#95d064>类型：</#95d064><shop_mode><br>" +
                        "<#95d064>物品：</#95d064><item_preview><#8348bf>[预览]</#8348bf></item_preview><br>" +
                        "<#95d064>单价：</#95d064><currency><price><br>" +
                        "<#95d064>库存：</#95d064><stock><br>" +
                        "<#95d064>位置：</#95d064><#b59f7b><chest_location></#b59f7b><br>" +
                        "<#e3a7bc><currency_raw></#e3a7bc><#edeed8>=====================</#edeed8><#e3a7bc><currency_raw></#e3a7bc>", shop.getTagResolver());
        player.sendMessage(generatedMessage);
    }
}
