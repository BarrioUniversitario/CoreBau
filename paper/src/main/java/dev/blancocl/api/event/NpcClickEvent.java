package dev.blancocl.api.event;

import dev.blancocl.api.npc.ClickType;
import dev.blancocl.api.npc.Npc;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.HandlerList;

/**
 * Fired on the main thread when a player clicks an NPC. Cancelling skips
 * the configured {@code actions:} block in {@code npcs.yml}.
 */
public final class NpcClickEvent extends NpcEvent implements Cancellable {

    private static final HandlerList HANDLERS = new HandlerList();

    private final Player    player;
    private final ClickType type;
    private boolean cancelled;

    public NpcClickEvent(Npc npc, Player player, ClickType type) {
        super(false, npc);
        this.player = player;
        this.type   = type;
    }

    public Player    player() { return player; }
    public ClickType type()   { return type;   }

    @Override public boolean isCancelled()           { return cancelled; }
    @Override public void    setCancelled(boolean c) { this.cancelled = c; }

    @Override public HandlerList getHandlers() { return HANDLERS; }
    public static  HandlerList   getHandlerList() { return HANDLERS; }
}
