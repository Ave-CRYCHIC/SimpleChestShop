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

    public static final Component INVALID_FORMAT = Component.text("输入错误", NamedTextColor.RED);

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
    public static final Component MESSAGE_SEPARATOR_SHOP = CURRENCY_SIGN.append(minimessage.deserialize("<#EAEAD5>======== <#76EBB9>交 易</#76EBB9> ========</#EAEAD5>")).append(CURRENCY_SIGN);
    public static final Component MESSAGE_SEPARATOR_MANAGE = CURRENCY_SIGN.append(minimessage.deserialize("<#EAEAD5>======== <#76EBB9>管 理</#76EBB9> ========</#EAEAD5>")).append(CURRENCY_SIGN);
    public static final Component MESSAGE_SEPARATOR = CURRENCY_SIGN.append(minimessage.deserialize("<#EAEAD5>=====================</#EAEAD5>")).append(CURRENCY_SIGN);
    public static final Component SHOP_TYPE_CHANGED_LEFT = minimessage.deserialize("<green>商店类型已经更改为</green>");
    public static final Component INPUT_PRICE = minimessage.deserialize("<#39C5BB>请在聊天栏输入物品单价：</#39C5BB>");
    public static final Component INPUT_AMOUNT = minimessage.deserialize("<#39C5BB>请在聊天栏输入物品数量：</#39C5BB>");

    public static final Component DELETING_SHOP_LEFT = minimessage.deserialize("<red>你位于</red>");
    public static final Component DELETING_SHOP_RIGHT = minimessage.deserialize("<red>的商店已经被删除</red>");

    public static final Component SHOP_TAG_PREPEND = minimessage.deserialize("<#EAEAD5> | </#EAEAD5>");

    public static final Component SHOP_TAG_FREE_SPACE = minimessage.deserialize("<green>剩余空间：</green>");
    public static final Component SHOP_TAG_OWNER = minimessage.deserialize("<green>店主：</green>");
    public static final Component SHOP_TAG_LOCATION = minimessage.deserialize("<green>位置：</green>");
    public static final Component SHOP_TAG_STOCK = minimessage.deserialize("<green>库存：</green>");
    public static final Component SHOP_TAG_ITEM = minimessage.deserialize("<green>物品：</green>");
    public static final Component SHOP_TAG_MODE = minimessage.deserialize("<green>类型：</green>");

    public static final Component SHOP_TAG_ITEM_PREVIEW = minimessage.deserialize("<aqua>[预览]</aqua>");

    public static final Component HOVER_PRIVATE_CHAT = Component.text("点击私聊", NamedTextColor.WHITE);
    public static final Component HOVER_COPY = Component.text("点击复制", NamedTextColor.WHITE);
    public static final Component TAG_LAST_SEEN = minimessage.deserialize("<green>最后在线：</green>");
    public static final Component SHOP_PREVIEW = minimessage.deserialize("<rainbow>商店预览</rainbow>");

    public static Component buildPriceComponent(double price) {
        return minimessage.deserialize("<green>单价：</green><price>", buildMoneyResolver(price));
    }

    public static TagResolver buildMoneyResolver(double price) {
        return Placeholder.component("price", minimessage.deserialize(String.format("<color:#EE578C>%s</color> <color:#F3C64B>%.1f</color>", AwesomeChestShop.getEconomy().currencyNamePlural(), price)));
    }

    public static Component buildLocationComponent(Location location) {
        return minimessage.deserialize(String.format("<green>位置：</green><hover:show_text:点击复制><click:copy_to_clipboard:%1$d %2$d %3$d><aqua>%d, %d, %d</aqua></click></hover>", location.getBlockX(), location.getBlockY(), location.getBlockZ()));
    }

    public static TagResolver buildBalanceComponent(OfflinePlayer player) {
        Economy economy = AwesomeChestShop.getEconomy();
        double balance = economy.getBalance(player);
        return Placeholder.component("balance", minimessage.deserialize(String.format("<color:#EE578C>%s</color> <color:#F3C64B>%.1f</color>", economy.currencyNamePlural(), balance)));
    }
}
