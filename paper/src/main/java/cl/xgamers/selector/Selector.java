package cl.xgamers.selector;

import cl.xgamers.selector.lobby.LobbyRoutingService;
import cl.xgamers.selector.utils.Colorize;
import cl.xgamers.selector.utils.Heads;
import cl.xgamers.selector.velocity.VelocityBridge;
import fr.mrmicky.fastinv.FastInv;
import fr.mrmicky.fastinv.ItemBuilder;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.Sound;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.scheduler.BukkitTask;
import cl.xgamers.corebau.CoreBauPlugin;
import cl.xgamers.corebau.module.Module;
import org.bukkit.configuration.file.YamlConfiguration;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import net.kyori.adventure.title.Title;

import java.time.Duration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

public class Selector implements Module, Listener {

    private CoreBauPlugin plugin;
    private java.io.File configFile;
    private FileConfiguration config;
    private boolean preventClose = false;
    private VelocityBridge velocityBridge;
    private LobbyRoutingService lobbyRouting;
    private LobbyGUI lobbyGUI;

    private final Map<UUID, PendingGui> pendingGUIRequests = new ConcurrentHashMap<>();
    private final Set<UUID> playersWithServerGUI = ConcurrentHashMap.newKeySet();
    private final Map<UUID, Map<Integer, String>> serverSlotIndex = new ConcurrentHashMap<>();

    private NamespacedKey selectorKey;
    private NamespacedKey lobbyOpenerKey;
    private NamespacedKey cosmeticsKey;
    private BukkitTask refreshTask;

    private enum PendingGui {
        SERVER,
        LOBBY
    }

    @Override
    public String id() {
        return "selector";
    }

    @Override
    public void enable(CoreBauPlugin plugin) {
        this.plugin = plugin;
        saveDefaultConfig();
        config = getConfig();
        loadConfig();

        selectorKey    = new NamespacedKey(plugin, "selector");
        lobbyOpenerKey = new NamespacedKey(plugin, "lobby-opener");
        cosmeticsKey   = new NamespacedKey(plugin, "cosmetics-opener");

        velocityBridge = new VelocityBridge(this);
        velocityBridge.setOnCountsUpdated(event -> onVelocityCountsUpdated(event.triggerPlayer()));
        velocityBridge.register();

        lobbyRouting = new LobbyRoutingService(this);
        lobbyGUI = new LobbyGUI(this, lobbyRouting);

        getServer().getPluginManager().registerEvents(this, plugin);

        getCommand("servidores").setExecutor((sender, command, label, args) -> {
            if (sender instanceof Player player) {
                requestPlayerCountsAndOpenGUI(player);
            } else {
                sender.sendMessage(Colorize.parseMessage(config.getString("messages.only-players")));
            }
            return true;
        });

        getCommand("lobbies").setExecutor((sender, command, label, args) -> {
            if (sender instanceof Player player) {
                requestPlayerCountsAndOpenLobbyGUI(player);
            } else {
                sender.sendMessage(Colorize.parseMessage(config.getString("messages.only-players")));
            }
            return true;
        });

        getCommand("reload").setExecutor((sender, command, label, args) -> {
            if (!sender.hasPermission("selector.reload")) {
                sender.sendMessage(Colorize.parseMessage(config.getString("messages.no-permission")));
                return true;
            }
            reloadConfig();
            config = getConfig();
            loadConfig();
            velocityBridge.clearCounts();
            lobbyRouting.reload();

            for (Player player : getServer().getOnlinePlayers()) {
                updatePlayerItems(player);
            }

            sender.sendMessage(Colorize.parseMessage(config.getString("messages.config-reloaded")));
            return true;
        });

        getLogger().info("Selector cargado (Velocity + cola de lobbies en tiempo real).");
    }

    @Override
    public void disable() {
        stopLobbyRefreshTask();
        if (velocityBridge != null) velocityBridge.unregister();
    }

    private void loadConfig() {
        preventClose = config.getBoolean("gui.prevent-close", false);
    }

    // ---- Métodos delegados al plugin compartido + config propio del módulo ----

    public CoreBauPlugin getPlugin() {
        return plugin;
    }

    public org.bukkit.Server getServer() {
        return plugin.getServer();
    }

    public java.util.logging.Logger getLogger() {
        return plugin.getLogger();
    }

    public org.bukkit.command.PluginCommand getCommand(String name) {
        return plugin.getCommand(name);
    }

    public java.io.File getDataFolder() {
        return plugin.getModuleFolder("selector");
    }

