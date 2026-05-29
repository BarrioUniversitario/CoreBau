package dev.blancocl.api.manager;

import dev.blancocl.api.npc.Npc;
import dev.blancocl.api.npc.NpcAction;
import dev.blancocl.api.npc.NpcType;
import org.bukkit.Location;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

public interface NpcManager {

    /**
     * Create and register a new NPC. The returned future completes once the NPC
     * is registered with the engine; spawning happens lazily on per-player visibility.
     */
    CompletableFuture<Npc> create(String id, NpcType type, Location location);

    Optional<Npc> get(String id);

    Collection<Npc> list();

    /** Remove the NPC and despawn it from every viewer. */
    CompletableFuture<Void> remove(String id);

    /** Persist current state to disk asynchronously. */
    CompletableFuture<Void> save();

    /**
     * Register a custom action type so it can be referenced from {@code npcs.yml}:
     * {@code {type: my-action, ...}}.
     */
    void registerActionType(String name, NpcActionFactory factory);

    @Nullable NpcActionFactory actionType(String name);

    @FunctionalInterface
    interface NpcActionFactory {
        NpcAction create(java.util.Map<String, Object> args);
    }
}
