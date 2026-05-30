package me.davidml16.baul.pets.abilities.flying_fish;

import me.davidml16.baul.pets.abilities.AbilityStats;
import me.davidml16.baul.pets.abilities.IAbility;
import org.bukkit.entity.Player;

public class RapidDecay implements IAbility {

    @Override
    public void onEquip(Player owner) {
        // Event-based ability - no equip action needed
    }

    @Override
    public void onUnequip(Player owner) {
        // Event-based ability - no unequip action needed
    }

    @Override
    public AbilityStats getAbilityStat() {
        return null;
    }
}
