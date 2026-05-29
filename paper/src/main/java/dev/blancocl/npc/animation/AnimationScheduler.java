package dev.blancocl.npc.animation;

import dev.blancocl.npc.NpcImpl;
import dev.blancocl.util.Threading;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

/**
 * Schedules per-NPC idle animations. Tick rate is one scheduled task per NPC
 * (low cardinality — most servers have &lt;1000 animated NPCs) which keeps
 * scheduling overhead negligible.
 */
public final class AnimationScheduler {

    private final Threading threading;
    private final Map<String, ScheduledFuture<?>> tasks = new ConcurrentHashMap<>();

    public AnimationScheduler(Threading threading) { this.threading = threading; }

    public void install(NpcImpl npc, NpcAnimation animation, int intervalTicks) {
        cancel(npc.id());
        if (animation == null || animation == NpcAnimation.NONE) return;
        long periodMs = Math.max(50, intervalTicks * 50L);

        ScheduledFuture<?> task = threading.scheduled().scheduleAtFixedRate(() -> {
            try { play(npc, animation); }
            catch (Throwable ignored) {}
        }, periodMs, periodMs, TimeUnit.MILLISECONDS);
        tasks.put(npc.id(), task);
    }

    public void cancel(String npcId) {
        ScheduledFuture<?> existing = tasks.remove(npcId);
        if (existing != null) existing.cancel(false);
    }

    public void shutdown() {
        tasks.values().forEach(t -> t.cancel(false));
        tasks.clear();
    }

    private void play(NpcImpl npc, NpcAnimation a) {
        var animation = a.toEntityAnimation();
        if (animation != null) {
            npc.handle().playAnimation(animation);
        }
    }
}
