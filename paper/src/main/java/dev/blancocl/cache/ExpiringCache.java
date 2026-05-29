package dev.blancocl.cache;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;

import java.time.Duration;
import java.util.Optional;
import java.util.function.Function;

/** Tiny ergonomic wrapper around Caffeine. */
public final class ExpiringCache<K, V> {

    private final Cache<K, V> backing;

    public ExpiringCache(int maxSize, Duration ttl) {
        this.backing = Caffeine.newBuilder()
                .maximumSize(maxSize)
                .expireAfterWrite(ttl)
                .build();
    }

    public Optional<V> get(K key) { return Optional.ofNullable(backing.getIfPresent(key)); }
    public void put(K key, V value) { backing.put(key, value); }
    public V computeIfAbsent(K key, Function<K, V> loader) { return backing.get(key, loader); }
    public void invalidate(K key) { backing.invalidate(key); }
    public void invalidateAll() { backing.invalidateAll(); }
    public long size() { return backing.estimatedSize(); }
}
