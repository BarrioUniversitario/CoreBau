package dev.blancocl.services;

/**
 * Lifecycle contract for every plugin service. Services are constructed
 * during plugin enable and disposed in reverse order on disable.
 */
public interface Service {

    /** Wire up async pools, register listeners, prepare caches. */
    void enable() throws Exception;

    /** Cancel schedulers, flush state, release resources. Must be idempotent. */
    void disable();

    /**
     * Re-read configuration and re-apply derived state. Called by {@code /npc reload}.
     * <p>Default impl is a no-op; services that hold cached config should override.</p>
     */
    default void reload() {}
}
