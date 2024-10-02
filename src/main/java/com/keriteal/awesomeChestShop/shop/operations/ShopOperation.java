package com.keriteal.awesomeChestShop.shop.operations;

import com.keriteal.awesomeChestShop.shop.ShopOperationType;
import org.bukkit.Location;

public interface ShopOperation {
    Location getShopLocation();

    Location getPlayerLocation();

    ShopOperationType getOperationType();
}
