package me.davidml16.baul.pets.abilities.tiger;

import me.davidml16.baul.pets.abilities.AbilityStats;
import me.davidml16.baul.pets.abilities.IAbility;
import me.davidml16.baul.pets.pet.Pet;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.entity.EntityDamageByEntityEvent;

public class ApexPredator implements IAbility {
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
        if (target instanceof Player) return; // Don't affect other players

        Pet pet = playerPetManager.getActivePet(owner);
        if (pet == null) return;

        if (isTargetIsolated(target)) {
            double damageBonus = getAbilityStat().getStatAmplifiers().getFirst().getStatAtLevel(pet.getLevel());
            double currentDamage = damageEvent.getDamage();
            double newDamage = currentDamage * (1.0 + damageBonus / 100.0);
            damageEvent.setDamage(newDamage);
        }
    }

    private boolean isTargetIsolated(LivingEntity target) {
        long nearbyMobCount = target.getNearbyEntities(15, 15, 15).stream()
                .filter(entity -> entity instanceof LivingEntity)
                .filter(entity -> !(entity instanceof Player))
                .filter(entity -> !entity.equals(target))
                .count();

        return nearbyMobCount == 0;
    }

    @Override
    public AbilityStats getAbilityStat() {
        return new AbilityStats().addStatAmplifier(1, 1);
    }
}
