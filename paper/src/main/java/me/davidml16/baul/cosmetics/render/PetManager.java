package me.davidml16.baul.cosmetics.render;

import me.davidml16.baul.Main;
import me.davidml16.baul.cosmetics.types.Pet;
import me.davidml16.baul.pets.PlayerPetManager;
import me.davidml16.baul.pets.api.PetAPI;
import me.davidml16.baul.pets.data.PlayerData;
import me.davidml16.baul.pets.utils.enums.Rarity;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

/**
 * Gestor de mascotas cosméticas. El renderizado delega 100% en el subsistema
 * embebido {@code me.davidml16.baul.pets}: cada entrada de {@code pets.yml}
 * debe declarar {@code betterPets: <baseId>_<rareza>} apuntando a una
 * plantilla del registro.
 *
 * Esta clase actúa como puente entre el sistema de cosméticos de Baul (equip /
 * unequip, marketplace, crafting) y la API del subsistema de pets (activación,
 * habilidades, niveles, persistencia).
 */
public class PetManager {

    private final Main main;
    /** UUIDs de jugadores que actualmente tienen un pet activo via el subsistema. */
    private final Set<UUID> active = new HashSet<>();

    public PetManager(Main main) {
        this.main = main;
    }

    public void start() {
        if (isAvailable()) {
            main.getLogger().info("[Baul] Mascotas cosméticas: subsistema de pets embebido activo.");
        } else {
            main.getLogger().warning("[Baul] Subsistema de pets no inicializado — las mascotas cosméticas no se mostrarán.");
        }
    }

    public void stop() {
        for (UUID uuid : new HashSet<>(active)) {
            despawn(uuid);
        }
        active.clear();
    }

    public void spawn(Player player, Pet cosmetic) {
        despawn(player.getUniqueId());
        if (cosmetic == null || !isAvailable()) return;

        String templateId = cosmetic.getBetterPetsId();
        if (templateId == null || templateId.isBlank()) {
            main.getLogger().warning("[Baul] Mascota cosmética '" + cosmetic.getId()
                    + "' sin id de plantilla (campo `betterPets`).");
            return;
        }

        if (activateTemplate(player, templateId)) {
            active.add(player.getUniqueId());
        }
    }

    public void despawn(UUID uuid) {
        Player player = Bukkit.getPlayer(uuid);
        if (player != null && isAvailable()) {
            try {
                main.getPlayerPetManager().deactivatePet(player);
            } catch (Throwable t) {
                main.getLogger().warning("[Baul] deactivatePet falló para " + player.getName() + ": " + t.getMessage());
            }
        }
        active.remove(uuid);
    }

    public boolean hasPet(UUID uuid) {
        return active.contains(uuid);
    }

    /**
     * No-op: la visibilidad por espectador la gestiona el propio subsistema a
     * través de {@link PlayerData#applyVisibility()} y su {@code VisibilityType}.
     */
    public void refreshVisibilityFor(Player viewer, boolean visible) {
        // gestionado por PlayerData del subsistema
    }

    private boolean activateTemplate(Player player, String betterPetsId) {
        try {
            String fullId = betterPetsId.toLowerCase().trim();
            PlayerPetManager ppm = main.getPlayerPetManager();

            // Asegura PlayerData (lo crea PlayerListener al join, pero por si el
            // cosmético se activa antes de que termine la carga).
            if (ppm.getPlayerData(player) == null) {
                ppm.setPlayerData(player, new PlayerData(player));
            }

            me.davidml16.baul.pets.pet.Pet pet = findOwnedPet(player, fullId);
            if (pet == null) {
                Parsed parsed = parseId(fullId);
                Optional<me.davidml16.baul.pets.pet.Pet> template;
                try {
                    template = PetAPI.getPetTemplate(parsed.baseId, Rarity.valueOf(parsed.rarity));
                } catch (IllegalArgumentException ex) {
                    warn("rareza inválida en id: " + fullId);
                    return false;
                }
                if (template.isEmpty()) {
                    warn("plantilla no encontrada: " + fullId);
                    return false;
                }
                pet = template.get().clone();
            } else {
                pet = pet.clone();
            }

            return ppm.activatePet(player, pet);
        } catch (Throwable t) {
            main.getLogger().warning("[Baul] Pets spawn falló para " + player.getName()
                    + " (" + betterPetsId + "): " + t.getMessage());
            return false;
        }
    }

    private me.davidml16.baul.pets.pet.Pet findOwnedPet(Player player, String fullId) {
        Set<me.davidml16.baul.pets.pet.Pet> owned = PetAPI.getPlayerPets(player);
        for (me.davidml16.baul.pets.pet.Pet pet : owned) {
            if (fullId.equals(pet.getId().toLowerCase())) return pet;
        }
        return null;
    }

    private boolean isAvailable() {
        return PetAPI.isInitialized() && main.getPlayerPetManager() != null;
    }

    private static Parsed parseId(String fullId) {
        for (String rarityName : new String[]{
                "MYTHIC", "LEGENDARY", "EPIC", "RARE", "UNCOMMON", "COMMON"
        }) {
            String suffix = "_" + rarityName.toLowerCase();
            if (fullId.endsWith(suffix)) {
                return new Parsed(fullId.substring(0, fullId.length() - suffix.length()), rarityName);
            }
        }
        return new Parsed(fullId, "COMMON");
    }

    private void warn(String msg) {
        main.getLogger().warning("[Baul] Pets cosmético: " + msg);
    }

    private record Parsed(String baseId, String rarity) {}
}
