package dev.blancocl.skin;

import dev.blancocl.api.skin.Skin;
import dev.blancocl.api.skin.SkinSource;
import dev.blancocl.util.Threading;

import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Deduplicates concurrent skin requests for the same username — every caller
 * gets the same {@link CompletableFuture}, so we never hit Mojang twice for
 * the same name in flight.
 */
public final class SkinLoadQueue {

    private final Threading threading;
    private final MojangClient mojang;
    private final SkinCache cache;
    private final Map<String, CompletableFuture<Skin>> inflight = new ConcurrentHashMap<>();

    public SkinLoadQueue(Threading threading, MojangClient mojang, SkinCache cache) {
        this.threading = threading;
        this.mojang    = mojang;
        this.cache     = cache;
    }

    public CompletableFuture<Skin> load(String username, Skin fallback) {
        String key = username.toLowerCase();
        var cached = cache.get(key);
        if (cached.isPresent()) return CompletableFuture.completedFuture(cached.get());

        return inflight.computeIfAbsent(key, k ->
                mojang.fetchByName(username).handleAsync((opt, err) -> {
                    try {
                        if (opt != null && opt.isPresent()) {
                            cache.put(key, opt.get());
                            return opt.get();
                        }
                        return fallback != null ? fallback
                                : new Skin(username, "", "", SkinSource.FALLBACK, System.currentTimeMillis());
                    } finally {
                        inflight.remove(key);
                    }
                }, threading.skinIo()));
    }
}
