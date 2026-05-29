package dev.blancocl.npc.visibility;

import dev.blancocl.api.event.NpcDespawnEvent;
import dev.blancocl.api.event.NpcSpawnEvent;
import dev.blancocl.config.PluginConfig;
import dev.blancocl.npc.NpcImpl;
import dev.blancocl.util.Profiler;
import dev.blancocl.util.Threading;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

/**
 * Periodic async pass deciding which NPCs each online player should see.
 * Decisions happen off-thread; the resulting spawn/despawn calls bounce back
 * to main inside {@link dev.blancocl.npc.engine.NpcEngine.Handle}.
 */
public final class VisibilityWorker {

    private final Plugin plugin;
    private final Threading threading;
    private final PluginConfig cfg;
    private final Profiler profiler;
    private final ChunkIndex chunks = new ChunkIndex();
    private final ViewTracker tracker = new ViewTracker();

    private volatile ScheduledFuture<?> task;

    public VisibilityWorker(Plugin plugin, Threading threading, PluginConfig cfg, Profiler profiler) {
        this.plugin    = plugin;
        this.threading = threading;
        this.cfg       = cfg;
        this.profiler  = profiler;
    }

    public ChunkIndex chunks()  { return chunks; }
    public ViewTracker tracker(){ return tracker; }

    public void start() {
        long ms = cfg.workerIntervalMs();
        task = threading.scheduled().scheduleAtFixedRate(this::tickSafely, ms, ms, TimeUnit.MILLISECONDS);
    }

    public void stop() {
        if (task != null) task.cancel(false);
        task = null;
    }

    private void tickSafely() {
        try { tick(); }
        catch (Throwable t) { plugin.getLogger().warning("VisibilityWorker tick failed: " + t); }
    }

    private void tick() {
        try (var __ = profiler.time("visibility.tick")) {
            // Take an immutable snapshot of online players on main, then process async.
            threading.supplyMain(() -> Bukkit.getOnlinePlayers().toArray(new Player[0]))
                    .thenAcceptAsync(this::processBatch, threading.visibility());
        }
    }

    private void processBatch(Player[] players) {
        double epsilon = cfg.movementEpsilon();
        int chunkRadius = Math.max(1, (cfg.maxVisiblePerPlayer() > 0 ? 8 : 4));

        for (Player p : players) {
            if (!p.isOnline()) continue;
            if (!tracker.movedSinceLast(p, epsilon) && tracker.visible(p).size() > 0) {
                // Cheap path: no significant movement, keep current view set.
                continue;
            }

            var loc = p.getLocation();
            int cx = loc.getBlockX() >> 4;
            int cz = loc.getBlockZ() >> 4;
            Set<NpcImpl> candidates = chunks.nearby(cx, cz, chunkRadius);

            Set<NpcImpl> shouldSee = new HashSet<>();
            int seen = 0;
            int cap = cfg.maxVisiblePerPlayer();

            for (NpcImpl npc : candidates) {
                if (cap > 0 && seen >= cap) break;
                if (!sameWorld(p, npc)) continue;
                double dist = npc.location().distance(loc);
                if (dist <= npc.viewDistance()) {
                    shouldSee.add(npc);
                    seen++;
                }
            }

            applyDiff(p, shouldSee);
        }
    }

    private boolean sameWorld(Player p, NpcImpl n) {
        var w = n.location().getWorld();
        return w != null && w.getUID().equals(p.getWorld().getUID());
    }

    private void applyDiff(Player p, Set<NpcImpl> shouldSee) {
        Set<NpcImpl> current = tracker.visible(p);
        // To spawn: shouldSee - current
        for (NpcImpl npc : shouldSee) {
            if (current.add(npc)) {
                npc.handle().spawn(p);
                Bukkit.getPluginManager().callEvent(new NpcSpawnEvent(npc, p));
                profiler.incr("visibility.spawn");
            }
        }
        // To despawn: current - shouldSee
        for (NpcImpl npc : Set.copyOf(current)) {
            if (!shouldSee.contains(npc)) {
                current.remove(npc);
                npc.handle().despawn(p);
                Bukkit.getPluginManager().callEvent(new NpcDespawnEvent(npc, p));
                profiler.incr("visibility.despawn");
            }
        }
    }
}
