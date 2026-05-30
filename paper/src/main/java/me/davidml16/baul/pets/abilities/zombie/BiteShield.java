package me.davidml16.baul.pets.abilities.zombie;

import me.davidml16.baul.pets.abilities.AbilityStats;
import me.davidml16.baul.pets.abilities.IAbility;
import me.davidml16.baul.pets.pet.Pet;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.entity.EntityDamageByEntityEvent;

public class BiteShield implements IAbility {
    @Override
    public void onEquip(Player paramPlayer) {

    }

    @Override
    public void onUnequip(Player paramPlayer) {

    }

    @Override
    public void handleEvent(Event event, Player owner) {
        if (!(event instanceof EntityDamageByEntityEvent damageEvent)) return;
        if (!(damageEvent.getEntity() instanceof Player player)) return;
        if (!player.equals(owner)) return;

        Pet pet = playerPetManager.getActivePet(player);
        if (pet == null) return;

        if (damageEvent.getDamager().getType() == EntityType.ZOMBIE ||
                damageEvent.getDamager().getType() == EntityType.ZOMBIE_VILLAGER ||
                damageEvent.getDamager().getType() == EntityType.HUSK ||
                damageEvent.getDamager().getType() == EntityType.DROWNED) {

            double damageReduction = getAbilityStat().getStatAmplifiers().getFirst().getStatAtLevel(pet.getLevel());
            double currentDamage = damageEvent.getDamage();
            double newDamage = currentDamage * (1.0 - damageReduction / 100.0);
            damageEvent.setDamage(newDamage);
        }
    }

    @Override
    public AbilityStats getAbilityStat() {
        return new AbilityStats().addStatAmplifier(15, 0.1);
    }
}
