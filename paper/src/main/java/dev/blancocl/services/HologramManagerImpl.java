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
        // Diferimos 60 ticks (~3s) para dar tiempo al NpcManager async-load
        // a poblar el registry. Si barremos antes, eliminariamos TDs que
        // estan a punto de ser registrados.
        renderer.sweepOrphansLater(60L);
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
        // Sweep tras un reload por si el reload del NpcManager dejo TDs sin trackear.
        renderer.sweepOrphans();
    }

    @Override
    public Hologram attach(Npc npc, double offsetY, List<String> lines) {
        if (!(npc instanceof NpcImpl impl))
            throw new IllegalArgumentException("Foreign Npc impl: " + npc.getClass());
        HologramImpl holo = new HologramImpl(impl, renderer, offsetY, 0.28, lines);
        impl.hologramsMutable().add(holo);
        renderer.spawn(holo);
        // Con DecentHolograms como backend, la animacion la maneja DH internamente
        // (placeholders %anim_x%, secuencias #ANIMATION:name#, etc). Ya no
        // necesitamos nuestro propio loop por-frame, asi que NO registramos.
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

    /**
     * Barrido de huerfanos por tag PDC: elimina TODO TextDisplay tagueado
     * con corebau:hologram_id cuyo UUID no esta en el map de tracking.
     * Cubre el caso "removi el npc pero el hologram quedo": despues de un
     * crash/reload anterior, un TD que se perdio del map no se eliminaba.
     */
    public int cleanupTaggedOrphans() {
        return renderer.sweepOrphansBlocking();
    }

    /**
     * Limpieza one-shot para huerfanos legacy. Reune las ubicaciones
     * aproximadas (NPC + offsetY) de cada hologram registrado y delega al
     * renderer para eliminar TextDisplays no trackeados en esa zona.
     */
    public int cleanupLegacyOrphans(double radius, double verticalRange) {
        List<Location> anchors = new ArrayList<>();
        for (NpcImpl npc : npcs.registry().all()) {
            Location base = npc.location();
            if (base.getWorld() == null) continue;
            // Cada hologram tiene su propio offsetY; si el NPC no tiene
            // ninguno aun, usamos el default ~2.3 para cubrir el caso de un
            // hologram antiguo que ya no esta vinculado al NPC.
            if (npc.holograms().isEmpty()) {
                anchors.add(base.clone().add(0, 2.3, 0));
            } else {
                for (Hologram h : npc.holograms()) {
                    anchors.add(base.clone().add(0, h.offsetY(), 0));
                }
            }
        }
        return renderer.sweepLegacyOrphansNear(anchors, radius, verticalRange);
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
