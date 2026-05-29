package cl.xgamers.selector.lobby;

import org.bukkit.configuration.ConfigurationSection;

public final class LobbyDefinition {

    private final String configKey;
    private final boolean enabled;
    private final int slot;
    private final String serverName;
    private final String fallbackServer;
    private final int maxPlayers;
    private final boolean requireOnline;
    private final boolean glowing;
    private final String name;
    private final String description;

    public LobbyDefinition(String configKey, ConfigurationSection section) {
        this.configKey = configKey;
        this.enabled = section.getBoolean("enabled", true);
        this.slot = section.getInt("slot");
        this.serverName = section.getString("server-name", "");
        this.fallbackServer = section.getString("fallback-server", "");
        this.maxPlayers = section.getInt("max-players", 100);
        this.requireOnline = section.getBoolean("require-online", true);
        this.glowing = section.getBoolean("glowing", true);
        this.name = section.getString("name", configKey);
        this.description = section.getString("description", "");
    }

    public String getConfigKey() {
        return configKey;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public int getSlot() {
        return slot;
    }

    public String getServerName() {
        return serverName;
    }

    public String getFallbackServer() {
        return fallbackServer;
    }

    public int getMaxPlayers() {
        return maxPlayers;
    }

    public boolean isRequireOnline() {
        return requireOnline;
    }

    public boolean isGlowing() {
        return glowing;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }
}
