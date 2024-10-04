package com.keriteal.awesomeChestShop.shop.operations;

import com.keriteal.awesomeChestShop.shop.ShopOperationType;
import org.bukkit.Location;

public interface IShopOperation {
    Location getShopLocation();

    ShopOperationType getOperationType();
}
