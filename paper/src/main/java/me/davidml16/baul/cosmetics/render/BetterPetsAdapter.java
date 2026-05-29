package me.davidml16.baul.cosmetics.render;

import me.davidml16.baul.Main;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

import java.lang.reflect.Method;

/**
 * Reflection-based bridge to the BetterPets plugin
 * (https://github.com/Psikuvit/BetterPets).
 *
 * If BetterPets is installed and enabled, cosmetic pet spawning/despawning
 * delegates to it. If it is NOT installed, {@link #isAvailable()} returns false
 * and {@link PetManager} falls back to its own entity-based implementation.
 *
 * Reflection is used so the project compiles without a hard dependency on
 * BetterPets (which may not be published to a public Maven repository).
 *
 * -----------------------------------------------------------------------
 * HOW TO VERIFY / UPDATE THE METHOD NAMES
 * -----------------------------------------------------------------------
 * Open the BetterPets source and check:
 *
 *   1. How to obtain the main plugin object (currently: getPlugin("BetterPets")).
 *   2. How to get the pet manager from the plugin object.
 *      Expected method names (try in order): getPetManager(), getPlayerPetManager().
 *   3. How to SPAWN a pet for a player.
 *      Expected signature: activatePet(Player player, String petId)
 *      or: spawnPet(Player, String), givePet(Player, String), equipPet(Player, String).
 *   4. How to DESPAWN / remove the active pet.
 *      Expected signature: deactivatePet(Player player)
 *      or: despawnPet(Player), removePet(Player), unequipPet(Player), clearPet(Player).
 *
 * Update SPAWN_METHOD_NAMES / DESPAWN_METHOD_NAMES arrays below to match.
 * -----------------------------------------------------------------------
 */
public class BetterPetsAdapter {

    /** Plugin name registered in Bukkit's plugin manager. */
    private static final String PLUGIN_NAME = "BetterPets";

    /** Method names to try (in order) when looking up the pet manager. */
    private static final String[] MANAGER_METHOD_NAMES = {
            "getPetManager", "getPlayerPetManager"
    };

    /** Spawn method signatures to try. All take (Player, String petId). */
    private static final String[] SPAWN_METHOD_NAMES = {
            "activatePet", "spawnPet", "givePet", "equipPet"
    };

    /** Despawn method signatures to try. All take (Player). */
    private static final String[] DESPAWN_METHOD_NAMES = {
            "deactivatePet", "despawnPet", "removePet", "unequipPet", "clearPet"
    };

    private final Main main;
    private Boolean available;

    private Plugin betterPetsPlugin;
    private Method getManagerMethod;
    private Method spawnMethod;
    private Method despawnMethod;

    public BetterPetsAdapter(Main main) {
        this.main = main;
    }

    /**
     * @return true if BetterPets is installed and all required API methods
     *         were bound successfully. Result is cached after first call.
     */
    public boolean isAvailable() {
        if (available != null) return available;
        try {
            betterPetsPlugin = Bukkit.getPluginManager().getPlugin(PLUGIN_NAME);
            if (betterPetsPlugin == null || !betterPetsPlugin.isEnabled()) {
                available = false;
                return false;
            }

            // Locate the manager getter
            getManagerMethod = resolveMethod(betterPetsPlugin.getClass(), MANAGER_METHOD_NAMES);
            if (getManagerMethod == null) {
                warn("no se encontró el método getManager en " + betterPetsPlugin.getClass().getName()
                        + ". Revisa MANAGER_METHOD_NAMES en BetterPetsAdapter.");
                available = false;
                return false;
            }

            Object manager = getManagerMethod.invoke(betterPetsPlugin);
            if (manager == null) {
                warn("getManager devolvió null. Revisa que BetterPets esté completamente cargado.");
                available = false;
                return false;
            }

            // Locate spawn method: (Player, String)
            spawnMethod = resolveMethod(manager.getClass(), SPAWN_METHOD_NAMES,
                    Player.class, String.class);
            if (spawnMethod == null) {
                warn("no se encontró un método de spawn (Player, String) en "
                        + manager.getClass().getName() + ". Revisa SPAWN_METHOD_NAMES en BetterPetsAdapter.");
                available = false;
                return false;
            }

            // Locate despawn method: (Player)
            despawnMethod = resolveMethod(manager.getClass(), DESPAWN_METHOD_NAMES, Player.class);
            if (despawnMethod == null) {
                warn("no se encontró un método de despawn (Player) en "
                        + manager.getClass().getName() + ". Revisa DESPAWN_METHOD_NAMES en BetterPetsAdapter.");
                available = false;
                return false;
            }

            available = true;
            main.getLogger().info("[Baul] BetterPets detectado — las mascotas cosméticas usarán BetterPets.");
        } catch (Throwable t) {
            warn("fallo al enlazar la API de BetterPets (" + t.getClass().getSimpleName()
                    + ": " + t.getMessage() + "). Usando mascotas integradas.");
            available = false;
        }
        return available;
    }

    /**
     * Spawns (activates) a BetterPets pet for the given player.
     *
     * @param player target player
     * @param petId  the pet identifier as defined in BetterPets' YAML configuration
     * @return true if spawned successfully; false if BetterPets is unavailable or an error occurred
     */
    public boolean spawn(Player player, String petId) {
        if (!isAvailable()) return false;
        try {
            Object manager = getManagerMethod.invoke(betterPetsPlugin);
            spawnMethod.invoke(manager, player, petId);
            return true;
        } catch (Throwable t) {
            main.getLogger().warning("[Baul] BetterPets spawn falló para " + player.getName()
                    + " (petId=" + petId + "): " + t.getMessage());
            return false;
        }
    }

    /**
     * Despawns (deactivates) the active BetterPets pet for the given player.
     * Safe to call even if no pet is active.
     *
     * @return true if called successfully; false if BetterPets is unavailable or an error occurred
     */
    public boolean despawn(Player player) {
        if (!isAvailable()) return false;
        try {
            Object manager = getManagerMethod.invoke(betterPetsPlugin);
            despawnMethod.invoke(manager, player);
            return true;
        } catch (Throwable t) {
            // Quiet: typically means no pet was active.
            return false;
        }
    }

    // -----------------------------------------------------------------------
    //  Internal helpers
    // -----------------------------------------------------------------------

    /** Tries each candidate name and returns the first method that matches. */
    private static Method resolveMethod(Class<?> clazz, String[] names, Class<?>... paramTypes) {
        for (String name : names) {
            try {
                Method m = clazz.getMethod(name, paramTypes);
                return m;
            } catch (NoSuchMethodException ignored) {
            }
        }
        return null;
    }

    private void warn(String msg) {
        main.getLogger().warning("[Baul] BetterPetsAdapter: " + msg);
    }
}
