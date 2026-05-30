package me.davidml16.baul.pets.utils.enums;

public enum VisibilityType {
    ALL("All Pets Visible"),
    NONE("Hide All Pets"),
    OWN("Show Only Your Pet");

    private final String displayName;

    VisibilityType(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }

    public VisibilityType next() {
        int nextOrdinal = (this.ordinal() + 1) % values().length;
        return values()[nextOrdinal];
    }

    public VisibilityType previous() {
        int prevOrdinal = (this.ordinal() - 1 + values().length) % values().length;
        return values()[prevOrdinal];
    }
}
