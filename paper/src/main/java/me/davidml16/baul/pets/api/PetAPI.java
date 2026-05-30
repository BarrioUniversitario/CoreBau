package me.davidml16.baul.pets.api;

import me.davidml16.baul.Main;
import me.davidml16.baul.pets.abilities.PetAbility;
import me.davidml16.baul.pets.api.events.PetEquipEvent;
import me.davidml16.baul.pets.api.events.PetExpGainEvent;
import me.davidml16.baul.pets.api.events.PetItemApplyEvent;
import me.davidml16.baul.pets.api.events.PetSkinApplyEvent;
import me.davidml16.baul.pets.api.events.PetUnequipEvent;
import me.davidml16.baul.pets.autopet.AutoPetException;
import me.davidml16.baul.pets.autopet.AutoPetRule;
import me.davidml16.baul.pets.autopet.TriggerType;
import me.davidml16.baul.pets.data.PlayerData;
import me.davidml16.baul.pets.items.PetItem;
import me.davidml16.baul.pets.pet.Pet;
import me.davidml16.baul.pets.pet.PetSkin;
import me.davidml16.baul.pets.utils.enums.PetType;
import me.davidml16.baul.pets.utils.enums.Rarity;
import me.davidml16.baul.pets.utils.enums.SortType;
import me.davidml16.baul.pets.utils.enums.VisibilityType;
import me.davidml16.baul.pets.utils.enums.XPSource;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

/**
 * The main API class for BetterPets plugin.
 * This class provides a comprehensive interface for interacting with the pet system,
 * including pet management, player data access, experience handling, and more.
 *
 * <p>Usage example:</p>
 * <pre>{@code
 * // Get a player's active pet
 * Optional<Pet> activePet = PetAPI.getActivePet(player);
 * if (activePet.isPresent()) {
 *     // Add experience to the pet
 *     PetAPI.addPetExperience(player, 100, XPSource.COMBAT);
 * }
 * }</pre>
 *
 * @author psikuvit
 * @version 2.0
 * @since 1.0
 */
public class PetAPI {
    private static Main plugin;

    public static void init(Main main) {
        plugin = main;
    }

    // ========== CORE PLAYER DATA METHODS ==========

    /**
     * Get a player's pet data
     *
     * @param player The player
     * @return Optional containing PlayerData if exists
     */
    public static Optional<PlayerData> getPlayerData(Player player) {
        return Optional.ofNullable(plugin.getPlayerPetManager().getPlayerData(player));
    }

    /**
     * Get a player's active pet
     *
     * @param player The player
     * @return Optional containing active Pet if exists
     */
    public static Optional<Pet> getActivePet(Player player) {
        return getPlayerData(player).map(PlayerData::getActivePet);
    }

    /**
     * Get all pets owned by a player
     *
     * @param player The player
     * @return Set of pets owned by the player
     */
    public static Set<Pet> getPlayerPets(Player player) {
        return getPlayerData(player).map(PlayerData::getOwnedPets).orElse(new HashSet<>());
    }

    // ========== PET MANAGEMENT METHODS ==========

    /**
     * Give a pet to a player
     *
     * @param player The player
     * @param petId  The pet ID
     * @return Pet if successful
     */
    public static Pet givePet(Player player, String petId, Rarity rarity) {
        return plugin.getPlayerPetManager().givePet(player, petId, rarity);
    }

    /**
     * Remove a pet from a player
     *
     * @param player The player
     * @param pet    The pet
     */
    public static void removePet(Player player, Pet pet) {
        plugin.getPlayerPetManager().removePetFromPlayer(player, pet);
    }

