package dev.blancocl.commands.sub;

import dev.blancocl.ServiceContainer;
import dev.blancocl.commands.SubCommand;
import dev.blancocl.commands.feedback.CommandResponder;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public final class MoveSub implements SubCommand {

    private final ServiceContainer services;
    private final CommandResponder responder;

    public MoveSub(ServiceContainer services, CommandResponder responder) {
        this.services = services; this.responder = responder;
    }

    @Override public String name()        { return "move"; }
    @Override public String permission()  { return "npc.command.move"; }
    @Override public String usage()       { return "/npc move <id>"; }
    @Override public String description() { return "Moves the NPC to your current location."; }

    @Override
    public boolean execute(CommandSender sender, String[] args) {
        if (!(sender instanceof Player player)) { responder.send(sender, "general.player-only"); return true; }
        if (args.length < 1) return false;
        String id = args[0];
        services.npcs().get(id).ifPresentOrElse(
                npc -> npc.teleport(player.getLocation()).thenRun(() -> {
                    services.npcs().save();
                    responder.send(sender, "command.move-success", Map.of("id", id));
                }),
                () -> responder.send(sender, "general.unknown-npc", Map.of("id", id)));
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
