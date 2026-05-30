package me.davidml16.baul.pets.utils.enums;

public enum PetType {
    MINING(XPSource.MINING),
    COMBAT(XPSource.COMBAT),
    FARMING(XPSource.FARMING),
    FORAGING(XPSource.WOODCUTTING),
    FISHING(XPSource.FISHING),
    ALCHEMY(XPSource.ALCHEMY),
    ENCHANTING(XPSource.ENCHANTING),
    TAMING(null);

    private final XPSource source;

    PetType(XPSource source) {
        this.source = source;
    }

    public XPSource getSource() {
        return source;
    }
}
