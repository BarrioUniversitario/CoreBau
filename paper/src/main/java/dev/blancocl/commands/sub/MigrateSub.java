package dev.blancocl.commands.sub;

import dev.blancocl.ServiceContainer;
import dev.blancocl.commands.SubCommand;
import dev.blancocl.commands.feedback.CommandResponder;
import dev.blancocl.npc.persistence.MySqlNpcRepository;
import dev.blancocl.npc.persistence.NpcRepository;
import dev.blancocl.npc.persistence.YamlNpcRepository;
import org.bukkit.command.CommandSender;

import java.util.List;
import java.util.Map;

/**
 * Copies the NPC set between the YAML and MySQL backends, regardless of which one
 * is currently active. Use before flipping {@code persistence.backend} so the new
 * backend already has the data.
 *
 * <p>Usage: {@code /npc migrate yaml-to-mysql} or {@code /npc migrate mysql-to-yaml}.</p>
 */
public final class MigrateSub implements SubCommand {

    private static final String DIR_YAML_TO_MYSQL = "yaml-to-mysql";
    private static final String DIR_MYSQL_TO_YAML = "mysql-to-yaml";

    private final ServiceContainer services;
    private final CommandResponder responder;

    public MigrateSub(ServiceContainer services, CommandResponder responder) {
        this.services = services; this.responder = responder;
    }

    @Override public String name()        { return "migrate"; }
    @Override public String permission()  { return "npc.command.migrate"; }
    @Override public String usage()       { return "/npc migrate <yaml-to-mysql|mysql-to-yaml>"; }
    @Override public String description() { return "Copies NPCs between the YAML and MySQL backends."; }

    @Override
    public boolean execute(CommandSender sender, String[] args) {
        if (args.length < 1) {
            responder.send(sender, "command.migrate-usage");
            return true;
        }
        String dir = args[0].toLowerCase();
        if (!DIR_YAML_TO_MYSQL.equals(dir) && !DIR_MYSQL_TO_YAML.equals(dir)) {
            responder.send(sender, "command.migrate-usage");
            return true;
        }

        NpcRepository source, dest;
        MySqlNpcRepository created = null;
        try {
            if (DIR_YAML_TO_MYSQL.equals(dir)) {
                source = new YamlNpcRepository(services.plugin(), services.threading());
                created = new MySqlNpcRepository(services.plugin(), services.threading(), services.config().config());
                dest = created;
            } else {
                created = new MySqlNpcRepository(services.plugin(), services.threading(), services.config().config());
                source = created;
                dest = new YamlNpcRepository(services.plugin(), services.threading());
            }
        } catch (Throwable t) {
            responder.send(sender, "command.migrate-failed",
                    Map.of("error", String.valueOf(t.getMessage())));
            return true;
        }

        responder.send(sender, "command.migrate-start", Map.of("dir", dir));

        MySqlNpcRepository toClose = created;
        long start = System.currentTimeMillis();
        source.loadAll()
                .thenCompose(snaps ->
                        dest.saveAll(snaps).thenApply(v -> snaps.size()))
                .whenComplete((count, err) -> {
                    if (toClose != null) {
                        try { toClose.close(); } catch (Throwable ignored) {}
                    }
                    if (err != null) {
                        responder.send(sender, "command.migrate-failed",
                                Map.of("error", String.valueOf(err.getMessage())));
                    } else {
                        responder.send(sender, "command.migrate-success", Map.of(
                                "count", Integer.toString(count),
                                "ms",    Long.toString(System.currentTimeMillis() - start),
                                "dir",   dir));
                    }
                });
        return true;
    }

    @Override
    public List<String> tabComplete(CommandSender sender, String[] args) {
        if (args.length == 1) {
            String partial = args[0].toLowerCase();
            return List.of(DIR_YAML_TO_MYSQL, DIR_MYSQL_TO_YAML).stream()
                    .filter(s -> s.startsWith(partial))
                    .toList();
        }
        return List.of();
    }
}
