package com.keriteal.awesomeChestShop.shop.operations;

import com.keriteal.awesomeChestShop.shop.ChestShop;
import com.keriteal.awesomeChestShop.shop.ShopOperationType;
import org.bukkit.Location;
import org.bukkit.entity.Player;

public final class ShopTradingOperation implements ShopOperation {
    private final Location shopLocation;
    private final Location playerLocation;

    public ShopTradingOperation(Location shopLocation, Location playerLocation) {
        this.shopLocation = shopLocation;
        this.playerLocation = playerLocation;
    }

    public ShopTradingOperation(ChestShop shop, Player player) {
        this.shopLocation = shop.getChestBlockLocation().clone();
        this.playerLocation = player.getLocation().clone();
    }

    @Override
    public Location getShopLocation() {
        return shopLocation;
    }

    @Override
    public Location getPlayerLocation() {
        return playerLocation;
    }

    @Override
    public ShopOperationType getOperationType() {
        return ShopOperationType.TRADING;
    }
}
