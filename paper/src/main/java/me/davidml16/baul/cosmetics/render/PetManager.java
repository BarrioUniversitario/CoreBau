package me.davidml16.baul.cosmetics.render;

import me.davidml16.baul.Main;
import me.davidml16.baul.cosmetics.Cosmetic;
import me.davidml16.baul.cosmetics.CosmeticCategory;
import me.davidml16.baul.cosmetics.types.Pet;
import me.davidml16.baul.objects.Profile;
import me.davidml16.baul.utils.MiniMessageUtils;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Mob;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Per-player pet entity manager.
 *
 * Spawns a non-AI, invulnerable, non-collidable entity next to a player when
 * they equip a Pet cosmetic. A repeating task (every 10 ticks = 0.5s) teleports
 * the pet back to the owner if they strayed more than ~6 blocks or changed
 * worlds. Pets are non-persistent so server restart cleans them up; they're
 * respawned automatically on next login via PlayerDataHandler.
 */
public class PetManager {

    // Pet behavior depends on owner movement state:
    //   • idle (player not moving, pet within WALK_MIN)    → do nothing, pet stands quiet
    //   • normal walking / sprinting                       → pathfinder.moveTo, real walking
    //   • flying / elytra / vehicle / fast velocity        → teleport (pathfinder can't keep up)
    //   • different world / distance > TELEPORT_DISTANCE   → teleport
    private static final double WALK_MIN_SQUARED = 4.0;            // 2 blocks
    private static final double TELEPORT_DISTANCE_SQUARED = 625.0; // 25 blocks
    private static final double FAST_VELOCITY_SQUARED = 0.16;      // ~0.4 b/tick = 8 b/s
    private static final double PATHFINDER_SPEED = 1.4;

    private final Main main;
    private final Map<UUID, Entity> pets = new HashMap<>();
    /** Last player location per tick — used to detect "owner is idle". */
    private final Map<UUID, Location> lastOwnerLocation = new HashMap<>();
    private int taskId = -1;
    private static final double OWNER_IDLE_THRESHOLD_SQUARED = 0.0025; // ~0.05 blocks/tick

    public PetManager(Main main) {
        this.main = main;
    }

    public void start() {
        if (taskId != -1) return;
        // 4 Hz — fast enough that flying owners don't out-run the teleport check,
        // slow enough that pathfinder ticks complete between updates.
        taskId = Bukkit.getScheduler().scheduleSyncRepeatingTask(main, this::tick, 20L, 5L);
    }

    public void stop() {
        if (taskId != -1) {
            Bukkit.getScheduler().cancelTask(taskId);
            taskId = -1;
        }
        for (Entity e : pets.values()) {
            if (e != null && !e.isDead()) e.remove();
        }
        pets.clear();
    }

    public void spawn(Player player, Pet pet) {
        despawn(player.getUniqueId());
        if (!pet.getEntityType().isSpawnable()) {
            main.getLogger().warning("Pet entity type " + pet.getEntityType() + " is not spawnable.");
            return;
        }

        // Prefer GoldmanPets if available — it handles AI, naming, and animations
        // more naturally than our reflection-free fallback.
        GoldmanPetsAdapter gp = main.getGoldmanPetsAdapter();
        if (gp != null && gp.isAvailable() && gp.spawn(player, pet.getEntityType())) {
            // GoldmanPets owns the entity. We track UUID→null so the lifecycle
            // hooks (quit/world change) call despawn() on it, which routes to GP.
            pets.put(player.getUniqueId(), null);
            return;
        }

        try {
            Entity entity = player.getWorld().spawnEntity(petLocation(player), pet.getEntityType());
            if (entity instanceof LivingEntity) {
                LivingEntity le = (LivingEntity) entity;
                le.setCollidable(false);
                le.setSilent(true);
                le.setInvulnerable(true);
                // AI on so Paper's pathfinder + animations work.
                le.setAI(true);
                if (le instanceof Mob) {
                    Mob mob = (Mob) le;
                    mob.setRemoveWhenFarAway(false);
                    // Strip every vanilla goal (wander, pollinate, follow-mate, etc.)
                    // so the pet ONLY moves when we tell it to via pathfinder.moveTo.
                    // Without this, bees fly to flowers, parrots dance, etc.
                    try {
                        Bukkit.getMobGoals().removeAllGoals(mob);
                    } catch (Throwable t) {
                        main.getLogger().warning("Could not strip mob goals for pet " + pet.getId() + ": " + t.getMessage());
                    }
                }
            }
            entity.setPersistent(false);
            String name = pet.getCustomName();
            if (name != null && !name.isEmpty()) {
                String resolved = name.replace("%player%", player.getName());
                entity.setCustomName(MiniMessageUtils.format(resolved));
                entity.setCustomNameVisible(true);
            }
            pets.put(player.getUniqueId(), entity);

            // Hide this pet from any viewer who opted out of seeing others' cosmetics.
            for (Player viewer : Bukkit.getOnlinePlayers()) {
                if (viewer.getUniqueId().equals(player.getUniqueId())) continue;
                Profile vp = main.getPlayerDataHandler().getData(viewer);
                if (vp != null && !vp.isCosmeticsVisible()) {
                    viewer.hideEntity(main, entity);
                }
            }
        } catch (Exception e) {
            main.getLogger().warning("Failed to spawn pet " + pet.getId() + " for " + player.getName() + ": " + e.getMessage());
        }
    }

