package com.keriteal.awesomeChestShop;

import com.keriteal.awesomeChestShop.datatypes.ItemStackDataType;
import com.keriteal.awesomeChestShop.datatypes.LocationArrayDataType;
import com.keriteal.awesomeChestShop.datatypes.PlayerDataType;
import com.keriteal.awesomeChestShop.datatypes.UUIDTagType;

public class DataTypes {
    public static final UUIDTagType UUID = new UUIDTagType();
    public static final ItemStackDataType ITEM_STACK = new ItemStackDataType();
    public static final PlayerDataType PLAYER = new PlayerDataType();
    public static final LocationArrayDataType LOCATION_ARRAY = new LocationArrayDataType();
}
