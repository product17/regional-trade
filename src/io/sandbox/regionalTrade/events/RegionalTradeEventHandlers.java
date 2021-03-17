package io.sandbox.regionalTrade.events;

import io.sandbox.regionalTrade.Main;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.block.Biome;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.FileConfigurationOptions;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Villager;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.VillagerAcquireTradeEvent;
import org.bukkit.event.inventory.InventoryOpenEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.MerchantRecipe;
import org.bukkit.inventory.meta.EnchantmentStorageMeta;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;

import java.lang.reflect.Array;
import java.util.*;

import static org.bukkit.Bukkit.getServer;

public class RegionalTradeEventHandlers implements Listener {
    private Plugin plugin = Main.getPlugin(Main.class);

    public int getRandomNumberUsingInts(int min, int max) {
        Random random = new Random();
        return random.ints(min, max).findFirst().getAsInt();
    }

    @EventHandler
    public void onOpenTradeWindow(InventoryOpenEvent event) {
        HumanEntity player = event.getPlayer();
        Inventory inventory =  event.getInventory();
        if (
            inventory.getType() == InventoryType.MERCHANT &&
            inventory.getHolder() instanceof Villager
        ) {
            List<MerchantRecipe> recipes = ((Villager) inventory.getHolder()).getRecipes();
            player.sendMessage("Trade count: " + recipes.size());
            for (MerchantRecipe recipe : recipes) {
                if (recipe.getResult().getType() != Material.ENCHANTED_BOOK) {
                    continue;
                }
                EnchantmentStorageMeta storedEnchants = (EnchantmentStorageMeta) recipe.getResult().getItemMeta();
                Map<Enchantment, Integer> existingEnchants = storedEnchants.getStoredEnchants();
                player.sendMessage(String.valueOf(existingEnchants.size()) + " is the count");
                if (existingEnchants.size() > 0) {
                    for (Map.Entry<Enchantment, Integer> enchant : existingEnchants.entrySet()) {
                        player.sendMessage(enchant.getKey().toString());
//                        getServer().getConsoleSender().sendMessage(ChatColor.BLUE + "hit");
//                        if (allowedEnchants.contains(Enchantment.getByKey(enchant.getKey()))) {
//                            isAllowed = true;
//                            enchantLevel = recipe.getResult().getEnchantments().get(enchant.toString());
//                        }
                    }
                }
            }
        }
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
                    Set<String> allowedEnchants = biome.getConfigurationSection("allowedEnchants").getKeys(false);
                    // TODO: make this into a map instead of a bool, to handle multiple enchants
                    Boolean isAllowed = false; // to be added list?
                    // TODO: same as above, or just combine the two, Map<Enchantment, Integer>
                    int enchantLevel = 1;

                    EnchantmentStorageMeta storedEnchants = (EnchantmentStorageMeta) recipe.getResult().getItemMeta();
                    Map<Enchantment, Integer> existingEnchants = storedEnchants.getStoredEnchants();

                    if (existingEnchants.size() > 0) {
                        for (Map.Entry<Enchantment, Integer> enchant : existingEnchants.entrySet()) {
                            if (allowedEnchants.contains(enchant.getKey().toString())) { // TODO: Not sure what this string looks like, check line 121 for enchant name stuff
                                isAllowed = true;
                                enchantLevel = recipe.getResult().getEnchantments().get(enchant.toString());
                            } else {
                                // TODO: remove it from the existingEnchants storedEnchants.removeStoredEnchant()
                            }
                        }
                    }

                    // TODO: turn this into a loop over the isAllowed Map and reset/remove any enchants not allowed
                    if (!isAllowed) {
                        // pick a random enchant, maybe need to remove any that exist on the book already
                        int selectedEnchantIndex = this.getRandomNumberUsingInts(0, allowedEnchants.size() - 1);
                        String updatedEnchantmentName = allowedEnchants.toArray()[selectedEnchantIndex].toString();

                        // Turn it into an Enchantment from name
                        Enchantment enchantment = Enchantment.getByKey(NamespacedKey.minecraft(updatedEnchantmentName));

                        // Check max level of selected enchant
                        int maxLevel = enchantment.getMaxLevel();

                        // reduce level to max if over
                        if (enchantLevel > maxLevel) {
                            enchantLevel = maxLevel;
                        }

                        // TODO: Add the enchant to the book "storedEnchants.addStoredEnchant()"
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
