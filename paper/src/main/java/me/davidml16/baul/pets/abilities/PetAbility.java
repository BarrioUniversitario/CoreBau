package me.davidml16.baul.pets.abilities;

import org.bukkit.entity.Player;
import org.bukkit.event.Event;

import java.util.List;

public record PetAbility(String name, List<String> description, IAbility ability) {

    public void onEquip(Player owner) {
        this.ability.onEquip(owner);
    }

    public void onUnequip(Player owner) {
        this.ability.onUnequip(owner);
    }

    public void handleEvent(Event event, Player owner) {
        this.ability.handleEvent(event, owner);
    }

    
    public double getStatValueAtLevel(int level, int index) {
        AbilityStats stats = this.ability.getAbilityStat();
        if (stats == null || stats.getStatAmplifiers().isEmpty()) {
            return 0.0;
        }
        
        AbilityStats.StatAmplifier firstStat = stats.getStatAmplifiers().get(index);
        return firstStat.getStatAtLevel(level);
    }

    public AbilityStats getAbilityStats() {
        return this.ability.getAbilityStat();
    }
}