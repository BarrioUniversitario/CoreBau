package me.davidml16.baul.pets.abilities.enderman;

import me.davidml16.baul.pets.abilities.AbilityStats;
import me.davidml16.baul.pets.abilities.IAbility;
import me.davidml16.baul.pets.pet.Pet;
import org.bukkit.World;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.entity.EntityDamageByEntityEvent;

public class Enderian implements IAbility {

    @Override
    public void onEquip(Player paramPlayer) {
    }

    @Override
    public void onUnequip(Player paramPlayer) {
    }

    @Override
    public void handleEvent(Event event, Player owner) {
        if (!(event instanceof EntityDamageByEntityEvent entityEvent)) return;
        if (!(entityEvent.getEntity() instanceof Player player)) return;
        if (!player.equals(owner)) return;
        if (!(entityEvent.getDamager() instanceof LivingEntity damager)) return;

        if (damager.getWorld().getEnvironment() == World.Environment.THE_END) {
            Pet pet = playerPetManager.getActivePet(owner);
            if (pet == null) return;

            double damageReduction = (0.3 + (0.3 * (pet.getLevel() - 1))) / 100.0;
            double originalDamage = entityEvent.getFinalDamage();
            double newDamage = originalDamage * (1 - damageReduction);

            entityEvent.setDamage(newDamage);
        }
    }

    @Override
    public AbilityStats getAbilityStat() {
        return null;
    }
}