    /**
     * Activate a pet for a player. Fires PetEquipEvent.
     *
     * @param player The player who owns the pet
     * @param pet    The pet to activate
     * @return true if the pet was successfully activated, false otherwise
     * @throws IllegalArgumentException if player or pet is null
     * @since 1.0
     */
    public static boolean activatePet(@NotNull Player player, @NotNull Pet pet) {
        Objects.requireNonNull(player, "Player cannot be null");
        Objects.requireNonNull(pet, "Pet cannot be null");

        PetEquipEvent event = new PetEquipEvent(player, pet);
        Bukkit.getPluginManager().callEvent(event);

        if (!event.isCancelled()) {
            return plugin.getPlayerPetManager().activatePet(player, pet);
        }
        return false;
    }

    /**
     * Deactivate a player's currently active pet. Fires PetUnequipEvent.
     *
     * @param player The player whose pet should be deactivated
     * @return true if a pet was deactivated, false if no pet was active
     * @since 1.0
     */
    public static boolean deactivatePet(@NotNull Player player) {
        Objects.requireNonNull(player, "Player cannot be null");

        Optional<Pet> activePet = getActivePet(player);
        if (activePet.isPresent()) {
            PetUnequipEvent event = new PetUnequipEvent(player, activePet.get());
            Bukkit.getPluginManager().callEvent(event);

            if (!event.isCancelled()) {
                plugin.getPlayerPetManager().deactivatePet(player);
                return true;
            }
        }
        return false;
    }

    /**
     * Get a pet by its UUID from a player's collection.
     *
     * @param player  The player who owns the pet
     * @param petUUID The UUID of the pet to find
     * @return Optional containing the pet if found
     * @since 1.0
     */
    @NotNull
    public static Optional<Pet> getPetByUUID(@NotNull Player player, @NotNull UUID petUUID) {
        Objects.requireNonNull(player, "Player cannot be null");
        Objects.requireNonNull(petUUID, "Pet UUID cannot be null");

        return getPlayerData(player)
                .map(data -> data.getPetByUUID(petUUID))
                .filter(Objects::nonNull);
    }

    /**
     * Get pets filtered by rarity.
     *
     * @param player The player whose pets to filter
     * @param rarity The rarity to filter by
     * @return List of pets with the specified rarity, sorted by level descending
     * @since 1.0
     */
    @NotNull
    public static List<Pet> getPetsByRarity(@NotNull Player player, @NotNull Rarity rarity) {
        Objects.requireNonNull(player, "Player cannot be null");
        Objects.requireNonNull(rarity, "Rarity cannot be null");

        return getPlayerPets(player).stream()
                .filter(pet -> pet.getRarity() == rarity)
                .sorted(Comparator.comparingInt(Pet::getLevel).reversed())
                .toList();
    }

    /**
     * Get pets filtered by type.
     *
     * @param player The player whose pets to filter
     * @param type   The pet type to filter by
     * @return List of pets with the specified type
     * @since 1.0
     */
    @NotNull
    public static List<Pet> getPetsByType(@NotNull Player player, @NotNull PetType type) {
        Objects.requireNonNull(player, "Player cannot be null");
        Objects.requireNonNull(type, "Pet type cannot be null");

        return getPlayerPets(player).stream()
                .filter(pet -> pet.getType() == type)
                .sorted(Comparator.comparingInt(Pet::getLevel).reversed())
                .toList();
    }

    /**
     * Get the total number of pets a player owns.
     *
     * @param player The player
     * @return Total number of owned pets
     * @since 1.0
     */
    public static int getTotalPetCount(@NotNull Player player) {
        Objects.requireNonNull(player, "Player cannot be null");
        return getPlayerPets(player).size();
    }

    /**
     * Check if a player owns a specific pet.
     *
     * @param player The player
     * @param pet    The pet to check
     * @return true if the player owns the pet
     * @since 1.0
     */
    public static boolean hasPlayerPet(@NotNull Player player, @NotNull Pet pet) {
        Objects.requireNonNull(player, "Player cannot be null");
        Objects.requireNonNull(pet, "Pet cannot be null");

        return plugin.getPlayerPetManager().playerOwnsPet(player, pet);
    }

