package com.keriteal.awesomeChestShop;

import org.bukkit.NamespacedKey;
import org.bukkit.block.Block;
import org.bukkit.block.TileState;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.java.JavaPlugin;

public class PersistentDataAccessor<P, C> {
    private final PersistentDataType<P, C> type;
    private final NamespacedKey key;
    private final C defaultValue;

    public PersistentDataAccessor(String keyName, PersistentDataType<P, C> type, C defaultValue) {
        this.type = type;
        this.key = new NamespacedKey(JavaPlugin.getPlugin(AwesomeChestShop.class), keyName);
        this.defaultValue = defaultValue;
    }

    public boolean hasValue(TileState tileState) {
        return hasValue(tileState.getPersistentDataContainer());
    }

    public boolean hasValue(PersistentDataContainer dataContainer) {
        return dataContainer.has(key, type);
    }

    public C getValueFrom(PersistentDataContainer dataContainer) {
        return dataContainer.getOrDefault(key, type, defaultValue);
    }

    public C getValueFrom(TileState tileState) {
        return getValueFrom(tileState.getPersistentDataContainer());
    }

    public void removeValueFrom(PersistentDataContainer container) {
        container.remove(key);
    }

    public void setValueTo(PersistentDataContainer dataContainer, C value) {
        dataContainer.set(key, type, value);
    }

    public C getDefaultValue() {
        return defaultValue;
    }
}
