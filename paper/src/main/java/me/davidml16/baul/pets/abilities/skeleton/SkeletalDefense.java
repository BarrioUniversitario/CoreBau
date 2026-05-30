package me.davidml16.baul.pets.abilities.skeleton;

import me.davidml16.baul.Main;
import me.davidml16.baul.pets.abilities.AbilityStats;
import me.davidml16.baul.pets.abilities.IAbility;
import me.davidml16.baul.pets.pet.Pet;
import me.davidml16.baul.pets.utils.enums.Stats;
import org.bukkit.entity.Arrow;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;
import org.bukkit.util.Vector;

import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class SkeletalDefense implements IAbility {

    private final Map<UUID, BukkitTask> defenseTasks = new HashMap<>();
    private final Map<UUID, Long> lastShotTime = new HashMap<>();
    private static final long COOLDOWN_MS = 5000;
    private static final double DETECTION_RANGE = 5.0;
    private static final double CRIT_DAMAGE_MULTIPLIER = 30.0;

    @Override
    public void onEquip(Player owner) {
        lastShotTime.put(owner.getUniqueId(), 0L);
        startDefenseTask(owner);
    }

    @Override
    public void onUnequip(Player owner) {
        // Cancel defense task
        BukkitTask task = defenseTasks.remove(owner.getUniqueId());
        if (task != null && !task.isCancelled()) {
            task.cancel();
        }

        lastShotTime.remove(owner.getUniqueId());
    }

    private void startDefenseTask(Player owner) {
        BukkitTask task = new BukkitRunnable() {
            @Override
            public void run() {
                if (!owner.isOnline()) {
                    cancel();
                    defenseTasks.remove(owner.getUniqueId());
                    lastShotTime.remove(owner.getUniqueId());
                    return;
                }

                Pet pet = playerPetManager.getActivePet(owner);
                if (pet == null) {
                    cancel();
                    defenseTasks.remove(owner.getUniqueId());
                    lastShotTime.remove(owner.getUniqueId());
                    return;
                }

                checkForNearbyMobs(owner, pet);
            }
        }.runTaskTimer(Main.getInstance(), 0L, 10L);

        defenseTasks.put(owner.getUniqueId(), task);
    }

    private void checkForNearbyMobs(Player owner, Pet pet) {
        long currentTime = System.currentTimeMillis();
        Long lastShot = lastShotTime.get(owner.getUniqueId());
        if (lastShot != null && (currentTime - lastShot) < COOLDOWN_MS) {
            return;
        }

        LivingEntity nearestMob = owner.getNearbyEntities(DETECTION_RANGE, DETECTION_RANGE, DETECTION_RANGE).stream()
                .filter(entity -> entity instanceof LivingEntity)
                .filter(entity -> !(entity instanceof Player))
                .map(entity -> (LivingEntity) entity)
                .filter(mob -> !mob.isDead())
                .filter(this::isHostileMob)
                .min(Comparator.comparingDouble(mob ->
                        owner.getLocation().distanceSquared(mob.getLocation())))
                .orElse(null);

        if (nearestMob != null) {
            shootDefensiveArrow(owner, nearestMob);
            lastShotTime.put(owner.getUniqueId(), currentTime);
        }
    }

    private boolean isHostileMob(LivingEntity entity) {
        return switch (entity.getType()) {
            case ZOMBIE, SKELETON, SPIDER, CREEPER, ENDERMAN, WITCH, ZOMBIE_VILLAGER, HUSK, STRAY, DROWNED, PHANTOM,
                 PILLAGER, VINDICATOR, EVOKER, RAVAGER, VEX, BLAZE, GHAST, MAGMA_CUBE, SLIME, WITHER_SKELETON, PIGLIN,
                 PIGLIN_BRUTE, HOGLIN, ZOGLIN -> true;
            case WOLF -> !((org.bukkit.entity.Wolf) entity).isTamed();
            case IRON_GOLEM -> ((org.bukkit.entity.IronGolem) entity).getTarget() != null;
            default -> false;
        };
    }

    private void shootDefensiveArrow(Player owner, LivingEntity target) {
        double critDamage = 5.0; // Default value (EcoSkills integration removed)
        double arrowDamage = critDamage * CRIT_DAMAGE_MULTIPLIER;

        Vector direction = target.getLocation().add(0, 1, 0).subtract(owner.getLocation().add(0, 1.5, 0)).toVector().normalize();

        Arrow arrow = owner.getWorld().spawnArrow(
                owner.getLocation().add(0, 1.5, 0),
                direction,
                3.0f,
                0.0f
        );

        arrow.setShooter(owner);
        arrow.setDamage(arrowDamage);
        arrow.setPierceLevel(1);
        arrow.setCritical(true);

        owner.getWorld().playSound(owner.getLocation(), org.bukkit.Sound.ENTITY_SKELETON_SHOOT, 1.0f, 1.0f);
    }

    @Override
    public AbilityStats getAbilityStat() {
        return null;
    }
}