    // ========== EXPERIENCE AND LEVELING METHODS ==========

    /**
     * Add experience to a player's active pet
     *
     * @param player The player
     * @param amount Amount of experience
     * @param source XP source
     * @return true if successful
     */
    public static boolean addPetExperience(Player player, int amount, XPSource source) {
        return getActivePet(player)
                .map(pet -> {
                    PetExpGainEvent event = new PetExpGainEvent(player, pet, amount, source);
                    Bukkit.getPluginManager().callEvent(event);
                    if (!event.isCancelled()) {
                        pet.gainExp(event.getAmount(), source);
                        return true;
                    }
                    return false;
                })
                .orElse(false);
    }

    /**
     * Calculate the experience required for a pet to reach the next level.
     *
     * @param pet The pet to calculate for
     * @return Experience required for next level, or -1 if already at max level
     * @since 1.0
     */
    public static int getExpRequiredForNextLevel(@NotNull Pet pet) {
        Objects.requireNonNull(pet, "Pet cannot be null");
        return pet.getRequiredExp();
    }

    /**
     * Get the progress percentage towards the next level.
     *
     * @param pet The pet to calculate for
     * @return Progress percentage (0.0 to 1.0)
     * @since 1.0
     */
    public static double getLevelProgress(@NotNull Pet pet) {
        Objects.requireNonNull(pet, "Pet cannot be null");
        return pet.getProgress();
    }

    /**
     * Check if a pet has reached max level
     *
     * @param pet The pet
     * @return true if max level
     */
    public static boolean isMaxLevel(Pet pet) {
        return pet.getLevel() >= 100;
    }

    // ========== PET ITEMS AND CUSTOMIZATION METHODS ==========

    /**
     * Apply a pet item to a pet
     *
     * @param player The player
     * @param pet    The pet
     * @param item   The pet item
     * @return true if successful
     */
    public static boolean applyPetItem(Player player, Pet pet, PetItem item) {
        PetItemApplyEvent event = new PetItemApplyEvent(player, pet, item);
        Bukkit.getPluginManager().callEvent(event);
        if (!event.isCancelled()) {
            pet.equipItem(item);
            return true;
        }
        return false;
    }

    /**
     * Get a pet's current equipped item
     *
     * @param pet The pet
     * @return Optional containing equipped PetItem if exists
     */
    public static Optional<PetItem> getPetItem(Pet pet) {
        return Optional.ofNullable(pet.getEquippedItem());
    }

    /**
     * Remove an item from a pet.
     *
     * @param player The player who owns the pet
     * @param pet    The pet to remove the item from
     * @return The removed item, or null if no item was equipped
     * @since 1.0
     */
    @Nullable
    public static PetItem removePetItem(@NotNull Player player, @NotNull Pet pet) {
        Objects.requireNonNull(player, "Player cannot be null");
        Objects.requireNonNull(pet, "Pet cannot be null");

        PetItem currentItem = pet.getEquippedItem();
        if (currentItem != null) {
            pet.unequipItem();
        }
        return currentItem;
    }

    /**
     * Apply a skin to a pet. Fires PetSkinApplyEvent.
     *
     * @param player The player who owns the pet
     * @param pet    The pet to apply the skin to
     * @param skin   The skin to apply
     * @return true if the skin was successfully applied
     * @since 1.0
     */
    public static boolean applySkinToPet(@NotNull Player player, @NotNull Pet pet, @NotNull PetSkin skin) {
        Objects.requireNonNull(player, "Player cannot be null");
        Objects.requireNonNull(pet, "Pet cannot be null");
        Objects.requireNonNull(skin, "Skin cannot be null");

        PetSkinApplyEvent event = new PetSkinApplyEvent(player, pet, skin);
        Bukkit.getPluginManager().callEvent(event);

        if (!event.isCancelled()) {
            pet.setPetSkin(skin);
            return true;
        }
        return false;
    }

    // ========== XP SHARING SYSTEM METHODS ==========

