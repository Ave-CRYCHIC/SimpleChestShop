package com.keriteal.awesomeChestShop.datatypes;

import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.persistence.PersistentDataAdapterContext;
import org.bukkit.persistence.PersistentDataType;
import org.jetbrains.annotations.NotNull;

import java.nio.ByteBuffer;
import java.util.UUID;

public class PlayerDataType implements PersistentDataType<byte[], OfflinePlayer> {
    @Override
    public @NotNull Class<byte[]> getPrimitiveType() {
        return byte[].class;
    }

    @Override
    public @NotNull Class<OfflinePlayer> getComplexType() {
        return OfflinePlayer.class;
    }

    @Override
    public byte @NotNull [] toPrimitive(@NotNull OfflinePlayer complex, @NotNull PersistentDataAdapterContext context) {
        UUID uuid = complex.getUniqueId();
        ByteBuffer bb = ByteBuffer.wrap(new byte[16]);
        bb.putLong(uuid.getMostSignificantBits());
        bb.putLong(uuid.getLeastSignificantBits());
        return bb.array();
    }

    @Override
    public @NotNull OfflinePlayer fromPrimitive(byte @NotNull [] primitive, @NotNull PersistentDataAdapterContext context) {
        ByteBuffer bb = ByteBuffer.wrap(primitive);
        long firstLong = bb.getLong();
        long secondLong = bb.getLong();
        UUID uuid = new UUID(firstLong, secondLong);
        Player player = Bukkit.getPlayer(uuid);
        if (player != null) {
            return player;
        }
        return Bukkit.getOfflinePlayer(uuid);
    }
}
