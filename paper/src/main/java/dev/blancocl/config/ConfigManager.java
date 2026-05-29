package dev.blancocl.config;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.Plugin;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;

/** Owns the three on-disk YAML files and surfaces typed views. */
public final class ConfigManager {

    private final Plugin plugin;
    private final File configFile;
    private final File messagesFile;
    private final File npcsFile;

    private volatile PluginConfig config;
    private volatile Messages messages;
    private volatile YamlConfiguration npcsRaw;

    public ConfigManager(Plugin plugin) {
        this.plugin = plugin;
        File dir = plugin.getDataFolder();
        if (!dir.exists() && !dir.mkdirs()) {
            plugin.getLogger().warning("Failed to create data folder: " + dir);
        }
        this.configFile   = new File(dir, "config.yml");
        this.messagesFile = new File(dir, "messages.yml");
        this.npcsFile     = new File(dir, "npcs.yml");
    }

    public void loadAll() {
        saveDefault(configFile,   "config.yml");
        saveDefault(messagesFile, "messages.yml");
        saveDefault(npcsFile,     "npcs.yml");

        YamlConfiguration cfgRaw = YamlConfiguration.loadConfiguration(configFile);
        if (ConfigMigrator.migrateConfig(cfgRaw, plugin.getLogger())) save(cfgRaw, configFile);
        this.config = new PluginConfig(cfgRaw);

        YamlConfiguration msgRaw = YamlConfiguration.loadConfiguration(messagesFile);
        if (ConfigMigrator.migrateMessages(msgRaw, plugin.getLogger())) save(msgRaw, messagesFile);
        this.messages = new Messages(msgRaw);

        YamlConfiguration npcs = YamlConfiguration.loadConfiguration(npcsFile);
        if (ConfigMigrator.migrateNpcs(npcs, plugin.getLogger())) save(npcs, npcsFile);
        this.npcsRaw = npcs;
    }

    public PluginConfig config()       { return config; }
    public Messages messages()         { return messages; }
    public FileConfiguration npcsRaw() { return npcsRaw; }
    public File npcsFile()             { return npcsFile; }

    /** Atomic save (write to temp, then move). */
    public void save(YamlConfiguration cfg, File target) {
        File tmp = new File(target.getParentFile(), target.getName() + ".tmp");
        try {
            cfg.save(tmp);
            Files.move(tmp.toPath(), target.toPath(),
                    StandardCopyOption.REPLACE_EXISTING, StandardCopyOption.ATOMIC_MOVE);
        } catch (IOException e) {
            plugin.getLogger().warning("Failed to save " + target.getName() + ": " + e);
            try { Files.deleteIfExists(tmp.toPath()); } catch (IOException ignored) {}
        }
    }

    private void saveDefault(File target, String resource) {
        if (target.exists()) return;
        try {
            plugin.saveResource(resource, false);
        } catch (IllegalArgumentException e) {
            plugin.getLogger().warning("Default resource missing: " + resource);
        }
    }
}
