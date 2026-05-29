package me.davidml16.baul.cosmetics;

import me.davidml16.baul.Main;
import me.davidml16.baul.cosmetics.types.Emote;
import me.davidml16.baul.cosmetics.types.Hat;
import me.davidml16.baul.cosmetics.types.JoinEffect;
import me.davidml16.baul.cosmetics.types.Pet;
import me.davidml16.baul.cosmetics.types.Trail;
import me.davidml16.baul.objects.Profile;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Lets players try a cosmetic for a fixed window (default 30s) without
 * granting ownership or writing equipped state to the database.
 *
 * Strategy: snapshot the player's in-memory equipped map, mutate it locally
 * (so PlayerTrailTask picks up the preview trail next tick), call the side-effect
 * appliers directly for HAT/PET, fire one-shots for JOIN_EFFECT/EMOTE. After
 * the window elapses, restore the snapshot and re-apply the prior hat/pet.
 *
 * No DB writes, no SyncManager broadcasts.
 */
public class PreviewManager {

    public static final long DEFAULT_PREVIEW_TICKS = 600L; // 30 seconds

    private final Main main;
    private final Map<UUID, Preview> active = new HashMap<>();

    public PreviewManager(Main main) {
        this.main = main;
    }

    public boolean isPreviewing(UUID uuid) {
        return active.containsKey(uuid);
    }

    public void preview(Player player, Cosmetic cosmetic) {
        preview(player, cosmetic, DEFAULT_PREVIEW_TICKS);
    }

    public void preview(Player player, Cosmetic cosmetic, long ticks) {
        UUID uuid = player.getUniqueId();
        // If already previewing, revert first so we don't stack snapshots.
        if (active.containsKey(uuid)) revert(uuid);

        Profile profile = main.getPlayerDataHandler().getData(player);
        if (profile == null) return;

        Map<String, String> snapshot = new HashMap<>(profile.getEquippedCosmetics());

        // Apply the preview effect by type.
        if (cosmetic instanceof JoinEffect) {
            ((JoinEffect) cosmetic).play(player);
            return; // one-shot, no revert needed, no snapshot to track
        }
        if (cosmetic instanceof Emote) {
            ((Emote) cosmetic).trigger(player);
            return;
        }

        String catId = cosmetic.getCategory().getId();
        profile.getEquippedCosmetics().put(catId, cosmetic.getId()); // in-memory only

        if (cosmetic instanceof Hat && main.getHatApplier() != null) {
            main.getHatApplier().restore(player); // clear any current cosmetic hat first
            main.getHatApplier().apply(player, (Hat) cosmetic);
        }
        if (cosmetic instanceof Pet && main.getPetManager() != null) {
            main.getPetManager().spawn(player, (Pet) cosmetic);
        }
        // Trail rendering picks up automatically from equippedCosmetics next tick.

        int taskId = Bukkit.getScheduler().runTaskLater(main, () -> revert(uuid), ticks).getTaskId();
        active.put(uuid, new Preview(snapshot, taskId));
    }

    public void revert(UUID uuid) {
        Preview p = active.remove(uuid);
        if (p == null) return;
        Bukkit.getScheduler().cancelTask(p.taskId);

        Player player = Bukkit.getPlayer(uuid);
        if (player == null || !player.isOnline()) return;
        Profile profile = main.getPlayerDataHandler().getData(player);
        if (profile == null) return;

        // Restore the equipped map exactly as it was.
        profile.setEquippedCosmetics(new HashMap<>(p.snapshot));

        // HAT: restore real helmet, then re-apply the original hat if one was equipped.
        if (main.getHatApplier() != null) {
            main.getHatApplier().restore(player);
            String prevHat = p.snapshot.get(CosmeticCategory.HAT.getId());
            if (prevHat != null) {
                Cosmetic c = main.getCosmeticRegistry().getById(prevHat);
                if (c instanceof Hat) main.getHatApplier().apply(player, (Hat) c);
            }
        }

        // PET: despawn the preview pet, respawn the original if one was equipped.
        if (main.getPetManager() != null) {
            main.getPetManager().despawn(uuid);
            String prevPet = p.snapshot.get(CosmeticCategory.PET.getId());
            if (prevPet != null) {
                Cosmetic c = main.getCosmeticRegistry().getById(prevPet);
                if (c instanceof Pet) main.getPetManager().spawn(player, (Pet) c);
            }
        }

        // Trail auto-clears next tick when equippedCosmetics no longer has it.
        // Mark trail var as referenced for future expansion (TPS gating etc.)
        Trail.class.hashCode();
    }

    public void forgetPlayer(UUID uuid) {
        Preview p = active.remove(uuid);
        if (p != null) Bukkit.getScheduler().cancelTask(p.taskId);
    }

    private static final class Preview {
        final Map<String, String> snapshot;
        final int taskId;
        Preview(Map<String, String> snapshot, int taskId) {
            this.snapshot = snapshot;
            this.taskId = taskId;
        }
    }
}
