package io.sandbox.regionalTrade.events;

import io.sandbox.helpers.Output;
import io.sandbox.regionalTrade.Main;
import io.sandbox.regionalTrade.RegionalTradeConfig;

import org.bukkit.Material;
import org.bukkit.Server;
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
	RegionalTradeConfig config;
    
    public RegionalTradeEventHandlers(Main theMain, RegionalTradeConfig tradeConfig) {
		main = theMain;
		server = main.getServer();
		output = new Output(server);
		config = tradeConfig;
	}

    public int getRandomNumberUsingInts(int min, int max) {
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
        
        // Here we can say that the merchant was NOT allowed to learn the enchant, so we need to swap it out.
        // TODO: remove it from the existingEnchants storedEnchants.removeStoredEnchant()
//        int selectedEnchantIndex = this.getRandomNumberUsingInts(0, allowedEnchants.size() - 1);
//
//        storedEnchantMeta.removeStoredEnchant(enchant.getKey());
//        ArrayList<Enchantment> randomEnchant = new ArrayList<Enchantment>(allowedEnchants.keySet());
//        Enchantment selectedEnchant = randomEnchant.get(selectedEnchantIndex);
//        Integer level = enchant.getValue() > selectedEnchant.getMaxLevel() ? selectedEnchant.getMaxLevel() : enchant.getValue();
//        output.consoleSuccess(selectedEnchant.toString() + " : " + level);
//        storedEnchantMeta.addStoredEnchant(selectedEnchant, level, false);
        // TODO: add a new trade... because you can't change the result?
    }
}
