package me.davidml16.baul.pets.items.types;

import me.davidml16.baul.pets.items.PetItem;
import me.davidml16.baul.pets.pet.Pet;
import me.davidml16.baul.pets.utils.enums.PetItemType;
import org.bukkit.Material;

import java.util.List;

public class ExpBoostItem extends PetItem {
    private final double amount;

    public ExpBoostItem(String id, String displayName, Material material, List<String> lore, double amount) {
        super(id, displayName, material, lore, PetItemType.EXP_BOOST);
        this.amount = amount;
    }

    @Override
    public void applyEffect(Pet pet) {
        // Convert percentage to multiplier (e.g., 20% -> 1.2)
        double multiplier = 1 + (amount / 100.0);
        pet.setExpMultiplier(pet.getExpMultiplier() * multiplier);
    }

    @Override
    public void removeEffect(Pet pet) {
        double multiplier = 1 + (amount / 100.0);
        pet.setExpMultiplier(pet.getExpMultiplier() / multiplier);
    }
}