    public void saveDefaultConfig() {
        configFile = new java.io.File(plugin.getModuleFolder("selector"), "config.yml");
        if (!configFile.exists()) plugin.saveResource("selector/config.yml", false);
        config = YamlConfiguration.loadConfiguration(configFile);
        try (InputStream def = plugin.getResource("selector/config.yml")) {
            if (def != null) config.setDefaults(YamlConfiguration.loadConfiguration(
                    new InputStreamReader(def, StandardCharsets.UTF_8)));
        } catch (java.io.IOException ignored) {
        }
    }

    public FileConfiguration getConfig() {
        if (config == null) saveDefaultConfig();
        return config;
    }

    public void reloadConfig() {
        if (configFile == null) configFile = new java.io.File(plugin.getModuleFolder("selector"), "config.yml");
        config = YamlConfiguration.loadConfiguration(configFile);
    }

    public FileConfiguration getPluginConfig() {
        return config;
    }

    public NamespacedKey getLobbyOpenerKey() {
        return lobbyOpenerKey;
    }

    public int getPlayerCount(String serverName) {
        return velocityBridge.getCount(serverName);
    }

    public boolean isServerOnline(String serverName) {
        return velocityBridge.isOnline(serverName);
    }

    public void requestPlayerCounts(Player player) {
        velocityBridge.requestAllCounts(player);
    }

    private void onVelocityCountsUpdated(Player triggerPlayer) {
        PendingGui pending = pendingGUIRequests.remove(triggerPlayer.getUniqueId());

        if (pending == PendingGui.SERVER) {
            openServerGUI(triggerPlayer);
        } else if (pending == PendingGui.LOBBY) {
            lobbyGUI.open(triggerPlayer);
            return;
        } else if (pending == null) {
            if (playersWithServerGUI.contains(triggerPlayer.getUniqueId())) {
                refreshServerGUIs(triggerPlayer);
            }
        }

        lobbyGUI.refreshAllOpen();
    }

    void startLobbyRefreshTask() {
        startRefreshTask();
    }

    void stopLobbyRefreshTask() {
        stopRefreshTask();
    }

    private void startRefreshTask() {
        if (refreshTask != null && !refreshTask.isCancelled()) {
            return;
        }

        int interval = config.getInt("lobby-routing.refresh-interval-ticks", 20);
        refreshTask = getServer().getScheduler().runTaskTimer(plugin, () -> {
            boolean anyOpen = lobbyGUI.hasAnyOpen() || !playersWithServerGUI.isEmpty();
            if (!anyOpen) {
                stopRefreshTask();
                return;
            }

            Player requester = Bukkit.getOnlinePlayers().stream()
                    .filter(p -> lobbyGUI.hasLobbyOpen(p.getUniqueId())
                            || playersWithServerGUI.contains(p.getUniqueId()))
                    .findFirst()
                    .orElse(null);

            if (requester != null) {
                velocityBridge.requestAllCounts(requester);
            }
        }, interval, interval);
    }

    private void stopRefreshTask() {
        if (refreshTask != null) {
            refreshTask.cancel();
            refreshTask = null;
        }
    }

    private void refreshServerGUIs() {
        for (UUID uuid : Set.copyOf(playersWithServerGUI)) {
            refreshServerGUIs(uuid);
        }
    }

    private void refreshServerGUIs(Player player) {
        refreshServerGUIs(player.getUniqueId());
    }

    private void refreshServerGUIs(UUID uuid) {
        Player player = Bukkit.getPlayer(uuid);
        if (player == null || !player.isOnline()) {
            playersWithServerGUI.remove(uuid);
            serverSlotIndex.remove(uuid);
            return;
        }

        Map<Integer, String> slotMap = serverSlotIndex.get(uuid);
        if (slotMap == null) {
            return;
        }

        ConfigurationSection serversSection = config.getConfigurationSection("servers");
        if (serversSection == null) {
            return;
        }

        Inventory top = player.getOpenInventory().getTopInventory();
        for (Map.Entry<Integer, String> entry : slotMap.entrySet()) {
            ConfigurationSection server = serversSection.getConfigurationSection(entry.getValue());
            if (server == null || !server.getBoolean("enabled", true)) {
                continue;
            }

            String serverName = server.getString("server-name");
            int playerCount = getPlayerCount(serverName);
            int maxPlayers = server.getInt("max-players", 100);
            String playersText = playerCount + "/" + maxPlayers + " jugadores";

            top.setItem(entry.getKey(), createServerItem(
                    server.getString("icon"),
                    server.getString("name"),
                    server.getString("description"),
                    playersText, playerCount, maxPlayers
            ));
        }
    }

