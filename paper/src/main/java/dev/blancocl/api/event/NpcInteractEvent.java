package dev.blancocl.api.event;

import dev.blancocl.api.npc.Npc;
import org.bukkit.entity.Player;
import org.bukkit.event.HandlerList;

/**
 * Fired after {@link NpcClickEvent} when at least one configured action ran.
 * Useful for analytics / cooldowns; cannot be cancelled.
 */
public final class NpcInteractEvent extends NpcEvent {

    private static final HandlerList HANDLERS = new HandlerList();
    private final Player player;
    private final int actionsExecuted;

    public NpcInteractEvent(Npc npc, Player player, int actionsExecuted) {
        super(false, npc);
        this.player = player;
        this.actionsExecuted = actionsExecuted;
    }

    public Player player()         { return player; }
    public int    actionsExecuted(){ return actionsExecuted; }

    @Override public HandlerList getHandlers() { return HANDLERS; }
    public static  HandlerList   getHandlerList() { return HANDLERS; }
}
