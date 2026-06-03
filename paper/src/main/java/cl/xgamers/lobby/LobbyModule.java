package cl.xgamers.lobby;

import cl.xgamers.corebau.CoreBauPlugin;
import cl.xgamers.corebau.module.Module;
import cl.xgamers.selector.utils.Colorize;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.NamespacedKey;
import org.bukkit.Sound;
import org.bukkit.World;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityTargetLivingEntityEvent;
import org.bukkit.event.entity.FoodLevelChangeEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.entity.ProjectileLaunchEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.event.player.PlayerArmorStandManipulateEvent;
import org.bukkit.event.player.PlayerBucketEmptyEvent;
import org.bukkit.event.player.PlayerBucketFillEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerItemDamageEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerPickupArrowEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.event.weather.WeatherChangeEvent;
import org.bukkit.event.world.PortalCreateEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.scheduler.BukkitTask;

import java.io.File;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;

public class LobbyModule implements Module, Listener, CommandExecutor {

    private CoreBauPlugin plugin;
    private File configFile;
    private FileConfiguration config;

    private NamespacedKey selectorKey;
    private NamespacedKey lobbyOpenerKey;
    private NamespacedKey cosmeticsKey;

    private BukkitTask voidTask;

    // Caches refrescados solo en reload(): los listeners se ejecutan miles de
    // veces por segundo y antes hacian getBoolean/getDouble sobre YAML en cada
    // invocacion. Ahora los flags son lookups directos a campos.
    private boolean antivoidEnabled;
    private double  antivoidMinY;
    private int     antivoidIntervalTicks;
    private boolean antiDamageEnabled;
    private boolean antiDamageCancelVoid;
    private boolean antiHungerEnabled;
    private boolean antiDeathKeepInventory;
    private boolean antiDropEnabled;
    private boolean antiDropProtectedOnly;
    private boolean antiInventoryMoveEnabled;
    private boolean antiBuildEnabled;
    private boolean antiWeatherEnabled;
    private boolean antiMobTargetEnabled;
    private boolean antiItemDamageEnabled;
    private boolean antiProjectilesEnabled;
    private boolean onJoinTeleport;
    private boolean onJoinHeal;
    private boolean onJoinAdventure;
    private boolean onJoinAllowFlight;
    private volatile Location cachedSpawn;

    @Override
    public String id() {
        return "lobby";
    }

    @Override
    public void enable(CoreBauPlugin plugin) {
        this.plugin = plugin;
        saveDefaultConfig();
        refreshCaches();

        this.selectorKey    = new NamespacedKey(plugin, "selector");
        this.lobbyOpenerKey = new NamespacedKey(plugin, "lobby-opener");
        this.cosmeticsKey   = new NamespacedKey(plugin, "cosmetics-opener");

        plugin.getServer().getPluginManager().registerEvents(this, plugin);

        if (plugin.getCommand("setspawn") != null) plugin.getCommand("setspawn").setExecutor(this);
        if (plugin.getCommand("spawn")    != null) plugin.getCommand("spawn").setExecutor(this);
        if (plugin.getCommand("lobbyreload") != null) plugin.getCommand("lobbyreload").setExecutor(this);

        startVoidTask();

        plugin.getLogger().info("Lobby system cargado (servidor: " + getCurrentServerId() + ").");
    }

    @Override
    public void disable() {
        if (voidTask != null) {
            voidTask.cancel();
            voidTask = null;
        }
    }

    // ------------------------------------------------------------------
    //  Config
    // ------------------------------------------------------------------

    private void saveDefaultConfig() {
        File folder = plugin.getModuleFolder("lobby");
        if (!folder.exists()) folder.mkdirs();
        configFile = new File(folder, "config.yml");
        if (!configFile.exists()) plugin.saveResource("lobby/config.yml", false);
        config = YamlConfiguration.loadConfiguration(configFile);
        try (InputStream def = plugin.getResource("lobby/config.yml")) {
            if (def != null) {
                config.setDefaults(YamlConfiguration.loadConfiguration(
                        new InputStreamReader(def, StandardCharsets.UTF_8)));
            }
        } catch (Exception ignored) {
        }
    }

