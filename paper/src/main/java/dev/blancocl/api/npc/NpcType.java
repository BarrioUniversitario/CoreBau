package dev.blancocl.api.npc;

/**
 * Visual archetype of an NPC. Only {@link #PLAYER} supports the full skin pipeline;
 * other types render their corresponding entity model (no skin).
 */
public enum NpcType {
    PLAYER,
    VILLAGER,
    ZOMBIE,
    SKELETON,
    ARMOR_STAND
}
