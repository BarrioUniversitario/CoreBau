package cl.xgamers.core;

import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.player.KickedFromServerEvent;
import com.velocitypowered.api.proxy.Player;
import com.velocitypowered.api.proxy.server.RegisteredServer;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;

import java.util.ArrayList;
import java.util.List;

public class LobbyKickListener {

    private final Core plugin;

    public LobbyKickListener(Core plugin) {
        this.plugin = plugin;
    }

    @Subscribe
    public void onKickedFromServer(KickedFromServerEvent event) {
        Player player = event.getPlayer();
        String serverName = event.getServer().getServerInfo().getName();

        List<String> lobbyServers = plugin.getLobbyServers();
        if (lobbyServers == null || !lobbyServers.contains(serverName.trim())) {
            return;
        }

        List<String> candidates = new ArrayList<>();
        for (String name : lobbyServers) {
            name = name.trim();
            if (!name.isEmpty() && !name.equals(serverName)) {
                candidates.add(name);
            }
        }

        RegisteredServer fallback = plugin.findBestLobby(candidates);
        if (fallback != null) {
            event.setResult(KickedFromServerEvent.RedirectPlayer.create(fallback));
            player.sendMessage(Component.text("Conectando a otro lobby...").color(NamedTextColor.GOLD));
            plugin.getLogger().info("Lobby fallback: " + player.getUsername()
                + " redirigido de " + serverName + " a " + fallback.getServerInfo().getName());
        }
    }
}
