package me.davidml16.baul.pets.abilities.sheep;

import me.davidml16.baul.pets.abilities.AbilityStats;
import me.davidml16.baul.pets.abilities.IAbility;
import me.davidml16.baul.pets.utils.enums.Stats;
import org.bukkit.entity.Player;

public class Overheal implements IAbility {

    @Override
    public void onEquip(Player paramPlayer) {
        applyAbilityStats(paramPlayer);
    }

    @Override
    public void onUnequip(Player paramPlayer) {
        removeAbilityStats(paramPlayer);
    }

    @Override
    public AbilityStats getAbilityStat() {
        return new AbilityStats().addStatAmplifier(Stats.HEALTH, 0.5, 0.5);
    }
}
