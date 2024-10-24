package com.keriteal.awesomeChestShop.utils;

import com.keriteal.awesomeChestShop.shop.ChestShop;
import com.keriteal.awesomeChestShop.Messages;
import com.keriteal.awesomeChestShop.NamespacedKeys;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.*;
import org.bukkit.block.data.type.WallSign;
import org.bukkit.entity.Player;
import org.bukkit.inventory.DoubleChestInventory;
import org.bukkit.persistence.PersistentDataContainer;

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

    public static boolean isShopSign(@Nullable Block targetBlock) {
        if (targetBlock == null || !(targetBlock.getState() instanceof Sign sign)) return false;
        return NamespacedKeys.SHOP_ID.hasValue(sign);
    }

    public static boolean isShopBlock(@Nullable Block testingBlock) {
        if (testingBlock == null) return false;
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

            Chest leftSide = (Chest) doubleChest.getLeftSide();
            Chest rightSide = (Chest) doubleChest.getRightSide();
            if (leftSide == null || rightSide == null) {
                return false;
            }

            return NamespacedKeys.SHOP_ID.hasValue(leftSide) || NamespacedKeys.SHOP_ID.hasValue(rightSide);
        }
        return false;
    }

    public static boolean isValidContainer(Block block) {
        return (block.getType() == Material.CHEST
                || block.getType() == Material.TRAPPED_CHEST
                || block.getType() == Material.BARREL
                || block.getType() == Material.SHULKER_BOX)
                && block.getState() instanceof Container;
    }
}
