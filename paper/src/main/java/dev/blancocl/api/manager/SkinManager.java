package dev.blancocl.api.manager;

import dev.blancocl.api.skin.Skin;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;
import java.util.concurrent.CompletableFuture;

public interface SkinManager {

    /** Resolve a skin by Mojang username. Cached in memory + on disk. Never blocks. */
    CompletableFuture<Skin> fetchByName(String username);

    /** Build a skin from a raw texture / signature pair (e.g. supplied in config). */
    Skin fromInline(@Nullable String name, String texture, String signature);

    /** Cache lookup only — no I/O. */
    Optional<Skin> cached(String username);

    /** Returned when {@link #fetchByName(String)} fails or is null. */
    Skin fallback();

    /** Eagerly load a list of names into the cache. */
    void preload(java.util.Collection<String> names);
}
