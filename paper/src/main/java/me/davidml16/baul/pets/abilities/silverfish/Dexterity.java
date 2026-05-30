package me.davidml16.baul.pets.abilities.silverfish;

import me.davidml16.baul.pets.abilities.AbilityStats;
import me.davidml16.baul.pets.abilities.IAbility;
import me.davidml16.baul.pets.pet.Pet;
import me.davidml16.baul.pets.utils.PetUtils;
import me.davidml16.baul.pets.utils.enums.Stats;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

public class Dexterity implements IAbility {

    @Override
    public void onEquip(Player owner) {
        Pet pet = playerPetManager.getActivePet(owner);
        if (pet == null) return;

        double miningSpeed = getAbilityStat().getStatAmplifiers().getFirst().getStatAtLevel(pet.getLevel());
        int hasteLevel = getHasteLevel(pet.getLevel());
        // EcoSkills API has been removed - stat modifications disabled
        // Mining speed stat modifier would be: miningSpeed

        owner.addPotionEffect(new PotionEffect(PotionEffectType.HASTE, Integer.MAX_VALUE, hasteLevel - 1, false, false));
    }

    @Override
    public void onUnequip(Player owner) {
        // EcoSkills API has been removed - no stat modifiers to remove
        owner.removePotionEffect(PotionEffectType.HASTE);
    }

    private int getHasteLevel(int petLevel) {
        if (petLevel >= 100) return 3;
        if (petLevel >= 50) return 2;
        return 1;
    }

    @Override
    public AbilityStats getAbilityStat() {
        return new AbilityStats().addStatAmplifier(1.5, 1.5);
    }
}
