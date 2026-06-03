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
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

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
    private final java.util.concurrent.atomic.AtomicBoolean updatePending = new java.util.concurrent.atomic.AtomicBoolean(false);

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

    public void requestServerCounts(Player player) {
        if (player == null || !player.isOnline()) return;
        // PlayerCountAll trae todos los conteos en un solo mensaje. Antes ademas
        // enviabamos un PlayerCount por servidor del registry y por placeholder
        // detectado en las lineas: cada respuesta disparaba un updateAllBoards
        // completo, multiplicando el coste por N. Ahora un solo request -> una
        // sola respuesta -> un solo update debounced.
        sendPlayerCountAllRequest(player);
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
                scheduleDebouncedUpdate();
            }
        } catch (IOException e) {
            getLogger().warning("Error leyendo mensaje de Velocity: " + e.getMessage());
        }
    }

    /**
     * Coalesce multiples respuestas de Velocity en un solo updateAllBoards por
     * tick. Sin esto, cada PlayerCount/PlayerCountAll disparaba una rebuild
     * completa del scoreboard de todos los jugadores online.
     */
    private void scheduleDebouncedUpdate() {
        if (!updatePending.compareAndSet(false, true)) return;
        plugin.getServer().getScheduler().runTask(plugin, () -> {
            updatePending.set(false);
            boardManager.updateAllBoards();
        });
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
