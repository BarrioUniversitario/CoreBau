package dev.blancocl.listeners;

import dev.blancocl.ServiceContainer;
import org.bukkit.event.Listener;

/**
 * Click events are sourced directly from the NPC engine (see
 * {@code NpcEngine.Handle#onClick}). This Bukkit listener is kept
 * as a registration point in case future engines emit a real Bukkit event.
 */
public final class NpcInteractListener implements Listener {

    @SuppressWarnings("unused")
    private final ServiceContainer services;

    public NpcInteractListener(ServiceContainer services) { this.services = services; }
}