    private void updatePlayerItems(Player player) {
        for (int i = 0; i < player.getInventory().getSize(); i++) {
            ItemStack item = player.getInventory().getItem(i);
            if (isSelectorItem(item) || lobbyGUI.isLobbyOpenerItem(item) || isCosmeticsItem(item)) {
                player.getInventory().setItem(i, null);
            }
        }

        int selectorSlot = config.getInt("selector-item.slot", 4);
        player.getInventory().setItem(selectorSlot, createSelectorItem());

        if (config.getBoolean("lobby-opener-item.enabled", true)) {
            int lobbySlot = config.getInt("lobby-opener-item.slot", 8);
            player.getInventory().setItem(lobbySlot, lobbyGUI.createOpenerItem());
        }

        if (config.getBoolean("cosmetics-item.enabled", true)) {
            int cosmeticsSlot = config.getInt("cosmetics-item.slot", 0);
            player.getInventory().setItem(cosmeticsSlot, createCosmeticsItem());
        }
    }

    private void requestPlayerCountsAndOpenGUI(Player player) {
        pendingGUIRequests.put(player.getUniqueId(), PendingGui.SERVER);
        requestPlayerCounts(player);

        getServer().getScheduler().runTaskLater(plugin, () -> {
            if (pendingGUIRequests.remove(player.getUniqueId()) == PendingGui.SERVER) {
                openServerGUI(player);
            }
        }, config.getInt("lobby-routing.request-timeout-ticks", 10));
    }

    private void requestPlayerCountsAndOpenLobbyGUI(Player player) {
        pendingGUIRequests.put(player.getUniqueId(), PendingGui.LOBBY);
        requestPlayerCounts(player);

        getServer().getScheduler().runTaskLater(plugin, () -> {
            if (pendingGUIRequests.remove(player.getUniqueId()) == PendingGui.LOBBY) {
                lobbyGUI.open(player);
            }
        }, config.getInt("lobby-routing.request-timeout-ticks", 10));
    }

    public void openServerGUI(Player player) {
        String title = Colorize.format(config.getString("gui.title", "Selector de Servidores"));
        int size = config.getInt("gui.size", 54);

        FastInv gui = new FastInv(size, title);

        if (config.getBoolean("gui.border.enabled")) {
            Material borderMaterial = Material.valueOf(
                    config.getString("gui.border.material", "PURPLE_STAINED_GLASS_PANE"));
            ItemStack borderItem = new ItemBuilder(borderMaterial).name("").build();

            for (int i = 0; i < 9; i++) {
                gui.setItem(i, borderItem);
                gui.setItem(size - 9 + i, borderItem);
            }
            for (int i = 9; i < size - 9; i += 9) {
                gui.setItem(i, borderItem);
                gui.setItem(i + 8, borderItem);
            }
        }

        if (config.getBoolean("gui.player-info.enabled")) {
            int slot = config.getInt("gui.player-info.slot", 4);
            gui.setItem(slot, createPlayerInfoItem(player));
        }

        Map<Integer, String> slots = new HashMap<>();
        ConfigurationSection serversSection = config.getConfigurationSection("servers");
        if (serversSection != null) {
            for (String key : serversSection.getKeys(false)) {
                ConfigurationSection server = serversSection.getConfigurationSection(key);
                if (server == null || !server.getBoolean("enabled", true)) {
                    continue;
                }

                int slot = server.getInt("slot");
                slots.put(slot, key);
                String icon = server.getString("icon");
                String name = server.getString("name");
                String serverName = server.getString("server-name");
                String description = server.getString("description");

                int playerCount = getPlayerCount(serverName);
                int maxPlayers = server.getInt("max-players", 100);
                String players = playerCount + "/" + maxPlayers + " jugadores";

                gui.setItem(slot, createServerItem(icon, name, description, players, playerCount, maxPlayers),
                        event -> {
                            event.setCancelled(true);
                            connectToServer(player, serverName);
                        });
            }
        }

        if (config.getBoolean("gui.close-button.enabled")) {
            int closeSlot = config.getInt("gui.close-button.slot", 49);
            Material closeMaterial = Material.valueOf(
                    config.getString("gui.close-button.material", "BARRIER"));
            String closeName = config.getString("gui.close-button.name", "Cerrar");

            gui.setItem(closeSlot, new ItemBuilder(closeMaterial)
                    .name(Colorize.format(closeName))
                    .lore(Colorize.formatList(config.getStringList("gui.close-button.lore")))
                    .build(), event -> {
                        event.setCancelled(true);
                        player.closeInventory();
                        playSound(player, config.getString("sounds.close-gui"));
                    });
        }

        playersWithServerGUI.add(player.getUniqueId());
        serverSlotIndex.put(player.getUniqueId(), slots);
        playSound(player, config.getString("sounds.open-gui"));
        gui.setCloseFilter(p -> preventClose);
        gui.open(player);
        startRefreshTask();
    }

