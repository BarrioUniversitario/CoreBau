package dev.blancocl.commands.sub;

import dev.blancocl.ServiceContainer;
import dev.blancocl.api.npc.ClickType;
import dev.blancocl.commands.SubCommand;
import dev.blancocl.commands.feedback.CommandResponder;
import dev.blancocl.npc.NpcImpl;
import dev.blancocl.npc.action.ConnectAction;
import org.bukkit.command.CommandSender;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/** Binds an NPC's left-click to a Velocity {@code Connect} action. */
public final class ServerSub implements SubCommand {

    private final ServiceContainer services;
    private final CommandResponder responder;

    public ServerSub(ServiceContainer services, CommandResponder responder) {
        this.services = services; this.responder = responder;
    }

    @Override public String name()        { return "server"; }
    @Override public String permission()  { return "npc.command.server"; }
    @Override public String usage()       { return "/npc server <id> <serverName>"; }
    @Override public String description() { return "Sets the target Velocity server for an NPC's click."; }

    @Override
    public boolean execute(CommandSender sender, String[] args) {
        if (args.length < 2) return false;
        String id = args[0];
        String server = args[1];
        var opt = services.npcs().get(id);
        if (opt.isEmpty()) {
            responder.send(sender, "general.unknown-npc", Map.of("id", id));
            return true;
        }
        if (opt.get() instanceof NpcImpl npc) {
            npc.actions(ClickType.LEFT).clear();
            npc.actions(ClickType.RIGHT).clear();
            ConnectAction action = new ConnectAction(services.velocity().messenger(), server);
            npc.registerAction(ClickType.LEFT, action);
            npc.registerAction(ClickType.RIGHT, action);

            // Mirror into round-trip state so saves restore this on next boot.
            java.util.Map<String, Object> entry = new java.util.LinkedHashMap<>();
            entry.put("type", "connect");
            entry.put("server", server);
            npc.actionsRaw().put("left-click",  java.util.List.of(entry));
            npc.actionsRaw().put("right-click", java.util.List.of(entry));
        }
        services.npcs().save();
        responder.send(sender, "command.server-set", Map.of("id", id, "server", server));
        return true;
    }

    @Override
    public List<String> tabComplete(CommandSender sender, String[] args) {
        if (args.length == 1) {
            String partial = args[0].toLowerCase();
            return services.npcs().registry().all().stream().map(n -> n.id())
                    .filter(n -> n.toLowerCase().startsWith(partial)).collect(Collectors.toList());
        }
        return List.of();
    }
}
