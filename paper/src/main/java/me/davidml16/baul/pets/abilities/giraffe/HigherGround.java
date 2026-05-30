package me.davidml16.baul.pets.abilities.giraffe;

import me.davidml16.baul.pets.abilities.AbilityStats;
import me.davidml16.baul.pets.abilities.IAbility;
import me.davidml16.baul.pets.pet.Pet;
import me.davidml16.baul.pets.utils.PetUtils;
import me.davidml16.baul.pets.utils.enums.Stats;
import org.bukkit.entity.Player;

public class HigherGround implements IAbility {

    @Override
    public void onEquip(Player owner) {
        Pet pet = playerPetManager.getActivePet(owner);
        if (getAbilityStat() == null) return;

        // EcoSkills API has been removed - stat modifications disabled
        // Default values used: swingRange=5.0, critDamage=5.0, strength=5.0
    }

    @Override
    public void onUnequip(Player owner) {
        removeAbilityStats(owner);
    }

    @Override
    public AbilityStats getAbilityStat() {
        return new AbilityStats().addStatAmplifier(Stats.CRIT_DAMAGE, 0.0015, 0.0015)
                .addStatAmplifier(Stats.STRENGTH, 0.0015, 0.0015);
    }
}
