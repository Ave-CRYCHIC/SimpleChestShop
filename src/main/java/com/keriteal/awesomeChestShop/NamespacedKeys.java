package com.keriteal.awesomeChestShop;

import org.bukkit.Location;
import org.bukkit.NamespacedKey;
import org.bukkit.OfflinePlayer;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.UUID;

public class NamespacedKeys {
    public static final PersistentDataAccessor<byte[], UUID> SHOP_ID = new PersistentDataAccessor<>("shop_id", DataTypes.UUID, null);
    public static final PersistentDataAccessor<byte[], UUID> SHOP_OWNER_ID = new PersistentDataAccessor<>("shop_owner", DataTypes.UUID, null);
    public static final PersistentDataAccessor<byte[], ItemStack> SHOP_ITEM = new PersistentDataAccessor<>("shop_item", DataTypes.ITEM_STACK, null);
    public static final PersistentDataAccessor<byte[], OfflinePlayer> SHOP_OWNER = new PersistentDataAccessor<>("shop_owner", DataTypes.PLAYER, null);
    public static final PersistentDataAccessor<Double, Double> SHOP_PRICE = new PersistentDataAccessor<>("shop_price", PersistentDataType.DOUBLE, Double.MAX_VALUE);
    public static final PersistentDataAccessor<Integer, Integer> SHOP_TYPE = new PersistentDataAccessor<>("shop_type", PersistentDataType.INTEGER, ShopType.SALE_MODE.ordinal());
    public static final PersistentDataAccessor<Integer, Integer> SIGN_ATTACHED_FACE = new PersistentDataAccessor<>("sign_attached_face", PersistentDataType.INTEGER, -1);
    public static final PersistentDataAccessor<int[], Location[]> SHOP_LIST = new PersistentDataAccessor<>("shop_list", DataTypes.LOCATION_ARRAY, new Location[0]);

    public final NamespacedKey shopListKey = new NamespacedKey(JavaPlugin.getPlugin(AwesomeChestShop.class), "shop_list");
}
