package dev.blancocl.listeners;

import dev.blancocl.ServiceContainer;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

public final class PlayerJoinListener implements Listener {

    private final ServiceContainer services;

    public PlayerJoinListener(ServiceContainer services) { this.services = services; }

    @EventHandler
    public void onJoin(PlayerJoinEvent ev) {
        // Trigger a visibility recompute on next tick so the player sees nearby NPCs ASAP.
        services.visibilityWorker().tracker().release(ev.getPlayer().getUniqueId());
    }
}
