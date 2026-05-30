package me.davidml16.baul.pets.abilities.monkey;

import me.davidml16.baul.pets.abilities.AbilityStats;
import me.davidml16.baul.pets.abilities.IAbility;
import me.davidml16.baul.pets.utils.enums.Stats;
import org.bukkit.entity.Player;

public class Treeborn implements IAbility {

    @Override
    public void onEquip(Player owner) {
        applyAbilityStats(owner);

    }

    @Override
    public void onUnequip(Player owner) {
        removeAbilityStats(owner);
    }

    @Override
    public AbilityStats getAbilityStat() {
        return new AbilityStats().addStatAmplifier(Stats.FORAGING_FORTUNE, 0.6, 0.6);
    }
}
