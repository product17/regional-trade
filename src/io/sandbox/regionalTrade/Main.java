package io.sandbox.regionalTrade;

import io.sandbox.helpers.ItemHelper;
import io.sandbox.helpers.OutputHelper;
import io.sandbox.regionalTrade.events.RegionalTradeEventHandlers;

import java.util.List;

import org.bukkit.Server;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Villager;
import org.bukkit.plugin.java.JavaPlugin;

public class Main extends JavaPlugin {
	Server server;
	OutputHelper output;
	RegionalTradeConfig tradeConfig;

    @Override
    public void onEnable() {
    	server = this.getServer();
		output = new OutputHelper(server);
		
		output.consoleInfo("RegionalTrade is loading its config...");
		this.loadConfig();
        
        output.consoleSuccess("RegionalTrade has been initiated!");
    }

    @Override
    public void onDisable() {
    	output.consoleError("RegionalTrade has been disabled!");
    }

    private void loadConfig() {
    	// First ensure that the config file exists or create it.
    	this.getConfig().options().copyDefaults(true);
    	this.saveConfig();
    	
    	// Now actually import settings and process them.
    	FileConfiguration config = this.getConfig();
    	tradeConfig = new RegionalTradeConfig();
    	
    	ConfigurationSection profSection = config.getConfigurationSection("professions");
    	if (profSection == null) { return; }
    		
    	for (String profName : profSection.getKeys(false)) {
    		this.configLoadProfession(config, profName);
    	}
    	
    	server.getPluginManager().registerEvents(new RegionalTradeEventHandlers(this, tradeConfig), this);
    }
    
    private void configLoadProfession(FileConfiguration config, String profName) {
    	Villager.Profession profession = Villager.Profession.valueOf(profName);
    	if (profession == null) { return; }

    	String prefix = "professions." + profName;
		ConfigurationSection biomeSection = config.getConfigurationSection(prefix + ".biomes");
		if (biomeSection == null) { return; }
		
		for (String biomeName : biomeSection.getKeys(false)) {
			this.configLoadBiome(config, prefix, profession, biomeName);
		}
    }
    
    private void configLoadBiome(FileConfiguration config, String thePrefix, Villager.Profession profession, String biomeName) {
    	Villager.Type villagerBiome = Villager.Type.valueOf(biomeName);
    	if (villagerBiome == null) { return; }
    	
    	List<String> enchants = config.getStringList(thePrefix + ".biomes." + biomeName + ".enchants");
    	if (enchants == null) { return; }
    	
    	for (String enchantName : enchants) {
    		this.configLoadEnchant(profession, villagerBiome, enchantName);
    	}
    }
    
    private void configLoadEnchant(Villager.Profession profession, Villager.Type villagerBiome, String enchantName) {
    	Enchantment enchant = ItemHelper.resolveEnchant(enchantName);
    	if (enchant == null) { return; }
    	
    	this.tradeConfig.addEnchantment(profession, villagerBiome, enchant);
    }
}
