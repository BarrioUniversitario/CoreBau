package cl.xgamers.selector.lobby;

import cl.xgamers.selector.Selector;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
/**
 * Lógica de cola de lobbies sincronizada con Velocity.
 *
 * <h2>Estados (material en GUI)</h2>
 * <ul>
 *   <li>{@link LobbyState#AVAILABLE} → {@code DIAMOND_BLOCK} (verde/disponible)</li>
 *   <li>{@link LobbyState#FULL} → {@code RED_CONCRETE} (lleno, sin cupo)</li>
 *   <li>{@link LobbyState#OFFLINE} / {@link LobbyState#DISABLED} → {@code RED_CONCRETE}</li>
 * </ul>
 *
 * <h2>Modo manual ({@code click-mode: manual})</h2>
 * El jugador elige un slot. Solo conecta al {@code server-name} de ESE lobby si está
 * {@link LobbyState#AVAILABLE}. Si está lleno u offline, el ítem queda desactivado y muestra
 * "Lleno" / "No disponible" — no redirige a otro lobby.
 *
 * <h2>Modo inteligente ({@code click-mode: smart})</h2>
 * Al hacer click en un lobby no disponible, recorre en orden:
 * <ol>
 *   <li>{@code server-name} del lobby clickeado</li>
 *   <li>{@code fallback-server} del lobby (nombre Velocity)</li>
 *   <li>Cadena global {@code auto-route-chain} (claves de {@code lobbies.*})</li>
 * </ol>
 * Conecta al primer servidor con estado {@link LobbyState#AVAILABLE}.
 *
 * <h2>Datos Velocity</h2>
 * Los conteos vienen del canal {@code serverconnector:main} → {@code PlayerCountAll} /
 * {@code PlayerCount}. Un servidor se considera online si aparece en el mapa de conteos.
 */
public class LobbyRoutingService {

    public enum ClickMode {
        MANUAL,
        SMART;

        public static ClickMode fromConfig(String value) {
            if (value != null && value.equalsIgnoreCase("smart")) {
                return SMART;
            }
            return MANUAL;
        }
    }

    private final Selector plugin;
    private Map<String, LobbyDefinition> lobbiesByKey = Collections.emptyMap();
    private Map<String, LobbyDefinition> lobbiesByServer = Collections.emptyMap();
    private List<String> autoRouteChain = Collections.emptyList();
    private ClickMode clickMode = ClickMode.MANUAL;

    public LobbyRoutingService(Selector plugin) {
        this.plugin = plugin;
        reload();
    }

    public void reload() {
        FileConfiguration config = plugin.getPluginConfig();
        Map<String, LobbyDefinition> byKey = new LinkedHashMap<>();
        Map<String, LobbyDefinition> byServer = new LinkedHashMap<>();

        ConfigurationSection lobbiesSection = config.getConfigurationSection("lobbies");
        if (lobbiesSection != null) {
            for (String key : lobbiesSection.getKeys(false)) {
                ConfigurationSection section = lobbiesSection.getConfigurationSection(key);
                if (section == null) {
                    continue;
                }
                LobbyDefinition def = new LobbyDefinition(key, section);
                byKey.put(key, def);
                if (!def.getServerName().isBlank()) {
                    byServer.put(def.getServerName().toLowerCase(), def);
                }
            }
        }

        this.lobbiesByKey = Collections.unmodifiableMap(byKey);
        this.lobbiesByServer = Collections.unmodifiableMap(byServer);

        validateSlotConflicts(lobbiesSection);

        ConfigurationSection routing = config.getConfigurationSection("lobby-routing");
        if (routing != null) {
            this.clickMode = ClickMode.fromConfig(routing.getString("click-mode", "manual"));
            this.autoRouteChain = List.copyOf(routing.getStringList("auto-route-chain"));
        } else {
            this.clickMode = ClickMode.MANUAL;
            this.autoRouteChain = Collections.emptyList();
        }
    }

    public ClickMode getClickMode() {
        return clickMode;
    }

    public Map<String, LobbyDefinition> getLobbiesByKey() {
        return lobbiesByKey;
    }

    public Optional<LobbyDefinition> getLobby(String configKey) {
        return Optional.ofNullable(lobbiesByKey.get(configKey));
    }

    public Optional<LobbyDefinition> getLobbyByServer(String serverName) {
        if (serverName == null || serverName.isBlank()) {
            return Optional.empty();
        }
        return Optional.ofNullable(lobbiesByServer.get(serverName.toLowerCase()));
    }

    /**
     * Estado real del lobby para pintar la GUI (sin aplicar fallback visual).
     */
    public LobbyState resolveDisplayState(LobbyDefinition lobby) {
        if (!lobby.isEnabled()) {
            return LobbyState.DISABLED;
        }

        int count = plugin.getPlayerCount(lobby.getServerName());
        boolean online = plugin.isServerOnline(lobby.getServerName());

        if (lobby.isRequireOnline() && !online) {
            return LobbyState.OFFLINE;
        }

        if (lobby.getMaxPlayers() > 0 && count >= lobby.getMaxPlayers()) {
            return LobbyState.FULL;
        }

        return LobbyState.AVAILABLE;
    }

