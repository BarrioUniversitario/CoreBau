package me.davidml16.baul.pets.data;

import me.davidml16.baul.Main;
import me.davidml16.baul.pets.autopet.AutoPetRule;
import me.davidml16.baul.pets.autopet.TriggerType;
import me.davidml16.baul.pets.menu.PetGUI;
import me.davidml16.baul.pets.pet.Pet;
import me.davidml16.baul.pets.utils.Messages;
import me.davidml16.baul.pets.utils.enums.SortType;
import me.davidml16.baul.pets.utils.enums.VisibilityType;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;

import java.io.Serializable;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

public class PlayerData implements Serializable {

    private final UUID playerUUID;
    private final Set<Pet> ownedPets;
    private final Set<Pet> sharedPets;
    private final Set<AutoPetRule> autoPetRules;
    private Pet activePet;
    private String offlinePetId;
    private Pet upgradingPet;
    private int selectedPage;
    private SortType sortType;
    private VisibilityType visibilityType;
    private final PetGUI petsInventory;

    public PlayerData(Player player) {
        this.playerUUID = player.getUniqueId();
        this.ownedPets = new HashSet<>();
        this.sharedPets = new HashSet<>();
        this.autoPetRules = new HashSet<>();
        this.activePet = null;
        this.offlinePetId = null;
        this.upgradingPet = null;
        this.selectedPage = 0;
        this.sortType = SortType.DEFAULT;
        this.visibilityType = VisibilityType.ALL;
        this.petsInventory = new PetGUI();
    }

    public void addPet(Pet pet) {
        this.ownedPets.add(pet);
    }

    public void removePet(Pet pet) {
        this.ownedPets.remove(pet);
        if (this.activePet != null && this.activePet.equals(pet))
            deactivatePet();
    }

    public boolean hasPet(Pet pet) {
        return this.ownedPets.contains(pet);
    }

    public Set<Pet> getOwnedPets() {
        return new HashSet<>(this.ownedPets);
    }

    public String getOfflinePetId() {
        return offlinePetId;
    }

    public void setOfflinePetId(String offlinePetId) {
        this.offlinePetId = offlinePetId;
    }

    public Pet getPetByUUID(UUID petUUID) {
        return this.ownedPets.stream()
                .filter(pet -> pet.getUuid().equals(petUUID))
                .findFirst()
                .orElse(null);
    }

    public boolean activatePet(Pet pet) {
        if (pet == null) return false;

        deactivatePet();
        this.activePet = pet;
        pet.setActive(true);
        return true;
    }

    public void deactivatePet() {
        if (this.activePet != null) {
            this.activePet.setActive(false);
            this.activePet = null;
        }
    }

    public Pet getActivePet() {
        return this.activePet;
    }

    public boolean isActivePet(Pet pet) {
        return (this.activePet != null && this.activePet.equals(pet));
    }

    public boolean hasActivePet() {
        return (this.activePet != null);
    }

    public int getSelectedPage() {
        return this.selectedPage;
    }

    public UUID getPlayerUUID() {
        return this.playerUUID;
    }

    public PetGUI getPetsInventory() {
        return this.petsInventory;
    }

    public void setInventoryPage(int page) {
        this.selectedPage = page;
        updateInventoryTitle();
    }

    private void updateInventoryTitle() {
        if (this.petsInventory != null) {
            Inventory newInv = Bukkit.createInventory(this.petsInventory, 54,
                    Messages.getGuiTitle(this.selectedPage + 1));
            newInv.setContents(this.petsInventory.getInventory().getContents());
            this.petsInventory.setInventory(newInv);
        }
    }

    public void openInventory() {
        Player player = Bukkit.getPlayer(this.playerUUID);
        if (player != null)
            player.openInventory(this.petsInventory.getInventory());
    }


    public Set<Pet> getSharedPets() {
        return this.sharedPets;
    }

    public void addSharedPet(Pet pet) {
        if (this.ownedPets.remove(pet)) {
            this.sharedPets.add(pet);
            if (this.activePet != null && this.activePet.equals(pet))
                deactivatePet();
        }
    }

    public void removeSharedPet(Pet pet) {
        if (this.sharedPets.remove(pet))
            this.ownedPets.add(pet);
    }

    public boolean isSharedPet(Pet pet) {
        return this.sharedPets.contains(pet);
    }

    public SortType getSortType() {
        return this.sortType;
    }

    public void setSortType(SortType sortType) {
        this.sortType = sortType;
    }

    public void cycleSortType() {
        this.sortType = this.sortType.next();
    }

    public VisibilityType getVisibilityType() {
        return this.visibilityType;
    }

    public void setVisibilityType(VisibilityType visibilityType) {
        this.visibilityType = visibilityType;
    }

    public void cycleVisibility() {
        this.visibilityType = this.visibilityType.next();
    }

    public void applyVisibility() {
        for (Player onlinePlayer : Bukkit.getOnlinePlayers()) {
            PlayerData otherData = Main.getInstance().getPlayerPetManager().getPlayerData(onlinePlayer);
            if (otherData == null) continue;

            Pet otherPet = otherData.getActivePet();
            if (otherPet == null || otherPet.getDisplay() == null || otherPet.getDisplay().isDead())
                continue;

            Player player = Bukkit.getPlayer(this.playerUUID);
            if (player == null)
                continue;

            switch (this.visibilityType) {
                case ALL:
                    player.showEntity(Main.getInstance(), otherPet.getDisplay());
                    break;
                case NONE:
                    player.hideEntity(Main.getInstance(), otherPet.getDisplay());
                    break;
                case OWN:
                    if (otherData.getPlayerUUID().equals(this.playerUUID)) {
                        player.showEntity(Main.getInstance(), otherPet.getDisplay());
                    } else {
                        player.hideEntity(Main.getInstance(), otherPet.getDisplay());
                    }
                    break;
            }
        }
    }

