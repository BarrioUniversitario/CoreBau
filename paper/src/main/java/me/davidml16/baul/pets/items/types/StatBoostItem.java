package me.davidml16.baul.pets.items.types;

import me.davidml16.baul.pets.items.PetItem;
import me.davidml16.baul.pets.pet.Pet;
import me.davidml16.baul.pets.pet.PetAttribute;
import me.davidml16.baul.pets.utils.enums.PetItemType;
import org.bukkit.Material;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class StatBoostItem extends PetItem {

    private final Map<String, Double> stats;

    public StatBoostItem(String id, String displayName, Material material, List<String> lore) {
        super(id, displayName, material, lore, PetItemType.STAT);
        this.stats = new HashMap<>();
    }

    public void addStatBoost(String stat, double amount) {
        stats.put(stat, amount);
    }

    @Override
    public void applyEffect(Pet pet) {
        // EcoSkills integration removed - stat boosts disabled
    }

    @Override
    public void removeEffect(Pet pet) {
        // EcoSkills integration removed - stat boosts disabled
    }

    public Map<String, Double> getStats() {
        return new HashMap<>(stats);
    }
}
