package me.davidml16.baul.pets.autopet;

import me.davidml16.baul.pets.pet.Pet;

import java.util.HashSet;
import java.util.Set;

/**
 * Represents a session for creating an AutoPet rule.
 * Tracks the player's progress through the rule creation process.
 */
public class AutoPetRuleCreationSession {

    private TriggerType triggerType;
    private Pet selectedPet;
    private final Set<AutoPetException> exceptions;

    public AutoPetRuleCreationSession() {
        this.exceptions = new HashSet<>();
    }

    public TriggerType getTriggerType() {
        return triggerType;
    }

    public void setTriggerType(TriggerType triggerType) {
        this.triggerType = triggerType;
    }

    public Pet getSelectedPet() {
        return selectedPet;
    }

    public void setSelectedPet(Pet pet) {
        this.selectedPet = pet;
    }

    public Set<AutoPetException> getExceptions() {
        return new HashSet<>(exceptions);
    }

    public void addException(AutoPetException exception) {
        exceptions.add(exception);
    }

    public void removeException(AutoPetException exception) {
        exceptions.remove(exception);
    }

    public boolean hasException(AutoPetException exception) {
        return exceptions.contains(exception);
    }

    public boolean isComplete() {
        return triggerType != null && selectedPet != null;
    }
}