    private ItemStack createPlayerInfoItem(Player player) {
        ConfigurationSection infoConfig = config.getConfigurationSection("gui.player-info");
        String icon = infoConfig.getString("icon", infoConfig.getString("material", "PLAYER_HEAD"));
        String name = infoConfig.getString("name", "<gold><bold>{player}")
                .replace("{player}", player.getName());

        Map<String, String> placeholders = new HashMap<>();
        placeholders.put("player", player.getName());
        placeholders.put("ping", String.valueOf(player.getPing()));
        placeholders.put("server", getCurrentServer());

        List<String> lore = infoConfig.getStringList("lore").stream()
                .map(line -> Colorize.formatTemplate(line, placeholders))
                .collect(Collectors.toList());

        ItemStack base;
        if (icon != null && (icon.equalsIgnoreCase("PLAYER_HEAD") || icon.equalsIgnoreCase("SKULL"))) {
            base = Heads.fromPlayer(player);
        } else {
            base = Heads.fromIcon(icon);
        }

        return new ItemBuilder(base)
                .name(Colorize.format(name))
                .lore(lore)
                .build();
    }

    private ItemStack createServerItem(String icon, String name, String description, String players,
                                       int currentPlayers, int maxPlayers) {
        boolean glowing = config.getBoolean("gui.server-items.glowing", true);
        ItemStack item = Heads.fromIcon(icon);

        ItemBuilder builder = new ItemBuilder(item).name(Colorize.format(name));

        Map<String, String> placeholders = new HashMap<>();
        placeholders.put("description", description);
        placeholders.put("players", players);
        placeholders.put("current", String.valueOf(currentPlayers));
        placeholders.put("max", String.valueOf(maxPlayers));

        List<String> lore = config.getStringList("gui.server-items.lore").stream()
                .map(line -> Colorize.formatTemplate(line, placeholders))
                .collect(Collectors.toList());

        if (config.getBoolean("gui.server-items.show-progress-bar", true)) {
            lore.add("");
            lore.add(LegacyComponentSerializer.legacySection()
                    .serialize(createProgressBar(currentPlayers, maxPlayers)));
        }

        builder.lore(lore);

        if (glowing) {
            builder.enchant(Enchantment.UNBREAKING, 1).flags(ItemFlag.HIDE_ENCHANTS);
        }

        return builder.build();
    }

    private Component createProgressBar(int current, int max) {
        if (max <= 0) {
            max = 1;
        }

        int percentage = (int) ((double) current / max * 100);
        int filledBars = (int) ((double) current / max * 10);

        NamedTextColor barColor;
        if (percentage < 50) {
            barColor = NamedTextColor.GREEN;
        } else if (percentage < 80) {
            barColor = NamedTextColor.YELLOW;
        } else {
            barColor = NamedTextColor.RED;
        }

        StringBuilder bar = new StringBuilder("§7[");
        for (int i = 0; i < 10; i++) {
            if (i < filledBars) {
                bar.append("§").append(barColor.toString().charAt(1)).append("█");
            } else {
                bar.append("§7█");
            }
        }
        bar.append("§7] §f").append(percentage).append("%");

        return Component.text(bar.toString());
    }

