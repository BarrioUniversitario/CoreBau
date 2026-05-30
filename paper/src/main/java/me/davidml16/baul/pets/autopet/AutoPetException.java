package me.davidml16.baul.pets.autopet;

public enum AutoPetException {
    IN_COMBAT("Don't trigger if in combat"),
    IN_PVP("Don't trigger if in PvP"),
    IN_CREATIVE("Don't trigger if in creative mode"),
    LOW_HEALTH("Don't trigger if health is below 50%"),
    HAS_ACTIVE_PET("Don't trigger if already have an active pet"),
    IN_SPECIFIC_WORLD("Don't trigger in specific worlds");

    private final String description;

    AutoPetException(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }
}
