package com.keriteal.awesomeChestShop;

import org.bukkit.configuration.serialization.ConfigurationSerializable;
import org.bukkit.persistence.PersistentDataAdapterContext;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.util.io.BukkitObjectInputStream;
import org.bukkit.util.io.BukkitObjectOutputStream;
import org.jetbrains.annotations.NotNull;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.UncheckedIOException;

public class ConfigurationSerializableDataType<T extends ConfigurationSerializable> implements PersistentDataType<byte[], T> {
    private final Class<T> type;

    public ConfigurationSerializableDataType(Class<T> type) {
        this.type = type;
    }

    @NotNull
    public Class<byte[]> getPrimitiveType() {
        return byte[].class;
    }

    @NotNull
    public Class<T> getComplexType() {
        return this.type;
    }

    @NotNull
    public byte[] toPrimitive(@NotNull T serializable, @NotNull PersistentDataAdapterContext persistentDataAdapterContext) {
        try {
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();

            byte[] var5;
            try {
                BukkitObjectOutputStream bukkitObjectOutputStream = new BukkitObjectOutputStream(outputStream);

                try {
                    bukkitObjectOutputStream.writeObject(serializable);
                    var5 = outputStream.toByteArray();
                } catch (Throwable var9) {
                    try {
                        bukkitObjectOutputStream.close();
                    } catch (Throwable var8) {
                        var9.addSuppressed(var8);
                    }

                    throw var9;
                }

                bukkitObjectOutputStream.close();
            } catch (Throwable var10) {
                try {
                    outputStream.close();
                } catch (Throwable var7) {
                    var10.addSuppressed(var7);
                }

                throw var10;
            }

            outputStream.close();
            return var5;
        } catch (IOException var11) {
            IOException e = var11;
            throw new UncheckedIOException(getExceptionMessage(this.type, SerializationType.SERIALIZATION), e);
        }
    }

    @NotNull
    public T fromPrimitive(@NotNull byte[] bytes, @NotNull PersistentDataAdapterContext persistentDataAdapterContext) {
        try {
            ByteArrayInputStream inputStream = new ByteArrayInputStream(bytes);

            T var5;
            try {
                BukkitObjectInputStream bukkitObjectInputStream = new BukkitObjectInputStream(inputStream);

                try {
                    var5 = (T) bukkitObjectInputStream.readObject();
                } catch (Throwable var9) {
                    try {
                        bukkitObjectInputStream.close();
                    } catch (Throwable var8) {
                        var9.addSuppressed(var8);
                    }

                    throw var9;
                }

                bukkitObjectInputStream.close();
            } catch (Throwable var10) {
                try {
                    inputStream.close();
                } catch (Throwable var7) {
                    var10.addSuppressed(var7);
                }

                throw var10;
            }

            inputStream.close();
            return var5;
        } catch (IOException var11) {
            IOException e = var11;
            throw new UncheckedIOException(getExceptionMessage(this.type, SerializationType.DESERIALIZATION), e);
        } catch (ClassNotFoundException var12) {
            ClassNotFoundException e = var12;
            throw new RuntimeException(getExceptionMessage(this.type, SerializationType.DESERIALIZATION), e);
        }
    }

    private static boolean isBukkitClass(Class<?> clazz) {
        return clazz.getPackage().getName().startsWith("org.bukkit.");
    }

    static String getExceptionMessage(Class<? extends ConfigurationSerializable> type, SerializationType serializationType) {
        String msg = "Could not " + serializationType + " object of type " + type.getName() + ".";
        if (!isBukkitClass(type)) {
            msg = msg + " This is not a bug in MorePersistentDataTypes, but a bug in your " + serializationType + ".";
            if (serializationType == SerializationType.DESERIALIZATION) {
                msg = msg + " Make sure that your class is properly registered for deserialization using org.bukkit.configuration.serialization.ConfigurationSerialization#registerClass(Class).";
            }
        }

        return msg;
    }

    static enum SerializationType {
        SERIALIZATION("serialization"),
        DESERIALIZATION("deserialization");

        private final String fancyName;

        private SerializationType(String fancyName) {
            this.fancyName = fancyName;
        }

        public String toString() {
            return this.fancyName;
        }
    }
}
