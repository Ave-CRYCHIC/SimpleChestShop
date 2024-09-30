package com.keriteal.awesomeChestShop;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextColor;

public class Messages {
    public static final TextComponent MESSAGE_NOT_CONTAINER = Component.text("不是箱子、木桶或潜影盒，取消创建", NamedTextColor.RED);
    public static final TextComponent MESSAGE_SIGN_NO_SPACE = Component.text("没有位置放置告示牌，创建商店失败", NamedTextColor.RED);
    public static final TextComponent TEMPLATE_AMOUNT_STOCK = Component.text("库存：");
    public static final TextComponent TEMPLATE_AMOUNT_FREE_SPACE = Component.text("剩余空间：");
    public static final TextComponent MESSAGE_CREATE_SUCCESS = Component.text("商店创建成功", TextColor.fromHexString("#39C5BB"));
    public static final TextComponent MESSAGE_NOT_CHEST_PART_LOCATION = Component.text("商店位置", NamedTextColor.BLUE);
    public static final TextComponent MESSAGE_SHOP_EXISTS = Component.text("已经有一个商店了", NamedTextColor.RED);
    public static final TextComponent MESSAGE_REENTER_PRICE = Component.text("请重新输入价格", NamedTextColor.YELLOW);
    public static final TextComponent MESSAGE_NO_TARGETING_BLOCK = Component.text("没有获取到指向的方块", NamedTextColor.YELLOW);
    public static final TextComponent MESSAGE_NO_PERMISSION = Component.text("你没有权限", NamedTextColor.RED);
    public static final TextComponent MESSAGE_NO_SHOP = Component.text("这里没有商店", NamedTextColor.RED);
    public static final TextComponent MESSAGE_SHOP_PROTECTED = Component.text("商店已经被保护");
    public static final TextComponent MESSAGE_SHOP_NOT_PROTECTED = Component.text("商店没有被保护");
    public static final TextComponent MESSAGE_SEPARATOR = Component.text("=====================");
    public static final TextComponent SHOP_TYPE_CHANGED_LEFT = Component.text("商店类型已经更改为", NamedTextColor.GREEN);
    public static final TextComponent INPUT_PRICE = Component.text("请输入物品单价", TextColor.fromHexString("#39C5BB"));
}
