package me.davidml16.baul.pets.abilities.blue_whale;

import me.davidml16.baul.pets.abilities.AbilityStats;
import me.davidml16.baul.pets.abilities.IAbility;
import me.davidml16.baul.pets.pet.Pet;
import me.davidml16.baul.pets.utils.PetUtils;
import me.davidml16.baul.pets.utils.enums.Stats;
import org.bukkit.entity.Player;

public class Bulk implements IAbility {

    @Override
    public void onEquip(Player owner) {
        Pet pet = playerPetManager.getActivePet(owner);
        if (getAbilityStat() == null) return;

        // EcoSkills API has been removed - stat modifications disabled
        // Default health value: 5.0
    }

    @Override
    public void onUnequip(Player owner) {
        // EcoSkills API has been removed - no modifiers to remove
    }

    @Override
    public AbilityStats getAbilityStat() {
        return new AbilityStats().addStatAmplifier(Stats.DEFENSE, 0.01, 0.01);
    }
}