    private void reload() {
        config = YamlConfiguration.loadConfiguration(configFile);
        refreshCaches();
    }

    private void refreshCaches() {
        antivoidEnabled          = config.getBoolean("antivoid.enabled", true);
        antivoidMinY             = config.getDouble("antivoid.min-y", 0.0);
        antivoidIntervalTicks    = Math.max(1, config.getInt("antivoid.check-interval-ticks", 10));
        antiDamageEnabled        = config.getBoolean("anti-damage.enabled", true);
        antiDamageCancelVoid     = config.getBoolean("anti-damage.cancel-void", true);
        antiHungerEnabled        = config.getBoolean("anti-hunger.enabled", true);
        antiDeathKeepInventory   = config.getBoolean("anti-death.keep-inventory", true);
        antiDropEnabled          = config.getBoolean("anti-drop.enabled", true);
        antiDropProtectedOnly    = config.getBoolean("anti-drop.protected-items-only", false);
        antiInventoryMoveEnabled = config.getBoolean("anti-inventory-move.enabled", true);
        antiBuildEnabled         = config.getBoolean("anti-build.enabled", true);
        antiWeatherEnabled       = config.getBoolean("anti-weather.enabled", true);
        antiMobTargetEnabled     = config.getBoolean("anti-mob-target.enabled", true);
        antiItemDamageEnabled    = config.getBoolean("anti-item-damage.enabled", true);
        antiProjectilesEnabled   = config.getBoolean("anti-projectiles.enabled", true);
        onJoinTeleport           = config.getBoolean("on-join.teleport-to-spawn", true);
        onJoinHeal               = config.getBoolean("on-join.heal", true);
        onJoinAdventure          = config.getBoolean("on-join.gamemode-adventure", true);
        onJoinAllowFlight        = config.getBoolean("on-join.allow-flight", false);
        cachedSpawn              = null; // forzar relectura en proximo loadSpawn()
    }

    private void saveConfigFile() {
        try {
            config.save(configFile);
        } catch (Exception e) {
            plugin.getLogger().severe("No se pudo guardar lobby/config.yml: " + e.getMessage());
        }
    }

    private String getCurrentServerId() {
        return config.getString("server-id", "lobby1");
    }

    private String spawnPath() {
        return "spawns." + getCurrentServerId();
    }

    // ------------------------------------------------------------------
    //  Commands
    // ------------------------------------------------------------------

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        String name = command.getName().toLowerCase();

        if (name.equals("lobbyreload")) {
            if (!sender.hasPermission("lobby.admin")) {
                sender.sendMessage(msg("messages.no-permission"));
                return true;
            }
            reload();
            sender.sendMessage(msg("messages.reloaded"));
            return true;
        }

        if (!(sender instanceof Player player)) {
            sender.sendMessage(msg("messages.only-players"));
            return true;
        }

        if (name.equals("setspawn")) {
            if (!player.hasPermission("lobby.setspawn")) {
                player.sendMessage(msg("messages.no-permission"));
                return true;
            }
            saveSpawn(player.getLocation());
            player.sendMessage(msg("messages.spawn-set")
                    .replaceText(b -> b.matchLiteral("{lobby}").replacement(getCurrentServerId())));
            playSound(player, config.getString("sounds.set-spawn", "ENTITY_EXPERIENCE_ORB_PICKUP"));
            return true;
        }

        if (name.equals("spawn")) {
            Location spawn = loadSpawn();
            if (spawn == null) {
                player.sendMessage(msg("messages.no-spawn"));
                return true;
            }
            player.teleport(spawn);
            player.sendMessage(msg("messages.teleported"));
            playSound(player, config.getString("sounds.teleport", "ENTITY_ENDERMAN_TELEPORT"));
            return true;
        }

