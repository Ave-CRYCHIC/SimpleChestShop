package com.keriteal.awesomeChestShop;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextColor;
import net.kyori.adventure.text.minimessage.MiniMessage;

public class Messages {
    private static final MiniMessage minimessage = MiniMessage.miniMessage();

    public static final TextColor CURRENCY_SIGN_COLOR = TextColor.fromHexString("#EE578C");
    public static final TextComponent CURRENCY_SIGN = Component.text(AwesomeChestShop.getEconomy().currencyNamePlural(), CURRENCY_SIGN_COLOR);

    public static final TextComponent INVALID_FORMAT = Component.text("输入错误", NamedTextColor.RED);

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
    public static final TextComponent MESSAGE_SEPARATOR_SHOP = CURRENCY_SIGN.append(minimessage.deserialize("======== <color:#39C5BB>交 易</color> ========").color(NamedTextColor.DARK_PURPLE)).append(CURRENCY_SIGN);
    public static final TextComponent MESSAGE_SEPARATOR_MANAGE = CURRENCY_SIGN.append(minimessage.deserialize("======== <color:#39C5BB>管 理</color> ========").color(NamedTextColor.DARK_PURPLE)).append(CURRENCY_SIGN);
    public static final TextComponent MESSAGE_SEPARATOR = CURRENCY_SIGN.append(Component.text("=====================", NamedTextColor.DARK_PURPLE)).append(CURRENCY_SIGN);
    public static final TextComponent SHOP_TYPE_CHANGED_LEFT = Component.text("商店类型已经更改为", NamedTextColor.GREEN);
    public static final TextComponent INPUT_PRICE = Component.text("请在聊天栏输入物品单价：", TextColor.fromHexString("#39C5BB"));
    public static final TextComponent INPUT_AMOUNT = Component.text("请在聊天栏输入物品数量：", TextColor.fromHexString("#39C5BB"));

    public static final TextComponent DELETING_SHOP_LEFT = Component.text("你位于", NamedTextColor.RED);
    public static final TextComponent DELETING_SHOP_RIGHT = Component.text("的商店已经被删除", NamedTextColor.RED);

    public static final TextComponent SHOP_TAG_PREPEND = Component.text("| ", NamedTextColor.DARK_PURPLE);

    public static final TextComponent SHOP_TAG_FREE_SPACE = SHOP_TAG_PREPEND.append(Component.text("剩余空间：", NamedTextColor.GREEN));
    public static final TextComponent SHOP_TAG_OWNER = SHOP_TAG_PREPEND.append(Component.text("店主：", NamedTextColor.GREEN));
    public static final TextComponent SHOP_TAG_LOCATION = SHOP_TAG_PREPEND.append(Component.text("位置：", NamedTextColor.GREEN));
    public static final TextComponent SHOP_TAG_PRICE = SHOP_TAG_PREPEND.append(Component.text("单价：", NamedTextColor.GREEN));
    public static final TextComponent SHOP_TAG_STOCK = SHOP_TAG_PREPEND.append(Component.text("库存：", NamedTextColor.GREEN));
    public static final TextComponent SHOP_TAG_POOL = SHOP_TAG_PREPEND.append(Component.text("奖池：", NamedTextColor.GREEN));
    public static final TextComponent SHOP_TAG_ITEM = SHOP_TAG_PREPEND.append(Component.text("物品：", NamedTextColor.GREEN));
    public static final TextComponent SHOP_TAG_MODE = SHOP_TAG_PREPEND.append(Component.text("类型：", NamedTextColor.GREEN));

    public static final TextComponent SHOP_TAG_ITEM_PREVIEW = Component.text("[预览]", NamedTextColor.AQUA);

    public static final TextComponent HOVER_PRIVATE_CHAT = Component.text("点击私聊", NamedTextColor.WHITE);
    public static final TextComponent HOVER_COPY = Component.text("点击复制", NamedTextColor.WHITE);
}
