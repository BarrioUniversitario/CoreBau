package dev.blancocl.npc.persistence;

import dev.blancocl.config.PluginConfig;
import dev.blancocl.util.Threading;
import org.bukkit.plugin.Plugin;

/** Selects between {@link YamlNpcRepository} and {@link MySqlNpcRepository} based on config. */
public final class RepositoryFactory {

    private RepositoryFactory() {}

    public static NpcRepository create(Plugin plugin, Threading threading, PluginConfig cfg) {
        String backend = cfg.persistenceBackend();
        if ("mysql".equals(backend)) {
            try {
                plugin.getLogger().info("Using MySQL persistence backend (host=" + cfg.mysqlHost()
                        + ", db=" + cfg.mysqlDatabase() + ")");
                return new MySqlNpcRepository(plugin, threading, cfg);
            } catch (Throwable t) {
                plugin.getLogger().severe("MySQL backend failed to initialize, falling back to YAML: " + t);
                return new YamlNpcRepository(plugin, threading);
            }
        }
        plugin.getLogger().info("Using YAML persistence backend (plugins/Npc/npcs.yml)");
        return new YamlNpcRepository(plugin, threading);
    }
}
