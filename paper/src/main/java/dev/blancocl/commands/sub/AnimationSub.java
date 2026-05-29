package dev.blancocl.commands.sub;

import dev.blancocl.ServiceContainer;
import dev.blancocl.commands.SubCommand;
import dev.blancocl.commands.feedback.CommandResponder;
import dev.blancocl.npc.NpcImpl;
import dev.blancocl.npc.animation.NpcAnimation;
import org.bukkit.command.CommandSender;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public final class AnimationSub implements SubCommand {

    private final ServiceContainer services;
    private final CommandResponder responder;

    public AnimationSub(ServiceContainer services, CommandResponder responder) {
        this.services = services; this.responder = responder;
    }

    @Override public String name()        { return "animation"; }
    @Override public String permission()  { return "npc.command.animation"; }
    @Override public String usage()       { return "/npc animation <id> <NONE|WAVE|SWING|EMOTE_NOD> [interval-ticks]"; }
    @Override public String description() { return "Sets an NPC's idle animation."; }

    @Override
    public boolean execute(CommandSender sender, String[] args) {
        if (args.length < 2) return false;
        String id = args[0];
        var opt = services.npcs().get(id);
        if (opt.isEmpty()) {
            responder.send(sender, "general.unknown-npc", Map.of("id", id));
            return true;
        }
        NpcAnimation anim;
        try { anim = NpcAnimation.valueOf(args[1].toUpperCase()); }
        catch (IllegalArgumentException e) { return false; }
        int interval = 200;
        if (args.length >= 3) {
            try { interval = Integer.parseInt(args[2]); }
            catch (NumberFormatException e) { return false; }
        }
        if (opt.get() instanceof NpcImpl npc) {
            services.npcs().animations().install(npc, anim, interval);
            npc.setAnimation(anim.name(), interval);
        }
        services.npcs().save();
        responder.send(sender, "command.animation-set", Map.of("id", id, "animation", anim.name()));
        return true;
    }

    @Override
    public List<String> tabComplete(CommandSender sender, String[] args) {
        if (args.length == 1) {
            String partial = args[0].toLowerCase();
            return services.npcs().registry().all().stream().map(n -> n.id())
                    .filter(n -> n.toLowerCase().startsWith(partial)).collect(Collectors.toList());
        }
        if (args.length == 2) {
            return Arrays.stream(NpcAnimation.values()).map(Enum::name)
                    .filter(n -> n.startsWith(args[1].toUpperCase())).collect(Collectors.toList());
        }
        return List.of();
    }
}
