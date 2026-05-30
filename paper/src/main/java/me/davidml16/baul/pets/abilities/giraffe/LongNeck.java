package me.davidml16.baul.pets.abilities.giraffe;

import me.davidml16.baul.pets.abilities.AbilityStats;
import me.davidml16.baul.pets.abilities.IAbility;
import me.davidml16.baul.pets.pet.Pet;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.entity.EntityDamageByEntityEvent;

public class LongNeck implements IAbility {

    private final double baseDamageBonus = 50.5; // 50.5% base damage bonus
    private final double increasePerLevel = 0.5; // +0.5% per level

    @Override
    public void handleEvent(Event event, Player owner) {
        if (!(event instanceof EntityDamageByEntityEvent damageEvent)) return;
        if (!(damageEvent.getDamager() instanceof Player player)) return;
        if (!player.equals(owner)) return; // Ensure it's the pet owner

        Pet pet = playerPetManager.getActivePet(player);
        if (pet == null) return;

        double distance = player.getLocation().distance(damageEvent.getEntity().getLocation());
        if (distance > 3.0) {
            double damageBonus = baseDamageBonus + (increasePerLevel * (pet.getLevel() - 1));
            double currentDamage = damageEvent.getDamage();
            double newDamage = currentDamage * (1.0 + damageBonus / 100.0);
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
        return null;
    }
}
