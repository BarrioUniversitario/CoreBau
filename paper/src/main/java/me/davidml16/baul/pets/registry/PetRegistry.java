package me.davidml16.baul.pets.registry;

import me.davidml16.baul.pets.pet.Pet;
import me.davidml16.baul.pets.utils.Messages;
import me.davidml16.baul.pets.utils.enums.Rarity;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;

public class PetRegistry {

    private final Map<String, PetTypeInfo> registeredPets = new HashMap<>();

    public void registerPet(String petId, Function<Rarity, Pet> petConstructor, Rarity... availableRarities) {
        if (petId == null || petId.isEmpty()) throw new IllegalArgumentException("Pet ID cannot be null or empty");
        if (petConstructor == null) throw new IllegalArgumentException("Pet constructor cannot be null");
        if (availableRarities == null || availableRarities.length == 0)
            throw new IllegalArgumentException("Pet must have at least one available rarity");

        PetTypeInfo typeInfo = new PetTypeInfo(petConstructor, availableRarities);
        registeredPets.put(petId.toLowerCase(), typeInfo);
    }

    public Pet createPet(String petId, Rarity rarity) {
        if (petId == null || rarity == null) {
            Messages.debug("Cannot create pet: petId or rarity is null");
            return null;
        }

        String lowercaseId = petId.toLowerCase();

        String basePetId = extractBasePetId(lowercaseId);
        PetTypeInfo typeInfo = registeredPets.get(basePetId);

        if (typeInfo == null) {
            Messages.debug("Pet type not registered: " + petId + " (base: " + basePetId + ")");
            return null;
        }

        boolean rarityAvailable = false;
        for (Rarity availableRarity : typeInfo.availableRarities) {
            if (availableRarity == rarity) {
                rarityAvailable = true;
                break;
            }
        }

        if (!rarityAvailable) {
            Messages.debug("Rarity " + rarity + " not available for pet type: " + petId);
            return null;
        }

        try {
            Pet pet = typeInfo.rarityConstructor.apply(rarity);

            if (pet == null) {
                Messages.debug("Failed to create pet with rarity: " + rarity + " for pet: " + petId);
                return null;
            }

            return pet;
        } catch (Exception e) {
            Messages.debug("Failed to create pet " + petId + ": " + e.getMessage());
            return null;
        }
    }

    public Pet createPet(String petId) {
        if (petId == null) {
            Messages.debug("Cannot create pet: petId is null");
            return null;
        }

        String lowercaseId = petId.toLowerCase();

        Rarity extractedRarity = extractRarityFromId(lowercaseId);
        if (extractedRarity != null) {
            return createPet(petId, extractedRarity);
        }

        PetTypeInfo typeInfo = registeredPets.get(lowercaseId);

        if (typeInfo == null) {
            Messages.debug("Pet type not registered: " + petId);
            return null;
        }

        try {
            Rarity defaultRarity = typeInfo.availableRarities.length > 0 ? typeInfo.availableRarities[0] : null;
            if (defaultRarity == null) {
                Messages.debug("No available rarities for pet: " + petId);
                return null;
            }
            return createPet(petId, defaultRarity);
        } catch (Exception e) {
            Messages.debug("Failed to create pet " + petId + ": " + e.getMessage());
            return null;
        }
    }

    public boolean isRegistered(String petId) {
        if (petId == null) return false;
        return registeredPets.containsKey(petId.toLowerCase());
    }

    public Set<String> getRegisteredPetIds() {
        return Set.copyOf(registeredPets.keySet());
    }

    public Rarity[] getAvailableRarities(String petId) {
        if (petId == null) return null;

        PetTypeInfo typeInfo = registeredPets.get(petId.toLowerCase());
        return typeInfo != null ? typeInfo.availableRarities.clone() : null;
    }

    public boolean unregisterPet(String petId) {
        if (petId == null) return false;

        PetTypeInfo removed = registeredPets.remove(petId.toLowerCase());
        if (removed != null) {
            Messages.debug("Unregistered pet type: " + petId);
            return true;
        }
        return false;
    }

    public void clearRegistry() {
        Messages.debug("Clearing pet registry - " + registeredPets.size() + " pet types removed");
        registeredPets.clear();
    }

    public int getRegisteredCount() {
        return registeredPets.size();
    }


    private String extractBasePetId(String petId) {
        if (petId == null) return null;

        for (Rarity rarity : Rarity.values()) {
            String suffix = "_" + rarity.name().toLowerCase();
            if (petId.endsWith(suffix)) {
                return petId.substring(0, petId.length() - suffix.length());
            }
        }

        return petId;
    }

    private Rarity extractRarityFromId(String petId) {
        if (petId == null) return null;

        for (Rarity rarity : Rarity.values()) {
            String suffix = "_" + rarity.name().toLowerCase();
            if (petId.endsWith(suffix)) {
                return rarity;
            }
        }

        return null;
    }

    private record PetTypeInfo(Function<Rarity, Pet> rarityConstructor, Rarity[] availableRarities) {
    }
}
