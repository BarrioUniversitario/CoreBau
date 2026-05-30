package me.davidml16.baul.pets.abilities.chicken;

import me.davidml16.baul.pets.abilities.AbilityStats;
import me.davidml16.baul.pets.abilities.IAbility;
import me.davidml16.baul.pets.pet.Pet;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.entity.EntityDamageEvent;

public class LightFeet implements IAbility {

    @Override
    public void handleEvent(Event event, Player owner) {
        if (!(event instanceof EntityDamageEvent damageEvent)) return;
        if (!damageEvent.getEntity().equals(owner)) return;
        if (damageEvent.getCause() != EntityDamageEvent.DamageCause.FALL) return;

        Pet pet = playerPetManager.getActivePet(owner);
        if (pet == null) return;

        double damageReduction = 1 + pet.getLevel();
        double originalDamage = damageEvent.getFinalDamage();
        double newDamage = originalDamage * (1 - damageReduction);

        damageEvent.setDamage(newDamage);
    }

    @Override
    public void onEquip(Player paramPlayer) {
        // Event-based ability - no equip action needed
    }

    @Override
    public void onUnequip(Player paramPlayer) {
        // Event-based ability - no unequip action needed
    }

    @Override
    public AbilityStats getAbilityStat() {
        return null;
    }
}
