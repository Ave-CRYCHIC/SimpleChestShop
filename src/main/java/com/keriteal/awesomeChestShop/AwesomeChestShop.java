package com.keriteal.awesomeChestShop;

import com.keriteal.awesomeChestShop.commands.ProtectionCommand;
import com.keriteal.awesomeChestShop.listeners.ProtectionListener;
import com.keriteal.awesomeChestShop.listeners.ShopLifetimeListener;
import com.keriteal.awesomeChestShop.listeners.ShopUpdateListener;
import net.milkbowl.vault.chat.Chat;
import net.milkbowl.vault.economy.Economy;
import net.milkbowl.vault.permission.Permission;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;
import org.slf4j.Logger;

public final class AwesomeChestShop extends JavaPlugin {

    private static Economy econ = null;
    private static Permission perms = null;
    private static Chat chat = null;

    private static NamespacedKeys Keys = null;

    public static JavaPlugin Instance = null;

    private static Logger logger;

    @Override
    public void onEnable() {
        // Plugin startup logic
        logger = this.getSLF4JLogger();
        Keys = new NamespacedKeys();
        Instance = this;
        ShopManager shopManager = new ShopManager(this);
        setupEconomy();
        getServer().getPluginManager().registerEvents(new ShopLifetimeListener(this, shopManager), this);
        getServer().getPluginManager().registerEvents(new ProtectionListener(this, shopManager), this);
        getServer().getPluginManager().registerEvents(new ShopUpdateListener(shopManager), this);
        getCommand("shop").setExecutor(new ProtectionCommand(this, shopManager));
    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic
    }

    public static JavaPlugin getPlugin() {
        if (Instance == null) {
            return getPlugin(AwesomeChestShop.class);
        }
        return Instance;
    }

    public static NamespacedKeys getKeys() {
        return Keys;
    }

    public static Logger logger() {
        return logger;
    }

    private boolean setupEconomy() {
        if (getServer().getPluginManager().getPlugin("Vault") == null) {
            return false;
        }
        RegisteredServiceProvider<Economy> rsp = getServer().getServicesManager().getRegistration(Economy.class);
        if (rsp == null) {
            return false;
        }
        econ = rsp.getProvider();
        return econ != null;
    }

    private boolean setupChat() {
        RegisteredServiceProvider<Chat> rsp = getServer().getServicesManager().getRegistration(Chat.class);
        chat = rsp.getProvider();
        return chat != null;
    }

    private boolean setupPermissions() {
        RegisteredServiceProvider<Permission> rsp = getServer().getServicesManager().getRegistration(Permission.class);
        perms = rsp.getProvider();
        return perms != null;
    }

    public static Economy getEconomy() {
        return econ;
    }

    public static Permission getPermissions() {
        return perms;
    }

    public static Chat getChat() {
        return chat;
    }
}
