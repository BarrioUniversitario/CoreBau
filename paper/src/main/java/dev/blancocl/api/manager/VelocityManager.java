package dev.blancocl.api.manager;

import org.bukkit.entity.Player;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

/**
 * Velocity / proxy integration over the {@code BungeeCord} plugin-message channel.
 * All calls are non-blocking. Methods that query the proxy return futures that
 * resolve when the proxy sends a {@code response} subchannel packet back.
 */
public interface VelocityManager {

    /** Whether plugin-messaging has produced any response in the last TTL window. */
    boolean proxyDetected();

    /** Send {@code player} to {@code serverName} via the proxy. */
    void connect(Player player, String serverName);

    /** Number of players on a backend (cached, TTL'd). */
    CompletableFuture<Integer> playerCount(String serverName);

    /** Total players on the proxy. */
    CompletableFuture<Integer> globalPlayerCount();

    /** All servers known to the proxy. */
    CompletableFuture<List<String>> servers();

    /** Cached snapshot of a server's player count, if any. */
    Optional<Integer> cachedPlayerCount(String serverName);
}
