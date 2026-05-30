package me.davidml16.baul.pets.abilities.lion;

import me.davidml16.baul.pets.abilities.AbilityStats;
import me.davidml16.baul.pets.abilities.IAbility;
import me.davidml16.baul.pets.pet.Pet;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.entity.EntityDamageByEntityEvent;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;


public class KingOfTheJungle implements IAbility {

    private final Map<UUID, Set<UUID>> attackedByMobs = new HashMap<>();

    @Override
    public void onEquip(Player owner) {
        attackedByMobs.putIfAbsent(owner.getUniqueId(), new HashSet<>());
    }

    @Override
    public void onUnequip(Player owner) {
        attackedByMobs.remove(owner.getUniqueId());
    }

    @Override
    public void handleEvent(Event event, Player owner) {
        if (!(event instanceof EntityDamageByEntityEvent damageEvent)) return;

        if (damageEvent.getEntity().equals(owner) &&
            damageEvent.getDamager() instanceof LivingEntity mob &&
            !(damageEvent.getDamager() instanceof Player)) {

            Set<UUID> mobsAttackedPlayer = attackedByMobs.get(owner.getUniqueId());
            if (mobsAttackedPlayer != null) {
                mobsAttackedPlayer.add(mob.getUniqueId());
            }
        }

        if (damageEvent.getDamager().equals(owner) &&
            damageEvent.getEntity() instanceof LivingEntity target &&
            !(damageEvent.getEntity() instanceof Player)) {

            Set<UUID> mobsAttackedPlayer = attackedByMobs.get(owner.getUniqueId());
            if (mobsAttackedPlayer != null && mobsAttackedPlayer.contains(target.getUniqueId())) {
                Pet pet = playerPetManager.getActivePet(owner);
                if (pet != null) {
                    double damageBonus = 1.5 * pet.getLevel();
                    double currentDamage = damageEvent.getDamage();
                    double newDamage = currentDamage * (1.0 + damageBonus / 100.0);
                    damageEvent.setDamage(newDamage);
                }
            }
        }
    }

    @Override
    public AbilityStats getAbilityStat() {
        return null;
    }
}
