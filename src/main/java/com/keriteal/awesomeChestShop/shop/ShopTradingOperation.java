package com.keriteal.awesomeChestShop.shop;

import com.keriteal.awesomeChestShop.shop.operations.AbstractShopOperation;
import com.keriteal.awesomeChestShop.shop.operations.IShopOperation;
import net.kyori.adventure.text.Component;
import org.apache.commons.lang3.math.NumberUtils;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;

import java.util.UUID;

public final class ShopTradingOperation extends AbstractShopOperation<Integer> implements IShopOperation {
    private final Location shopLocation;

    ShopTradingOperation(ShopManager shopManager, UUID playerId, Location shopLocation) {
        super(result -> {
            int amount = NumberUtils.toInt(result, -1);
            if (amount == -1) return false;

            shopManager.doTrade(playerId, amount);

            return true;
        }, () -> {
            shopManager.cancelOperation(playerId);
        });
        this.shopLocation = shopLocation.clone();
    }

    @Override
    public Location getShopLocation() {
        return shopLocation;
    }

    @Override
    public ShopOperationType getOperationType() {
        return ShopOperationType.TRADING;
    }

    @Override
    public Component getUiTitle() {
        return Component.text("输入购买数量：");
    }

    @Override
    public boolean checkInput() {
        return true;
    }

    @Override
    public Integer getResult() {
        return -1;
    }
}
