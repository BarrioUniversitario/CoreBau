package me.davidml16.baul.pets.abilities.ender_dragon;

import me.davidml16.baul.pets.abilities.AbilityStats;
import me.davidml16.baul.pets.abilities.IAbility;
import me.davidml16.baul.pets.pet.Pet;
import me.davidml16.baul.pets.utils.PetUtils;
import me.davidml16.baul.pets.utils.enums.Stats;
import org.bukkit.entity.Player;

public class Superior implements IAbility {

    private final String[] stats = {
            "health", "strength", "defense", "speed",
            "crit_chance", "crit_damage", "wisdom",
            "ferocity", "attack_speed", "ability_damage"
    };

    @Override
    public void onEquip(Player paramPlayer) {
        Pet pet = playerPetManager.getActivePet(paramPlayer);
        if (pet == null) return;

        // EcoSkills API has been removed - stat modifications disabled
        // Default stat values: 5.0
        // Stat bonus calculation would be: 0.1 + (0.1 * (pet.getLevel() - 1))
    }

    @Override
    public void onUnequip(Player paramPlayer) {
        // EcoSkills API has been removed - no modifiers to remove
    }

    @Override
    public AbilityStats getAbilityStat() {
        return null;
    }
}