    /**
     * Get pets being shared XP with
     *
     * @param player The player
     * @return Set of shared pets
     */
    public static Set<Pet> getSharedPets(Player player) {
        return getPlayerData(player).map(PlayerData::getSharedPets).orElse(new HashSet<>());
    }

    /**
     * Check if a player has reached the maximum number of pets they can share XP with.
     *
     * @param player The player to check
     * @return true if the player has reached the maximum (4 pets)
     * @since 1.0
     */
    public static boolean hasMaxSharedPets(@NotNull Player player) {
        Objects.requireNonNull(player, "Player cannot be null");
        return getSharedPets(player).size() >= 4;
    }

    /**
     * Add a pet to XP sharing.
     *
     * @param player The player who owns the pet
     * @param pet    The pet to add to XP sharing
     * @return true if successfully added, false if already sharing or at maximum
     * @since 1.0
     */
    public static boolean addPetToXPShare(@NotNull Player player, @NotNull Pet pet) {
        Objects.requireNonNull(player, "Player cannot be null");
        Objects.requireNonNull(pet, "Pet cannot be null");

        return getPlayerData(player)
                .filter(data -> !hasMaxSharedPets(player))
                .map(data -> {
                    if (!data.isSharedPet(pet)) {
                        data.addSharedPet(pet);
                        return true;
                    }
                    return false;
                })
                .orElse(false);
    }

    /**
     * Remove a pet from XP sharing.
     *
     * @param player The player who owns the pet
     * @param pet    The pet to remove from XP sharing
     * @return true if successfully removed, false if not currently sharing
     * @since 1.0
     */
    public static boolean removePetFromXPShare(@NotNull Player player, @NotNull Pet pet) {
        Objects.requireNonNull(player, "Player cannot be null");
        Objects.requireNonNull(pet, "Pet cannot be null");

        return getPlayerData(player)
                .map(data -> {
                    if (data.isSharedPet(pet)) {
                        data.removeSharedPet(pet);
                        return true;
                    }
                    return false;
                })
                .orElse(false);
    }

    // ========== AUTOPET RULES METHODS ==========

    /**
     * Get all AutoPet rules for a player.
     *
     * @param player The player
     * @return Set of AutoPet rules
     * @since 2.0
     */
    @NotNull
    public static Set<AutoPetRule> getAutoPetRules(@NotNull Player player) {
        Objects.requireNonNull(player, "Player cannot be null");
        return getPlayerData(player)
                .map(PlayerData::getAutoPetRules)
                .orElse(new HashSet<>());
    }

    /**
     * Get an AutoPet rule for a specific trigger type.
     *
     * @param player      The player
     * @param triggerType The trigger type
     * @return Optional containing the AutoPet rule if exists
     * @since 2.0
     */
    @NotNull
    public static Optional<AutoPetRule> getAutoPetRule(@NotNull Player player, @NotNull TriggerType triggerType) {
        Objects.requireNonNull(player, "Player cannot be null");
        Objects.requireNonNull(triggerType, "Trigger type cannot be null");

        return getPlayerData(player)
                .map(data -> data.getAutoPetRule(triggerType))
                .filter(Objects::nonNull);
    }

    /**
     * Check if a player has an AutoPet rule for a specific trigger.
     *
     * @param player      The player
     * @param triggerType The trigger type
     * @return true if the player has a rule for this trigger
     * @since 2.0
     */
    public static boolean hasAutoPetRule(@NotNull Player player, @NotNull TriggerType triggerType) {
        Objects.requireNonNull(player, "Player cannot be null");
        Objects.requireNonNull(triggerType, "Trigger type cannot be null");

        return getPlayerData(player)
                .map(data -> data.hasAutoPetRule(triggerType))
                .orElse(false);
    }

