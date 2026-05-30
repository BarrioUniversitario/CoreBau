package me.davidml16.baul.pets.items.perks;

import me.davidml16.baul.pets.items.types.PerkItem;
import me.davidml16.baul.pets.pet.Pet;
import org.bukkit.Material;

import java.util.List;

public class Example extends PerkItem {

    public Example(String id, String displayName, Material material, List<String> lore) {
        super(id, displayName, material, lore);
    }

    @Override
    public void applyEffect(Pet pet) {

    }

    @Override
    public void removeEffect(Pet pet) {

    }
}
