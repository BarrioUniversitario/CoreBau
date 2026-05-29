package dev.blancocl.npc.action;

import dev.blancocl.api.npc.ClickContext;
import dev.blancocl.api.npc.NpcAction;
import dev.blancocl.util.Mini;

import java.util.Map;

/** Sends a MiniMessage line to the clicking player. */
public final class MessageAction implements NpcAction {

    private final String miniMessage;

    public MessageAction(String miniMessage) { this.miniMessage = miniMessage; }

    public static MessageAction fromMap(Map<String, Object> args) {
        return new MessageAction(String.valueOf(args.getOrDefault("value", "")));
    }

    @Override
    public void run(ClickContext ctx) {
        Mini.send(ctx.player(), miniMessage);
    }
}
