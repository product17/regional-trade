package io.sandbox.regionalTrade;

import java.util.ArrayList;
import java.util.HashMap;

import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Villager;

public class RegionalTradeConfig {
	HashMap<Villager.Profession, HashMap<Villager.Type, ArrayList<Enchantment>>> enchantSets;
	
	public RegionalTradeConfig() {
		enchantSets = new HashMap<Villager.Profession, HashMap<Villager.Type, ArrayList<Enchantment>>>();
	}
	
	public ArrayList<Enchantment> enchantList(Villager.Profession profession, Villager.Type villagerBiome) {
		HashMap<Villager.Type, ArrayList<Enchantment>> typeSet = enchantSets.get(profession);
		if (typeSet == null) {
			typeSet = new HashMap<Villager.Type, ArrayList<Enchantment>>();
			enchantSets.put(profession, typeSet);
		}
		
		ArrayList<Enchantment> enchantments = typeSet.get(villagerBiome);
		if (enchantments == null) {
			enchantments = new ArrayList<Enchantment>();
			typeSet.put(villagerBiome, enchantments);
		}
		
		return enchantments;
	}
	
	public boolean addEnchantment(Villager.Profession profession, Villager.Type villagerBiome, Enchantment enchant) {
		ArrayList<Enchantment> enchantments = this.enchantList(profession, villagerBiome);

		if (enchantments.contains(enchant)) {
			return false;
		}
		
		enchantments.add(enchant);
		return true;
	}
	
	public boolean canLearn(Villager.Profession profession, Villager.Type villagerBiome, Enchantment enchant) {
		ArrayList<Enchantment> enchantments = this.enchantList(profession, villagerBiome);
		return enchantments.contains(enchant);
	}
	
	public boolean cannotLearn(Villager.Profession profession, Villager.Type villagerBiome, Enchantment enchant) {
		return !this.canLearn(profession, villagerBiome, enchant);
	}
}
