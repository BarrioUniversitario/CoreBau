package dev.blancocl.api.event;

import dev.blancocl.api.npc.Npc;
import org.bukkit.entity.Player;
import org.bukkit.event.HandlerList;

/** Fired (always async) when an NPC becomes visible to a player. */
public final class NpcSpawnEvent extends NpcEvent {

    private static final HandlerList HANDLERS = new HandlerList();
    private final Player viewer;

    public NpcSpawnEvent(Npc npc, Player viewer) {
        super(true, npc);
        this.viewer = viewer;
    }

    public Player viewer() { return viewer; }

    @Override public HandlerList getHandlers() { return HANDLERS; }
    public static  HandlerList   getHandlerList() { return HANDLERS; }
}
