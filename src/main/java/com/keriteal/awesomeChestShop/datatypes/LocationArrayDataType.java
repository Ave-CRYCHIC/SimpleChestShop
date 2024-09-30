package com.keriteal.awesomeChestShop.datatypes;

import org.bukkit.Location;
import org.bukkit.persistence.PersistentDataAdapterContext;
import org.bukkit.persistence.PersistentDataType;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

public class LocationArrayDataType implements PersistentDataType<int[], Location[]> {
    @Override
    public @NotNull Class<int[]> getPrimitiveType() {
        return int[].class;
    }

    @Override
    public @NotNull Class<Location[]> getComplexType() {
        return Location[].class;
    }

    @Override
    public int @NotNull [] toPrimitive(Location @NotNull [] complex, @NotNull PersistentDataAdapterContext context) {
        List<Integer> result = new LinkedList<>();
        for (Location location : complex) {
            result.add(location.getBlockX());
            result.add(location.getBlockY());
            result.add(location.getBlockZ());
        }
        return result.stream().mapToInt(Integer::intValue).toArray();
    }

    @Override
    public Location @NotNull [] fromPrimitive(int @NotNull [] primitive, @NotNull PersistentDataAdapterContext context) {
        List<Location> result = new ArrayList<>();
        for (int i = 0; i < primitive.length; i += 3) {
            result.add(new Location(null, primitive[i], primitive[i + 1], primitive[i + 2]));
        }
        return result.toArray(Location[]::new);
    }
}
