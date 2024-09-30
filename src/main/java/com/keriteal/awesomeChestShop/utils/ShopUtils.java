package com.keriteal.awesomeChestShop.utils;

import com.keriteal.awesomeChestShop.AwesomeChestShop;
import com.keriteal.awesomeChestShop.ChestShop;
import com.keriteal.awesomeChestShop.Messages;
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
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;

import javax.annotation.Nullable;
import java.util.Optional;
import java.util.UUID;

public class ShopUtils {
    public static Optional<Sign> getShopSign(Block shopBlock) {
        if (shopBlock == null) return Optional.empty();
        if (shopBlock.getState() instanceof Sign signState && signState.getPersistentDataContainer().has(AwesomeChestShop.getKeys().shopOwnerKey)) {
            return Optional.of(signState);
        } else if (shopBlock.getState() instanceof Chest chestState) {
            int signAttachedFace = chestState.getPersistentDataContainer().getOrDefault(AwesomeChestShop.getKeys().signAttachedFace, PersistentDataType.INTEGER, -1);
            if (signAttachedFace == -1) return Optional.empty();

            Block signBlock = shopBlock.getRelative(BlockFace.values()[signAttachedFace]);
            if (signBlock.getState() instanceof Sign signState && signState.getPersistentDataContainer().has(AwesomeChestShop.getKeys().shopOwnerKey)) {
                return Optional.of(signState);
            }
        }

        return Optional.empty();
    }

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
        if (block.getState() instanceof Chest chest) {
            PersistentDataContainer container = chest.getPersistentDataContainer();
            String shopId = container.get(AwesomeChestShop.getKeys().shopIdKey, PersistentDataType.STRING);
            if (shopId == null) return null;
            if (!(chest.getInventory() instanceof DoubleChestInventory doubleChestInventory)) return null;

            Location leftLocation = doubleChestInventory.getLeftSide().getLocation();
            Location rightLocation = doubleChestInventory.getRightSide().getLocation();
            if (leftLocation == null || rightLocation == null) return null;

            Block leftBlock = leftLocation.getBlock();
            Block rightBlock = rightLocation.getBlock();
            if (!(leftBlock.getState() instanceof Chest leftChest) || !(rightBlock.getState() instanceof Chest rightChest))
                return null;

            PersistentDataContainer leftContainer = leftChest.getPersistentDataContainer();
            PersistentDataContainer rightContainer = rightChest.getPersistentDataContainer();
            if (leftContainer.has(AwesomeChestShop.getKeys().shopIdKey, PersistentDataType.STRING)) {
                return UUID.fromString(leftContainer.getOrDefault(AwesomeChestShop.getKeys().shopIdKey, PersistentDataType.STRING, ""));
            } else if (rightContainer.has(AwesomeChestShop.getKeys().shopIdKey, PersistentDataType.STRING)) {
                return UUID.fromString(rightContainer.getOrDefault(AwesomeChestShop.getKeys().shopIdKey, PersistentDataType.STRING, ""));
            }
        } else if (block.getState() instanceof Sign sign) {
            PersistentDataContainer container = sign.getPersistentDataContainer();
            if (container.has(AwesomeChestShop.getKeys().shopIdKey, PersistentDataType.STRING)) {
                return UUID.fromString(container.getOrDefault(AwesomeChestShop.getKeys().shopIdKey, PersistentDataType.STRING, ""));
            }
        }
        return null;
    }

    public static boolean isShopBlock(Block testingBlock) {
        if (testingBlock.getState() instanceof TileState state) {
            return state.getPersistentDataContainer().has(AwesomeChestShop.getKeys().shopIdKey);
        }

        return false;
    }

    /**
     * 检查一个地方是否可以创建商店
     *
     * @param checkingLocation
     * @return
     */
    public static boolean checkShopAvailable(Location checkingLocation) {
        Block checkingBlock = checkingLocation.getBlock();
        if (isValidContainer(checkingBlock)) return false;

        if (checkingBlock.getState() instanceof Chest chest) {
            if (chest.getInventory() instanceof DoubleChestInventory doubleChest) {
                World world = checkingLocation.getWorld();
                Block leftSide = world.getBlockAt(doubleChest.getLeftSide().getLocation());
                Block rightSide = world.getBlockAt(doubleChest.getRightSide().getLocation());

                if (leftSide.getState() instanceof Chest leftChest
                        && leftChest.getPersistentDataContainer().has(AwesomeChestShop.getKeys().shopIdKey)) {
                    return false;
                }

                if (rightSide.getState() instanceof Chest rightChest
                        && rightChest.getPersistentDataContainer().has(AwesomeChestShop.getKeys().shopIdKey)) {
                    return false;
                }
            } else return !chest.getPersistentDataContainer().has(AwesomeChestShop.getKeys().shopIdKey);
        }

        return true;
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