    /**
     * Create an AutoPet rule for a player.
     *
     * @param player      The player
     * @param triggerType The trigger type
     * @param selectedPet The pet to equip when triggered
     * @param exceptions  Optional exceptions to the rule
     * @return true if the rule was successfully created
     * @since 2.0
     */
    public static boolean createAutoPetRule(@NotNull Player player, @NotNull TriggerType triggerType,
                                            @NotNull Pet selectedPet, @Nullable Set<AutoPetException> exceptions) {
        Objects.requireNonNull(player, "Player cannot be null");
        Objects.requireNonNull(triggerType, "Trigger type cannot be null");
        Objects.requireNonNull(selectedPet, "Selected pet cannot be null");

        return getPlayerData(player)
                .map(data -> {
                    // Remove existing rule if present
                    if (data.hasAutoPetRule(triggerType)) {
                        AutoPetRule existingRule = data.getAutoPetRule(triggerType);
                        if (existingRule != null) {
                            data.removeAutoPetRule(existingRule);
                        }
                    }

                    // Create new rule
                    AutoPetRule newRule = new AutoPetRule(triggerType, selectedPet);
                    if (exceptions != null) {
                        for (AutoPetException exception : exceptions) {
                            newRule.addException(exception);
                        }
                    }

                    data.addAutoPetRule(newRule);
                    return true;
                })
                .orElse(false);
    }

    /**
     * Remove an AutoPet rule for a player.
     *
     * @param player      The player
     * @param triggerType The trigger type
     * @return true if the rule was successfully removed
     * @since 2.0
     */
    public static boolean removeAutoPetRule(@NotNull Player player, @NotNull TriggerType triggerType) {
        Objects.requireNonNull(player, "Player cannot be null");
        Objects.requireNonNull(triggerType, "Trigger type cannot be null");

        return getPlayerData(player)
                .map(data -> {
                    if (data.hasAutoPetRule(triggerType)) {
                        AutoPetRule rule = data.getAutoPetRule(triggerType);
                        if (rule != null) {
                            data.removeAutoPetRule(rule);
                            return true;
                        }
                    }
                    return false;
                })
                .orElse(false);
    }

    /**
     * Remove an AutoPet rule by its UUID.
     *
     * @param player The player
     * @param ruleId The rule UUID
     * @return true if the rule was successfully removed
     * @since 2.0
     */
    public static boolean removeAutoPetRule(@NotNull Player player, @NotNull UUID ruleId) {
        Objects.requireNonNull(player, "Player cannot be null");
        Objects.requireNonNull(ruleId, "Rule ID cannot be null");

        return getPlayerData(player)
                .map(data -> {
                    data.removeAutoPetRule(ruleId);
                    return true;
                })
                .orElse(false);
    }

    // ========== PET UPGRADE SYSTEM METHODS ==========

    /**
     * Get the pet currently being upgraded by a player.
     *
     * @param player The player
     * @return Optional containing the upgrading pet if exists
     * @since 2.0
     */
    @NotNull
    public static Optional<Pet> getUpgradingPet(@NotNull Player player) {
        Objects.requireNonNull(player, "Player cannot be null");
        return getPlayerData(player)
                .map(PlayerData::getUpgradingPet)
                .filter(Objects::nonNull);
    }

    /**
     * Check if a player has a pet currently being upgraded.
     *
     * @param player The player
     * @return true if the player has an upgrading pet
     * @since 2.0
     */
    public static boolean hasUpgradingPet(@NotNull Player player) {
        Objects.requireNonNull(player, "Player cannot be null");
        return getPlayerData(player)
                .map(PlayerData::hasUpgradingPet)
                .orElse(false);
    }

    /**
     * Start upgrading a pet for a player.
     *
     * @param player The player
     * @param pet    The pet to upgrade
     * @return true if the upgrade was successfully started
     * @since 2.0
     */
    public static boolean startPetUpgrade(@NotNull Player player, @NotNull Pet pet) {
        Objects.requireNonNull(player, "Player cannot be null");
        Objects.requireNonNull(pet, "Pet cannot be null");

        if (pet.getUpgrade() == null) {
            return false;
        }

        return getPlayerData(player)
                .map(data -> {
                    if (!data.hasUpgradingPet()) {
                        data.setUpgradingPet(pet);
                        return true;
                    }
                    return false;
                })
                .orElse(false);
    }