    public void despawn(UUID uuid) {
        // Always try GoldmanPets first — no-op if pet wasn't spawned through it.
        Player p = Bukkit.getPlayer(uuid);
        GoldmanPetsAdapter gp = main.getGoldmanPetsAdapter();
        if (p != null && gp != null && gp.isAvailable()) gp.despawn(p);

        Entity entity = pets.remove(uuid);
        lastOwnerLocation.remove(uuid);
        if (entity != null && !entity.isDead()) entity.remove();
    }

    public boolean hasPet(UUID uuid) {
        return pets.containsKey(uuid);
    }

    /**
     * Called when {@code viewer} toggles their cosmeticsVisible setting.
     * Shows or hides every other player's pet for them accordingly.
     */
    public void refreshVisibilityFor(Player viewer, boolean visible) {
        for (Map.Entry<UUID, Entity> entry : pets.entrySet()) {
            if (entry.getKey().equals(viewer.getUniqueId())) continue;
            Entity pet = entry.getValue();
            if (pet == null || pet.isDead()) continue;
            if (visible) viewer.showEntity(main, pet);
            else viewer.hideEntity(main, pet);
        }
    }

    private Location petLocation(Player player) {
        Location base = player.getLocation().clone();
        // Slight offset behind and to the right of the player.
        return base.add(base.getDirection().multiply(-0.8).setY(0)).add(0.5, 0.2, 0);
    }

    private void tick() {
        for (Map.Entry<UUID, Entity> entry : new HashMap<>(pets).entrySet()) {
            UUID uuid = entry.getKey();
            Entity pet = entry.getValue();
            Player player = Bukkit.getPlayer(uuid);
            if (player == null || !player.isOnline()) {
                despawn(uuid);
                continue;
            }
            // GoldmanPets-owned pet (we tracked uuid→null on spawn). It manages
            // movement and AI itself; we don't touch it.
            if (pet == null) continue;
            if (pet.isDead()) {
                // Got removed somehow; respawn from equipped state.
                Profile profile = main.getPlayerDataHandler().getData(player);
                if (profile == null) { pets.remove(uuid); continue; }
                String petId = profile.getEquipped(CosmeticCategory.PET.getId());
                if (petId == null) { pets.remove(uuid); continue; }
                Cosmetic c = main.getCosmeticRegistry().getById(petId);
                if (c instanceof Pet) spawn(player, (Pet) c);
                else pets.remove(uuid);
                continue;
            }
            // Different world: pathfinder can't cross, teleport.
            if (!pet.getWorld().equals(player.getWorld())) {
                pet.teleport(petLocation(player));
                continue;
            }

            double distSq = pet.getLocation().distanceSquared(player.getLocation());

            // Fast owner — fly, elytra, vehicle, or fast velocity — pathfinder won't
            // keep up, so teleport the pet next to the player every tick.
            if (isOwnerFast(player) || distSq > TELEPORT_DISTANCE_SQUARED) {
                pet.teleport(petLocation(player));
                continue;
            }

            // Idle detection: compare current player location to last tick's. If the
            // player didn't move meaningfully AND the pet is already close enough,
            // skip the pathfinder.moveTo call entirely so the pet stays put. Without
            // this, repeatedly setting the same destination makes the pet micro-jitter.
            Location currentOwnerLoc = player.getLocation();
            Location lastOwnerLoc = lastOwnerLocation.get(uuid);
            boolean ownerIdle = lastOwnerLoc != null
                    && lastOwnerLoc.getWorld().equals(currentOwnerLoc.getWorld())
                    && lastOwnerLoc.distanceSquared(currentOwnerLoc) < OWNER_IDLE_THRESHOLD_SQUARED;
            lastOwnerLocation.put(uuid, currentOwnerLoc.clone());

            if (pet instanceof Mob && (!ownerIdle || distSq > WALK_MIN_SQUARED)) {
                ((Mob) pet).getPathfinder().moveTo(currentOwnerLoc, PATHFINDER_SPEED);
            }
            // else: owner standing still and pet within ~2 blocks → leave it alone.
        }
    }

    private boolean isOwnerFast(Player player) {
        if (player.isFlying() || player.isGliding()) return true;
        if (player.getVehicle() != null) return true;
        Vector v = player.getVelocity();
        double xzSq = v.getX() * v.getX() + v.getZ() * v.getZ();
        return xzSq > FAST_VELOCITY_SQUARED;
    }
}