    private ItemStack createSelectorItem() {
        ConfigurationSection selectorConfig = config.getConfigurationSection("selector-item");
        String icon = selectorConfig.getString("icon", selectorConfig.getString("material", "COMPASS"));
        String title = selectorConfig.getString("title");
        String description = selectorConfig.getString("description");

        Map<String, String> placeholders = new HashMap<>();
        placeholders.put("description", description);

        List<String> lore = selectorConfig.getStringList("lore").stream()
                .map(line -> Colorize.formatTemplate(line, placeholders))
                .collect(Collectors.toList());

        ItemStack base = Heads.fromIcon(icon);
        ItemBuilder builder = new ItemBuilder(base)
                .name(Colorize.format(title))
                .lore(lore)
                .meta(meta -> meta.getPersistentDataContainer().set(
                        selectorKey, PersistentDataType.BOOLEAN, true));

        if (selectorConfig.getBoolean("glowing", true)) {
            builder.enchant(Enchantment.UNBREAKING, 1).flags(ItemFlag.HIDE_ENCHANTS);
        }

        return builder.build();
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        updatePlayerItems(player);

        String welcomeMsg = config.getString("messages.welcome",
                "<green>¡Bienvenido! Usa el selector para viajar entre servidores.");
        player.sendMessage(Colorize.parseMessage(welcomeMsg));
        playSound(player, config.getString("sounds.selector-given"));
    }

    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        ItemStack item = event.getItem();
        if (item == null || !item.hasItemMeta()) {
            return;
        }

        Player player = event.getPlayer();

        if (isCosmeticsItem(item)) {
            event.setCancelled(true);
            String cmd = config.getString("cosmetics-item.click-command", "baul cosmetics").trim();
            if (!cmd.isEmpty()) player.performCommand(cmd);
            return;
        }

        if (isSelectorItem(item)) {
            event.setCancelled(true);
            if (pendingGUIRequests.containsKey(player.getUniqueId())
                    || playersWithServerGUI.contains(player.getUniqueId())
                    || lobbyGUI.hasLobbyOpen(player.getUniqueId())) {
                return;
            }
            requestPlayerCountsAndOpenGUI(player);
            return;
        }

