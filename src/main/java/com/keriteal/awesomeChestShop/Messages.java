package com.keriteal.awesomeChestShop;

import com.keriteal.awesomeChestShop.shop.ChestShop;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextColor;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;
import net.milkbowl.vault.economy.Economy;
import org.bukkit.Location;
import org.bukkit.OfflinePlayer;

public class Messages {
    private static final MiniMessage minimessage = MiniMessage.miniMessage();

    public static final Component CURRENCY_SIGN = minimessage.deserialize(String.format("<color:#EE578C>%s</color>", AwesomeChestShop.getEconomy().currencyNamePlural()));

    public static final Component MESSAGE_NOT_CONTAINER = Component.text("不是箱子、木桶或潜影盒，取消创建", NamedTextColor.RED);
    public static final Component MESSAGE_SIGN_NO_SPACE = Component.text("没有位置放置告示牌，创建商店失败", NamedTextColor.RED);
    public static final Component TEMPLATE_AMOUNT_STOCK = Component.text("库存：");
    public static final Component TEMPLATE_AMOUNT_FREE_SPACE = Component.text("剩余空间：");
    public static final Component MESSAGE_CREATE_SUCCESS = Component.text("商店创建成功", TextColor.fromHexString("#39C5BB"));
    public static final Component MESSAGE_NOT_CHEST_PART_LOCATION = Component.text("商店位置", NamedTextColor.BLUE);
    public static final Component MESSAGE_SHOP_EXISTS = Component.text("已经有一个商店了", NamedTextColor.RED);
    public static final Component MESSAGE_REENTER_PRICE = Component.text("请重新输入价格", NamedTextColor.YELLOW);
    public static final Component MESSAGE_NO_TARGETING_BLOCK = Component.text("没有获取到指向的方块", NamedTextColor.YELLOW);
    public static final Component MESSAGE_NO_PERMISSION = Component.text("你没有权限", NamedTextColor.RED);
    public static final Component MISSING_SHOP = Component.text("这里没有商店", NamedTextColor.RED);
    public static final Component MESSAGE_SHOP_PROTECTED = Component.text("商店已经被保护");
    public static final Component MESSAGE_SHOP_NOT_PROTECTED = Component.text("商店没有被保护");
    public static final Component MESSAGE_SEPARATOR = CURRENCY_SIGN.append(minimessage.deserialize("<#EAEAD5>=====================</#EAEAD5>")).append(CURRENCY_SIGN);
    public static final Component SHOP_TYPE_CHANGED_LEFT = minimessage.deserialize("<green>商店类型已经更改为</green>");

    public static final Component DELETING_SHOP_LEFT = minimessage.deserialize("<red>你位于</red>");
    public static final Component DELETING_SHOP_RIGHT = minimessage.deserialize("<red>的商店已经被删除</red>");
    public static final Component SHOP_PREVIEW = minimessage.deserialize("<rainbow>商店预览</rainbow>");

    public static TagResolver buildBalanceComponent(OfflinePlayer player) {
        Economy economy = AwesomeChestShop.getEconomy();
        double balance = economy.getBalance(player);
        return Placeholder.component("balance", minimessage.deserialize(String.format("<color:#EE578C>%s</color> <color:#F3C64B>%.1f</color>", economy.currencyNamePlural(), balance)));
    }
}
