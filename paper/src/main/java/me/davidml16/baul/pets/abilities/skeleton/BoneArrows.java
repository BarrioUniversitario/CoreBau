package me.davidml16.baul.pets.abilities.skeleton;

import me.davidml16.baul.pets.abilities.AbilityStats;
import me.davidml16.baul.pets.abilities.IAbility;
import me.davidml16.baul.pets.pet.Pet;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.entity.EntityDamageByEntityEvent;

public class BoneArrows implements IAbility {

    @Override
    public void onEquip(Player owner) {
        applyAbilityStats(owner);
    }

    @Override
    public void onUnequip(Player owner) {
        removeAbilityStats(owner);
    }

    @Override
    public void handleEvent(Event event, Player owner) {
        if (!(event instanceof EntityDamageByEntityEvent damageEvent)) return;
        if (!(damageEvent.getDamager() instanceof org.bukkit.entity.Arrow arrow)) return;
        if (!(arrow.getShooter() instanceof Player shooter)) return;
        if (!shooter.equals(owner)) return;

        Pet pet = playerPetManager.getActivePet(owner);
        if (pet == null) return;

        double damageBonus = getAbilityStat().getStatAmplifiers().getFirst().getStatAtLevel(pet.getLevel());
        String worldName = owner.getWorld().getName().toLowerCase();
        
        if (worldName.contains("dungeon") ||
               worldName.endsWith("_dungeon")) {
            damageBonus *= 2;
        }

        double currentDamage = damageEvent.getDamage();
        double newDamage = currentDamage * (1.0 + damageBonus / 100.0);
        damageEvent.setDamage(newDamage);
    }

    @Override
    public AbilityStats getAbilityStat() {
        return new AbilityStats().addStatAmplifier(0.75, 0.75);
    }
}
