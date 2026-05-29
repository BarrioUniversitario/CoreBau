package dev.blancocl.commands.sub;

import dev.blancocl.ServiceContainer;
import dev.blancocl.commands.SubCommand;
import dev.blancocl.commands.feedback.CommandResponder;
import org.bukkit.command.CommandSender;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Generic property setter. Usage: {@code /npc edit <id> <property> <value>}.
 * Supported properties: {@code look}, {@code view-distance}.
 */
public final class EditSub implements SubCommand {

    private final ServiceContainer services;
    private final CommandResponder responder;

    public EditSub(ServiceContainer services, CommandResponder responder) {
        this.services = services; this.responder = responder;
    }

    @Override public String name()        { return "edit"; }
    @Override public String permission()  { return "npc.command.edit"; }
    @Override public String usage()       { return "/npc edit <id> <look|view-distance|name> <value>"; }
    @Override public String description() { return "Edits a property of an NPC."; }

    @Override
    public boolean execute(CommandSender sender, String[] args) {
        if (args.length < 3) return false;
        String id = args[0];
        var opt = services.npcs().get(id);
        if (opt.isEmpty()) {
            responder.send(sender, "general.unknown-npc", Map.of("id", id));
            return true;
        }
        var npc = opt.get();
        String prop = args[1].toLowerCase();
        String value = args[2];

        switch (prop) {
            case "name" -> {
                npc.setName(value);
                responder.send(sender, "command.name-set", Map.of("id", id, "name", value));
            }
            case "look" -> npc.setLookAtPlayer(Boolean.parseBoolean(value));
            case "view-distance", "view" -> {
                try { npc.setViewDistance(Integer.parseInt(value)); }
                catch (NumberFormatException e) { return false; }
            }
            default -> { return false; }
        }
        services.npcs().save();
        return true;
    }

    @Override
    public List<String> tabComplete(CommandSender sender, String[] args) {
        if (args.length == 1) {
            String partial = args[0].toLowerCase();
            return services.npcs().registry().all().stream().map(n -> n.id())
                    .filter(n -> n.toLowerCase().startsWith(partial)).collect(Collectors.toList());
        }
        if (args.length == 2) return List.of("look", "view-distance", "name");
        if (args.length == 3 && args[1].equalsIgnoreCase("look")) return List.of("true", "false");
        return List.of();
    }
}
