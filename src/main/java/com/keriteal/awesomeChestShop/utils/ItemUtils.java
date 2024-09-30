package com.keriteal.awesomeChestShop.utils;

import net.kyori.adventure.text.Component;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.EnchantmentStorageMeta;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.Map;

public class ItemUtils {
    public static Component getItemName(ItemStack itemStack) {
        if (itemStack.getType() == Material.ENCHANTED_BOOK) {
            ItemMeta meta = itemStack.getItemMeta();
            if (meta instanceof EnchantmentStorageMeta storageMeta && storageMeta.hasStoredEnchants()) {
                Map<Enchantment, Integer> enchantmentMap = storageMeta.getStoredEnchants();
                if (enchantmentMap.size() == 1) {
                    Map.Entry<Enchantment, Integer> first = enchantmentMap.entrySet().stream().findFirst().get();
                    return first.getKey().displayName(first.getValue());
                }
            }
        }

        if (itemStack.hasItemMeta()) {
            ItemMeta meta = itemStack.getItemMeta();
            if (meta.hasDisplayName()) {
                return meta.displayName();
            }
        }

        return Component.translatable(itemStack.getType().translationKey());
    }
}
