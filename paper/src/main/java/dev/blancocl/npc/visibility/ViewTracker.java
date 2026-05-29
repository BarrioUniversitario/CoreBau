package dev.blancocl.npc.visibility;

import dev.blancocl.npc.NpcImpl;
import org.bukkit.entity.Player;

import java.util.Collections;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/** Per-player record of which NPCs they currently see. */
public final class ViewTracker {

    private final ConcurrentHashMap<UUID, Set<NpcImpl>> byPlayer = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<UUID, double[]> lastSample = new ConcurrentHashMap<>();

    public Set<NpcImpl> visible(Player p) {
        return byPlayer.computeIfAbsent(p.getUniqueId(), id -> ConcurrentHashMap.newKeySet());
    }

    public Set<NpcImpl> visibleSnapshot(Player p) {
        Set<NpcImpl> s = byPlayer.get(p.getUniqueId());
        return s == null ? Collections.emptySet() : Set.copyOf(s);
    }

    public void release(UUID playerId) {
        byPlayer.remove(playerId);
        lastSample.remove(playerId);
    }

    public void releaseAllOf(NpcImpl npc) {
        byPlayer.values().forEach(s -> s.remove(npc));
    }

    /** Returns true if {@code current} is far enough from the last sampled position to warrant a recompute. */
    public boolean movedSinceLast(Player p, double epsilon) {
        double[] prev = lastSample.get(p.getUniqueId());
        var loc = p.getLocation();
        if (prev == null) {
            lastSample.put(p.getUniqueId(), new double[]{loc.getX(), loc.getY(), loc.getZ()});
            return true;
        }
        double dx = loc.getX() - prev[0];
        double dy = loc.getY() - prev[1];
        double dz = loc.getZ() - prev[2];
        if (dx * dx + dy * dy + dz * dz >= epsilon * epsilon) {
            prev[0] = loc.getX(); prev[1] = loc.getY(); prev[2] = loc.getZ();
            return true;
        }
        return false;
    }
}
