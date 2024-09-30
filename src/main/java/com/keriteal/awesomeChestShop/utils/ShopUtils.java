package com.keriteal.awesomeChestShop.utils;

import com.keriteal.awesomeChestShop.*;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.*;
import org.bukkit.entity.Player;
import org.bukkit.inventory.DoubleChestInventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;

import javax.annotation.Nullable;
import java.util.UUID;

public class ShopUtils {
    /**
     * 获取商店的ID (更慢)
     *
     * @param location
     * @return
     */
    @Nullable
    public static UUID getShopIdAt(Location location) {
        Block block = location.getBlock();
        return getShopId(block);
    }

    /**
     * 获取商店的ID (更快)
     *
     * @param block
     * @return
     */
    @Nullable
    public static UUID getShopId(Block block) {
        if (block.getState() instanceof Container container) {
            UUID shopId = NamespacedKeys.SHOP_ID.getValueFrom(container);
            if (shopId != null) return shopId;

            if (!(container.getInventory() instanceof DoubleChestInventory doubleChestInventory)) {
                return null;
            }

            Location leftLocation = doubleChestInventory.getLeftSide().getLocation();
            Location rightLocation = doubleChestInventory.getRightSide().getLocation();
            if (leftLocation == null || rightLocation == null) {
                return null;
            }

            Block leftBlock = leftLocation.getBlock();
            if (!(leftBlock.getState() instanceof Chest leftChest)) {
                return null;
            }

            PersistentDataContainer leftContainer = leftChest.getPersistentDataContainer();
            UUID leftUuid = NamespacedKeys.SHOP_ID.getValueFrom(leftContainer);
            if (leftUuid != NamespacedKeys.SHOP_ID.getDefaultValue()) return leftUuid;

            Block rightBlock = rightLocation.getBlock();
            if (!(rightBlock.getState() instanceof Chest rightChest)) {
                return null;
            }
            PersistentDataContainer rightContainer = rightChest.getPersistentDataContainer();
            UUID rightShopId = NamespacedKeys.SHOP_ID.getValueFrom(rightContainer);
            if (rightShopId != NamespacedKeys.SHOP_ID.getDefaultValue()) return rightShopId;
        } else if (block.getState() instanceof Sign sign) {
            return NamespacedKeys.SHOP_ID.getValueFrom(sign);
        }
        return null;
    }

    public static boolean isShopMainBlock(Block testingBlock) {
        if (!(testingBlock.getState() instanceof TileState state)) {
            return false;
        }

        return NamespacedKeys.SHOP_ID.hasValue(state);
    }

    public static boolean isShopBlock(Block testingBlock) {
        if (!(testingBlock.getState() instanceof TileState state)) {
            return false;
        }

        if (NamespacedKeys.SHOP_ID.hasValue(state)) {
            return true;
        }

        if (testingBlock.getState() instanceof Container container && container.getInventory() instanceof DoubleChestInventory doubleChestInventory) {
            DoubleChest doubleChest = doubleChestInventory.getHolder();
            if (doubleChest == null) {
                return false;
            }

            System.out.println("isShopBlock: double chest");
            Chest leftSide = (Chest) doubleChest.getLeftSide();
            Chest rightSide = (Chest) doubleChest.getRightSide();
            if (leftSide == null || rightSide == null) {
                System.out.println("isShopBlock: left or right side is null");
                return false;
            }

            return NamespacedKeys.SHOP_ID.hasValue(leftSide) || NamespacedKeys.SHOP_ID.hasValue(rightSide);
        }
        return false;
    }

    private static final MiniMessage miniMessage = MiniMessage.miniMessage();

    public static boolean isValidContainer(Block block) {
        return (block.getType() == Material.CHEST
                || block.getType() == Material.TRAPPED_CHEST
                || block.getType() == Material.BARREL
                || block.getType() == Material.SHULKER_BOX)
                && block.getState() instanceof Container;
    }

    public static void showManagementMenu(Player player, ChestShop shop) {
        Component message = Messages.MESSAGE_SEPARATOR.color(NamedTextColor.DARK_PURPLE);
        message = message.appendNewline()
                .append(Component.text("商店管理", NamedTextColor.GREEN));
        if (player.isOp()) {
            message = message.appendNewline()
                    .append(Component.text("商店ID:", NamedTextColor.WHITE))
                    .append(miniMessage.deserialize("[<shop-id>]", Placeholder.component("shop-id", Component.text(shop.getShopUuid().toString())))
                            .color(NamedTextColor.AQUA));
        }
        player.sendMessage(message);
    }
}
