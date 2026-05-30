package me.davidml16.baul.pets.abilities.zombie;

import me.davidml16.baul.pets.abilities.AbilityStats;
import me.davidml16.baul.pets.abilities.IAbility;
import me.davidml16.baul.pets.pet.Pet;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.entity.EntityDamageByEntityEvent;

public class RottenBlade implements IAbility {
    @Override
    public void onEquip(Player paramPlayer) {

    }

    @Override
    public void onUnequip(Player paramPlayer) {

    }

    @Override
    public void handleEvent(Event event, Player owner) {
        if (!(event instanceof EntityDamageByEntityEvent damageEvent)) return;
        if (!damageEvent.getDamager().equals(owner)) return;
        if (!(damageEvent.getEntity() instanceof LivingEntity target)) return;

        Pet pet = playerPetManager.getActivePet(owner);
        if (pet == null) return;

        if (target.getType() == EntityType.ZOMBIE ||
                target.getType() == EntityType.ZOMBIE_VILLAGER ||
                target.getType() == EntityType.HUSK ||
                target.getType() == EntityType.DROWNED) {

            double damageBonus = getAbilityStat().getStatAmplifiers().getFirst().getStatAtLevel(pet.getLevel());
            double currentDamage = damageEvent.getDamage();
            double newDamage = currentDamage * (1.0 + damageBonus / 100.0);
            damageEvent.setDamage(newDamage);
        }
    }

    @Override
    public AbilityStats getAbilityStat() {
        return new AbilityStats().addStatAmplifier(26, 1.25);
    }
}
