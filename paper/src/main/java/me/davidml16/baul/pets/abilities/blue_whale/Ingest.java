package me.davidml16.baul.pets.abilities.blue_whale;

import me.davidml16.baul.pets.abilities.AbilityStats;
import me.davidml16.baul.pets.abilities.IAbility;
import me.davidml16.baul.pets.pet.Pet;
import me.davidml16.baul.pets.utils.PetUtils;
import me.davidml16.baul.pets.utils.enums.Stats;
import org.bukkit.entity.Player;

public class Ingest implements IAbility {

    @Override
    public void onEquip(Player owner) {
        int potions = owner.getActivePotionEffects().size();
        Pet pet = playerPetManager.getActivePet(owner);

        // EcoSkills API has been removed - stat modifications disabled
        // Health bonus calculation would be: potions * getAbilityStat().getStatAmplifier(Stats.HEALTH).getStatAtLevel(pet.getLevel())
    }

    @Override
    public void onUnequip(Player owner) {
        // EcoSkills API has been removed - no modifiers to remove
    }


    @Override
    public AbilityStats getAbilityStat() {
        return new AbilityStats().addStatAmplifier(Stats.HEALTH, 2.5, 2.5);
    }
}
