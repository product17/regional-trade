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
        MerchantRecipe newRecipe = null;

        switch (profession) {
        case LIBRARIAN:
        	switch (recipe.getResult().getType()) {
        	case ENCHANTED_BOOK:
        		newRecipe = enchantingTradeHandler(event, recipe, merchant, profession, villagerBiome);
        	default:
        	}
        default:
        }
        
        if (newRecipe == null) { return; }
        
        event.setRecipe(newRecipe);
    }
    
    private MerchantRecipe enchantingTradeHandler(VillagerAcquireTradeEvent event, MerchantRecipe recipe, Villager merchant, Villager.Profession profession, Villager.Type villagerBiome) {
    	// If there are no allowed enchants listed then we should allow the default game behavior.
    	ArrayList<Enchantment> allowedEnchants = config.enchantList(profession, villagerBiome);
    	output.consoleInfo(villagerBiome.toString());
    	output.consoleInfo(allowedEnchants.toString());
        if (allowedEnchants.isEmpty()) { return null; }
        
        int merchantLevel = merchant.getVillagerLevel();
        Enchantment selectedEnchant = null;
    	EnchantmentStorageMeta storedEnchantMeta = null;
    	
    	for (MerchantRecipe ownedRecipe : merchant.getRecipes()) {
    		if (ownedRecipe.getResult().getType() != Material.ENCHANTED_BOOK) { continue; }
    		
    		storedEnchantMeta = (EnchantmentStorageMeta) recipe.getResult().getItemMeta();
            for (Enchantment enchant : storedEnchantMeta.getStoredEnchants().keySet()) { // This will loop even though in this case it would always only be one item.
            	selectedEnchant = enchant;
            	break;
            }
    	}
        
    	if (selectedEnchant == null || selectedEnchant == Enchantment.BINDING_CURSE || selectedEnchant == Enchantment.VANISHING_CURSE) {
    		storedEnchantMeta = (EnchantmentStorageMeta) recipe.getResult().getItemMeta();
            for (Enchantment enchant : storedEnchantMeta.getStoredEnchants().keySet()) { // This will loop even though in this case it would always only be one item.
            	// We want the curses to just always be allowed on everything.
            	if (enchant == Enchantment.BINDING_CURSE || enchant == Enchantment.VANISHING_CURSE) { return null; }
            	
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
    	}
        
        // Select a new enchantment level.
        int levelOffset = 5 - selectedEnchant.getMaxLevel();
        Integer level = merchantLevel - levelOffset;
        
        // Create a new result book item and add the enchant we selected to it.
        ItemStack newBook = new ItemStack(Material.ENCHANTED_BOOK);
        storedEnchantMeta = (EnchantmentStorageMeta) newBook.getItemMeta();
        if (level > 0) {
            storedEnchantMeta.addEnchant(selectedEnchant, level, false);
        } else {
            String levelName = "";
            switch (levelOffset) {
            case 1:
            	levelName = "APPRENTICE (2)";
            	break;
            case 2:
            	levelName = "JOURNEYMAN (3)";
            	break;
            case 3:
            	levelName = "EXPERT (4)";
            	break;
            case 4:
            	levelName = "MASTER (5)";
            }
            storedEnchantMeta.addEnchant(selectedEnchant, 1, false);
            storedEnchantMeta.setDisplayName("GAINED AT  @  " + levelName);
        }
        newBook.setItemMeta(storedEnchantMeta);
        
        // Create a new recipe, set its ingredients.
        MerchantRecipe newRecipe = new MerchantRecipe(newBook, 1);
        newRecipe.addIngredient(new ItemStack(Material.BOOK));
        newRecipe.addIngredient(new ItemStack(Material.EMERALD, ItemHelper.getWeightedEmeraldCost(selectedEnchant, level)));
        
        if (level <= 0) {
        	newRecipe.setMaxUses(0);
        }
        
        // Finally, return the resulting recipe.
        return newRecipe;
    }
}
