package me.davidml16.baul.pets.abilities.bee;

import me.davidml16.baul.pets.abilities.AbilityStats;
import me.davidml16.baul.pets.abilities.IAbility;
import me.davidml16.baul.pets.utils.enums.Stats;
import org.bukkit.entity.Player;

public class BusyBuzz implements IAbility {

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
        AbilityStats abilityStats = new AbilityStats();
        abilityStats.addStatAmplifier(Stats.FARMING_FORTUNE, 0.3, 0.3);
        abilityStats.addStatAmplifier(Stats.FORAGING_FORTUNE, 0.3, 0.3);
        abilityStats.addStatAmplifier(Stats.MINING_FORTUNE, 0.3, 0.3);
        return abilityStats;
    }
}
