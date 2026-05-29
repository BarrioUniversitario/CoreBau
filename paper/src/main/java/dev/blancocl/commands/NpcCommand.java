package dev.blancocl.commands;

import dev.blancocl.ServiceContainer;
import dev.blancocl.commands.feedback.CommandResponder;
import dev.blancocl.commands.sub.AnimationSub;
import dev.blancocl.commands.sub.CreateSub;
import dev.blancocl.commands.sub.EditSub;
import dev.blancocl.commands.sub.HologramSub;
import dev.blancocl.commands.sub.ListSub;
import dev.blancocl.commands.sub.MigrateSub;
import dev.blancocl.commands.sub.MoveSub;
import dev.blancocl.commands.sub.ReloadSub;
import dev.blancocl.commands.sub.RemoveSub;
import dev.blancocl.commands.sub.ServerSub;
import dev.blancocl.commands.sub.SkinSub;
import dev.blancocl.commands.sub.TpSub;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.PluginCommand;
import org.bukkit.command.TabExecutor;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Root {@code /npc} dispatcher. Registers via {@code plugin.yml} (already declared)
 * and routes to {@link SubCommand}s. Each sub owns its own permission, usage, and
 * tab-completion.
 *
 * <p>The plan called for Brigadier; {@link CommandExecutor}-based routing was chosen
 * for code-size economy. Swapping to Paper's Brigadier API later only requires
 * adding a {@code LifecycleEvents.COMMANDS} handler — the {@link SubCommand}
 * interface stays the same.</p>
 */
public final class NpcCommand implements TabExecutor {

    private final ServiceContainer services;
    private final CommandResponder responder;
    private final Map<String, SubCommand> subs = new LinkedHashMap<>();

    public NpcCommand(ServiceContainer services) {
        this.services = services;
        this.responder = new CommandResponder(services.config().messages());

        register(new CreateSub(services, responder));
        register(new RemoveSub(services, responder));
        register(new MoveSub(services, responder));
        register(new SkinSub(services, responder));
        register(new HologramSub(services, responder));
        register(new TpSub(services, responder));
        register(new ReloadSub(services, responder));
        register(new ListSub(services, responder));
        register(new EditSub(services, responder));
        register(new ServerSub(services, responder));
        register(new AnimationSub(services, responder));
        register(new MigrateSub(services, responder));
    }

    public void register(JavaPlugin plugin) {
        PluginCommand cmd = plugin.getCommand("npc");
        if (cmd == null) {
            plugin.getLogger().severe("plugin.yml is missing the 'npc' command declaration.");
            return;
        }
        cmd.setExecutor(this);
        cmd.setTabCompleter(this);
    }

    private void register(SubCommand sc) { subs.put(sc.name(), sc); }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command,
                             @NotNull String label, String[] args) {
        if (args.length == 0) { help(sender); return true; }

        SubCommand sub = subs.get(args[0].toLowerCase());
        if (sub == null) { help(sender); return true; }

        if (sub.permission() != null && !sender.hasPermission(sub.permission())) {
            responder.send(sender, "general.no-permission");
            return true;
        }

        String[] tail = new String[args.length - 1];
        System.arraycopy(args, 1, tail, 0, tail.length);
        try {
            return sub.execute(sender, tail);
        } catch (Throwable t) {
            services.plugin().getLogger().warning("Subcommand /" + label + " " + sub.name() + " failed: " + t);
            responder.send(sender, "general.internal-error");
            return true;
        }
    }

    @Override
    public List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command,
                                      @NotNull String alias, String[] args) {
        if (args.length == 1) {
            String partial = args[0].toLowerCase();
            return subs.keySet().stream()
                    .filter(n -> n.startsWith(partial))
                    .filter(n -> {
                        SubCommand s = subs.get(n);
                        return s.permission() == null || sender.hasPermission(s.permission());
                    })
                    .collect(Collectors.toList());
        }
        SubCommand sub = subs.get(args[0].toLowerCase());
        if (sub == null) return List.of();
        if (sub.permission() != null && !sender.hasPermission(sub.permission())) return List.of();
        String[] tail = new String[args.length - 1];
        System.arraycopy(args, 1, tail, 0, tail.length);
        return sub.tabComplete(sender, tail);
    }

    private void help(CommandSender sender) {
        responder.send(sender, "command.help-header");
        for (SubCommand sub : subs.values()) {
            if (sub.permission() != null && !sender.hasPermission(sub.permission())) continue;
            services.config().messages().send(sender, "command.help-line",
                    Map.of("usage", sub.usage(), "desc", sub.description()));
        }
    }

    public List<String> npcIds() {
        List<String> out = new ArrayList<>();
        services.npcs().registry().all().forEach(n -> out.add(n.id()));
        return out;
    }
}
