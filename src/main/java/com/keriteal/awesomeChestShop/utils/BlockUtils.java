package com.keriteal.awesomeChestShop.utils;

import com.keriteal.awesomeChestShop.InventorySpace;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.entity.LivingEntity;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

public class BlockUtils {
    public static InventorySpace countInventorySpace(Inventory inventory, ItemStack itemStack) {
        long itemCount = 0;
        long freeSpace = 0;
        for (ItemStack item : inventory.getContents()) {
            if (item == null || item.isEmpty()) {
                freeSpace += itemStack.getMaxStackSize();
                continue;
            }

            if (item.isSimilar(itemStack)) {
                itemCount += item.getAmount();
                freeSpace += item.getMaxStackSize() - item.getAmount();
            }
        }
        return new InventorySpace(itemCount, freeSpace);
    }

    public static Location getBlockCenterLocation(Block block) {
        Location blockCornerLocation = block.getLocation();
        return new Location(blockCornerLocation.getWorld(),
                getRelativeCoord(blockCornerLocation.getBlockX()),
                getRelativeCoord(blockCornerLocation.getBlockY()),
                getRelativeCoord(blockCornerLocation.getBlockZ()));
    }

    private static double getRelativeCoord(int i) {
        double d = i;
        d = d < 0 ? d - .5 : d + .5;
        return d;
    }

    public static Block getTargetingBlock(LivingEntity entity) {
        return entity.getTargetBlockExact(10);
    }
}
