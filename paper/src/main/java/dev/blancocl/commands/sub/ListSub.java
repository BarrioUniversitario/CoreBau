package dev.blancocl.commands.sub;

import dev.blancocl.ServiceContainer;
import dev.blancocl.commands.SubCommand;
import dev.blancocl.commands.feedback.CommandResponder;
import org.bukkit.command.CommandSender;

import java.util.Locale;
import java.util.Map;

public final class ListSub implements SubCommand {

    private final ServiceContainer services;
    private final CommandResponder responder;

    public ListSub(ServiceContainer services, CommandResponder responder) {
        this.services = services; this.responder = responder;
    }

    @Override public String name()        { return "list"; }
    @Override public String permission()  { return "npc.command.list"; }
    @Override public String usage()       { return "/npc list"; }
    @Override public String description() { return "Lists every registered NPC."; }

    @Override
    public boolean execute(CommandSender sender, String[] args) {
        var all = services.npcs().registry().all();
        if (all.isEmpty()) {
            responder.send(sender, "command.list-empty");
            return true;
        }
        responder.send(sender, "command.list-header", Map.of("count", Integer.toString(all.size())));
        for (var n : all) {
            var loc = n.location();
            responder.send(sender, "command.list-line", Map.of(
                    "id",    n.id(),
                    "type",  n.type().name().toLowerCase(Locale.ROOT),
                    "world", loc.getWorld() == null ? "?" : loc.getWorld().getName(),
                    "x",     fmt(loc.getX()),
                    "y",     fmt(loc.getY()),
                    "z",     fmt(loc.getZ())));
        }
        return true;
    }

    private static String fmt(double d) { return String.format(Locale.ROOT, "%.2f", d); }
}
