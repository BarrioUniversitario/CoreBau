package dev.blancocl.integration.update;

import dev.blancocl.util.Threading;
import org.bukkit.plugin.java.JavaPlugin;

/**
 * Async-only update check. Stub implementation — point it at a release URL
 * (Modrinth / SpigotMC / GitHub Releases) when distribution is decided.
 */
public final class UpdateChecker {

    private UpdateChecker() {}

    public static void checkAsync(JavaPlugin plugin, Threading threading) {
        threading.skinIo().execute(() -> {
            try {
                // TODO: replace with real GET to the release endpoint.
                plugin.getLogger().fine("Update check stub — current version: " + plugin.getPluginMeta().getVersion());
            } catch (Throwable ignored) {}
        });
    }
}
