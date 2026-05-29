package dev.blancocl.api.event;

import dev.blancocl.api.npc.Npc;
import org.bukkit.event.Event;

/** Shared base for NPC lifecycle events. */
public abstract class NpcEvent extends Event {

    protected final Npc npc;

    protected NpcEvent(boolean async, Npc npc) {
        super(async);
        this.npc = npc;
    }

    public Npc npc() { return npc; }
}
