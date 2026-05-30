package me.davidml16.baul.pets.abilities.elephant;

import me.davidml16.baul.pets.abilities.AbilityStats;
import me.davidml16.baul.pets.abilities.IAbility;
import me.davidml16.baul.pets.pet.Pet;
import me.davidml16.baul.pets.utils.PetUtils;
import me.davidml16.baul.pets.utils.enums.Stats;
import org.bukkit.entity.Player;

public class Stomp implements IAbility {

    @Override
    public void onEquip(Player owner) {
        Pet pet = playerPetManager.getActivePet(owner);
        if (pet == null) return;

        // EcoSkills API has been removed - stat modifications disabled
        // Default speed value: 5.0
    }

    @Override
    public void onUnequip(Player owner) {
        // EcoSkills API has been removed - no modifiers to remove
    }

    @Override
    public AbilityStats getAbilityStat() {
        return new AbilityStats().addStatAmplifier(Stats.DEFENSE, 0.2, 0.2);
    }
}
