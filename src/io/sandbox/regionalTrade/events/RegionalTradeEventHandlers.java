package io.sandbox.regionalTrade.events;

import io.sandbox.helpers.OutputHelper;
import io.sandbox.regionalTrade.Main;
import io.sandbox.regionalTrade.RegionalTradeConfig;

import org.bukkit.Material;
import org.bukkit.Server;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Villager;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.VillagerAcquireTradeEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.MerchantRecipe;
import org.bukkit.inventory.meta.EnchantmentStorageMeta;
import java.util.*;

public class RegionalTradeEventHandlers implements Listener {
	Main main;
	Server server;
	OutputHelper output;
	RegionalTradeConfig config;
    
    public RegionalTradeEventHandlers(Main theMain, RegionalTradeConfig tradeConfig) {
		main = theMain;
		server = main.getServer();
		output = new OutputHelper(server);
		config = tradeConfig;
	}

    private int getRandomNumberUsingInts(int min, int max) {
        Random random = new Random();
        return random.ints(min, max).findFirst().getAsInt();
    }

    @EventHandler
    public void onVillagerAcquiresTrade(VillagerAcquireTradeEvent event) {
        MerchantRecipe recipe = event.getRecipe();
        if (recipe.getResult().getType() != Material.ENCHANTED_BOOK) {
            return; // cut out early, we only care about enchanted_books for now
        }
        
        Villager merchant = (Villager) event.getEntity();
        Villager.Profession profession = merchant.getProfession();
        Villager.Type villagerBiome = merchant.getVillagerType();

        EnchantmentStorageMeta storedEnchantMeta = ((EnchantmentStorageMeta) recipe.getResult().getItemMeta());
        for (Enchantment enchant : storedEnchantMeta.getStoredEnchants().keySet()) { // This will loop even though in this case it would always only be one item.
        	// We can leave early if the merchant is allowed to learn the enchant.
        	if (config.canLearn(profession, villagerBiome, enchant)) { return; }
        }
        
        // Select a new enchantment.
        ArrayList<Enchantment> allowedEnchants = config.enchantList(profession, villagerBiome);
        int selectedEnchantIndex = this.getRandomNumberUsingInts(0, allowedEnchants.size() - 1);
        Enchantment selectedEnchant = allowedEnchants.get(selectedEnchantIndex);
        
        // Select a new enchantment level.
        Integer level = this.getRandomNumberUsingInts(1, selectedEnchant.getMaxLevel());
        
        // Create a new result book item and add the enchant we selected to it.
        ItemStack newBook = new ItemStack(Material.ENCHANTED_BOOK);
        newBook.addEnchantment(selectedEnchant, level);
        
        // Create a new recipe, set its ingredients.
        MerchantRecipe newRecipe = new MerchantRecipe(newBook, 1);
        newRecipe.addIngredient(new ItemStack(Material.BOOK));
        newRecipe.addIngredient(new ItemStack(Material.EMERALD, 3));
        
        // Finally, set the resulting recipe to the event.
        event.setRecipe(newRecipe);
    }
}
