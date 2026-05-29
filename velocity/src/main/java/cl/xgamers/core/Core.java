package cl.xgamers.core;

import com.google.inject.Inject;
import com.velocitypowered.api.command.CommandSource;
import com.velocitypowered.api.command.SimpleCommand;
import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.connection.PluginMessageEvent;
import com.velocitypowered.api.event.proxy.ProxyInitializeEvent;
import com.velocitypowered.api.plugin.Plugin;
import com.velocitypowered.api.plugin.annotation.DataDirectory;
import com.velocitypowered.api.proxy.Player;
import com.velocitypowered.api.proxy.ProxyServer;
import com.velocitypowered.api.proxy.ServerConnection;
import com.velocitypowered.api.proxy.messages.MinecraftChannelIdentifier;
import com.velocitypowered.api.proxy.server.RegisteredServer;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.Properties;
import java.util.logging.Logger;

@Plugin(
    id = "core",
    name = "Core",
    version = "1.0.0",
    authors = {"xGamers"}
)
public class Core {

    private final ProxyServer server;
    private final Logger logger;
    private final Path dataDirectory;
    private boolean debug;
    private List<String> lobbyServers = List.of();
    private String lobbyMessage;
    private String lobbyNoServers;
    private boolean lobbyFallbackOnKick;
    private static final MinecraftChannelIdentifier IDENTIFIER = 
        MinecraftChannelIdentifier.from("serverconnector:main");
    private static final MinecraftChannelIdentifier BAUL_IDENTIFIER =
        MinecraftChannelIdentifier.create("baul", "sync");
    private static final MinecraftChannelIdentifier NPC_SYNC_IDENTIFIER =
        MinecraftChannelIdentifier.create("npc", "sync");
    private boolean baulSyncEnabled;
    private boolean npcSyncEnabled;

    @Inject
    public Core(ProxyServer server, Logger logger, @DataDirectory Path dataDirectory) {
        this.server = server;
        this.logger = logger;
        this.dataDirectory = dataDirectory;
    }

    @Subscribe
    public void onProxyInitialization(ProxyInitializeEvent event) {
        loadConfig();
        registerCommands();

        server.getChannelRegistrar().register(IDENTIFIER);
        if (baulSyncEnabled) {
            server.getChannelRegistrar().register(BAUL_IDENTIFIER);
        }
        if (npcSyncEnabled) {
            server.getChannelRegistrar().register(NPC_SYNC_IDENTIFIER);
        }
        
        logger.info("========================================");
        logger.info("Core Velocity iniciado");
        logger.info("Canal registrado: " + IDENTIFIER.getId());
        if (baulSyncEnabled) {
            logger.info("Canal registrado: " + BAUL_IDENTIFIER.getId() + " (baul sync)");
        }
        if (npcSyncEnabled) {
            logger.info("Canal registrado: " + NPC_SYNC_IDENTIFIER.getId() + " (npc sync)");
        }
        logger.info("========================================");
        logger.info("Servidores disponibles:");
        for (RegisteredServer registeredServer : server.getAllServers()) {
            logger.info("  - " + registeredServer.getServerInfo().getName() + 
                       " (" + registeredServer.getServerInfo().getAddress() + ")");
        }
        logger.info("Lobbies configurados: " + String.join(", ", lobbyServers));
        logger.info("========================================");    
        logger.info("Core Velocity cargado correctamente!");
        if (debug) {
            logger.info("Modo debug activo (logs de PlayerCountAll habilitados)");
        }

        if (lobbyFallbackOnKick) {
            server.getEventManager().register(this, new LobbyKickListener(this));
        }
    }

    private void loadConfig() {
        Path configFile = dataDirectory.resolve("config.properties");
        Properties defaults = new Properties();
        defaults.setProperty("debug", "false");
        defaults.setProperty("lobbies", "lobby1,lobby2");
        defaults.setProperty("lobby-message", "<gold>Conectando al lobby...</gold>");
        defaults.setProperty("lobby-no-servers", "<red>No hay lobbies disponibles.</red>");
        defaults.setProperty("lobby-fallback-on-kick", "true");
        defaults.setProperty("baul-sync-enabled", "false");
        defaults.setProperty("npc-sync-enabled", "true");

        try {
            Files.createDirectories(dataDirectory);
            if (!Files.exists(configFile)) {
                try (InputStream in = getClass().getResourceAsStream("/config.properties")) {
                    if (in != null) {
                        Files.copy(in, configFile);
                    } else {
                        defaults.store(Files.newOutputStream(configFile), "Core plugin configuration");
                    }
                }
            }

            Properties props = new Properties(defaults);
            try (InputStream in = Files.newInputStream(configFile)) {
                props.load(in);
            }
            debug = Boolean.parseBoolean(props.getProperty("debug", "false"));
            lobbyServers = List.of(props.getProperty("lobbies", "lobby1").split(","));
            lobbyMessage = props.getProperty("lobby-message", "<gold>Conectando al lobby...</gold>");
            lobbyNoServers = props.getProperty("lobby-no-servers", "<red>No hay lobbies disponibles.</red>");
            lobbyFallbackOnKick = Boolean.parseBoolean(props.getProperty("lobby-fallback-on-kick", "true"));
            baulSyncEnabled = Boolean.parseBoolean(props.getProperty("baul-sync-enabled", "false"));
            npcSyncEnabled = Boolean.parseBoolean(props.getProperty("npc-sync-enabled", "true"));
        } catch (Exception e) {
            debug = false;
            lobbyServers = List.of("lobby1");
            logger.warning("No se pudo cargar config.properties: " + e.getMessage());
        }
    }

