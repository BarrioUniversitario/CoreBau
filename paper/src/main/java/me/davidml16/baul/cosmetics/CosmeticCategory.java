package me.davidml16.baul.cosmetics;

public enum CosmeticCategory {
    TRAIL("trail"),
    HAT("hat"),
    JOIN_EFFECT("join"),
    EMOTE("emote"),
    PET("pet");

    private final String id;

    CosmeticCategory(String id) {
        this.id = id;
    }

    public String getId() {
        return id;
    }

    public static CosmeticCategory fromId(String id) {
        if (id == null) return null;
        for (CosmeticCategory c : values()) {
            if (c.id.equalsIgnoreCase(id)) return c;
        }
        return null;
    }
}
