package dev.blancocl.listeners;

import dev.blancocl.ServiceContainer;
import dev.blancocl.npc.NpcImpl;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;

public final class PlayerQuitListener implements Listener {

    private final ServiceContainer services;

    public PlayerQuitListener(ServiceContainer services) { this.services = services; }

    @EventHandler
    public void onQuit(PlayerQuitEvent ev) {
        Player player = ev.getPlayer();
        var tracker = services.visibilityWorker().tracker();
        // CRITICAL: tell every NPC the player was seeing to despawn for them. This invokes
        // npc-lib's stopTrackingPlayer(), which removes the UUID from the engine's internal
        // trackedPlayers set. Without this call, on rejoin forceTrackPlayer() is a no-op
        // (the UUID is still "tracked" from the previous session) and no spawn packets are
        // sent — the NPC is invisible to the returning player until /npc reload destroys and
        // rebuilds every handle. This bug shows up whenever a player switches servers via a
        // ConnectAction NPC click and comes back.
        for (NpcImpl npc : tracker.visibleSnapshot(player)) {
            try { npc.handle().despawn(player); } catch (Throwable ignored) {}
        }
        tracker.release(player.getUniqueId());
    }
}