    public void updateVisibility(VisibilityType newType) {
        this.visibilityType = newType;
        applyVisibility();
    }

    public Map<String, Object> serialize() {
        Map<String, Object> data = new HashMap<>();
        data.put("playerUUID", playerUUID.toString());

        List<Map<String, Object>> ownedPetsData = ownedPets.stream()
                .map(Pet::serialize)
                .toList();
        data.put("ownedPets", ownedPetsData);

        List<Map<String, Object>> sharedPetsData = sharedPets.stream()
                .map(Pet::serialize).
                toList();
        data.put("sharedPets", sharedPetsData);

        List<Map<String, Object>> rulesData = autoPetRules.stream()
                .map(AutoPetRule::serialize)
                .toList();

        data.put("petRules", rulesData);

        data.put("activePet", activePet != null ? activePet.serialize() : null);
        data.put("offlinePet", offlinePetId);
        data.put("petUpgrade", upgradingPet != null ? upgradingPet.serialize() : null);

        data.put("selectedPage", selectedPage);
        data.put("sortType", sortType.name());
        data.put("visibilityType", visibilityType.name());

        return data;
    }

    @SuppressWarnings("unchecked")
    public PlayerData deserialize(Map<String, Object> data) {
        UUID uuid = UUID.fromString((String) data.get("playerUUID"));
        Player player = Bukkit.getPlayer(uuid);
        if (player == null) return null;

        List<Map<String, Object>> ownedPetsData = (List<Map<String, Object>>) data.get("ownedPets");
        if (ownedPetsData != null) {
            ownedPetsData.forEach(petData -> {
                Pet pet = Pet.deserialize(petData);
                if (pet != null) ownedPets.add(pet);
            });
        }

        List<Map<String, Object>> sharedPetsData = (List<Map<String, Object>>) data.get("sharedPets");
        if (sharedPetsData != null) {
            sharedPetsData.forEach(petData -> {
                Pet pet = Pet.deserialize(petData);
                if (pet != null) sharedPets.add(pet);
            });
        }

        List<Map<String, Object>> petRules = (List<Map<String, Object>>) data.get("petRules");
        if (petRules != null) {
            petRules.forEach(petRule -> {
                AutoPetRule rule = AutoPetRule.deserialize(petRule);
                autoPetRules.add(rule);
            });
        }

        Map<String, Object> activePetData = (Map<String, Object>) data.get("activePet");
        if (activePetData != null) activePet = Pet.deserialize(activePetData);
        else activePet = null;

        offlinePetId = (String) data.get("offlinePet");

        Map<String, Object> upgradingPetData = (Map<String, Object>) data.get("petUpgrade");
        if (upgradingPetData != null) upgradingPet = Pet.deserialize(upgradingPetData);
        else upgradingPet = null;

        selectedPage = (int) data.getOrDefault("selectedPage", 0);
        sortType = SortType.valueOf((String) data.getOrDefault("sortType", SortType.DEFAULT.name()));
        visibilityType = VisibilityType.valueOf((String) data.getOrDefault("visibilityType", VisibilityType.ALL.name()));

        return this;
    }

    public Pet getUpgradingPet() {
        return upgradingPet;
    }

    public void setUpgradingPet(Pet upgradingPet) {
        if (upgradingPet == null || upgradingPet.getUpgrade() == null) {
            Messages.debug("Cannot start pet upgrade: pet or upgrade is null");
            this.upgradingPet = null;
            return;
        }

        try {
            upgradingPet.getUpgrade().startUpgrade();
            this.upgradingPet = upgradingPet;
        } catch (Exception e) {
            Messages.debug("Failed to start pet upgrade: " + e.getMessage());
        }
    }

    public void updatePet() {
        if (this.upgradingPet != null) {
            this.upgradingPet.finishUpgrade();
        }
    }

    public boolean hasUpgradingPet() {
        return this.upgradingPet != null;
    }

    public void clearUpgradingPet() {
        this.upgradingPet = null;
    }

    public void addAutoPetRule(AutoPetRule rule) {
        this.autoPetRules.add(rule);
    }

    public void removeAutoPetRule(AutoPetRule rule) {
        this.autoPetRules.remove(rule);
    }

    public void removeAutoPetRule(UUID ruleId) {
        this.autoPetRules.removeIf(rule -> rule.getRuleId().equals(ruleId));
    }

    public Set<AutoPetRule> getAutoPetRules() {
        return new HashSet<>(autoPetRules);
    }

    public AutoPetRule getAutoPetRule(TriggerType triggerType) {
        return autoPetRules.stream()
                .filter(rule -> rule.getTrigger() == triggerType)
                .findFirst()
                .orElse(null);
    }

    public boolean hasAutoPetRule(TriggerType triggerType) {
        return autoPetRules.stream()
                .anyMatch(rule -> rule.getTrigger() == triggerType);
    }

    public String toString() {
        return "PlayerData{" +
                "playerUUID=" + playerUUID +
                ", ownedPets=" + ownedPets +
                ", sharedPets=" + sharedPets +
                ", activePet=" + activePet +
                ", offlinePet=" + offlinePetId +
                ", selectedPage=" + selectedPage +
                ", sortType=" + sortType +
                ", visibilityType=" + visibilityType +
                ", petsInventory=" + petsInventory +
                '}';
    }
}
