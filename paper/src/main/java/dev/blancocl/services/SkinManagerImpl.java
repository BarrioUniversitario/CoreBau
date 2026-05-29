package dev.blancocl.services;

import dev.blancocl.api.manager.SkinManager;
import dev.blancocl.api.skin.Skin;
import dev.blancocl.api.skin.SkinSource;
import dev.blancocl.skin.FallbackSkins;
import dev.blancocl.skin.SkinCache;
import dev.blancocl.skin.SkinLoadQueue;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

public final class SkinManagerImpl implements SkinManager, Service {

    private final SkinCache cache;
    private final SkinLoadQueue queue;

    public SkinManagerImpl(SkinCache cache, SkinLoadQueue queue) {
        this.cache = cache;
        this.queue = queue;
    }

    @Override
    public void enable() {}

    @Override
    public void disable() {}

    @Override
    public void reload() { cache.clear(); }

    @Override
    public CompletableFuture<Skin> fetchByName(String username) {
        return queue.load(username, FallbackSkins.STEVE);
    }

    @Override
    public Skin fromInline(@Nullable String name, String texture, String signature) {
        return new Skin(name == null ? "inline" : name, texture, signature,
                SkinSource.INLINE, System.currentTimeMillis());
    }

    @Override
    public Optional<Skin> cached(String username) { return cache.get(username); }

    @Override
    public Skin fallback() { return FallbackSkins.STEVE; }

    @Override
    public void preload(Collection<String> names) {
        for (String n : names) queue.load(n, FallbackSkins.STEVE);
    }
}
