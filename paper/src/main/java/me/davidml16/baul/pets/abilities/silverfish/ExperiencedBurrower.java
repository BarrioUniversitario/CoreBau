package me.davidml16.baul.pets.abilities.silverfish;

import me.davidml16.baul.pets.abilities.AbilityStats;
import me.davidml16.baul.pets.abilities.IAbility;
import me.davidml16.baul.pets.utils.enums.Stats;
import org.bukkit.entity.Player;

public class ExperiencedBurrower implements IAbility {

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
        return new AbilityStats().addStatAmplifier(Stats.MINING_WISDOM, 0.3, 0.3);
    }
}
