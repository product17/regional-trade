package io.sandbox.regionalTrade.events;

import io.sandbox.helpers.ItemHelper;
import io.sandbox.regionalTrade.Main;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Villager;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.VillagerAcquireTradeEvent;
import org.bukkit.inventory.MerchantRecipe;
import org.bukkit.inventory.meta.EnchantmentStorageMeta;
import org.bukkit.plugin.Plugin;
import java.util.*;

import static org.bukkit.Bukkit.getServer;

public class RegionalTradeEventHandlers implements Listener {
    private Plugin plugin = Main.getPlugin(Main.class);

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
        FileConfiguration options = plugin.getConfig();

        // Load Profession config for villager profession
        if (options.isSet(villagerProfession.toString().toLowerCase())) {
            ConfigurationSection profession = options.getConfigurationSection(villagerProfession.toString().toLowerCase());

            if (profession.isSet(villagerBiome.toString().toLowerCase())) {
                // Load the biome list for the profession
                ConfigurationSection biome = profession.getConfigurationSection(villagerBiome.toString().toLowerCase());

                if (biome.isSet("allowedEnchants")) {
                    getServer().getConsoleSender().sendMessage(ChatColor.BLUE + "What is null 1");
                    ConfigurationSection allowedEnchantsConfig = biome.getConfigurationSection("allowedEnchants");
                    getServer().getConsoleSender().sendMessage(ChatColor.BLUE + "What is null 2");
                    Set<String> allowedEnchantNames = allowedEnchantsConfig.getKeys(false);
                    Map<Enchantment, Integer> allowedEnchants = new HashMap<>();
                    for (String enchantName : allowedEnchantNames) {
                        getServer().getConsoleSender().sendMessage(ChatColor.BLUE + enchantName);
                        Enchantment enchantment = ItemHelper.resolveEnchant(enchantName);
                        Integer level = allowedEnchantsConfig.getInt(enchantName);
                        allowedEnchants.put(enchantment, level);
                    }

                    // Enchants to replace and their level
                    Map<Enchantment, Integer> enchantmentsToReplace = new HashMap<Enchantment, Integer>();

                    // The set of enchants on the book (it's not actually enchanted...)
                    EnchantmentStorageMeta storedEnchantMeta = ((EnchantmentStorageMeta) recipe.getResult().getItemMeta());
                    Map<Enchantment, Integer> storedEnchants = storedEnchantMeta.getStoredEnchants();

                    if (storedEnchants.size() > 0) {
                        for (Map.Entry<Enchantment, Integer> enchant : storedEnchants.entrySet()) {
                            getServer().getConsoleSender().sendMessage(ChatColor.BLUE + enchant.getKey().toString());
                            if (!allowedEnchants.containsKey(enchant.getKey())) {
                                // TODO: remove it from the existingEnchants storedEnchants.removeStoredEnchant()
                                int selectedEnchantIndex = this.getRandomNumberUsingInts(0, allowedEnchants.size() - 1);

                                storedEnchantMeta.removeStoredEnchant(enchant.getKey());
                                ArrayList<Enchantment> randomEnchant = new ArrayList<Enchantment>(allowedEnchants.keySet());
                                Enchantment selectedEnchant = randomEnchant.get(selectedEnchantIndex);
                                Integer level = enchant.getValue() > selectedEnchant.getMaxLevel() ? selectedEnchant.getMaxLevel() : enchant.getValue();
                                getServer().getConsoleSender().sendMessage(ChatColor.GREEN + selectedEnchant.toString() + " : " + level);
                                storedEnchantMeta.addStoredEnchant(selectedEnchant, level, false);
                                // TODO: add a new trade... because you can't change the result?
                            }
                        }
                    }

                    getServer().getConsoleSender().sendMessage(ChatColor.BLUE + "Book was added successfully... I think");
                } else {
                    getServer().getConsoleSender().sendMessage(ChatColor.RED + "No allowedEnchants");
                }
            } else {
                getServer().getConsoleSender().sendMessage(ChatColor.RED + "Biome not set: " + villagerBiome.toString());
            }
        } else {
            getServer().getConsoleSender().sendMessage(ChatColor.RED + "Profession not set: " + villagerProfession.toString());
        }
    }
}
