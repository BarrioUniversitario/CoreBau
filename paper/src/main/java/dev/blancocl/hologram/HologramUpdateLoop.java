package dev.blancocl.hologram;

import dev.blancocl.config.PluginConfig;
import dev.blancocl.util.Threading;

import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

/** Periodic tick to push fresh frames to animated holograms / PAPI placeholders. */
public final class HologramUpdateLoop {

    private final Threading threading;
    private final PluginConfig cfg;
    private final HologramRenderer renderer;
    private final CopyOnWriteArrayList<HologramImpl> animated = new CopyOnWriteArrayList<>();

    private volatile ScheduledFuture<?> task;

    public HologramUpdateLoop(Threading threading, PluginConfig cfg, HologramRenderer renderer) {
        this.threading = threading;
        this.cfg = cfg;
        this.renderer = renderer;
    }

    public void register(HologramImpl holo) { animated.addIfAbsent(holo); }

    public void unregister(HologramImpl holo) { animated.remove(holo); }

    public void start() {
        long ms = cfg.hologramTickMs();
        task = threading.scheduled().scheduleAtFixedRate(this::tick, ms, ms, TimeUnit.MILLISECONDS);
    }

    public void stop() {
        if (task != null) task.cancel(false);
        task = null;
    }

    private void tick() {
        for (HologramImpl h : animated) {
            try {
                for (HologramImpl.LineImpl line : h.internalLines()) line.advanceFrame();
                renderer.broadcastUpdate(h);
            } catch (Throwable ignored) {}
        }
    }
}
