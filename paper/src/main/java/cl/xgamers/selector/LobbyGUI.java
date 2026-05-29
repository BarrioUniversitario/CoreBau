package cl.xgamers.selector;

import cl.xgamers.selector.lobby.LobbyConnectResult;
import cl.xgamers.selector.lobby.LobbyDefinition;
import cl.xgamers.selector.lobby.LobbyRoutingService;
import cl.xgamers.selector.lobby.LobbyState;
import cl.xgamers.selector.utils.Colorize;
import fr.mrmicky.fastinv.FastInv;
import fr.mrmicky.fastinv.ItemBuilder;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

public class LobbyGUI {

    private final Selector plugin;
    private final LobbyRoutingService routing;
    private final Set<UUID> playersWithLobbyGUI = ConcurrentHashMap.newKeySet();
    private final Map<UUID, Map<Integer, String>> slotIndexByPlayer = new ConcurrentHashMap<>();

    public LobbyGUI(Selector plugin, LobbyRoutingService routing) {
        this.plugin = plugin;
        this.routing = routing;
    }

    public void open(Player player) {
        FileConfiguration config = plugin.getPluginConfig();
        ConfigurationSection guiConfig = config.getConfigurationSection("lobby-gui");
        if (guiConfig == null) {
            return;
        }

        int size = guiConfig.getInt("size", 45);
        String title = Colorize.format(guiConfig.getString("title", "Cola de Lobbies"));
        FastInv gui = new FastInv(size, title);

        Map<Integer, String> slotMap = new HashMap<>();

        if (guiConfig.getBoolean("border.enabled", true)) {
            Material borderMaterial = Material.valueOf(
                    guiConfig.getString("border.material", "LIGHT_BLUE_STAINED_GLASS_PANE"));
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

        Material availableMaterial = Material.valueOf(
                guiConfig.getString("available-material", "DIAMOND_BLOCK"));
        Material unavailableMaterial = Material.valueOf(
                guiConfig.getString("unavailable-material", "RED_CONCRETE"));

        for (LobbyDefinition lobby : routing.getLobbiesByKey().values()) {
            int slot = lobby.getSlot();
            slotMap.put(slot, lobby.getConfigKey());

            LobbyState state = routing.resolveDisplayState(lobby);
            Material displayMaterial = state.usesAvailableMaterial() ? availableMaterial : unavailableMaterial;
            ItemStack item = buildLobbyItem(lobby, state, displayMaterial, guiConfig);

            gui.setItem(slot, item, event -> {
                event.setCancelled(true);
                handleLobbyClick(player, lobby.getConfigKey());
            });
        }

        if (guiConfig.getBoolean("close-button.enabled", true)) {
            int closeSlot = guiConfig.getInt("close-button.slot", 40);
            Material closeMaterial = Material.valueOf(
                    guiConfig.getString("close-button.material", "BARRIER"));
            String closeName = guiConfig.getString("close-button.name", "<red><bold>Cerrar");

            gui.setItem(closeSlot, new ItemBuilder(closeMaterial)
                    .name(Colorize.format(closeName))
                    .lore(Colorize.formatList(guiConfig.getStringList("close-button.lore")))
                    .build(), event -> {
                        event.setCancelled(true);
                        player.closeInventory();
                        playSound(player, config.getString("sounds.close-gui", "BLOCK_CHEST_CLOSE"));
                    });
        }

        playersWithLobbyGUI.add(player.getUniqueId());
        slotIndexByPlayer.put(player.getUniqueId(), slotMap);
        gui.setCloseFilter(p -> guiConfig.getBoolean("prevent-close", false));
        playSound(player, config.getString("sounds.open-gui", "BLOCK_CHEST_OPEN"));
        gui.open(player);

        plugin.startLobbyRefreshTask();
    }

    /**
     * Actualiza materiales y lore en tiempo real según conteos Velocity.
     */
    public void refreshAllOpen() {
        FileConfiguration config = plugin.getPluginConfig();
        ConfigurationSection guiConfig = config.getConfigurationSection("lobby-gui");
        if (guiConfig == null) {
            return;
        }

        Material availableMaterial = Material.valueOf(
                guiConfig.getString("available-material", "DIAMOND_BLOCK"));
        Material unavailableMaterial = Material.valueOf(
                guiConfig.getString("unavailable-material", "RED_CONCRETE"));

        for (UUID uuid : Set.copyOf(playersWithLobbyGUI)) {
            Player player = Bukkit.getPlayer(uuid);
            if (player == null || !player.isOnline()) {
                playersWithLobbyGUI.remove(uuid);
                slotIndexByPlayer.remove(uuid);
                continue;
            }

            Inventory top = player.getOpenInventory().getTopInventory();
            if (top.getSize() != guiConfig.getInt("size", 45)) {
                continue;
            }

            Map<Integer, String> slotMap = slotIndexByPlayer.get(uuid);
            if (slotMap == null) {
                continue;
            }

            for (Map.Entry<Integer, String> entry : slotMap.entrySet()) {
                routing.getLobby(entry.getValue()).ifPresent(lobby -> {
                    LobbyState state = routing.resolveDisplayState(lobby);
                    Material material = state.usesAvailableMaterial()
                            ? availableMaterial
                            : unavailableMaterial;
                    top.setItem(entry.getKey(), buildLobbyItem(lobby, state, material, guiConfig));
                });
            }
        }

        if (playersWithLobbyGUI.isEmpty()) {
            plugin.stopLobbyRefreshTask();
        }
    }

    private void handleLobbyClick(Player player, String lobbyKey) {
        FileConfiguration config = plugin.getPluginConfig();
        LobbyConnectResult result = routing.resolveConnect(lobbyKey);

        if (!result.isSuccess()) {
            String template = switch (result.getDeniedState()) {
                case FULL -> config.getString("messages.lobby-full",
                        "<red>El lobby <white>{lobby} <red>está <bold>lleno<red>. Elige otro.");
                case OFFLINE -> config.getString("messages.lobby-offline",
                        "<red>El lobby <white>{lobby} <red>no está en línea.");
                case DISABLED -> config.getString("messages.lobby-disabled",
                        "<red>El lobby <white>{lobby} <red>no está disponible.");
                default -> config.getString("messages.lobby-unavailable",
                        "<red>El lobby <white>{lobby} <red>no está disponible.");
            };
            player.sendMessage(Colorize.parseMessage(
                    template.replace("{lobby}", lobbyKey).replace("{server}", lobbyKey)));
            playSound(player, config.getString("sounds.lobby-denied", "BLOCK_NOTE_BLOCK_BASS"));
            return;
        }

        if (result.isRoutedViaSmart()) {
            String msg = config.getString("messages.lobby-smart-route",
                    "<gold>Lobby lleno. Enviándote a <white>{server}<gold>...");
            player.sendMessage(Colorize.parseMessage(msg
                    .replace("{server}", result.getTargetServer())
                    .replace("{lobby}", result.getSourceLobbyKey())));
        }

        plugin.connectToServer(player, result.getTargetServer());
    }

    public ItemStack createOpenerItem() {
        FileConfiguration config = plugin.getPluginConfig();
        ConfigurationSection opener = config.getConfigurationSection("lobby-opener-item");
        if (opener == null) {
            return new ItemStack(Material.EMERALD);
        }

        Material material = Material.valueOf(opener.getString("material", "EMERALD"));
        String title = opener.getString("title", "<green><bold>Cola de Lobbies");
        String description = opener.getString("description", "<gray>Selecciona un lobby");

        Map<String, String> openerPlaceholders = new HashMap<>();
        openerPlaceholders.put("description", description);

        List<String> lore = opener.getStringList("lore").stream()
                .map(line -> Colorize.formatTemplate(line, openerPlaceholders))
                .collect(Collectors.toList());

        ItemBuilder builder = new ItemBuilder(material)
                .name(Colorize.format(title))
                .lore(lore)
                .meta(meta -> meta.getPersistentDataContainer().set(
                        plugin.getLobbyOpenerKey(),
                        org.bukkit.persistence.PersistentDataType.BOOLEAN,
                        true
                ));

        if (opener.getBoolean("glowing", true)) {
            builder.enchant(Enchantment.UNBREAKING, 1).flags(ItemFlag.HIDE_ENCHANTS);
        }

        return builder.build();
    }

    public boolean isLobbyOpenerItem(ItemStack item) {
        if (item == null || !item.hasItemMeta()) {
            return false;
        }
        return item.getItemMeta().getPersistentDataContainer().has(
                plugin.getLobbyOpenerKey(),
                org.bukkit.persistence.PersistentDataType.BOOLEAN
        );
    }

    public boolean hasLobbyOpen(UUID uuid) {
        return playersWithLobbyGUI.contains(uuid);
    }

    public boolean hasAnyOpen() {
        return !playersWithLobbyGUI.isEmpty();
    }

    public void onInventoryClose(UUID uuid) {
        playersWithLobbyGUI.remove(uuid);
        slotIndexByPlayer.remove(uuid);
        if (playersWithLobbyGUI.isEmpty()) {
            plugin.stopLobbyRefreshTask();
        }
    }

    public boolean handleClick(Player player, int slot) {
        if (!playersWithLobbyGUI.contains(player.getUniqueId())) {
            return false;
        }

        FileConfiguration config = plugin.getPluginConfig();
        ConfigurationSection guiConfig = config.getConfigurationSection("lobby-gui");
        if (guiConfig != null && guiConfig.getBoolean("close-button.enabled", true)
                && guiConfig.getInt("close-button.slot", 40) == slot) {
            player.closeInventory();
            playSound(player, config.getString("sounds.close-gui", "BLOCK_CHEST_CLOSE"));
            return true;
        }

        Map<Integer, String> slotMap = slotIndexByPlayer.get(player.getUniqueId());
        if (slotMap != null && slotMap.containsKey(slot)) {
            handleLobbyClick(player, slotMap.get(slot));
            return true;
        }

        return true;
    }

    private ItemStack buildLobbyItem(LobbyDefinition lobby, LobbyState state,
                                     Material material, ConfigurationSection guiConfig) {
        ConfigurationSection itemsConfig = guiConfig.getConfigurationSection("lobby-items");
        int current = routing.getPlayerCount(lobby);
        int max = lobby.getMaxPlayers();

        String statusText = resolveStatusText(state, itemsConfig);
        String actionText = resolveActionText(state, itemsConfig);

        Map<String, String> placeholders = new HashMap<>();
        placeholders.put("description", lobby.getDescription());
        placeholders.put("status", statusText);
        placeholders.put("players", current + "/" + max);
        placeholders.put("current", String.valueOf(current));
        placeholders.put("max", String.valueOf(max));
        placeholders.put("action", actionText);
        placeholders.put("server", lobby.getServerName());
        placeholders.put("lobby", lobby.getConfigKey());

        List<String> loreTemplate = itemsConfig != null
                ? itemsConfig.getStringList("lore")
                : List.of();
        List<String> lore = loreTemplate.stream()
                .map(line -> Colorize.formatTemplate(line, placeholders))
                .collect(Collectors.toList());

        ItemBuilder builder = new ItemBuilder(material)
                .name(Colorize.format(lobby.getName()))
                .lore(lore);

        if (lobby.isGlowing() && state == LobbyState.AVAILABLE) {
            builder.enchant(Enchantment.UNBREAKING, 1).flags(ItemFlag.HIDE_ENCHANTS);
        }

        return builder.build();
    }

    private String resolveStatusText(LobbyState state, ConfigurationSection itemsConfig) {
        if (itemsConfig == null) {
            return state.name();
        }
        return switch (state) {
            case AVAILABLE -> itemsConfig.getString("available-status", "<green>✔ Disponible");
            case FULL -> itemsConfig.getString("full-status", "<red>✖ Lleno");
            case OFFLINE -> itemsConfig.getString("offline-status", "<red>✖ Offline");
            case DISABLED -> itemsConfig.getString("unavailable-status", "<red>✖ No disponible");
        };
    }

    private String resolveActionText(LobbyState state, ConfigurationSection itemsConfig) {
        if (itemsConfig == null) {
            return "";
        }
        boolean smart = routing.getClickMode() == LobbyRoutingService.ClickMode.SMART;
        return switch (state) {
            case AVAILABLE -> itemsConfig.getString("click-available", "<yellow><bold>▶ Click para entrar");
            case FULL -> smart
                    ? itemsConfig.getString("click-smart-full",
                    "<gold><bold>▶ Click para buscar otro lobby")
                    : itemsConfig.getString("click-unavailable", "<dark_gray>Lleno — no disponible");
            case OFFLINE, DISABLED -> itemsConfig.getString("click-unavailable", "<dark_gray>No puedes entrar");
        };
    }

    private void playSound(Player player, String soundName) {
        FileConfiguration config = plugin.getPluginConfig();
        if (!config.getBoolean("sounds.enabled", true)) {
            return;
        }
        try {
            player.playSound(player.getLocation(), Sound.valueOf(soundName), 1.0f, 1.0f);
        } catch (IllegalArgumentException ignored) {
        }
    }
}
