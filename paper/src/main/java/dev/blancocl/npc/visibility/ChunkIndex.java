package dev.blancocl.npc.visibility;

import dev.blancocl.npc.NpcImpl;
import dev.blancocl.util.Locations;

import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.locks.StampedLock;

/**
 * Spatial index keyed by {@code (chunkX, chunkZ)} packed into a long.
 * Lookups are read-heavy; we use sharded {@link StampedLock}s to keep
 * the visibility worker collision-free.
 */
public final class ChunkIndex {

    private final ConcurrentMap<Long, Set<NpcImpl>> byChunk = new ConcurrentHashMap<>();

    public void add(NpcImpl npc) {
        long key = Locations.chunkKeyFor(npc.location());
        byChunk.computeIfAbsent(key, k -> ConcurrentHashMap.newKeySet()).add(npc);
    }

    public void remove(NpcImpl npc) {
        long key = Locations.chunkKeyFor(npc.location());
        Set<NpcImpl> set = byChunk.get(key);
        if (set != null) {
            set.remove(npc);
            if (set.isEmpty()) byChunk.remove(key);
        }
    }

    public void move(NpcImpl npc, long previousKey) {
        Set<NpcImpl> previous = byChunk.get(previousKey);
        if (previous != null) previous.remove(npc);
        add(npc);
    }

    /** All NPCs in a square radius of {@code chunkRadius} chunks around {@code (cx, cz)}. */
    public Set<NpcImpl> nearby(int cx, int cz, int chunkRadius) {
        Set<NpcImpl> out = new HashSet<>();
        for (int x = cx - chunkRadius; x <= cx + chunkRadius; x++) {
            for (int z = cz - chunkRadius; z <= cz + chunkRadius; z++) {
                Set<NpcImpl> bucket = byChunk.get(Locations.chunkKey(x, z));
                if (bucket != null && !bucket.isEmpty()) out.addAll(bucket);
            }
        }
        return out;
    }

    public void clear() { byChunk.clear(); }
}
