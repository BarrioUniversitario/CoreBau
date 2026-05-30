package me.davidml16.baul.pets.abilities.endermite;

import me.davidml16.baul.pets.abilities.AbilityStats;
import me.davidml16.baul.pets.abilities.IAbility;
import org.bukkit.entity.Player;

public class Sacrificer implements IAbility {

    @Override
    public void onEquip(Player owner) {
    }

    @Override
    public void onUnequip(Player owner) {
        // Remove any applied bonus odds
    }

    @Override
    public AbilityStats getAbilityStat() {
        return null;
    }
}
