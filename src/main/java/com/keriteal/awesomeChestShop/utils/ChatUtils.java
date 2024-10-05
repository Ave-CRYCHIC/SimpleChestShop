package com.keriteal.awesomeChestShop.utils;

import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.minimessage.tag.resolver.Formatter;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;
import org.bukkit.Location;
import org.intellij.lang.annotations.Subst;

public class ChatUtils {
    public static TagResolver buildLocationComponent(@Subst("") String prepend, Location location) {
        String prepended = prepend + "-";
        TagResolver xyzResolver = TagResolver.resolver(
                Formatter.number(prepended + "x", location.getBlockX()),
                Formatter.number(prepended + "y", location.getBlockY()),
                Formatter.number(prepended + "z", location.getBlockZ()));
        return TagResolver.resolver(
                xyzResolver,
                Placeholder.component(prepended + "location", MiniMessage.miniMessage().deserialize(String.format("<%1$sx>, <%1$sy>, <%1$sz>", prepended)))
        );
    }
}
