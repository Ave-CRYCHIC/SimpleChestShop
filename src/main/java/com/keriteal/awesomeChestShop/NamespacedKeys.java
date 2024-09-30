package com.keriteal.awesomeChestShop;

import org.bukkit.NamespacedKey;
import org.bukkit.plugin.java.JavaPlugin;

public class NamespacedKeys {
    /**
     * 商店ID，存在告示牌，箱子，展示物品上
     */
    public final NamespacedKey shopIdKey = new NamespacedKey(JavaPlugin.getPlugin(AwesomeChestShop.class), "shop_id");

    /**
     * 创建者 UUID，存在告示牌上
     */
    public final NamespacedKey shopOwnerKey = new NamespacedKey(JavaPlugin.getPlugin(AwesomeChestShop.class), "shop_owner");

    /**
     * 商店出售的物品，存在告示牌上
     */
    public final NamespacedKey shopItemKey = new NamespacedKey(JavaPlugin.getPlugin(AwesomeChestShop.class), "shop_item");

    /**
     * 商店物品售价，存在告示牌上
     */
    public final NamespacedKey shopPriceKey = new NamespacedKey(JavaPlugin.getPlugin(AwesomeChestShop.class), "shop_price");

    /**
     * 商店是否为出售模式，存在告示牌上
     */
    public final NamespacedKey shopTypeKey = new NamespacedKey(JavaPlugin.getPlugin(AwesomeChestShop.class), "shop_sale");

    /**
     * 关联的告示牌的位置，存在箱子上
     */
    public final NamespacedKey signAttachedFace = new NamespacedKey(JavaPlugin.getPlugin(AwesomeChestShop.class), "sign_attached_face");

    public final NamespacedKey shopListKey = new NamespacedKey(JavaPlugin.getPlugin(AwesomeChestShop.class), "shop_list");
}
