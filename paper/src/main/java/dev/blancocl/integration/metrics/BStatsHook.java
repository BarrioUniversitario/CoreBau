package dev.blancocl.integration.metrics;

import org.bstats.bukkit.Metrics;
import org.bukkit.plugin.java.JavaPlugin;

/** bStats wrapper. Replace {@code 0} with the real bStats plugin id after registration. */
public final class BStatsHook {

    private static final int BSTATS_PLUGIN_ID = 0; // TODO: register on bstats.org and replace
    private final JavaPlugin plugin;

    public BStatsHook(JavaPlugin plugin) { this.plugin = plugin; }

    public void start() {
        if (BSTATS_PLUGIN_ID <= 0) {
            plugin.getLogger().info("bStats: plugin id not set — metrics disabled.");
            return;
        }
        new Metrics(plugin, BSTATS_PLUGIN_ID);
    }
}
