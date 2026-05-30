package me.davidml16.baul.pets.abilities.tiger;

import me.davidml16.baul.pets.abilities.AbilityStats;
import me.davidml16.baul.pets.abilities.IAbility;
import me.davidml16.baul.pets.utils.enums.Stats;
import org.bukkit.entity.Player;

public class MercilessSwipe implements IAbility {
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
        return new AbilityStats().addStatAmplifier(Stats.FEROCITY, 0.3, 0.3);
    }
}
