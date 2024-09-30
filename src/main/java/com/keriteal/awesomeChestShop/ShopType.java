package com.keriteal.awesomeChestShop;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextColor;

public enum ShopType {
    SALE_MODE("出售", Component.text("[出售]", NamedTextColor.GREEN), "sale", NamedTextColor.GREEN),
    BUY_MODE("收购", Component.text("[收购]", NamedTextColor.YELLOW), "buy", NamedTextColor.YELLOW),
    GOTCHA_MODE("抽奖", Component.text("[抽奖]", NamedTextColor.AQUA), "gotcha", NamedTextColor.AQUA);

    private final String name;
    private final TextComponent coloredName;
    private final String commandName;
    private final TextColor textColor;

    ShopType(String name, TextComponent coloredName, String commandName, TextColor textColor) {
        this.name = name;
        this.coloredName = coloredName;
        this.commandName = commandName;
        this.textColor = textColor;
    }

    public String getName() {
        return this.name;
    }

    public TextComponent getColoredName() {
        return this.coloredName;
    }

    public String getCommandName() {
        return this.commandName;
    }

    public TextColor getTextColor() {
        return this.textColor;
    }

    public ShopType nextType() {
        return ShopType.values()[(this.ordinal() + 1) % ShopType.values().length];
    }
}
