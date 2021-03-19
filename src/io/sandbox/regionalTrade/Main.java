package io.sandbox.regionalTrade;

import io.sandbox.helpers.Output;
import io.sandbox.regionalTrade.events.RegionalTradeEventHandlers;

import org.bukkit.Server;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

public class Main extends JavaPlugin {
	Server server;
	Output output;
	FileConfiguration config;

    @Override
    public void onEnable() {
    	server = this.getServer();
		output = new Output(server);
		
		output.consoleInfo("RegionalTrade is loading its config...");
		this.loadConfig();

        server.getPluginManager().registerEvents(new RegionalTradeEventHandlers(this), this);
        
        output.consoleSuccess("RegionalTrade has been initiated!");
    }

    @Override
    public void onDisable() {
    	output.consoleError("RegionalTrade has been disabled!");
    }

    public void loadConfig() {
    	// First ensure that the config file exists or create it.
    	this.getConfig().options().copyDefaults(true);
    	this.saveConfig();
    	
    	// Now actually import settings and process them.
        config = this.getConfig();
    }
}
