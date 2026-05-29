package dev.blancocl.listeners;

import dev.blancocl.ServiceContainer;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerMoveEvent;

/**
 * No-op on PlayerMove — the visibility worker polls positions on its own
 * cadence to avoid amplification under move-spam. Kept as a hook for
 * future logic (e.g. instant despawn when leaving a world).
 */
public final class PlayerMoveListener implements Listener {

    @SuppressWarnings("unused")
    private final ServiceContainer services;

    public PlayerMoveListener(ServiceContainer services) { this.services = services; }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onMove(PlayerMoveEvent ev) {
        // intentional no-op
    }
}
