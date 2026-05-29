package cl.xgamers.board;

import cl.xgamers.corebau.CoreBauPlugin;
import cl.xgamers.corebau.module.Module;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.plugin.messaging.PluginMessageListener;
import org.bukkit.scheduler.BukkitTask;
import org.jetbrains.annotations.NotNull;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Módulo Board de CoreBau (antes plugin independiente).
 * Integración con Velocity/Core vía canal {@value #CHANNEL}.
 */
public final class Board implements Module, Listener, PluginMessageListener {

    public static final String CHANNEL = "serverconnector:main";

    private CoreBauPlugin plugin;
    private FileConfiguration config;
    private File configFile;

    private BoardManager boardManager;
    private BoardServerRegistry serverRegistry;
    private BukkitTask updateTask;
    private BukkitTask syncTask;
    private final Map<String, Integer> serverCounts = new ConcurrentHashMap<>();

    @Override
    public String id() {
        return "board";
    }

    @Override
    public void enable(CoreBauPlugin plugin) {
        this.plugin = plugin;
        saveDefaultConfig();

        serverRegistry = new BoardServerRegistry(this);
        boardManager = new BoardManager(this);

        plugin.getServer().getPluginManager().registerEvents(this, plugin);
        plugin.getCommand("board").setExecutor(new BoardCommand(this));

        if (Bukkit.getPluginManager().isPluginEnabled("PlaceholderAPI")) {
            new BoardExpansion(this).register();
            getLogger().info("PlaceholderAPI expansion registrado correctamente.");
        }

        plugin.getServer().getMessenger().registerOutgoingPluginChannel(plugin, CHANNEL);
        plugin.getServer().getMessenger().registerIncomingPluginChannel(plugin, CHANNEL, this);

        for (Player player : plugin.getServer().getOnlinePlayers()) {
            boardManager.createBoard(player);
        }

        scheduleBoardUpdates();
        scheduleVelocitySync();

        if (!plugin.getServer().getOnlinePlayers().isEmpty()) {
            plugin.getServer().getScheduler().runTaskLater(plugin, (Runnable) this::requestServerCounts, 20L);
        }
    }

    @Override
    public void disable() {
        if (updateTask != null) updateTask.cancel();
        if (syncTask != null) syncTask.cancel();
        plugin.getServer().getMessenger().unregisterOutgoingPluginChannel(plugin);
        plugin.getServer().getMessenger().unregisterIncomingPluginChannel(plugin, CHANNEL, this);
        if (boardManager != null) {
            for (Player player : plugin.getServer().getOnlinePlayers()) {
                boardManager.removeBoard(player);
            }
        }
    }

    // ---- Config propio del módulo (plugins/CoreBau/board/config.yml) ----

    public void saveDefaultConfig() {
        configFile = new File(plugin.getModuleFolder("board"), "config.yml");
        if (!configFile.exists()) {
            plugin.saveResource("board/config.yml", false);
        }
        config = YamlConfiguration.loadConfiguration(configFile);
        try (InputStream def = plugin.getResource("board/config.yml")) {
            if (def != null) {
                config.setDefaults(YamlConfiguration.loadConfiguration(
                        new InputStreamReader(def, StandardCharsets.UTF_8)));
            }
        } catch (IOException ignored) {
        }
    }

    public FileConfiguration getConfig() {
        if (config == null) saveDefaultConfig();
        return config;
    }

    public void reloadConfig() {
        if (configFile == null) configFile = new File(plugin.getModuleFolder("board"), "config.yml");
        config = YamlConfiguration.loadConfiguration(configFile);
    }

    public java.util.logging.Logger getLogger() {
        return plugin.getLogger();
    }

    public org.bukkit.Server getServer() {
        return plugin.getServer();
    }

    public File getDataFolder() {
        return plugin.getModuleFolder("board");
    }

    public CoreBauPlugin getPlugin() {
        return plugin;
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        boardManager.createBoard(event.getPlayer());
        plugin.getServer().getScheduler().runTaskLater(plugin, () -> requestServerCounts(event.getPlayer()), 20L);
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        boardManager.removeBoard(event.getPlayer());
    }

    public BoardManager getBoardManager() {
        return boardManager;
    }

    public void scheduleBoardUpdates() {
        if (updateTask != null) updateTask.cancel();
        int interval = Math.max(1, getConfig().getInt("animations.interval", 20));
        updateTask = plugin.getServer().getScheduler().runTaskTimer(plugin, boardManager::updateAllBoards, 0L, interval);
    }

    public void rescheduleBoardUpdates() {
        scheduleBoardUpdates();
    }

    private void scheduleVelocitySync() {
        if (syncTask != null) syncTask.cancel();
        int interval = Math.max(20, getConfig().getInt("velocity.sync-interval-ticks", 20));
        syncTask = plugin.getServer().getScheduler().runTaskTimer(plugin, (Runnable) this::requestServerCounts, interval, interval);
    }

    public void rescheduleVelocitySync() {
        scheduleVelocitySync();
    }

    public void requestServerCounts() {
        if (plugin.getServer().getOnlinePlayers().isEmpty()) return;
        requestServerCounts(plugin.getServer().getOnlinePlayers().iterator().next());
    }

    private static final Pattern PLACEHOLDER_PATTERN = Pattern.compile("%board_(\\w+?)_(?:online|connected|max)%");

    public void requestServerCounts(Player player) {
        if (player == null || !player.isOnline()) return;

        Set<String> requested = new HashSet<>();

        for (BoardServerRegistry.ServerEntry entry : serverRegistry.getEntries().values()) {
            for (String velocityName : entry.getVelocityNames()) {
                if (requested.add(velocityName)) {
                    sendPlayerCountRequest(player, velocityName);
                }
            }
        }

        Set<String> configuredIds = serverRegistry.getEntries().keySet();
        List<String> allLines = new ArrayList<>();
        allLines.addAll(getConfig().getStringList("lines"));
        allLines.addAll(getConfig().getStringList("header.lines"));
        allLines.addAll(getConfig().getStringList("footer.lines"));
        for (String line : allLines) {
            Matcher matcher = PLACEHOLDER_PATTERN.matcher(line);
            while (matcher.find()) {
                String id = matcher.group(1).toLowerCase(Locale.ROOT);
                if (!configuredIds.contains(id) && requested.add(id)) {
                    sendPlayerCountRequest(player, id);
                }
            }
        }

        sendPlayerCountAllRequest(player);
    }

    private void sendPlayerCountRequest(Player player, String serverName) {
        try (ByteArrayOutputStream buffer = new ByteArrayOutputStream();
             DataOutputStream out = new DataOutputStream(buffer)) {
            out.writeUTF("PlayerCount");
            out.writeUTF(serverName);
            player.sendPluginMessage(plugin, CHANNEL, buffer.toByteArray());
        } catch (IOException e) {
            getLogger().warning("Error enviando PlayerCount a Velocity: " + e.getMessage());
        }
    }

    private void sendPlayerCountAllRequest(Player player) {
        try (ByteArrayOutputStream buffer = new ByteArrayOutputStream();
             DataOutputStream out = new DataOutputStream(buffer)) {
            out.writeUTF("PlayerCountAll");
            player.sendPluginMessage(plugin, CHANNEL, buffer.toByteArray());
        } catch (IOException e) {
            getLogger().warning("Error enviando PlayerCountAll a Velocity: " + e.getMessage());
        }
    }

    @Override
    public void onPluginMessageReceived(@NotNull String channel, @NotNull Player player, byte[] message) {
        if (!CHANNEL.equals(channel)) return;

        try (DataInputStream in = new DataInputStream(new ByteArrayInputStream(message))) {
            String subChannel = in.readUTF();

            boolean updated = false;
            if ("PlayerCount".equals(subChannel)) {
                serverCounts.put(in.readUTF(), in.readInt());
                updated = true;
            } else if ("PlayerCountAll".equals(subChannel)) {
                if (in.available() >= 4) {
                    in.mark(0);
                    int possibleCount = in.readInt();
                    if (possibleCount > 0 && possibleCount < 1024) {
                        for (int i = 0; i < possibleCount && in.available() > 0; i++) {
                            serverCounts.put(in.readUTF(), in.readInt());
                        }
                    } else {
                        in.reset();
                        while (in.available() > 0) {
                            serverCounts.put(in.readUTF(), in.readInt());
                        }
                    }
                }
                updated = true;
            } else {
                getLogger().fine("Subcanal no manejado: " + subChannel);
            }
            if (updated) {
                if (getConfig().getBoolean("velocity.debug", false)) {
                    getLogger().info("Conteos Velocity: " + serverCounts);
                }
                boardManager.updateAllBoards();
            }
        } catch (IOException e) {
            getLogger().warning("Error leyendo mensaje de Velocity: " + e.getMessage());
        }
    }

    public BoardServerRegistry getServerRegistry() {
        return serverRegistry;
    }

    public void reloadServerRegistry() {
        serverRegistry.reload();
    }

    public int getRawServerCount(String velocityName) {
        if (velocityName == null) return 0;
        Integer count = serverCounts.get(velocityName);
        if (count != null) return count;
        for (Map.Entry<String, Integer> entry : serverCounts.entrySet()) {
            if (entry.getKey().equalsIgnoreCase(velocityName)) {
                return entry.getValue();
            }
        }
        return 0;
    }

    public int getServerCount(String placeholderId) {
        return serverRegistry.getCount(placeholderId);
    }
}
