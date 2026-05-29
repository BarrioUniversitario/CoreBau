package dev.blancocl.velocity;

import dev.blancocl.cache.ExpiringCache;
import dev.blancocl.config.PluginConfig;
import dev.blancocl.util.Threading;

import java.time.Duration;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

/** Periodically refreshes player counts for every server referenced by an NPC. */
public final class ServerStateCache {

    private final Threading threading;
    private final PluginMessenger messenger;
    private final PluginConfig cfg;
    private final ExpiringCache<String, Integer> playerCounts;
    private final Set<String> tracked = java.util.concurrent.ConcurrentHashMap.newKeySet();

    private volatile ScheduledFuture<?> task;

    public ServerStateCache(Threading threading, PluginMessenger messenger, PluginConfig cfg) {
        this.threading = threading;
        this.messenger = messenger;
        this.cfg = cfg;
        this.playerCounts = new ExpiringCache<>(cfg.velocityStateMax(),
                Duration.ofMillis(Math.max(cfg.velocityPingTtlMs(), 1000)));
    }

    public void enable() {
        long ms = cfg.velocityStateRefreshMs();
        task = threading.scheduled().scheduleAtFixedRate(this::refresh, ms, ms, TimeUnit.MILLISECONDS);
    }

    public void disable() {
        if (task != null) task.cancel(false);
        task = null;
    }

    public void track(String server) {
        if (server != null && !server.isEmpty()) tracked.add(server);
    }

    public Optional<Integer> cachedCount(String server) {
        return playerCounts.get(server);
    }

    public Set<String> tracked() { return new HashSet<>(tracked); }

    private void refresh() {
        for (String server : tracked) {
            messenger.playerCount(server).thenAccept(count -> {
                if (count >= 0) playerCounts.put(server, count);
            });
        }
    }
}
