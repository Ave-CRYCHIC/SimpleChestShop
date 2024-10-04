package com.keriteal.awesomeChestShop.shop;

import com.keriteal.awesomeChestShop.shop.operations.AbstractShopOperation;
import com.keriteal.awesomeChestShop.shop.operations.IShopOperation;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.apache.commons.lang3.math.NumberUtils;
import org.bukkit.Location;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.UUID;

public final class ShopCreationOperation extends AbstractShopOperation<Double> implements IShopOperation {
    private final Location shopLocation;
    private final ItemStack item;
    private final BlockFace signRelativeFace;

    ShopCreationOperation(ShopManager shopManager, Location shopLocation, UUID playerId, ItemStack item, BlockFace signRelativeFace) {
        super(result -> {
            double price = NumberUtils.toDouble(result, -1);
            if (price == -1) return false;
            shopManager.doCreate(playerId, price);
            return true;
        }, () -> {
            shopManager.cancelOperation(playerId);
        });
        this.shopLocation = shopLocation.clone();
        this.item = item.clone();
        this.signRelativeFace = signRelativeFace;
    }

    @Override
    public Location getShopLocation() {
        return shopLocation;
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

    @Override
    public Component getUiTitle() {
        return Component.text("输入单价：", NamedTextColor.GOLD);
    }

    @Override
    public boolean checkInput() {
        return true;
    }

    @Override
    public Double getResult() {
        return null;
    }
}
