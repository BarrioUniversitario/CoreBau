package dev.blancocl.integration.papi;

import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import dev.blancocl.ServiceContainer;
import org.bukkit.OfflinePlayer;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Exposes Npc state to PlaceholderAPI under the {@code npc_} prefix.
 *
 * <ul>
 *   <li>{@code %npc_count%} — number of NPCs registered.</li>
 *   <li>{@code %npc_server_count_<name>%} — cached online count for backend {@code name}.</li>
 *   <li>{@code %npc_proxy_detected%} — {@code true}/{@code false}.</li>
 * </ul>
 */
public final class NpcExpansion extends PlaceholderExpansion {

    private final ServiceContainer services;

    public NpcExpansion(ServiceContainer services) { this.services = services; }

    @Override public @NotNull String getIdentifier() { return "npc"; }
    @Override public @NotNull String getAuthor()     { return "davidml16"; }
    @Override public @NotNull String getVersion()    { return "1.0.0"; }
    @Override public boolean persist()               { return true; }

    @Override
    public @Nullable String onRequest(OfflinePlayer player, @NotNull String params) {
        if ("count".equalsIgnoreCase(params)) return Integer.toString(services.npcs().registry().size());
        if ("proxy_detected".equalsIgnoreCase(params)) return Boolean.toString(services.velocity().proxyDetected());

        if (params.startsWith("server_count_")) {
            String server = params.substring("server_count_".length());
            return services.velocity().cachedPlayerCount(server).map(String::valueOf).orElse("0");
        }
        return null;
    }
}