    /**
     * Cancel a pet upgrade for a player.
     *
     * @param player The player
     * @return true if an upgrade was cancelled
     * @since 2.0
     */
    public static boolean cancelPetUpgrade(@NotNull Player player) {
        Objects.requireNonNull(player, "Player cannot be null");

        return getPlayerData(player)
                .map(data -> {
                    if (data.hasUpgradingPet()) {
                        data.clearUpgradingPet();
                        return true;
                    }
                    return false;
                })
                .orElse(false);
    }

    /**
     * Get the remaining upgrade time for a pet.
     *
     * @param pet The pet
     * @return Remaining time in milliseconds, or -1 if not upgrading
     * @since 2.0
     */
    public static long getUpgradeRemainingTime(@NotNull Pet pet) {
        Objects.requireNonNull(pet, "Pet cannot be null");

        if (pet.getUpgrade() == null) {
            return -1;
        }

        long endTime = pet.getUpgrade().getEndTime();
        if (endTime <= 0) {
            return -1;
        }

        return Math.max(0, endTime - System.currentTimeMillis());
    }

    // ========== PET TEMPLATE AND CONFIGURATION METHODS ==========

    /**
     * Get a pet template by ID. This returns the base pet configuration, not a player's pet.
     *
     * @param petId The pet ID to look up
     * @return Optional containing the pet template if found
     * @since 1.0
     */
    @NotNull
    public static Optional<Pet> getPetTemplate(@NotNull String petId, Rarity rarity) {
        Objects.requireNonNull(petId, "Pet ID cannot be null");
        return Optional.ofNullable(plugin.getRegistryPetManager().getPet(petId, rarity));
    }

    /**
     * Get all available pet templates.
     *
     * @return Map of pet ID to pet template
     * @since 1.0
     */
    @NotNull
    public static Map<String, Pet> getAllPetTemplates() {
        return plugin.getRegistryPetManager().getLoadedPets();
    }

    /**
     * Get a pet item by ID.
     *
     * @param itemId The item ID
     * @return Optional containing the pet item if found
     * @since 2.0
     */
    @NotNull
    public static Optional<PetItem> getPetItemTemplate(@NotNull String itemId) {
        Objects.requireNonNull(itemId, "Item ID cannot be null");
        return Optional.ofNullable(plugin.getPetItemLoader().getItem(itemId));
    }

    /**
     * Get all available pet item templates.
     *
     * @return Map of item ID to pet item template
     * @since 2.0
     */
    @NotNull
    public static Map<String, PetItem> getAllPetItemTemplates() {
        return plugin.getPetItemLoader().getItems();
    }

    /**
     * Get a pet skin by ID.
     *
     * @param skinId The skin ID
     * @return Optional containing the pet skin if found
     * @since 2.0
     */
    @NotNull
    public static Optional<PetSkin> getPetSkinTemplate(@NotNull String skinId) {
        Objects.requireNonNull(skinId, "Skin ID cannot be null");
        return Optional.ofNullable(plugin.getPetSkinLoader().getSkinById(skinId));
    }

    /**
     * Get all available pet skins for a specific pet.
     *
     * @param petId The pet ID
     * @return List of available skins for the pet
     * @since 2.0
     */
    @NotNull
    public static List<PetSkin> getPetSkins(@NotNull String petId) {
        Objects.requireNonNull(petId, "Pet ID cannot be null");
        return plugin.getPetSkinLoader().getSkinsForPet(petId);
    }

    // ========== PET ABILITIES METHODS ==========

