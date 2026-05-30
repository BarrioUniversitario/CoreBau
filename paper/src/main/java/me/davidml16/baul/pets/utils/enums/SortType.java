package me.davidml16.baul.pets.utils.enums;

public enum SortType {
    DEFAULT("Default"),
    ALPHABETICAL("Alphabetical"),
    PET_EXP("Pet Exp"),
    SKILL("Skill");

    private final String displayName;

    SortType(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }

    public SortType next() {
        int nextOrdinal = (this.ordinal() + 1) % values().length;
        return values()[nextOrdinal];
    }

    public SortType previous() {
        int prevOrdinal = (this.ordinal() - 1 + values().length) % values().length;
        return values()[prevOrdinal];
    }
}
