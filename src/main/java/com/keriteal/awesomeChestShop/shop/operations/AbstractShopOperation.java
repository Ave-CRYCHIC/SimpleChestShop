package com.keriteal.awesomeChestShop.shop.operations;

import com.google.common.collect.ImmutableList;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.function.Function;

public abstract class AbstractShopOperation<TResult> implements InventoryHolder, IShopOperation {
    private static final ItemStack EMPTY_ITEM = new ItemStack(Material.GRAY_STAINED_GLASS_PANE);
    private static final List<Component> EMPTY_ITEM_LORE = ImmutableList.of(
            Component.text("点击确认")
    );

    static {
        ItemMeta meta = EMPTY_ITEM.getItemMeta();
        meta.displayName(Component.empty());
        meta.lore(EMPTY_ITEM_LORE);
        EMPTY_ITEM.setItemMeta(meta);
    }

    public abstract Component getUiTitle();

    public abstract boolean checkInput();

    public abstract TResult getResult();

    private Inventory inventory;

    private final Function<String, Boolean> onClickConsumer;
    private final Runnable onCloseConsumer;

    public AbstractShopOperation(Function<String, Boolean> onClickConsumer, Runnable onCloseConsumer) {
        this.onClickConsumer = onClickConsumer;
        this.onCloseConsumer = onCloseConsumer;
    }

    @Override
    public @NotNull Inventory getInventory() {
        if (inventory == null) {
            inventory = Bukkit.createInventory(this, InventoryType.ANVIL, getUiTitle());
            inventory.setItem(0, EMPTY_ITEM);
        }
        return inventory;
    }

    public final boolean onClick(@NotNull String result) {
        return this.onClickConsumer.apply(result);
    }

    public final void onClose() {
        this.onCloseConsumer.run();
    }
}
