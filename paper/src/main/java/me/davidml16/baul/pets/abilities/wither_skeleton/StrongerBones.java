package me.davidml16.baul.pets.abilities.wither_skeleton;

import me.davidml16.baul.pets.abilities.AbilityStats;
import me.davidml16.baul.pets.abilities.IAbility;
import me.davidml16.baul.pets.pet.Pet;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.entity.EntityDamageByEntityEvent;

public class StrongerBones implements IAbility {

    @Override
    public void handleEvent(Event event, Player owner) {
        if (!(event instanceof EntityDamageByEntityEvent damageEvent)) return;
        if (!(damageEvent.getEntity() instanceof Player player)) return;

        Pet pet = playerPetManager.getActivePet(player);
        if (pet == null) return;

        if (damageEvent.getDamager().getType() == EntityType.SKELETON ||
                damageEvent.getDamager().getType() == EntityType.WITHER_SKELETON ||
                damageEvent.getDamager().getType() == EntityType.STRAY) {

            double damageReduction = getAbilityStat().getStatAmplifiers().getFirst().getStatAtLevel(pet.getLevel());
            double currentDamage = damageEvent.getDamage();
            double newDamage = currentDamage * (1.0 - damageReduction / 100.0);
            damageEvent.setDamage(newDamage);
        }
    }

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
        return new AbilityStats().addStatAmplifier(0.3, 0.3);
    }
}
