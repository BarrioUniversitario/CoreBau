package dev.blancocl.commands.sub;

import dev.blancocl.ServiceContainer;
import dev.blancocl.commands.SubCommand;
import dev.blancocl.commands.feedback.CommandResponder;
import org.bukkit.command.CommandSender;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public final class SkinSub implements SubCommand {

    private final ServiceContainer services;
    private final CommandResponder responder;

    public SkinSub(ServiceContainer services, CommandResponder responder) {
        this.services = services; this.responder = responder;
    }

    @Override public String name()        { return "skin"; }
    @Override public String permission()  { return "npc.command.skin"; }
    @Override public String usage()       { return "/npc skin <id> <username>"; }
    @Override public String description() { return "Applies a Mojang skin to an NPC."; }

    @Override
    public boolean execute(CommandSender sender, String[] args) {
        if (args.length < 2) return false;
        String id = args[0];
        String skinName = args[1];
        var npcOpt = services.npcs().get(id);
        if (npcOpt.isEmpty()) {
            responder.send(sender, "general.unknown-npc", Map.of("id", id));
            return true;
        }
        responder.send(sender, "command.skin-fetching", Map.of("skin", skinName));
        services.skins().fetchByName(skinName).whenComplete((skin, err) -> {
            if (skin == null || skin.value().isEmpty()) {
                responder.send(sender, "command.skin-not-found", Map.of("skin", skinName));
                return;
            }
            if (npcOpt.get() instanceof dev.blancocl.npc.NpcImpl impl) impl.setSkinName(skinName);
            npcOpt.get().applySkin(skin);
            services.npcs().save();
            responder.send(sender, "command.skin-applied",
                    Map.of("id", id, "skin", skinName));
        });
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
