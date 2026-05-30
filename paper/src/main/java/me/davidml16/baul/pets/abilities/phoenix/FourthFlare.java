package me.davidml16.baul.pets.abilities.phoenix;

import me.davidml16.baul.Main;
import me.davidml16.baul.pets.abilities.AbilityStats;
import me.davidml16.baul.pets.abilities.IAbility;
import me.davidml16.baul.pets.pet.Pet;
import me.davidml16.baul.pets.utils.enums.Stats;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class FourthFlare implements IAbility {

    private final Map<UUID, Integer> strikeCounter = new HashMap<>();
    private final Map<UUID, BukkitRunnable> burningEntities = new HashMap<>();

    @Override
    public void onEquip(Player owner) {
        strikeCounter.put(owner.getUniqueId(), 0);
    }

    @Override
    public void onUnequip(Player owner) {
        strikeCounter.remove(owner.getUniqueId());

        burningEntities.values().forEach(task -> {
            if (!task.isCancelled()) {
                task.cancel();
            }
        });
        burningEntities.clear();
    }

    @Override
    public void handleEvent(Event event, Player owner) {
        if (!(event instanceof EntityDamageByEntityEvent damageEvent)) return;
        if (!damageEvent.getDamager().equals(owner)) return;
        if (!(damageEvent.getEntity() instanceof LivingEntity target)) return;
        if (target instanceof Player) return; // Don't affect other players

        Pet pet = playerPetManager.getActivePet(owner);
        if (pet == null) return;

        int currentStrikes = strikeCounter.getOrDefault(owner.getUniqueId(), 0) + 1;
        strikeCounter.put(owner.getUniqueId(), currentStrikes);

        if (currentStrikes >= 4) {
            strikeCounter.put(owner.getUniqueId(), 0); // Reset counter
            igniteTarget(owner, target, pet);
        }
    }

    private void igniteTarget(Player owner, LivingEntity target, Pet pet) {
        // EcoSkills API has been removed - crit damage stat disabled
        double critDamage = 5.0; // Default critical damage value
        double damageMultiplier = 1.1 + (0.14 * pet.getLevel());
        double damagePerSecond = critDamage * damageMultiplier;

        double burnDurationSeconds = 2.0 + (0.03 * pet.getLevel());
        int burnTicks = (int) (burnDurationSeconds * 20);

        UUID targetId = target.getUniqueId();
        BukkitRunnable existingBurn = burningEntities.get(targetId);
        if (existingBurn != null && !existingBurn.isCancelled()) {
            existingBurn.cancel();
        }

        target.setFireTicks(burnTicks);

        BukkitRunnable burnTask = new BukkitRunnable() {
            int ticksRemaining = burnTicks;

            @Override
            public void run() {
                if (ticksRemaining <= 0 || target.isDead() || !target.isValid()) {
                    burningEntities.remove(targetId);
                    cancel();
                    return;
                }

                if (ticksRemaining % 20 == 0) {
                    target.damage(damagePerSecond, owner);
                }

                ticksRemaining--;
            }
        };

        burningEntities.put(targetId, burnTask);
        burnTask.runTaskTimer(Main.getInstance(), 0L, 1L);
    }

    @Override
    public AbilityStats getAbilityStat() {
        return null;
    }
}
