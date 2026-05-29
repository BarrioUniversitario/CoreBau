package dev.blancocl.services;

import dev.blancocl.api.hologram.Hologram;
import dev.blancocl.api.manager.HologramManager;
import dev.blancocl.api.npc.Npc;
import dev.blancocl.hologram.HologramImpl;
import dev.blancocl.hologram.HologramRenderer;
import dev.blancocl.hologram.HologramUpdateLoop;
import dev.blancocl.npc.NpcImpl;
import org.bukkit.Chunk;
import org.bukkit.Location;

import java.util.ArrayList;
import java.util.List;

public final class HologramManagerImpl implements HologramManager, Service {

    private final HologramRenderer renderer;
    private final HologramUpdateLoop loop;
    private final NpcManagerImpl npcs;

    public HologramManagerImpl(HologramRenderer renderer, HologramUpdateLoop loop, NpcManagerImpl npcs) {
        this.renderer = renderer;
        this.loop = loop;
        this.npcs = npcs;
    }

    @Override
    public void enable() {
        loop.start();
    }

    @Override
    public void disable() {
        loop.stop();
        renderer.shutdown();
    }

    @Override
    public void reload() {
        // Re-render every hologram so PAPI / new spacing kick in.
        for (Hologram h : all()) h.refresh();
    }

    @Override
    public Hologram attach(Npc npc, double offsetY, List<String> lines) {
        if (!(npc instanceof NpcImpl impl))
            throw new IllegalArgumentException("Foreign Npc impl: " + npc.getClass());
        HologramImpl holo = new HologramImpl(impl, renderer, offsetY, 0.28, lines);
        impl.hologramsMutable().add(holo);
        renderer.spawn(holo);
        if (containsAnimatedTags(lines)) loop.register(holo);
        return holo;
    }

    @Override
    public void detachAll(Npc npc) {
        if (!(npc instanceof NpcImpl impl)) return;
        for (Hologram h : impl.holograms()) {
            detachRendered(h);
        }
        impl.hologramsMutable().clear();
    }

    public void detach(Hologram hologram) {
        if (!(hologram instanceof HologramImpl impl)) return;
        detachRendered(impl);
        impl.owner().hologramsMutable().remove(impl);
    }

    @Override
    public List<Hologram> all() {
        List<Hologram> out = new ArrayList<>();
        for (NpcImpl npc : npcs.registry().all()) out.addAll(npc.holograms());
        return out;
    }

    public void refreshChunk(Chunk chunk) {
        for (NpcImpl npc : npcs.registry().all()) {
            Location loc = npc.location();
            if (loc.getWorld() == null || !loc.getWorld().getUID().equals(chunk.getWorld().getUID())) continue;
            if ((loc.getBlockX() >> 4) == chunk.getX() && (loc.getBlockZ() >> 4) == chunk.getZ()) {
                npc.holograms().forEach(Hologram::refresh);
            }
        }
    }

    private boolean containsAnimatedTags(List<String> lines) {
        for (String l : lines) {
            if (l.contains("<rainbow") || l.contains("<gradient") || l.contains("<#ANIM:") || l.contains("%")) return true;
        }
        return false;
    }

    private void detachRendered(Hologram hologram) {
        if (hologram instanceof HologramImpl hi) {
            hi.detach();
            loop.unregister(hi);
            renderer.despawn(hi);
        }
    }
}