    public int getPlayerCount(LobbyDefinition lobby) {
        return plugin.getPlayerCount(lobby.getServerName());
    }

    /**
     * Resuelve servidor destino al hacer click en un lobby.
     */
    public LobbyConnectResult resolveConnect(String lobbyKey) {
        Optional<LobbyDefinition> lobbyOpt = getLobby(lobbyKey);
        if (lobbyOpt.isEmpty()) {
            return LobbyConnectResult.denied(LobbyState.DISABLED, lobbyKey);
        }

        LobbyDefinition lobby = lobbyOpt.get();
        LobbyState displayState = resolveDisplayState(lobby);

        if (clickMode == ClickMode.MANUAL) {
            if (displayState == LobbyState.AVAILABLE) {
                return LobbyConnectResult.ok(lobby.getServerName(), lobbyKey, false);
            }
            return LobbyConnectResult.denied(displayState, lobbyKey);
        }

        return resolveSmartConnect(lobby);
    }

    /**
     * Busca el primer lobby disponible en la cadena global (útil para entrada automática).
     */
    public Optional<LobbyConnectResult> resolveBestAvailableLobby() {
        LinkedHashSet<String> visited = new LinkedHashSet<>();

        for (String chainKey : autoRouteChain) {
            Optional<LobbyDefinition> def = getLobby(chainKey);
            if (def.isEmpty()) {
                continue;
            }
            LobbyDefinition lobby = def.get();
            if (visited.add(lobby.getServerName().toLowerCase())
                    && resolveDisplayState(lobby) == LobbyState.AVAILABLE) {
                return Optional.of(LobbyConnectResult.ok(lobby.getServerName(), lobby.getConfigKey(), true));
            }
        }

        for (LobbyDefinition lobby : lobbiesByKey.values()) {
            if (!lobby.isEnabled()) {
                continue;
            }
            if (visited.add(lobby.getServerName().toLowerCase())
                    && resolveDisplayState(lobby) == LobbyState.AVAILABLE) {
                return Optional.of(LobbyConnectResult.ok(lobby.getServerName(), lobby.getConfigKey(), true));
            }
        }

        return Optional.empty();
    }

    private LobbyConnectResult resolveSmartConnect(LobbyDefinition requested) {
        if (resolveDisplayState(requested) == LobbyState.AVAILABLE) {
            return LobbyConnectResult.ok(requested.getServerName(), requested.getConfigKey(), false);
        }
        List<String> serverChain = buildServerChain(requested);

        for (String serverName : serverChain) {
            Optional<LobbyDefinition> def = getLobbyByServer(serverName);
            if (def.isEmpty()) {
                if (plugin.isServerOnline(serverName)
                        && plugin.getPlayerCount(serverName) < Integer.MAX_VALUE) {
                    return LobbyConnectResult.ok(serverName, requested.getConfigKey(), true);
                }
                continue;
            }

            LobbyDefinition lobby = def.get();
            if (resolveDisplayState(lobby) == LobbyState.AVAILABLE) {
                boolean smart = !serverName.equalsIgnoreCase(requested.getServerName());
                return LobbyConnectResult.ok(serverName, requested.getConfigKey(), smart);
            }
        }

        return LobbyConnectResult.denied(resolveDisplayState(requested), requested.getConfigKey());
    }

    private List<String> buildServerChain(LobbyDefinition requested) {
        List<String> chain = new ArrayList<>();
        addServer(chain, requested.getServerName());

        if (!requested.getFallbackServer().isBlank()) {
            addServer(chain, requested.getFallbackServer());
        }

        for (String configKey : autoRouteChain) {
            getLobby(configKey).ifPresent(def -> addServer(chain, def.getServerName()));
        }

        for (LobbyDefinition def : lobbiesByKey.values()) {
            addServer(chain, def.getServerName());
        }

        return chain;
    }

    private void validateSlotConflicts(ConfigurationSection lobbiesSection) {
        if (lobbiesSection == null) {
            return;
        }
        Map<Integer, String> slotMap = new java.util.HashMap<>();
        for (String key : lobbiesSection.getKeys(false)) {
            int slot = lobbiesSection.getInt(key + ".slot", -1);
            if (slot < 0) {
                continue;
            }
            String existing = slotMap.get(slot);
            if (existing != null) {
                plugin.getLogger().warning("Conflicto de slots en lobbies: '" + key
                        + "' y '" + existing + "' comparten el slot " + slot);
            } else {
                slotMap.put(slot, key);
            }
        }
    }

    private static void addServer(List<String> chain, String serverName) {
        if (serverName == null || serverName.isBlank()) {
            return;
        }
        String lower = serverName.toLowerCase();
        boolean exists = chain.stream().anyMatch(s -> s.equalsIgnoreCase(lower));
        if (!exists) {
            chain.add(serverName);
        }
    }
}