        if (lobbyGUI.isLobbyOpenerItem(item)) {
            event.setCancelled(true);
            if (pendingGUIRequests.containsKey(player.getUniqueId())
                    || playersWithServerGUI.contains(player.getUniqueId())
                    || lobbyGUI.hasLobbyOpen(player.getUniqueId())) {
                return;
            }
            requestPlayerCountsAndOpenLobbyGUI(player);
        }
    }

    // -----------------------------------------------------------------------
    //  Ítem de Cosméticos — cofre ender que abre el menú de Baul
    // -----------------------------------------------------------------------

    private ItemStack createCosmeticsItem() {
        ConfigurationSection cfg = config.getConfigurationSection("cosmetics-item");
        if (cfg == null) {
            return new ItemStack(Material.ENDER_CHEST);
        }
        Material material;
        try {
            material = Material.valueOf(cfg.getString("material", "ENDER_CHEST").toUpperCase());
        } catch (IllegalArgumentException e) {
            material = Material.ENDER_CHEST;
        }
        String title = cfg.getString("title", "<light_purple><bold>Cosméticos");
        List<String> lore = cfg.getStringList("lore").stream()
                .map(Colorize::format)
                .collect(Collectors.toList());

        ItemBuilder builder = new ItemBuilder(material)
                .name(Colorize.format(title))
                .lore(lore)
                .meta(meta -> meta.getPersistentDataContainer().set(
                        cosmeticsKey, PersistentDataType.BOOLEAN, true));

        if (cfg.getBoolean("glowing", false)) {
            builder.enchant(Enchantment.UNBREAKING, 1).flags(ItemFlag.HIDE_ENCHANTS);
        }
        return builder.build();
    }

    private boolean isCosmeticsItem(ItemStack item) {
        if (item == null || !item.hasItemMeta()) return false;
        return item.getItemMeta().getPersistentDataContainer()
                .has(cosmeticsKey, PersistentDataType.BOOLEAN);
    }

    private boolean isSelectorItem(ItemStack item) {
        if (item == null || !item.hasItemMeta()) {
            return false;
        }
        return item.getItemMeta().getPersistentDataContainer()
                .has(selectorKey, PersistentDataType.BOOLEAN);
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player player)) {
            return;
        }

        if (event.getClickedInventory() == player.getInventory()
                && (isSelectorItem(event.getCurrentItem())
                        || lobbyGUI.isLobbyOpenerItem(event.getCurrentItem())
                        || isCosmeticsItem(event.getCurrentItem()))) {
            event.setCancelled(true);
            return;
        }

        if (lobbyGUI.hasLobbyOpen(player.getUniqueId())
                && event.getClickedInventory() == event.getView().getTopInventory()) {
            event.setCancelled(true);
            lobbyGUI.handleClick(player, event.getSlot());
            return;
        }

        if (playersWithServerGUI.contains(player.getUniqueId())
                && event.getClickedInventory() == event.getView().getTopInventory()) {
            event.setCancelled(true);

            int slot = event.getSlot();
            ConfigurationSection serversSection = config.getConfigurationSection("servers");
            if (serversSection != null) {
                for (String key : serversSection.getKeys(false)) {
                    ConfigurationSection server = serversSection.getConfigurationSection(key);
                    if (server == null || !server.getBoolean("enabled", true)) {
                        continue;
                    }
                    if (server.getInt("slot") == slot) {
                        connectToServer(player, server.getString("server-name"));
                        return;
                    }
                }
            }

            if (config.getBoolean("gui.close-button.enabled")
                    && config.getInt("gui.close-button.slot", 49) == slot) {
                player.closeInventory();
                playSound(player, config.getString("sounds.close-gui"));
            }
        }
    }

    @EventHandler
    public void onInventoryDrag(InventoryDragEvent event) {
        for (ItemStack item : event.getNewItems().values()) {
            if (isSelectorItem(item) || lobbyGUI.isLobbyOpenerItem(item) || isCosmeticsItem(item)) {
                event.setCancelled(true);
                return;
            }
        }
    }

    @EventHandler
    public void onPlayerDropItem(PlayerDropItemEvent event) {
        Player player = event.getPlayer();
        ItemStack dropped = event.getItemDrop().getItemStack();

        if (isSelectorItem(dropped) || lobbyGUI.isLobbyOpenerItem(dropped) || isCosmeticsItem(dropped)) {
            event.setCancelled(true);
            return;
        }

        if (config.getBoolean("item-protection.prevent-drop.enabled", true)) {
            List<String> protectedGamemodes = config.getStringList("item-protection.prevent-drop.gamemodes");
            boolean allItems = config.getBoolean("item-protection.prevent-drop.all-items", true);

            if (protectedGamemodes.contains(player.getGameMode().toString()) && allItems) {
                event.setCancelled(true);
            }
        }
    }

    @EventHandler
    public void onInventoryClose(InventoryCloseEvent event) {
        UUID uuid = event.getPlayer().getUniqueId();
        playersWithServerGUI.remove(uuid);
        serverSlotIndex.remove(uuid);
        lobbyGUI.onInventoryClose(uuid);
        if (playersWithServerGUI.isEmpty() && !lobbyGUI.hasAnyOpen()) {
            stopRefreshTask();
        }
    }

    public void connectToServer(Player player, String serverName) {
        try {
            velocityBridge.connect(player, serverName);
        } catch (Exception e) {
            getLogger().severe("Error enviando Connect a Velocity: " + e.getMessage());
            player.sendMessage(Colorize.parseMessage("<red>Error al intentar conectar. Contacta a un administrador."));
            return;
        }

        boolean lobbyPreventClose = lobbyGUI.hasLobbyOpen(player.getUniqueId())
                && config.getBoolean("lobby-gui.prevent-close", false);
        if (!preventClose && !lobbyPreventClose) {
            player.closeInventory();
        }

        String connectMsg = config.getString("messages.connecting", "<green>Conectando a <white>{server}<green>...")
                .replace("{server}", serverName);
        player.sendMessage(Colorize.parseMessage(connectMsg));

        if (config.getBoolean("titles.enabled", true)) {
            String titleText = config.getString("titles.title", "<gold><bold>Conectando...")
                    .replace("{server}", serverName);
            String subtitleText = config.getString("titles.subtitle", "<aqua>{server}")
                    .replace("{server}", serverName);

            Title title = Title.title(
                    Colorize.parseMessage(titleText),
                    Colorize.parseMessage(subtitleText),
                    Title.Times.times(
                            Duration.ofMillis(config.getInt("titles.fade-in", 10) * 50L),
                            Duration.ofMillis(config.getInt("titles.stay", 40) * 50L),
                            Duration.ofMillis(config.getInt("titles.fade-out", 10) * 50L)
                    )
            );
            player.showTitle(title);
        }

        playSound(player, config.getString("sounds.connect", "ENTITY_ENDERMAN_TELEPORT"));
    }

    private void playSound(Player player, String soundName) {
        if (!config.getBoolean("sounds.enabled", true) || soundName == null) {
            return;
        }
        try {
            player.playSound(player.getLocation(), Sound.valueOf(soundName), 1.0f, 1.0f);
        } catch (IllegalArgumentException ignored) {
        }
    }

    private String getCurrentServer() {
        return config.getString("gui.player-info.current-server-name", "Lobby");
    }
}
