package dev.blancocl.npc.action;

import dev.blancocl.api.npc.ClickContext;
import dev.blancocl.api.npc.NpcAction;
import dev.blancocl.velocity.PluginMessenger;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;

import java.util.Map;

/** Sends the clicking player to {@code server} via the Velocity proxy. */
public final class ConnectAction implements NpcAction {

    private static final Component NO_SERVER = MiniMessage.miniMessage().deserialize("<red>This NPC is not connected to any server.</red>");

    private final PluginMessenger messenger;
    private final String server;

    public ConnectAction(PluginMessenger messenger, String server) {
        this.messenger = messenger;
        this.server    = server;
    }

    public static NpcAction fromMap(PluginMessenger messenger, Map<String, Object> args) {
        return new ConnectAction(messenger, String.valueOf(args.getOrDefault("server", "")));
    }

    @Override
    public void run(ClickContext ctx) {
        if (server == null || server.isEmpty()) {
            ctx.player().sendMessage(NO_SERVER);
            return;
        }
        messenger.connect(ctx.player(), server);
    }
}
