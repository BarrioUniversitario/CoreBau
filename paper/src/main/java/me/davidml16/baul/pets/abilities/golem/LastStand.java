package me.davidml16.baul.pets.abilities.golem;

import me.davidml16.baul.pets.abilities.AbilityStats;
import me.davidml16.baul.pets.abilities.IAbility;
import org.bukkit.attribute.Attribute;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.entity.EntityDamageEvent;

public class LastStand implements IAbility {
    @Override
    public void onEquip(Player owner) {
    }

    @Override
    public void onUnequip(Player owner) {
    }

    @Override
    public void handleEvent(Event event, Player owner) {
        if (event instanceof EntityDamageEvent damageEvent) {
            double maxHealth = owner.getAttribute(Attribute.MAX_HEALTH).getValue();
            double health = owner.getHealth();
            if (health / maxHealth <= 0.2) {
                double damage = damageEvent.getFinalDamage();
                double newDamage = damage * (1 - 0.2);
                damageEvent.setDamage(newDamage);
            }
        }
    }

    @Override
    public AbilityStats getAbilityStat() {
        return null;
    }
}
