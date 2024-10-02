package com.keriteal.awesomeChestShop.listeners;

import com.keriteal.awesomeChestShop.AwesomeChestShop;
import com.keriteal.awesomeChestShop.Messages;
import com.keriteal.awesomeChestShop.ShopType;
import com.keriteal.awesomeChestShop.shop.ChestShop;
import com.keriteal.awesomeChestShop.shop.ShopManager;
import com.keriteal.awesomeChestShop.shop.ShopOperationType;
import com.keriteal.awesomeChestShop.shop.operations.ShopOperation;
import com.keriteal.awesomeChestShop.utils.ItemUtils;
import io.papermc.paper.event.player.AsyncChatEvent;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.event.HoverEvent;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.minimessage.tag.resolver.Formatter;
import org.apache.commons.lang3.math.NumberUtils;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.OfflinePlayer;
import org.bukkit.block.Block;
import org.bukkit.block.Sign;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.plugin.java.JavaPlugin;
import org.slf4j.Logger;

import java.util.UUID;

public class ShopOperationListener implements Listener {
    private final ShopManager shopManager;
    private final JavaPlugin plugin;
    private final Logger logger;
    private final MiniMessage miniMessage = MiniMessage.miniMessage();

    public ShopOperationListener(JavaPlugin plugin, ShopManager shopManager) {
        this.shopManager = shopManager;
        this.plugin = plugin;
        this.logger = plugin.getSLF4JLogger();
    }

    @EventHandler
    public void onPlayerCancelOperation(PlayerMoveEvent event) {
        if (!shopManager.hasOperation(event.getPlayer())) return;

        ShopOperation operation = shopManager.getOperation(event.getPlayer());
        if (operation.getPlayerLocation().distance(event.getTo()) > 2) {
            shopManager.cancelOperation(event.getPlayer());
        }
    }

    @EventHandler
    public void onShopInteracted(PlayerInteractEvent event) {
        Block clickedBlock = event.getClickedBlock();
        if (clickedBlock == null || !(clickedBlock.getState() instanceof Sign)) return;
        if (event.getAction() != Action.RIGHT_CLICK_BLOCK) return;

        ChestShop shop = shopManager.loadShopAt(clickedBlock);
        if (shop == null) return;

        Player player = event.getPlayer();
        Location location = clickedBlock.getLocation();
        plugin.getSLF4JLogger().debug("Player {}[{}] interacting a shop at {}, {}, {}, owner: {}, shop id: {}",
                event.getPlayer().getName(),
                event.getPlayer().getUniqueId(),
                location.getBlockX(), location.getBlockY(), location.getBlockZ(),
                shop.getOwnerId(),
                shop.getShopUuid());

        if (!shop.getOwnerId().equals(player.getUniqueId())) {
            logger.info("Operation: Trade");
            sendTradeMessage(player, shop.getShopUuid());
            shopManager.prepareTrade(player, shop.getShopUuid());
            return;
        }

        if (event.getPlayer().isSneaking()) {
            logger.info("Operation: Manage");
            sendManagementMessage(event.getPlayer(), shop.getShopUuid());
            return;
        }

        logger.info("Changing shop type at {}, {}, {}", location.getBlockX(), location.getBlockY(), location.getBlockZ());
        shop.setShopType(shop.getShopType().nextType());
    }

    private void sendManagementMessage(Player player, UUID shopId) {
        ChestShop shop = shopManager.getShop(shopId);
        Component message = Messages.MESSAGE_SEPARATOR_MANAGE;
    }

    private void sendTradeMessage(Player player, UUID shopId) {
        ChestShop shop = shopManager.getShop(shopId);
        OfflinePlayer owner = Bukkit.getOfflinePlayer(shop.getOwnerId());
        Component message = Messages.MESSAGE_SEPARATOR_SHOP;
        Component messageOwner = Messages.SHOP_TAG_OWNER
                .append(Component.text(owner.getName(), NamedTextColor.GOLD)
                        .hoverEvent(HoverEvent.showText(Messages.HOVER_PRIVATE_CHAT))
                        .clickEvent(ClickEvent.suggestCommand("/tell " + owner.getName() + " ")));
        Location chestLocation = shop.getChestBlockLocation();
        Component messageLocation = Messages.SHOP_TAG_LOCATION
                .append(miniMessage.deserialize("<x>, <y>, <z>",
                                Formatter.number("x", chestLocation.getBlockX()),
                                Formatter.number("y", chestLocation.getBlockY()),
                                Formatter.number("z", chestLocation.getBlockZ()))
                        .color(NamedTextColor.AQUA)
                        .hoverEvent(HoverEvent.showText(Messages.HOVER_COPY))
                        .clickEvent(ClickEvent.copyToClipboard(chestLocation.getBlockX() + " " + chestLocation.getBlockY() + " " + chestLocation.getBlockZ())));
        Component messageMode = Messages.SHOP_TAG_MODE
                .append(shop.getShopType().getColoredName());
        Component messageItem = Messages.SHOP_TAG_ITEM
                .append(ItemUtils.getItemName(shop.getItemStack()))
                .appendSpace()
                .append(Messages.SHOP_TAG_ITEM_PREVIEW.hoverEvent(shop.getItemStack()));
        Component messagePrice = Messages.SHOP_TAG_PRICE
                .append(Component.text(AwesomeChestShop.getEconomy().currencyNamePlural(), Messages.CURRENCY_SIGN_COLOR))
                .appendSpace()
                .append(Component.text(shop.getPrice(), NamedTextColor.GOLD));
        Component messageStock;
        if (shop.getShopType() == ShopType.BUY_MODE) {
            messageStock = Messages.SHOP_TAG_FREE_SPACE.append(Component.text(shop.getStock(), NamedTextColor.GOLD));
        } else {
            messageStock = Messages.SHOP_TAG_STOCK.append(Component.text(shop.getStock(), NamedTextColor.GOLD));
        }

        player.sendMessage(message
                .appendNewline().append(messageOwner)
                .appendNewline().append(messageMode)
                .appendNewline().append(messageItem)
                .appendNewline().append(messagePrice)
                .appendNewline().append(messageStock)
                .appendNewline().append(messageLocation)
                .appendNewline().append(Messages.MESSAGE_SEPARATOR)
                .appendNewline().append(Messages.INPUT_AMOUNT));
    }

    @EventHandler
    public void onTradeOrCreate(AsyncChatEvent event) {
        Player player = event.getPlayer();

        if (!shopManager.hasOperation(player)) return;

        ShopOperation operation = shopManager.getOperation(player);

        if (!(event.message() instanceof TextComponent textComponent)) {
            player.sendMessage(Messages.INVALID_FORMAT);
            return;
        }

        logger.info("Player {}[{}] input: {}, operation: {}", player.getName(), player.getUniqueId(), textComponent.content(), operation.getOperationType());

        if (operation.getOperationType() == ShopOperationType.CREATING) {
            logger.info("Has create operation");
            event.setCancelled(true);

            double price = NumberUtils.toDouble(textComponent.content(), -1);
            if (price == -1 || Double.isNaN(price)) {
                player.sendMessage(Messages.INVALID_FORMAT);
                return;
            }

            shopManager.doCreate(player, price);
        } else if (operation.getOperationType() == ShopOperationType.TRADING) {
            logger.info("Has trade operation");
            event.setCancelled(true);

            int amount = NumberUtils.toInt(textComponent.content(), -1);
            if (amount == -1) {
                player.sendMessage(Messages.INVALID_FORMAT);
                return;
            }

            shopManager.doTrade(player, amount);
        }
    }
}
