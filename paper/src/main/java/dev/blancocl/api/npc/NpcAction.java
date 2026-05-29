package dev.blancocl.api.npc;

/** Pluggable click handler. Implementations must be thread-safe and non-blocking. */
@FunctionalInterface
public interface NpcAction {

    /**
     * Invoked on the Bukkit main thread immediately after a click is observed.
     * Blocking operations must be dispatched to {@code CompletableFuture.runAsync(...)}.
     */
    void run(ClickContext ctx);
}