    /**
     * Get all abilities for a pet.
     *
     * @param pet The pet
     * @return List of pet abilities
     * @since 2.0
     */
    @NotNull
    public static List<PetAbility> getPetAbilities(@NotNull Pet pet) {
        Objects.requireNonNull(pet, "Pet cannot be null");
        return new ArrayList<>(pet.getAbilities());
    }

    // ========== VISIBILITY AND SORTING METHODS ==========

    /**
     * Set pet visibility for a player
     *
     * @param player  The player
     * @param visible true to show pets
     */
    public static void setPetVisibility(Player player, boolean visible) {
        getPlayerData(player).ifPresent(PlayerData::applyVisibility);
    }

    /**
     * Get player's pet sort type
     *
     * @param player The player
     * @return Current sort type
     */
    public static SortType getPetSort(Player player) {
        return getPlayerData(player).map(PlayerData::getSortType).orElse(SortType.DEFAULT);
    }

    /**
     * Set player's pet sort type
     *
     * @param player   The player
     * @param sortType Sort type to set
     */
    public static void setPetSort(Player player, SortType sortType) {
        getPlayerData(player).ifPresent(data -> data.setSortType(sortType));
    }

    /**
     * Get player's pet visibility type.
     *
     * @param player The player
     * @return Current visibility type
     * @since 2.0
     */
    public static VisibilityType getPetVisibilityType(@NotNull Player player) {
        Objects.requireNonNull(player, "Player cannot be null");
        return getPlayerData(player)
                .map(PlayerData::getVisibilityType)
                .orElse(VisibilityType.ALL);
    }

    /**
     * Set player's pet visibility type.
     *
     * @param player         The player
     * @param visibilityType The visibility type to set
     * @since 2.0
     */
    public static void setPetVisibilityType(@NotNull Player player, @NotNull VisibilityType visibilityType) {
        Objects.requireNonNull(player, "Player cannot be null");
        Objects.requireNonNull(visibilityType, "Visibility type cannot be null");

        getPlayerData(player).ifPresent(data -> data.setVisibilityType(visibilityType));
    }

    // ========== GUI AND MENU METHODS ==========

    /**
     * Open the pet management GUI for a player.
     *
     * @param player The player
     * @param page   The page to open (0-based)
     * @since 2.0
     */
    public static void openPetGUI(@NotNull Player player, int page) {
        Objects.requireNonNull(player, "Player cannot be null");
        getPlayerData(player).ifPresent(data -> {
            data.getPetsInventory().openPetsMenu(player, page);
        });
    }

    /**
     * Open the pet upgrade GUI for a player.
     *
     * @param player The player
     * @since 2.0
     */
    public static void openUpgradeGUI(@NotNull Player player) {
        Objects.requireNonNull(player, "Player cannot be null");
        new me.davidml16.baul.pets.menu.UpgradeMenu().openUpgradeMenu(player);
    }

    /**
     * Open the AutoPet rules GUI for a player.
     *
     * @param player The player
     * @since 2.0
     */
    public static void openAutoPetRulesGUI(@NotNull Player player) {
        Objects.requireNonNull(player, "Player cannot be null");
        getPlayerData(player).ifPresent(data -> {
            new me.davidml16.baul.pets.menu.AutoPetRuleMenu(
                    me.davidml16.baul.pets.menu.AutoPetRuleMenu.MenuType.RULE_MANAGEMENT,
                    data
            ).openMenu();
        });
    }

    // ========== ASYNC API METHODS ==========

    /**
     * Asynchronously load player data from the database.
     *
     * @param player The player to load data for
     * @return CompletableFuture that will complete with the player's data
     * @since 1.0
     */
    @NotNull
    public static CompletableFuture<Optional<PlayerData>> loadPlayerDataAsync(@NotNull Player player) {
        Objects.requireNonNull(player, "Player cannot be null");

        return CompletableFuture.supplyAsync(() -> {
            try {
                PlayerData data = new PlayerData(player);
                return plugin.getDatabaseManager().loadPlayerData(player.getUniqueId(), data)
                        .thenApply(Optional::ofNullable)
                        .join();
            } catch (Exception e) {
                plugin.getLogger().severe("Failed to load player data for " + player.getName() + ": " + e.getMessage());
                return Optional.empty();
            }
        });
    }

