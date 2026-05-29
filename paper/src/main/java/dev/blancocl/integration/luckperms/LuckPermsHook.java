package dev.blancocl.integration.luckperms;

import dev.blancocl.ServiceContainer;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

/**
 * Reflective LuckPerms bridge. Used by visibility filters such as
 * {@code visibility: {permission: lobby.vip}}. Reflective so the plugin
 * still loads when LuckPerms is absent.
 */
public final class LuckPermsHook {

    private LuckPermsHook() {}

    public static void install(ServiceContainer services) {
        // Nothing global to install today — visibility checks call into hasPermission(Player, String).
        services.plugin().getLogger().info("LuckPerms detected: per-NPC permission gating active.");
    }

    public static boolean hasPermission(Player player, String node) {
        if (node == null || node.isEmpty()) return true;
        return player.hasPermission(node);
    }

    public static boolean luckPermsLoaded() {
        return Bukkit.getPluginManager().isPluginEnabled("LuckPerms");
    }
}
