package dev.blancocl.commands.sub;

import dev.blancocl.ServiceContainer;
import dev.blancocl.api.npc.NpcType;
import dev.blancocl.commands.SubCommand;
import dev.blancocl.commands.feedback.CommandResponder;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public final class CreateSub implements SubCommand {

    private final ServiceContainer services;
    private final CommandResponder responder;

    public CreateSub(ServiceContainer services, CommandResponder responder) {
        this.services = services; this.responder = responder;
    }

    @Override public String name()        { return "create"; }
    @Override public String permission()  { return "npc.command.create"; }
    @Override public String usage()       { return "/npc create <id> [type]"; }
    @Override public String description() { return "Creates a new NPC at your location."; }

    @Override
    public boolean execute(CommandSender sender, String[] args) {
        if (!(sender instanceof Player player)) { responder.send(sender, "general.player-only"); return true; }
        if (args.length < 1) { responder.send(sender, "command.help-header"); return true; }

        String id = args[0];
        NpcType type = NpcType.PLAYER;
        if (args.length >= 2) {
            try { type = NpcType.valueOf(args[1].toUpperCase()); }
            catch (IllegalArgumentException e) { type = NpcType.PLAYER; }
        }
        services.npcs().create(id, type, player.getLocation()).whenComplete((npc, err) -> {
            if (err != null) {
                responder.send(sender, "general.internal-error");
                return;
            }
            responder.send(sender, "command.create-success",
                    Map.of("id", id, "type", npc.type().name()));
            services.npcs().save();
        });
        return true;
    }

    @Override
    public List<String> tabComplete(CommandSender sender, String[] args) {
        if (args.length == 2) {
            return Arrays.stream(NpcType.values()).map(Enum::name)
                    .filter(n -> n.startsWith(args[1].toUpperCase()))
                    .collect(Collectors.toList());
        }
        return List.of();
    }
}