    /**
     * Asynchronously save player data to the database.
     *
     * @param player The player whose data to save
     * @return CompletableFuture that will complete when the save operation finishes
     * @since 1.0
     */
    @NotNull
    public static CompletableFuture<Void> savePlayerDataAsync(@NotNull Player player) {
        Objects.requireNonNull(player, "Player cannot be null");

        return getPlayerData(player)
                .map(data -> plugin.getDatabaseManager().savePlayerData(player.getUniqueId(), data))
                .orElse(CompletableFuture.completedFuture(null));
    }

    // ========== UTILITY METHODS ==========

    /**
     * Check if the API is properly initialized.
     *
     * @return true if the API is ready to use
     * @since 1.0
     */
    public static boolean isInitialized() {
        return plugin != null;
    }

    /**
     * Get the BetterPets plugin instance. Use with caution.
     *
     * @return The plugin instance, or null if not initialized
     * @since 1.0
     */
    @Nullable
    public static Main getPlugin() {
        return plugin;
    }

    /**
     * Get the total number of pets a player has at max level.
     *
     * @param player The player
     * @return Number of max level pets
     * @since 2.0
     */
    public static int getMaxLevelPetCount(@NotNull Player player) {
        Objects.requireNonNull(player, "Player cannot be null");
        return (int) getPlayerPets(player).stream()
                .filter(PetAPI::isMaxLevel)
                .count();
    }

    /**
     * Get the highest level pet a player owns.
     *
     * @param player The player
     * @return Optional containing the highest level pet
     * @since 2.0
     */
    @NotNull
    public static Optional<Pet> getHighestLevelPet(@NotNull Player player) {
        Objects.requireNonNull(player, "Player cannot be null");
        return getPlayerPets(player).stream()
                .max(Comparator.comparingInt(Pet::getLevel));
    }

    /**
     * Get the rarest pet a player owns.
     *
     * @param player The player
     * @return Optional containing the rarest pet
     * @since 2.0
     */
    @NotNull
    public static Optional<Pet> getRarestPet(@NotNull Player player) {
        Objects.requireNonNull(player, "Player cannot be null");
        return getPlayerPets(player).stream()
                .max(Comparator.comparingInt(pet -> pet.getRarity().ordinal()));
    }

    /**
     * Get pets that are currently active for all online players.
     *
     * @return Map of player to their active pet
     * @since 2.0
     */
    @NotNull
    public static Map<Player, Pet> getAllActivePets() {
        Map<Player, Pet> activePets = new HashMap<>();
        for (Player player : Bukkit.getOnlinePlayers()) {
            getActivePet(player).ifPresent(pet -> activePets.put(player, pet));
        }
        return activePets;
    }

    /**
     * Get all pets of a specific type that are currently active.
     *
     * @param type The pet type
     * @return List of active pets of the specified type
     * @since 2.0
     */
    @NotNull
    public static List<Pet> getActivePetsByType(@NotNull PetType type) {
        Objects.requireNonNull(type, "Pet type cannot be null");
        return getAllActivePets().values().stream()
                .filter(pet -> pet.getType() == type)
                .toList();
    }

    /**
     * Get all pets of a specific rarity that are currently active.
     *
     * @param rarity The pet rarity
     * @return List of active pets of the specified rarity
     * @since 2.0
     */
    @NotNull
    public static List<Pet> getActivePetsByRarity(@NotNull Rarity rarity) {
        Objects.requireNonNull(rarity, "Pet rarity cannot be null");
        return getAllActivePets().values().stream()
                .filter(pet -> pet.getRarity() == rarity)
                .toList();
    }
}
