package me.davidml16.baul.pets.abilities.chicken;

import me.davidml16.baul.pets.abilities.AbilityStats;
import me.davidml16.baul.pets.abilities.IAbility;
import me.davidml16.baul.pets.utils.enums.Stats;
import org.bukkit.entity.Player;

public class FreeRange implements IAbility {
    @Override
    public void onEquip(Player paramPlayer) {

    }

    @Override
    public void onUnequip(Player paramPlayer) {

    }

    @Override
    public AbilityStats getAbilityStat() {
        return new AbilityStats().addStatAmplifier(Stats.FARMING_FORTUNE, 1, 1);
    }
}
