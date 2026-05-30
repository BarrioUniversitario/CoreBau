package me.davidml16.baul.pets.abilities.monkey;

import me.davidml16.baul.pets.abilities.AbilityStats;
import me.davidml16.baul.pets.abilities.IAbility;
import me.davidml16.baul.pets.utils.enums.Stats;
import org.bukkit.entity.Player;

public class VineSwing implements IAbility {

    @Override
    public void onEquip(Player owner) {
        applyAbilityStats(owner);
    }

    @Override
    public void onUnequip(Player owner) {
        removeAbilityStats(owner);
    }

    private boolean isInThePark(Player player) {
        return player.getWorld().getName().equalsIgnoreCase("park") ||
               player.getWorld().getName().toLowerCase().contains("park");
    }

    @Override
    public AbilityStats getAbilityStat() {
        return new AbilityStats().addStatAmplifier(Stats.SPEED, 1, 1);
    }
}
