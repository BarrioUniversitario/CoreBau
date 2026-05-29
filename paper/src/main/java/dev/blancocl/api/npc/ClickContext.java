package dev.blancocl.api.npc;

import org.bukkit.entity.Player;

/** Context delivered to {@link NpcAction#run(ClickContext)} when a player clicks an NPC. */
public interface ClickContext {

    Npc       npc();
    Player    player();
    ClickType type();
}
