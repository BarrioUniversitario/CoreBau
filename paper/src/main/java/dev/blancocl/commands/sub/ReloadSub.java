package dev.blancocl.commands.sub;

import dev.blancocl.ServiceContainer;
import dev.blancocl.commands.SubCommand;
import dev.blancocl.commands.feedback.CommandResponder;
import org.bukkit.command.CommandSender;

import java.util.Map;

public final class ReloadSub implements SubCommand {

    private final ServiceContainer services;
    private final CommandResponder responder;

    public ReloadSub(ServiceContainer services, CommandResponder responder) {
        this.services = services; this.responder = responder;
    }

    @Override public String name()        { return "reload"; }
    @Override public String permission()  { return "npc.command.reload"; }
    @Override public String usage()       { return "/npc reload"; }
    @Override public String description() { return "Reloads configuration and NPC definitions."; }

    @Override
    public boolean execute(CommandSender sender, String[] args) {
        long start = System.currentTimeMillis();
        try {
            services.reload();
            responder.send(sender, "general.reload-success",
                    Map.of("ms", Long.toString(System.currentTimeMillis() - start)));
        } catch (Throwable t) {
            responder.send(sender, "general.reload-failed", Map.of("error", String.valueOf(t.getMessage())));
        }
        return true;
    }
}