        return false;
    }

    private void saveSpawn(Location loc) {
        String path = spawnPath();
        config.set(path + ".world", loc.getWorld().getName());
        config.set(path + ".x", loc.getX());
        config.set(path + ".y", loc.getY());
        config.set(path + ".z", loc.getZ());
        config.set(path + ".yaw", loc.getYaw());
        config.set(path + ".pitch", loc.getPitch());
        saveConfigFile();
        cachedSpawn = loc.clone();
    }

    private Location loadSpawn() {
        Location cached = cachedSpawn;
        if (cached != null) return cached;
        String path = spawnPath();
        if (!config.isConfigurationSection(path)) return null;
        String worldName = config.getString(path + ".world");
        if (worldName == null) return null;
        World world = Bukkit.getWorld(worldName);
        if (world == null) return null;
        Location loc = new Location(
                world,
                config.getDouble(path + ".x"),
                config.getDouble(path + ".y"),
                config.getDouble(path + ".z"),
                (float) config.getDouble(path + ".yaw"),
                (float) config.getDouble(path + ".pitch")
        );
        cachedSpawn = loc;
        return loc.clone();
    }

    // ------------------------------------------------------------------
    //  Antivoid
    // ------------------------------------------------------------------

    private void startVoidTask() {
        int interval = antivoidIntervalTicks;
        voidTask = plugin.getServer().getScheduler().runTaskTimer(plugin, () -> {
            if (!antivoidEnabled) return;
            double minY = antivoidMinY;
            for (Player p : Bukkit.getOnlinePlayers()) {
                if (p.getLocation().getY() < minY) {
                    sendToSpawn(p);
                }
            }
        }, interval, interval);
    }

    private void sendToSpawn(Player player) {
        Location spawn = loadSpawn();
        if (spawn != null) {
            player.teleport(spawn);
        } else {
            // Fallback: spawn del mundo
            player.teleport(player.getWorld().getSpawnLocation());
        }
    }

    // ------------------------------------------------------------------
    //  Listeners de protección
    // ------------------------------------------------------------------

    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        Player p = event.getPlayer();
        if (onJoinTeleport) {
            Location spawn = loadSpawn();
            if (spawn != null) p.teleport(spawn);
        }
        if (onJoinHeal) {
            p.setHealth(p.getMaxHealth());
            p.setFoodLevel(20);
            p.setSaturation(20f);
            p.setFireTicks(0);
        }
        if (onJoinAdventure) {
            p.setGameMode(GameMode.ADVENTURE);
        }
        p.setAllowFlight(onJoinAllowFlight);
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onDamage(EntityDamageEvent event) {
        if (!(event.getEntity() instanceof Player)) return;
        if (!antiDamageEnabled) return;
        if (event.getCause() == EntityDamageEvent.DamageCause.VOID && !antiDamageCancelVoid) {
            return;
        }
        event.setCancelled(true);
    }

    @EventHandler(ignoreCancelled = true)
    public void onHunger(FoodLevelChangeEvent event) {
        if (!antiHungerEnabled) return;
        event.setCancelled(true);
        if (event.getEntity() instanceof Player p) {
            p.setFoodLevel(20);
            p.setSaturation(20f);
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onDeath(PlayerDeathEvent event) {
        if (!antiDeathKeepInventory) return;
        event.setKeepInventory(true);
        event.getDrops().clear();
        event.setKeepLevel(true);
        event.setDroppedExp(0);
    }

    @EventHandler
    public void onRespawn(PlayerRespawnEvent event) {
        Location spawn = loadSpawn();
        if (spawn != null) event.setRespawnLocation(spawn);
    }

    @EventHandler(ignoreCancelled = true)
    public void onDrop(PlayerDropItemEvent event) {
        if (!antiDropEnabled) return;
        Player p = event.getPlayer();
        if (p.hasPermission("lobby.bypass.drop")) return;

        ItemStack dropped = event.getItemDrop().getItemStack();
        if (antiDropProtectedOnly) {
            if (isProtectedLobbyItem(dropped)) event.setCancelled(true);
        } else {
            event.setCancelled(true);
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void onInvClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player p)) return;
        if (p.hasPermission("lobby.bypass.inventory")) return;
        if (isProtectedLobbyItem(event.getCurrentItem())
                || isProtectedLobbyItem(event.getCursor())) {
            event.setCancelled(true);
            return;
        }
        if (antiInventoryMoveEnabled && p.getGameMode() != GameMode.CREATIVE) {
            event.setCancelled(true);
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void onInvDrag(InventoryDragEvent event) {
        if (!(event.getWhoClicked() instanceof Player p)) return;
        if (p.hasPermission("lobby.bypass.inventory")) return;
        if (antiInventoryMoveEnabled && p.getGameMode() != GameMode.CREATIVE) {
            event.setCancelled(true);
            return;
        }
        for (ItemStack i : event.getNewItems().values()) {
            if (isProtectedLobbyItem(i)) {
                event.setCancelled(true);
                return;
            }
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void onBlockBreak(BlockBreakEvent event) {
        if (!antiBuildEnabled) return;
        if (event.getPlayer().hasPermission("lobby.bypass.build")) return;
        event.setCancelled(true);
    }

    @EventHandler(ignoreCancelled = true)
    public void onBlockPlace(BlockPlaceEvent event) {
        if (!antiBuildEnabled) return;
        if (event.getPlayer().hasPermission("lobby.bypass.build")) return;
        event.setCancelled(true);
    }

    @EventHandler(ignoreCancelled = true)
    public void onBucketEmpty(PlayerBucketEmptyEvent event) {
        if (!antiBuildEnabled) return;
        if (event.getPlayer().hasPermission("lobby.bypass.build")) return;
        event.setCancelled(true);
    }

    @EventHandler(ignoreCancelled = true)
    public void onBucketFill(PlayerBucketFillEvent event) {
        if (!antiBuildEnabled) return;
        if (event.getPlayer().hasPermission("lobby.bypass.build")) return;
        event.setCancelled(true);
    }

    @EventHandler(ignoreCancelled = true)
    public void onPortalCreate(PortalCreateEvent event) {
        if (antiBuildEnabled) event.setCancelled(true);
    }

    @EventHandler(ignoreCancelled = true)
    public void onWeatherChange(WeatherChangeEvent event) {
        if (antiWeatherEnabled && event.toWeatherState()) {
            event.setCancelled(true);
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void onEntityTarget(EntityTargetLivingEntityEvent event) {
        if (event.getTarget() instanceof Player && antiMobTargetEnabled) {
            event.setCancelled(true);
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void onItemDamage(PlayerItemDamageEvent event) {
        if (antiItemDamageEnabled) {
            event.setCancelled(true);
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void onProjectile(ProjectileLaunchEvent event) {
        if (!antiProjectilesEnabled) return;
        if (event.getEntity().getShooter() instanceof Player p
                && !p.hasPermission("lobby.bypass.projectiles")) {
            event.setCancelled(true);
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void onPickupArrow(PlayerPickupArrowEvent event) {
        if (antiProjectilesEnabled) event.setCancelled(true);
    }

    @EventHandler(ignoreCancelled = true)
    public void onArmorStand(PlayerArmorStandManipulateEvent event) {
        if (antiBuildEnabled && !event.getPlayer().hasPermission("lobby.bypass.build")) {
            event.setCancelled(true);
        }
    }

    // onMove eliminado a proposito: el chequeo de void ya lo cubre voidTask cada
    // antivoidIntervalTicks. PlayerMoveEvent se dispara varias veces por tick por
    // jugador y antes hacia getBoolean/getDouble sobre YAML en cada uno.

    // ------------------------------------------------------------------
    //  Helpers
    // ------------------------------------------------------------------

    private boolean isProtectedLobbyItem(ItemStack item) {
        if (item == null || !item.hasItemMeta()) return false;
        var pdc = item.getItemMeta().getPersistentDataContainer();
        return pdc.has(selectorKey, PersistentDataType.BOOLEAN)
                || pdc.has(lobbyOpenerKey, PersistentDataType.BOOLEAN)
                || pdc.has(cosmeticsKey, PersistentDataType.BOOLEAN);
    }

    private Component msg(String path) {
        return Colorize.parseMessage(config.getString(path, ""));
    }

    private void playSound(Player p, String soundName) {
        if (soundName == null || soundName.isEmpty()) return;
        try {
            p.playSound(p.getLocation(), Sound.valueOf(soundName), 1f, 1f);
        } catch (IllegalArgumentException ignored) {
        }
    }
}
