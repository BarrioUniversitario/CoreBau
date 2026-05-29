package dev.blancocl.config;

import org.bukkit.configuration.file.YamlConfiguration;

import java.util.logging.Logger;

/** Stub migrator. Adds new defaults in-place when {@code config-version} bumps. */
final class ConfigMigrator {

    static final int CURRENT_CONFIG_VERSION   = 2;
    static final int CURRENT_MESSAGES_VERSION = 3;
    static final int CURRENT_NPCS_VERSION     = 1;

    private ConfigMigrator() {}

    static boolean migrateConfig(YamlConfiguration cfg, Logger log) {
        int v = cfg.getInt("config-version", 1);
        if (v >= CURRENT_CONFIG_VERSION) return false;
        log.warning("Migrating config.yml from v" + v + " to v" + CURRENT_CONFIG_VERSION);
        if (v < 2) {
            // Migrar canales antiguos no estándar (serverconnector:main, BungeeCord)
            // al canal estándar que Velocity maneja nativamente.
            String currentChannel = cfg.getString("velocity.channel", "");
            if (currentChannel == null || currentChannel.isEmpty() || "BungeeCord".equals(currentChannel)) {
                cfg.set("velocity.channel", "bungeecord:main");
                log.info("config.yml: velocity.channel migrado a 'bungeecord:main' (canal estándar de Velocity).");
            }
            // Bloque persistence completo si falta
            if (!cfg.contains("persistence")) {
                cfg.set("persistence.backend", "yaml");
                cfg.set("persistence.mysql.host", "localhost");
                cfg.set("persistence.mysql.port", 3306);
                cfg.set("persistence.mysql.database", "npcs");
                cfg.set("persistence.mysql.user", "npc");
                cfg.set("persistence.mysql.password", "changeme");
                cfg.set("persistence.mysql.pool-size", 4);
                cfg.set("persistence.sync-enabled", true);
            }
        }
        cfg.set("config-version", CURRENT_CONFIG_VERSION);
        return true;
    }

    static boolean migrateMessages(YamlConfiguration cfg, Logger log) {
        int v = cfg.getInt("messages-version", 1);
        if (v >= CURRENT_MESSAGES_VERSION) return false;
        log.warning("Migrating messages.yml from v" + v + " to v" + CURRENT_MESSAGES_VERSION);
        if (v < 2) {
            cfg.set("command.name-set", "<prefix><green>NPC name set to <yellow><name></yellow> for <yellow><id></yellow>.");
        }
        if (v < 3) {
            // Hologram editor expansion: set / insert / swap / list / offset / spacing.
            cfg.set("command.hologram-line-set",
                    "<prefix><green>Set line <yellow><line></yellow> on <yellow><id></yellow>.");
            cfg.set("command.hologram-line-inserted",
                    "<prefix><green>Inserted line at <yellow><line></yellow> on <yellow><id></yellow>.");
            cfg.set("command.hologram-line-swapped",
                    "<prefix><green>Swapped lines <yellow><i></yellow> ↔ <yellow><j></yellow> on <yellow><id></yellow>.");
            cfg.set("command.hologram-offset-set",
                    "<prefix><green>Set offsetY = <yellow><offset></yellow> on <yellow><id></yellow>.");
            cfg.set("command.hologram-spacing-set",
                    "<prefix><green>Set line spacing = <yellow><spacing></yellow> on <yellow><id></yellow>.");
            cfg.set("command.hologram-list-header",
                    "<prefix><white>Hologram <gray>(<yellow><id></yellow>)</gray> — "
                  + "<yellow><count></yellow> line(s), offsetY=<yellow><offset></yellow>, "
                  + "spacing=<yellow><spacing></yellow>");
            cfg.set("command.hologram-list-line",
                    "<gray> <hover:show_text:'<gray>Click to edit line <yellow><index></yellow>'>"
                  + "<click:suggest_command:'/npc hologram <id> set <index> '>"
                  + "<yellow><index></yellow></click></hover> "
                  + "<dark_gray>│</dark_gray> <preview>");
        }
        cfg.set("messages-version", CURRENT_MESSAGES_VERSION);
        return true;
    }

    static boolean migrateNpcs(YamlConfiguration cfg, Logger log) {
        int v = cfg.getInt("schema-version", 1);
        if (v == CURRENT_NPCS_VERSION) return false;
        log.warning("Migrating npcs.yml from v" + v + " to v" + CURRENT_NPCS_VERSION);
        cfg.set("schema-version", CURRENT_NPCS_VERSION);
        return true;
    }
}
