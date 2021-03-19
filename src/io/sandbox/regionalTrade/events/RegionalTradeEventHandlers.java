package io.sandbox.regionalTrade.events;

import io.sandbox.helpers.ItemHelper;
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

    @EventHandler
    public void onVillagerAcquiresTrade(VillagerAcquireTradeEvent event) {
        MerchantRecipe recipe = event.getRecipe();
        Villager merchant = (Villager) event.getEntity();
        Villager.Profession profession = merchant.getProfession();
        Villager.Type villagerBiome = merchant.getVillagerType();
        
        switch (profession) {
        case LIBRARIAN:
        	switch (recipe.getResult().getType()) {
        	case ENCHANTED_BOOK:
        		enchantingTradeHandler(event, recipe, merchant, profession, villagerBiome);
        	default:
        	}
        default:
        }
    }
    
    private void enchantingTradeHandler(VillagerAcquireTradeEvent event, MerchantRecipe recipe, Villager merchant, Villager.Profession profession, Villager.Type villagerBiome) {
    	// If there are no allowed enchants listed then we should allow the default game behavior.
    	ArrayList<Enchantment> allowedEnchants = config.enchantList(profession, villagerBiome);
        if (allowedEnchants.isEmpty()) { return; }
        
        Enchantment selectedEnchant = null;
    	EnchantmentStorageMeta storedEnchantMeta = ((EnchantmentStorageMeta) recipe.getResult().getItemMeta());
        for (Enchantment enchant : storedEnchantMeta.getStoredEnchants().keySet()) { // This will loop even though in this case it would always only be one item.
        	// We want the curses to just always be allowed on everything.
        	if (enchant == Enchantment.BINDING_CURSE || enchant == Enchantment.VANISHING_CURSE) { return; }
        	
        	// We can leave early if the merchant is allowed to learn the enchant.
        	if (config.canLearn(profession, villagerBiome, enchant)) {
        		selectedEnchant = enchant;
        	}
        }
        
        // Select a new enchantment if the one we had was not allowed.
        if (selectedEnchant == null) {
        	int selectedEnchantIndex = ItemHelper.getRandomInt(0, allowedEnchants.size() - 1);
        	selectedEnchant = allowedEnchants.get(selectedEnchantIndex);
        }
        
        // Select a new enchantment level.
        Integer level = merchant.getVillagerLevel();
        if (level > selectedEnchant.getMaxLevel()) {
        	level = selectedEnchant.getMaxLevel();
        }
        
        // Create a new result book item and add the enchant we selected to it.
        ItemStack newBook = new ItemStack(Material.ENCHANTED_BOOK);
        storedEnchantMeta = (EnchantmentStorageMeta) newBook.getItemMeta();
        storedEnchantMeta.addEnchant(selectedEnchant, level, false);
        newBook.setItemMeta(storedEnchantMeta);
        
        // Create a new recipe, set its ingredients.
        MerchantRecipe newRecipe = new MerchantRecipe(newBook, 1);
        newRecipe.addIngredient(new ItemStack(Material.BOOK));
        newRecipe.addIngredient(new ItemStack(Material.EMERALD, ItemHelper.getWeightedEmeraldCost(selectedEnchant, level)));
        
        // Finally, set the resulting recipe to the event.
        event.setRecipe(newRecipe);
    }
}
