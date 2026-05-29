package dev.blancocl.listeners;

import dev.blancocl.ServiceContainer;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.world.ChunkLoadEvent;
import org.bukkit.event.world.ChunkUnloadEvent;

/** Chunk-aware spawn/despawn — visibility worker re-evaluates NPCs near loaded chunks. */
public final class ChunkLoadListener implements Listener {

    private final ServiceContainer services;

    public ChunkLoadListener(ServiceContainer services) { this.services = services; }

    @EventHandler
    public void onLoad(ChunkLoadEvent ev) {
        services.holograms().refreshChunk(ev.getChunk());
    }

    @EventHandler public void onUnload(ChunkUnloadEvent ev) { /* worker tick will pick it up */ }
}
