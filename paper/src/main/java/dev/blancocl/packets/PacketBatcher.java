package dev.blancocl.packets;

import dev.blancocl.config.PluginConfig;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Coalesces packet sends per-player per-tick. Currently a passthrough hook —
 * the engine layer already batches internally. Kept so future changes
 * (e.g. raw PacketEvents path) have a single place to plug in.
 */
public final class PacketBatcher {

    private final Plugin plugin;
    private final PluginConfig cfg;
    private final Map<UUID, Deque<Runnable>> queues = new HashMap<>();

    public PacketBatcher(Plugin plugin, PluginConfig cfg) {
        this.plugin = plugin;
        this.cfg = cfg;
    }

    public synchronized void enqueue(Player p, Runnable send) {
        if (!cfg.packetBatchingEnabled()) {
            Bukkit.getScheduler().runTask(plugin, send);
            return;
        }
        var q = queues.computeIfAbsent(p.getUniqueId(), id -> new ArrayDeque<>());
        q.add(send);
        if (q.size() == 1) {
            // Schedule a flush at the next tick.
            Bukkit.getScheduler().runTaskLater(plugin, () -> flush(p.getUniqueId()), 1L);
        }
    }

    private synchronized void flush(UUID id) {
        Deque<Runnable> q = queues.remove(id);
        if (q == null) return;
        int budget = cfg.maxBatchSize();
        Runnable r;
        while (budget-- > 0 && (r = q.poll()) != null) {
            try { r.run(); } catch (Throwable ignored) {}
        }
        if (!q.isEmpty()) queues.put(id, q); // overflow — next flush picks up
    }
}
