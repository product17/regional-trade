package io.sandbox.regionalTrade;

import io.sandbox.helpers.Output;
import io.sandbox.regionalTrade.events.RegionalTradeEventHandlers;

import org.bukkit.Server;
import org.bukkit.plugin.java.JavaPlugin;

public class Main extends JavaPlugin {
	Server server;
	Output output;
    public static final String MOD_ID = "RegionalTrade";

    @Override
    public void onEnable() {
    	server = getServer();
		output = new Output(server);
        server.getPluginManager().registerEvents(new RegionalTradeEventHandlers(this), this);
        output.consoleSuccess("Regional Trade has been initiated!");
        this.loadConfig();
    }

    @Override
    public void onDisable() {
    	output.consoleError("Plugin is disabled!");
    }

    public void loadConfig() {
        getConfig().options().copyDefaults(true);
        saveConfig();
    }
}
