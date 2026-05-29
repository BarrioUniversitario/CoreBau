package dev.blancocl.npc.persistence;

import dev.blancocl.api.npc.NpcType;
import org.bukkit.Location;

import java.util.List;
import java.util.Map;

/**
 * Plain data carrier representing an NPC entry in {@code npcs.yml}.
 * Lives outside the runtime types so the repository never depends on
 * the live engine or registry.
 */
public record NpcSnapshot(
        String id,
        String name,
        NpcType type,
        String skin,
        Location location,
        boolean lookAtPlayer,
        int viewDistance,
        List<HologramSnapshot> holograms,
        Map<String, List<Map<String, Object>>> actions,
        AnimationSnapshot animation,
        VisibilitySnapshot visibility
) {

    public record HologramSnapshot(double offsetY, double lineSpacing, boolean animated, List<String> lines) {}

    public record AnimationSnapshot(String idle, int intervalTicks) {}

    public record VisibilitySnapshot(String permission, String world, String serverOnline) {}
}
