package io.sandbox.regionalTrade;

import io.sandbox.regionalTrade.events.RegionalTradeEventHandlers;
import org.bukkit.ChatColor;
import org.bukkit.plugin.java.JavaPlugin;

public class Main extends JavaPlugin {
    public static final String MOD_ID = "RegionalTrade";

    @Override
    public void onEnable() {
        getServer().getPluginManager().registerEvents(new RegionalTradeEventHandlers(), this);
        getServer().getConsoleSender().sendMessage(ChatColor.GREEN + "Regional Trade has been initiated!");
        this.loadConfig();
    }

    @Override
    public void onDisable() {
        getServer().getConsoleSender().sendMessage(ChatColor.RED + "Plugin is disabled!");
    }

    public void loadConfig() {
        getConfig().options().copyDefaults(true);
        saveConfig();
    }
}
