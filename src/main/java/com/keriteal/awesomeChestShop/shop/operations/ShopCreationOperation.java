package com.keriteal.awesomeChestShop.shop.operations;

import com.keriteal.awesomeChestShop.shop.ShopOperationType;
import org.bukkit.Location;
import org.bukkit.block.BlockFace;
import org.bukkit.inventory.ItemStack;

public final class ShopCreationOperation implements ShopOperation {
    private final Location shopLocation;
    private final Location playerLocation;
    private final ItemStack item;
    private final BlockFace signRelativeFace;

    public ShopCreationOperation(Location shopLocation, Location playerLocation, ItemStack item, BlockFace signRelativeFace) {
        this.shopLocation = shopLocation.clone();
        this.playerLocation = playerLocation.clone();
        this.item = item.clone();
        this.signRelativeFace = signRelativeFace;
    }

    @Override
    public Location getShopLocation() {
        return shopLocation;
    }

    @Override
    public Location getPlayerLocation() {
        return playerLocation;
    }

    public ItemStack getItem() {
        return item;
    }

    public BlockFace getSignRelativeFace() {
        return signRelativeFace;
    }

    @Override
    public ShopOperationType getOperationType() {
        return ShopOperationType.CREATING;
    }
}
