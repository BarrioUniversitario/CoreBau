package me.davidml16.baul.cosmetics.render;

import me.davidml16.baul.Main;
import org.bukkit.Bukkit;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

import java.lang.reflect.Method;

/**
 * Reflection-based bridge to the GoldmanPets plugin
 * (https://github.com/LeeTheTech/GoldmanPets).
 *
 * If the GoldmanPets plugin is installed and enabled on the server, our pet
 * cosmetics delegate to it: {@code GoldmanPets.getPetManager().spawn(...)}
 * gives us nicer vanilla-feeling pets (their own controllers, naming, etc.)
 * without us reimplementing follow / AI logic.
 *
 * If GoldmanPets is NOT installed, {@link #isAvailable()} returns false and
 * {@link PetManager} falls back to its own entity-based implementation.
 *
 * Reflection is used because GoldmanPets is not published to a Maven repo
 * with a stable API surface. Method handles are resolved lazily on the first
 * call and cached.
 */
public class GoldmanPetsAdapter {

    private final Main main;
    private Boolean available;
    private Plugin goldmanPetsPlugin;
    private Method getPetManagerMethod;
    private Method spawnMethod;
    private Method removeActivePetMethod;

    public GoldmanPetsAdapter(Main main) {
        this.main = main;
    }

    /**
     * @return true if GoldmanPets is installed and the API methods bound successfully.
     *         Cached after the first call. Logs the detection result once.
     */
    public boolean isAvailable() {
        if (available != null) return available;
        try {
            goldmanPetsPlugin = Bukkit.getPluginManager().getPlugin("GoldmanPets");
            if (goldmanPetsPlugin == null || !goldmanPetsPlugin.isEnabled()) {
                available = false;
                return false;
            }
            getPetManagerMethod = goldmanPetsPlugin.getClass().getMethod("getPetManager");
            Object petManager = getPetManagerMethod.invoke(goldmanPetsPlugin);
            // PetManager.spawn(Player, Location, int id, EntityType, String[] data)
            spawnMethod = petManager.getClass().getMethod("spawn",
                    Player.class,
                    org.bukkit.Location.class,
                    int.class,
                    EntityType.class,
                    String[].class);
            // PetManager.removeActivePet(Player)
            removeActivePetMethod = petManager.getClass().getMethod("removeActivePet", Player.class);
            available = true;
            main.getLogger().info("GoldmanPets detected — pet cosmetics will delegate to it.");
        } catch (Throwable t) {
            main.getLogger().warning("Could not bind GoldmanPets API (" + t.getClass().getSimpleName() + ": "
                    + t.getMessage() + "). Falling back to built-in pet logic.");
            available = false;
        }
        return available;
    }

    /**
     * Attempts to spawn a pet through GoldmanPets.
     *
     * @return true on success (GoldmanPets now owns the entity), false on
     *         failure or if GoldmanPets isn't available.
     */
    public boolean spawn(Player player, EntityType type) {
        if (!isAvailable()) return false;
        try {
            Object petManager = getPetManagerMethod.invoke(goldmanPetsPlugin);
            // id=1 (per-player single cosmetic pet), data=empty (no custom name/color from us;
            // pet name comes from our Pet config and is set via setCustomName after spawn).
            spawnMethod.invoke(petManager, player, player.getLocation(), 1, type, new String[0]);
            return true;
        } catch (Throwable t) {
            main.getLogger().warning("GoldmanPets spawn failed for " + player.getName()
                    + ": " + t.getMessage() + ". Will fall back to vanilla pet.");
            return false;
        }
    }

    /**
     * Despawns the player's active GoldmanPet, if any. Safe to call even if
     * no pet was spawned through GoldmanPets — it just no-ops.
     */
    public boolean despawn(Player player) {
        if (!isAvailable()) return false;
        try {
            Object petManager = getPetManagerMethod.invoke(goldmanPetsPlugin);
            removeActivePetMethod.invoke(petManager, player);
            return true;
        } catch (Throwable t) {
            // Quiet: probably just no active pet.
            return false;
        }
    }
}