    private void registerCommands() {
        server.getCommandManager().register("lobby", new SimpleCommand() {
            @Override
            public void execute(Invocation invocation) {
                CommandSource source = invocation.source();
                if (!(source instanceof Player player)) {
                    source.sendMessage(Component.text("Solo los jugadores pueden usar este comando.")
                        .color(NamedTextColor.RED));
                    return;
                }
                handleLobbyCommand(player);
            }
        });
    }

    private void handleLobbyCommand(Player player) {
        List<String> availableLobbies = new ArrayList<>();
        for (String name : lobbyServers) {
            name = name.trim();
            if (!name.isEmpty() && server.getServer(name).isPresent()) {
                availableLobbies.add(name);
            }
        }

        if (availableLobbies.isEmpty()) {
            player.sendMessage(miniMessage(lobbyNoServers));
            return;
        }

        RegisteredServer target = findBestLobby(availableLobbies);
        if (target == null) {
            player.sendMessage(miniMessage(lobbyNoServers));
            return;
        }

        player.sendMessage(miniMessage(lobbyMessage));
        player.createConnectionRequest(target).fireAndForget();
        logDebug("Lobby command: " + player.getUsername() + " → " + target.getServerInfo().getName());
    }

    RegisteredServer findBestLobby(List<String> candidates) {
        return candidates.stream()
            .map(name -> server.getServer(name).orElse(null))
            .filter(java.util.Objects::nonNull)
            .min(Comparator.comparingInt(s -> s.getPlayersConnected().size()))
            .orElse(null);
    }

    Optional<RegisteredServer> findBestLobby() {
        List<String> available = new ArrayList<>();
        for (String name : lobbyServers) {
            name = name.trim();
            if (!name.isEmpty() && server.getServer(name).isPresent()) {
                available.add(name);
            }
        }
        return Optional.ofNullable(findBestLobby(available));
    }

    List<String> getLobbyServers() {
        return lobbyServers;
    }

    Logger getLogger() {
        return logger;
    }

    boolean isDebug() {
        return debug;
    }

    private void logDebug(String message) {
        if (debug) {
            logger.info(message);
        }
    }

    private Component miniMessage(String text) {
        return net.kyori.adventure.text.minimessage.MiniMessage.builder().build()
            .deserialize(text);
    }

    @Subscribe
    public void onPluginMessage(PluginMessageEvent event) {
        if (!event.getIdentifier().equals(IDENTIFIER)) {
            return;
        }

        if (!(event.getSource() instanceof ServerConnection)) {
            logger.warning("Plugin message sin ServerConnection: " + event.getSource().getClass().getName());
            return;
        }

        ServerConnection connection = (ServerConnection) event.getSource();
        Player player = connection.getPlayer();

        try {
            ByteArrayInputStream stream = new ByteArrayInputStream(event.getData());
            DataInputStream in = new DataInputStream(stream);
            
            String subChannel = in.readUTF();

            if (subChannel.equals("Connect")) {
                String serverName = in.readUTF();
                logger.info("Conexión solicitada: " + player.getUsername()
                    + " → " + serverName + " (desde " + connection.getServerInfo().getName() + ")");

                Optional<RegisteredServer> serverCheck = this.server.getServer(serverName);
                if (serverCheck.isPresent()) {
                    connectPlayer(player, serverName);
                } else {
                    logger.warning("Servidor '" + serverName + "' no encontrado para " + player.getUsername());
                    player.sendMessage(Component.text("El servidor '" + serverName + "' no está disponible.")
                        .color(NamedTextColor.RED));
                }
                
            } else if (subChannel.equals("PlayerCount")) {
                String requestedServer = in.readUTF();
                logDebug("PlayerCount " + requestedServer + " ← " + player.getUsername());
                sendPlayerCount(player, requestedServer, debug);
                
            } else if (subChannel.equals("PlayerCountAll") || subChannel.equals("GetServerCounts")) {
                logDebug("PlayerCountAll ← " + player.getUsername()
                    + " (" + connection.getServerInfo().getName() + ")");
                sendAllPlayerCounts(player, debug);

            } else if (subChannel.equals("GetServers")) {
                logDebug("GetServers ← " + player.getUsername());
                sendServerList(player);

            } else {
                logger.warning("SubChannel desconocido: " + subChannel
                    + " (jugador " + player.getUsername() + ")");
            }
            
        } catch (Exception e) {
            logger.severe("Error procesando plugin message: " + e.getMessage());
            e.printStackTrace();
        }

        event.setResult(PluginMessageEvent.ForwardResult.handled());
    }

