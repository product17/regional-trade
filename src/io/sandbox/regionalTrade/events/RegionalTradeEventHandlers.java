package io.sandbox.regionalTrade.events;

import io.sandbox.helpers.ItemHelper;
import io.sandbox.helpers.Output;
import io.sandbox.regionalTrade.Main;
import org.bukkit.Material;
import org.bukkit.Server;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Villager;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.VillagerAcquireTradeEvent;
import org.bukkit.inventory.MerchantRecipe;
import org.bukkit.inventory.meta.EnchantmentStorageMeta;
import java.util.*;

public class RegionalTradeEventHandlers implements Listener {
	Main main;
	Server server;
	Output output;
    
    public RegionalTradeEventHandlers(Main theMain) {
		main = theMain;
		server = main.getServer();
		output = new Output(server);
	}

    public int getRandomNumberUsingInts(int min, int max) {
        Random random = new Random();
        return random.ints(min, max).findFirst().getAsInt();
    }

    @EventHandler
    public void onVillagerAcquiresTrade(VillagerAcquireTradeEvent event) {
        Villager merchant = (Villager)event.getEntity();
        Villager.Profession villagerProfession = merchant.getProfession();
        Villager.Type villagerBiome = merchant.getVillagerType();
        MerchantRecipe recipe = event.getRecipe();

        if (recipe.getResult().getType() != Material.ENCHANTED_BOOK) {
            return; // cut out early, we only care about enchanted_books for now
        }

        // Load the config
        FileConfiguration options = main.getConfig();

        // Load Profession config for villager profession
        if (options.isSet(villagerProfession.toString().toLowerCase())) {
            ConfigurationSection profession = options.getConfigurationSection(villagerProfession.toString().toLowerCase());

            if (profession.isSet(villagerBiome.toString().toLowerCase())) {
                // Load the biome list for the profession
                ConfigurationSection biome = profession.getConfigurationSection(villagerBiome.toString().toLowerCase());

                if (biome.isSet("allowedEnchants")) {
                	output.consoleInfo("What is null 1");
                    ConfigurationSection allowedEnchantsConfig = biome.getConfigurationSection("allowedEnchants");
                    output.consoleInfo("What is null 2");
                    Set<String> allowedEnchantNames = allowedEnchantsConfig.getKeys(false);
                    Map<Enchantment, Integer> allowedEnchants = new HashMap<>();
                    for (String enchantName : allowedEnchantNames) {
                    	output.consoleInfo(enchantName);
                        Enchantment enchantment = ItemHelper.resolveEnchant(enchantName);
                        Integer level = allowedEnchantsConfig.getInt(enchantName);
                        allowedEnchants.put(enchantment, level);
                    }

                    // Enchants to replace and their level
//                    Map<Enchantment, Integer> enchantmentsToReplace = new HashMap<Enchantment, Integer>();

                    // The set of enchants on the book (it's not actually enchanted...)
                    EnchantmentStorageMeta storedEnchantMeta = ((EnchantmentStorageMeta) recipe.getResult().getItemMeta());
                    Map<Enchantment, Integer> storedEnchants = storedEnchantMeta.getStoredEnchants();

                    for (Map.Entry<Enchantment, Integer> enchant : storedEnchants.entrySet()) {
                    	output.consoleInfo(enchant.getKey().toString());
                        if (!allowedEnchants.containsKey(enchant.getKey())) {
                            // TODO: remove it from the existingEnchants storedEnchants.removeStoredEnchant()
                            int selectedEnchantIndex = this.getRandomNumberUsingInts(0, allowedEnchants.size() - 1);

                            storedEnchantMeta.removeStoredEnchant(enchant.getKey());
                            ArrayList<Enchantment> randomEnchant = new ArrayList<Enchantment>(allowedEnchants.keySet());
                            Enchantment selectedEnchant = randomEnchant.get(selectedEnchantIndex);
                            Integer level = enchant.getValue() > selectedEnchant.getMaxLevel() ? selectedEnchant.getMaxLevel() : enchant.getValue();
                            output.consoleSuccess(selectedEnchant.toString() + " : " + level);
                            storedEnchantMeta.addStoredEnchant(selectedEnchant, level, false);
                            // TODO: add a new trade... because you can't change the result?
                        }
                    }

                    output.consoleInfo("Book was added successfully... I think");
                } else {
                	output.consoleError("No allowedEnchants");
                }
            } else {
            	output.consoleError("Biome not set: " + villagerBiome.toString());
            }
        } else {
        	output.consoleError("Profession not set: " + villagerProfession.toString());
        }
    }
}
