package dev.blancocl.commands.sub;

import dev.blancocl.ServiceContainer;
import dev.blancocl.commands.SubCommand;
import dev.blancocl.commands.feedback.CommandResponder;
import org.bukkit.command.CommandSender;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public final class RemoveSub implements SubCommand {

    private final ServiceContainer services;
    private final CommandResponder responder;

    public RemoveSub(ServiceContainer services, CommandResponder responder) {
        this.services = services; this.responder = responder;
    }

    @Override public String name()        { return "remove"; }
    @Override public String permission()  { return "npc.command.remove"; }
    @Override public String usage()       { return "/npc remove <id>"; }
    @Override public String description() { return "Deletes an NPC."; }

    @Override
    public boolean execute(CommandSender sender, String[] args) {
        if (args.length < 1) return false;
        String id = args[0];
        if (services.npcs().get(id).isEmpty()) {
            responder.send(sender, "general.unknown-npc", Map.of("id", id));
            return true;
        }
        services.npcs().remove(id).whenComplete((v, err) ->
                responder.send(sender, "command.remove-success", Map.of("id", id)));
        return true;
    }

    @Override
    public List<String> tabComplete(CommandSender sender, String[] args) {
        if (args.length == 1) {
            String partial = args[0].toLowerCase();
            return services.npcs().registry().all().stream()
                    .map(n -> n.id())
                    .filter(n -> n.toLowerCase().startsWith(partial))
                    .collect(Collectors.toList());
        }
        return List.of();
    }
}
