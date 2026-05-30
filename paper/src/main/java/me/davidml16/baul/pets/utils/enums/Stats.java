package me.davidml16.baul.pets.utils.enums;

public enum Stats {
    HEALTH("health"),
    DEFENSE("defense"),
    SPEED("speed"),
    STRENGTH("strength"),
    CRIT_DAMAGE("crit_damage"),
    CRIT_CHANCE("crit_chance"),
    INTELLIGENCE("wisdom"),
    ABILITY_DAMAGE("ability_damage"),
    SWING_RANGE("swing_range"),
    ATTACK_SPEED("attack_speed"),
    FEROCITY("ferocity"),
    DOUBLE_HOOK_CHANCE("double_hook_chance"),
    FISHING_SPEED("fishing_speed"),
    SEA_CREATURE_CHANCE("sea_creature_chance"),
    TREASURE_CHANCE("treasure_chance"),
    FARMING_FORTUNE("farming_fortune"),
    MINING_FORTUNE("mining_fortune"),
    FORAGING_FORTUNE("foraging_fortune"),
    MINING_SPEED("mining_speed"),
    ALCHEMY_WISDOM("alchemy_wisdom"),
    CARPENTRY_WISDOM("carpentry_wisdom"),
    COMBAT_WISDOM("combat_wisdom"),
    ENCHANTING_WISDOM("enchanting_wisdom"),
    FISHING_WISDOM("fishing_wisdom"),
    FORAGING_WISDOM("foraging_wisdom"),
    MINING_WISDOM("mining_wisdom"),
    TAMING_WISDOM("taming_wisdom"),
    ;

    private final String id;

    Stats(String id) {
        this.id = id;
    }

    public String getId() {
        return id;
    }
}
