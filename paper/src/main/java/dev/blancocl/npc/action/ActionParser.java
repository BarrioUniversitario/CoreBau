package dev.blancocl.npc.action;

import dev.blancocl.api.manager.NpcManager;
import dev.blancocl.api.npc.ClickType;
import dev.blancocl.api.npc.NpcAction;
import dev.blancocl.npc.NpcImpl;
import dev.blancocl.velocity.PluginMessenger;

import java.util.List;
import java.util.Map;

/** Parses the {@code actions:} block in {@code npcs.yml} into runtime {@link NpcAction}s. */
public final class ActionParser {

    private final NpcManager npcManager;
    private final PluginMessenger messenger;

    public ActionParser(NpcManager npcManager, PluginMessenger messenger) {
        this.npcManager = npcManager;
        this.messenger  = messenger;
    }

    @SuppressWarnings("unchecked")
    public void install(NpcImpl npc, ClickType type, List<?> raw) {
        if (raw == null) return;
        for (Object entry : raw) {
            if (!(entry instanceof Map<?, ?> mapRaw)) continue;
            Map<String, Object> args = (Map<String, Object>) mapRaw;
            NpcAction action = build(args);
            if (action != null) npc.actions(type).add(action);
        }
    }

    public NpcAction build(Map<String, Object> args) {
        String t = String.valueOf(args.getOrDefault("type", "")).toLowerCase();
        return switch (t) {
            case "connect", "server"   -> ConnectAction.fromMap(messenger, args);
            case "command"             -> CommandAction.fromMap(args);
            case "message", "msg"      -> MessageAction.fromMap(args);
            default -> {
                NpcManager.NpcActionFactory factory = npcManager.actionType(t);
                yield factory == null ? null : factory.create(args);
            }
        };
    }
}
