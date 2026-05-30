package me.davidml16.baul.pets.abilities.guardian;

import me.davidml16.baul.Main;
import me.davidml16.baul.pets.abilities.AbilityStats;
import me.davidml16.baul.pets.abilities.IAbility;
import me.davidml16.baul.pets.pet.Pet;
import me.davidml16.baul.pets.utils.enums.Stats;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

import java.util.HashMap;
import java.util.Map;

public class LaserBeam implements IAbility {

    private final Map<Player, BukkitTask> activeTasks = new HashMap<>();
    private final double damageMultiplierIncrease = 1.2;

    @Override
    public void onEquip(Player owner) {
        startLaserBeamTask(owner);
    }

    @Override
    public void onUnequip(Player owner) {
        stopLaserBeamTask(owner);
    }

    private void startLaserBeamTask(Player owner) {
        stopLaserBeamTask(owner); // Ensure no duplicate tasks

        BukkitTask task = new BukkitRunnable() {
            @Override
            public void run() {
                if (!owner.isOnline()) {
                    cancel();
                    activeTasks.remove(owner);
                    return;
                }

                Pet pet = playerPetManager.getActivePet(owner);
                if (pet == null) {
                    cancel();
                    activeTasks.remove(owner);
                    return;
                }

                zapNearbyEnemies(owner, pet);
            }
        }.runTaskTimer(Main.getInstance(), 60L, 60L); // 3 seconds = 60 ticks

        activeTasks.put(owner, task);
    }

    private void stopLaserBeamTask(Player owner) {
        BukkitTask task = activeTasks.remove(owner);
        if (task != null && !task.isCancelled()) {
            task.cancel();
        }
    }

    private void zapNearbyEnemies(Player owner, Pet pet) {
        double intelligence = 5.0; // Default value (EcoSkills integration removed)
        double damageMultiplier = 1.2 + (damageMultiplierIncrease * (pet.getLevel() - 1));
        double zapDamage = intelligence * damageMultiplier;

        // Find nearby hostile entities within reasonable range
        owner.getNearbyEntities(8, 8, 8).stream()
                .filter(entity -> entity instanceof LivingEntity)
                .filter(entity -> !(entity instanceof Player))
                .map(entity -> (LivingEntity) entity)
                .filter(entity -> !entity.isDead())
                .forEach(entity -> entity.damage(zapDamage, owner)
                );
    }

    @Override
    public AbilityStats getAbilityStat() {
        return null; // This ability doesn't provide stat bonuses, it's an active effect
    }
}
