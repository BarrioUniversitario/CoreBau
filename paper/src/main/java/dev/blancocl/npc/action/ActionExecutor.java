package dev.blancocl.npc.action;

import dev.blancocl.api.event.NpcClickEvent;
import dev.blancocl.api.event.NpcInteractEvent;
import dev.blancocl.api.npc.ClickContext;
import dev.blancocl.api.npc.ClickType;
import dev.blancocl.api.npc.Npc;
import dev.blancocl.npc.NpcImpl;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

/** Dispatches click events through the Bukkit event bus, then runs configured actions. */
public final class ActionExecutor {

    public void execute(NpcImpl npc, Player player, ClickType type) {
        NpcClickEvent ev = new NpcClickEvent(npc, player, type);
        Bukkit.getPluginManager().callEvent(ev);
        if (ev.isCancelled()) return;

        ClickContext ctx = new ImmutableCtx(npc, player, type);
        int ran = npc.actions(type).dispatch(ctx);
        if (ran > 0) {
            Bukkit.getPluginManager().callEvent(new NpcInteractEvent(npc, player, ran));
        }
    }

    private record ImmutableCtx(Npc npc, Player player, ClickType type) implements ClickContext {}
}