    private void connectPlayer(Player player, String serverName) {
        Optional<RegisteredServer> serverOpt = this.server.getServer(serverName);
        
        if (serverOpt.isPresent()) {
            player.createConnectionRequest(serverOpt.get()).fireAndForget();
            logDebug("Conexión enviada: " + player.getUsername() + " → " + serverName);
        } else {
            logger.warning("Servidor no existe: " + serverName);
            player.sendMessage(Component.text("El servidor " + serverName + " no existe!")
                .color(NamedTextColor.RED));
        }
    }

    private void sendPlayerCount(Player player, String serverName, boolean verbose) {
        Optional<RegisteredServer> server = this.server.getServer(serverName);
        
        if (server.isPresent()) {
            int count = server.get().getPlayersConnected().size();
            
            try {
                ByteArrayOutputStream stream = new ByteArrayOutputStream();
                DataOutputStream out = new DataOutputStream(stream);
                
                out.writeUTF("PlayerCount");
                out.writeUTF(serverName);
                out.writeInt(count);
                
                player.getCurrentServer().ifPresent(serverConnection -> {
                    serverConnection.sendPluginMessage(IDENTIFIER, stream.toByteArray());
                });
                
                if (verbose) {
                    logDebug("Player count enviado: " + serverName + " = " + count);
                }
                
            } catch (Exception e) {
                logger.warning("Error enviando conteo de jugadores: " + e.getMessage());
            }
        }
    }

    @Subscribe
    public void onNpcSyncPluginMessage(PluginMessageEvent event) {
        if (!npcSyncEnabled) return;
        if (!event.getIdentifier().equals(NPC_SYNC_IDENTIFIER)) return;
        if (!(event.getSource() instanceof ServerConnection)) return;

        event.setResult(PluginMessageEvent.ForwardResult.handled());

        ServerConnection sourceConn = (ServerConnection) event.getSource();
        String sourceServer = sourceConn.getServerInfo().getName();
        byte[] data = event.getData();

        for (RegisteredServer registeredServer : server.getAllServers()) {
            if (registeredServer.getServerInfo().getName().equals(sourceServer)) continue;
            registeredServer.sendPluginMessage(NPC_SYNC_IDENTIFIER, data);
        }

        logDebug("npc:sync relayed from " + sourceServer
            + " (" + (server.getAllServers().size() - 1) + " targets)");
    }

    @Subscribe
    public void onBaulPluginMessage(PluginMessageEvent event) {
        if (!baulSyncEnabled) return;
        if (!event.getIdentifier().equals(BAUL_IDENTIFIER)) return;
        if (!(event.getSource() instanceof ServerConnection)) return;

        event.setResult(PluginMessageEvent.ForwardResult.handled());

        byte[] data = event.getData();

        String sourceServer;
        try {
            DataInputStream in = new DataInputStream(new ByteArrayInputStream(data));
            in.readUTF(); // action
            in.readUTF(); // uuid
            sourceServer = in.readUTF();
        } catch (Exception e) {
            logger.warning("Error leyendo mensaje baul:sync: " + e.getMessage());
            return;
        }

        for (RegisteredServer registeredServer : server.getAllServers()) {
            String serverName = registeredServer.getServerInfo().getName();
            if (serverName.equals(sourceServer)) continue;
            registeredServer.sendPluginMessage(BAUL_IDENTIFIER, data);
        }
    }

    private void sendServerList(Player player) {
        try {
            ByteArrayOutputStream stream = new ByteArrayOutputStream();
            DataOutputStream out = new DataOutputStream(stream);

            out.writeUTF("GetServers");

            StringBuilder csv = new StringBuilder();
            for (RegisteredServer registered : this.server.getAllServers()) {
                if (csv.length() > 0) csv.append(", ");
                csv.append(registered.getServerInfo().getName());
            }
            out.writeUTF(csv.toString());

            player.getCurrentServer().ifPresent(serverConnection ->
                serverConnection.sendPluginMessage(IDENTIFIER, stream.toByteArray())
            );
        } catch (Exception e) {
            logger.warning("Error enviando lista de servidores: " + e.getMessage());
        }
    }

    private void sendAllPlayerCounts(Player player, boolean verbose) {
        try {
            ByteArrayOutputStream stream = new ByteArrayOutputStream();
            DataOutputStream out = new DataOutputStream(stream);
            
            out.writeUTF("PlayerCountAll");
            
            for (RegisteredServer server : this.server.getAllServers()) {
                String name = server.getServerInfo().getName();
                int count = server.getPlayersConnected().size();
                
                out.writeUTF(name);
                out.writeInt(count);

                if (verbose) {
                    logDebug("  " + name + ": " + count);
                }
            }
            
            player.getCurrentServer().ifPresent(serverConnection ->
                serverConnection.sendPluginMessage(IDENTIFIER, stream.toByteArray())
            );
            
        } catch (Exception e) {
            logger.severe("Error enviando conteo total de jugadores:");
            e.printStackTrace();
        }
    }
}
