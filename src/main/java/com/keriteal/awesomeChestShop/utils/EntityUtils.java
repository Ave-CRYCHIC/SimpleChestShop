package com.keriteal.awesomeChestShop.utils;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.ProtocolManager;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.utility.MinecraftReflection;
import com.comphenix.protocol.wrappers.WrappedDataValue;
import com.comphenix.protocol.wrappers.WrappedDataWatcher;
import org.bukkit.Location;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class EntityUtils {
    private static final WrappedDataWatcher.Serializer ITEM_STACK_SERIALIZER = WrappedDataWatcher.Registry.getItemStackSerializer(false);
    private static final WrappedDataWatcher.Serializer BOOLEAN_SERIALIZER = WrappedDataWatcher.Registry.get(Boolean.class);
    private static final WrappedDataWatcher.WrappedDataWatcherObject NO_GRAVITY = new WrappedDataWatcher.WrappedDataWatcherObject(5, BOOLEAN_SERIALIZER);
    private static final WrappedDataWatcher.WrappedDataWatcherObject ITEM_STACK = new WrappedDataWatcher.WrappedDataWatcherObject(8, ITEM_STACK_SERIALIZER);

    private static final ProtocolManager manager = ProtocolLibrary.getProtocolManager();

    public static void spawnFakeItemAt(@NotNull Player player, @NotNull Location location, @NotNull ItemStack itemStack, int shopEntityId) {
        final ItemStack shownItemStack = itemStack.clone();
        shownItemStack.setAmount(1);

        final UUID identity = UUID.nameUUIDFromBytes(("ChestShop:" + shopEntityId).getBytes(StandardCharsets.UTF_8));
        final PacketContainer fakeItemPacket = manager.createPacket(PacketType.Play.Server.SPAWN_ENTITY);
        fakeItemPacket.getIntegers()
                .write(0, shopEntityId)
                .write(1, 0)
                .write(2, 0)
                .write(3, 0);
        fakeItemPacket.getEntityTypeModifier()
                .write(0, EntityType.ITEM);
        fakeItemPacket.getUUIDs()
                .write(0, identity);
        fakeItemPacket.getDoubles()
                .write(0, location.getX())
                .write(1, location.getY())
                .write(2, location.getZ());

        final PacketContainer fakeItemMetaPacket = manager.createPacket(PacketType.Play.Server.ENTITY_METADATA);
        fakeItemMetaPacket.getIntegers()
                .write(0, shopEntityId);

        List<WrappedDataValue> dataValues = new ArrayList<>();
        dataValues.add(new WrappedDataValue(5, BOOLEAN_SERIALIZER, true)); // set gravity
        dataValues.add(new WrappedDataValue(8, ITEM_STACK_SERIALIZER, MinecraftReflection.getMinecraftItemStack(shownItemStack)));

        fakeItemMetaPacket.getDataValueCollectionModifier().write(0, dataValues);

        manager.sendServerPacket(player, fakeItemPacket);
        manager.sendServerPacket(player, fakeItemMetaPacket);
    }
}
