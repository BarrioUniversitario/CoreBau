package me.davidml16.baul.pets.abilities.horse;

import me.davidml16.baul.pets.abilities.AbilityStats;
import me.davidml16.baul.pets.abilities.IAbility;
import me.davidml16.baul.pets.pet.Pet;
import me.davidml16.baul.pets.utils.Keys;
import me.davidml16.baul.pets.utils.enums.Stats;
import org.bukkit.Bukkit;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeModifier;
import org.bukkit.entity.Horse;
import org.bukkit.entity.Player;

public class Run implements IAbility {

    @Override
    public void onEquip(Player paramPlayer) {
        applyAbilityStats(paramPlayer);
        
        Horse horse = (Horse) Bukkit.getEntity(mountManager.getMount(paramPlayer.getUniqueId()));
        Pet pet = playerPetManager.getActivePet(paramPlayer);

        double increase = 1.2 * pet.getLevel();

        horse.getAttribute(Attribute.MOVEMENT_SPEED).addModifier(new AttributeModifier(
                Keys.MOUNT_SPEED_KEY,
                increase / 100,
                AttributeModifier.Operation.ADD_NUMBER
        ));

    }

    @Override
    public void onUnequip(Player paramPlayer) {
        removeAbilityStats(paramPlayer);
    }

    @Override
    public AbilityStats getAbilityStat() {
        return new AbilityStats().addStatAmplifier(Stats.SPEED, 0.5, 0.5);
    }
}
