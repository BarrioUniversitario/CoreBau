package me.davidml16.baul.pets;

import me.davidml16.baul.Main;
import me.davidml16.baul.pets.autopet.AutoPetRule;
import me.davidml16.baul.pets.autopet.TriggerType;
import me.davidml16.baul.pets.data.PlayerData;
import me.davidml16.baul.pets.database.DatabaseManager;
import me.davidml16.baul.pets.menu.PetGUI;
import me.davidml16.baul.pets.pet.Pet;
import me.davidml16.baul.pets.utils.PetUtils;
import me.davidml16.baul.pets.utils.enums.Rarity;
import me.davidml16.baul.pets.utils.enums.SortType;
import me.davidml16.baul.pets.utils.enums.VisibilityType;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

public class PlayerPetManager {

    private final Map<UUID, PlayerData> playerDataMap;
    private final DatabaseManager databaseManager;

    public PlayerPetManager(Main plugin) {
        this.playerDataMap = new HashMap<>();
        this.databaseManager = plugin.getDatabaseManager();
    }

    public void setPlayerData(Player player, PlayerData data) {
        playerDataMap.put(player.getUniqueId(), data);
    }

    public PlayerData getPlayerData(Player player) {
        return playerDataMap.get(player.getUniqueId());
    }

    public void removePlayerData(Player player) {
        playerDataMap.remove(player.getUniqueId());
    }

    public void addPetToPlayer(Player player, Pet pet) {
        PlayerData data = getPlayerData(player);
        if (data != null) {
            data.addPet(pet);
        }
    }

    public void removePetFromPlayer(Player player, Pet pet) {
        PlayerData data = getPlayerData(player);
        if (data != null) {
            data.removePet(pet);
        }
    }

    public boolean activatePet(Player player, Pet pet) {
        PlayerData data = getPlayerData(player);
        if (data == null) {
            return false;
        }

        pet.setOwner(player);
        data.activatePet(pet);
        pet.spawnHead(player.getLocation(), player);

        for (PlayerData otherData : playerDataMap.values()) {
            otherData.applyVisibility();
        }
        data.setOfflinePetId(null);
        return true;
    }

    public void deactivatePet(Player player) {
        PlayerData data = getPlayerData(player);
        if (data != null) {
            data.deactivatePet();
        }
    }

    public boolean playerOwnsPet(Player player, Pet pet) {
        PlayerData data = getPlayerData(player);
        return data != null && data.hasPet(pet);
    }

    public Set<Pet> getPlayerPets(Player player) {
        PlayerData data = getPlayerData(player);
        return data != null ? data.getOwnedPets() : Set.of();
    }

    public Pet getActivePet(Player player) {
        PlayerData data = getPlayerData(player);
        return data != null ? data.getActivePet() : null;
    }

    public boolean isActivePet(Player player, Pet pet) {
        PlayerData data = getPlayerData(player);
        return data != null && data.isActivePet(pet);
    }

    public Pet givePet(Player player, String petId, Rarity rarity) {
        Pet template = Main.getInstance().getRegistryPetManager().getPet(petId, rarity);
        if (template != null) {
            Pet clonedPet = template.clone();
            ItemStack itemStack = PetUtils.petToItem(clonedPet);
            player.getInventory().addItem(itemStack);
            return clonedPet;
        }
        return null;
    }

    public Pet getPetByUUID(Player player, UUID petUUID) {
        PlayerData data = getPlayerData(player);
        return data != null ? data.getPetByUUID(petUUID) : null;
    }

    // Shared Pets Methods
    public Set<Pet> getPlayerSharedPets(Player player) {
        PlayerData data = getPlayerData(player);
        return data != null ? data.getSharedPets() : Set.of();
    }

    public void addSharedPetToPlayer(Player player, Pet pet) {
        PlayerData data = getPlayerData(player);
        if (data != null) {
            data.addSharedPet(pet);
        }
    }

    public void removeSharedPetFromPlayer(Player player, Pet pet) {
        PlayerData data = getPlayerData(player);
        if (data != null) {
            data.removeSharedPet(pet);
        }
    }

    public boolean isSharedPet(Player player, Pet pet) {
        PlayerData data = getPlayerData(player);
        return data != null && data.isSharedPet(pet);
    }

    // AutoPet Rules Methods
    public Set<AutoPetRule> getPlayerAutoPetRules(Player player) {
        PlayerData data = getPlayerData(player);
        return data != null ? data.getAutoPetRules() : Set.of();
    }

    public void addAutoPetRule(Player player, AutoPetRule rule) {
        PlayerData data = getPlayerData(player);
        if (data != null) {
            data.addAutoPetRule(rule);
        }
    }

    public void removeAutoPetRule(Player player, AutoPetRule rule) {
        PlayerData data = getPlayerData(player);
        if (data != null) {
            data.removeAutoPetRule(rule);
        }
    }

    public void removeAutoPetRule(Player player, UUID ruleId) {
        PlayerData data = getPlayerData(player);
        if (data != null) {
            data.removeAutoPetRule(ruleId);
        }
    }

    public AutoPetRule getAutoPetRule(Player player, TriggerType triggerType) {
        PlayerData data = getPlayerData(player);
        return data != null ? data.getAutoPetRule(triggerType) : null;
    }

    public boolean hasAutoPetRule(Player player, TriggerType triggerType) {
        PlayerData data = getPlayerData(player);
        return data != null && data.hasAutoPetRule(triggerType);
    }

    // Upgrading Pet Methods
    public Pet getUpgradingPet(Player player) {
        PlayerData data = getPlayerData(player);
        return data != null ? data.getUpgradingPet() : null;
    }

    public void setUpgradingPet(Player player, Pet pet) {
        PlayerData data = getPlayerData(player);
        if (data != null) {
            data.setUpgradingPet(pet);
        }
    }

    public PetGUI getPetsInventory(Player player) {
        PlayerData data = getPlayerData(player);
        return data != null ? data.getPetsInventory() : null;
    }

    public void openInventory(Player player) {
        PlayerData data = getPlayerData(player);
        if (data != null) {
            data.openInventory();
        }
    }

    // Sort and Visibility Methods
    public SortType getSortType(Player player) {
        PlayerData data = getPlayerData(player);
        return data != null ? data.getSortType() : SortType.DEFAULT;
    }

    public void setSortType(Player player, SortType sortType) {
        PlayerData data = getPlayerData(player);
        if (data != null) {
            data.setSortType(sortType);
        }
    }

    public VisibilityType getVisibilityType(Player player) {
        PlayerData data = getPlayerData(player);
        return data != null ? data.getVisibilityType() : VisibilityType.ALL;
    }

    public void setVisibilityType(Player player, VisibilityType visibilityType) {
        PlayerData data = getPlayerData(player);
        if (data != null) {
            data.setVisibilityType(visibilityType);
        }
    }

    // Utility Methods
    public boolean hasActivePet(Player player) {
        PlayerData data = getPlayerData(player);
        return data != null && data.hasActivePet();
    }

    public UUID getPlayerUUID(Player player) {
        PlayerData data = getPlayerData(player);
        return data != null ? data.getPlayerUUID() : player.getUniqueId();
    }

    public void cleanup() {
        for (PlayerData data : playerDataMap.values()) {
            databaseManager.savePlayerData(data.getPlayerUUID(), data);
        }
        playerDataMap.clear();
        databaseManager.shutdown();
    }
}
