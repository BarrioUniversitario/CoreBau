package dev.blancocl.packets;

import dev.blancocl.cache.ExpiringCache;

import java.time.Duration;

/**
 * Reserved for future use: prebuilt entity-metadata / equipment packets keyed by
 * NPC id + revision. Today the engine layer rebuilds packets on each send,
 * which is plenty fast at the current scale. When raw PacketEvents replaces
 * npc-lib, this cache will hold the serialised buffers.
 */
public final class PacketCache {

    private final ExpiringCache<String, byte[]> cache;

    public PacketCache(int maxEntries) {
        this.cache = new ExpiringCache<>(maxEntries, Duration.ofMinutes(10));
    }

    public byte[] get(String key) { return cache.get(key).orElse(null); }
    public void put(String key, byte[] payload) { cache.put(key, payload); }
    public void invalidate(String key) { cache.invalidate(key); }
}
