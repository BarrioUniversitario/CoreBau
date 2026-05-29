package dev.blancocl;

import dev.blancocl.api.NpcAPI;
import dev.blancocl.commands.NpcCommand;
import dev.blancocl.integration.luckperms.LuckPermsHook;
import dev.blancocl.integration.metrics.BStatsHook;
import dev.blancocl.integration.papi.NpcExpansion;
import dev.blancocl.integration.update.UpdateChecker;
import dev.blancocl.listeners.ChunkLoadListener;
import dev.blancocl.listeners.NpcInteractListener;
import dev.blancocl.listeners.PlayerJoinListener;
import dev.blancocl.listeners.PlayerMoveListener;
import dev.blancocl.listeners.PlayerQuitListener;
import dev.blancocl.services.NpcApiImpl;
import org.bukkit.Bukkit;
import org.bukkit.plugin.ServicePriority;
import org.bukkit.plugin.java.JavaPlugin;

/**
 * Entry point for the Npc plugin.
 *
 * <p>Responsibilities are deliberately tiny:</p>
 * <ol>
 *   <li>Construct and boot the {@link ServiceContainer}.</li>
 *   <li>Register Brigadier commands and Bukkit listeners.</li>
 *   <li>Expose the public {@link NpcAPI} via Bukkit's {@code ServicesManager}.</li>
 *   <li>Shut everything down cleanly on disable.</li>
 * </ol>
 */
public final class NpcPlugin implements cl.xgamers.corebau.module.Module, org.bukkit.plugin.Plugin {

    private cl.xgamers.corebau.CoreBauPlugin host;
    private boolean enabledFlag = true;
    private org.bukkit.configuration.file.FileConfiguration moduleConfig;
    private java.io.File moduleConfigFile;

    private ServiceContainer services;
    private NpcAPI api;

    @Override
    public String id() {
        return "npc";
    }

    @Override
    public void enable(cl.xgamers.corebau.CoreBauPlugin host) {
        this.host = host;
        this.enabledFlag = true;
        long t0 = System.currentTimeMillis();
        try {
            this.services = new ServiceContainer(this);
            services.boot();

            this.api = new NpcApiImpl(services);
            Bukkit.getServicesManager().register(NpcAPI.class, api, host, ServicePriority.Normal);

            registerListeners();
            // register() y bStats requieren un JavaPlugin real → usamos el host.
            new NpcCommand(services).register(host);

            initSoftIntegrations();

            getLogger().info("Npc enabled in " + (System.currentTimeMillis() - t0) + "ms.");
        } catch (Throwable t) {
            getLogger().severe("Failed to enable Npc module. Cause: " + t);
            t.printStackTrace();
        }
    }

    @Override
    public void disable() {
        if (api != null) Bukkit.getServicesManager().unregister(NpcAPI.class, api);
        if (services != null) services.shutdown();
        api = null;
        services = null;
    }

    public ServiceContainer services() { return services; }
    public NpcAPI api() { return api; }

    private void registerListeners() {
        var pm = Bukkit.getPluginManager();
        pm.registerEvents(new PlayerJoinListener(services), this);
        pm.registerEvents(new PlayerQuitListener(services), this);
        pm.registerEvents(new PlayerMoveListener(services), this);
        pm.registerEvents(new ChunkLoadListener(services), this);
        pm.registerEvents(new NpcInteractListener(services), this);
    }

    private void initSoftIntegrations() {
        var cfg = services.config().config();

        if (cfg.papiEnabled() && Bukkit.getPluginManager().isPluginEnabled("PlaceholderAPI")) {
            new NpcExpansion(services).register();
            getLogger().info("Hooked into PlaceholderAPI.");
        }
        if (cfg.luckPermsEnabled() && Bukkit.getPluginManager().isPluginEnabled("LuckPerms")) {
            LuckPermsHook.install(services);
            getLogger().info("Hooked into LuckPerms.");
        }
        if (cfg.metricsEnabled()) {
            new BStatsHook(host).start();
        }
        if (cfg.updateCheckerEnabled()) {
            UpdateChecker.checkAsync(host, services.threading());
        }
    }

    // ============================================================
    //  org.bukkit.plugin.Plugin delegado al host CoreBau
    //  (archivos/config scopeados a la subcarpeta "npc/").
    // ============================================================

    @Override
    public java.io.File getDataFolder() {
        return host.getModuleFolder("npc");
    }

    @Override
    public java.io.InputStream getResource(String filename) {
        return host.getResource("npc/" + filename);
    }

    @Override
    public void saveResource(String resourcePath, boolean replace) {
        if (resourcePath == null || resourcePath.isEmpty()) return;
        String path = resourcePath.replace('\\', '/');
        java.io.InputStream in = getResource(path);
        if (in == null) return;
        java.io.File out = new java.io.File(getDataFolder(), path);
        java.io.File dir = out.getParentFile();
        if (!dir.exists()) dir.mkdirs();
        try {
            if (!out.exists() || replace) {
                java.nio.file.Files.copy(in, out.toPath(), java.nio.file.StandardCopyOption.REPLACE_EXISTING);
            }
            in.close();
        } catch (java.io.IOException e) {
            getLogger().warning("No se pudo guardar el recurso npc/" + path + ": " + e.getMessage());
        }
    }

    @Override
    public org.bukkit.configuration.file.FileConfiguration getConfig() {
        if (moduleConfig == null) reloadConfig();
        return moduleConfig;
    }

    @Override
    public void reloadConfig() {
        moduleConfigFile = new java.io.File(getDataFolder(), "config.yml");
        moduleConfig = org.bukkit.configuration.file.YamlConfiguration.loadConfiguration(moduleConfigFile);
        java.io.InputStream def = getResource("config.yml");
        if (def != null) {
            moduleConfig.setDefaults(org.bukkit.configuration.file.YamlConfiguration.loadConfiguration(
                    new java.io.InputStreamReader(def, java.nio.charset.StandardCharsets.UTF_8)));
        }
    }

    @Override
    public void saveConfig() {
        if (moduleConfig == null || moduleConfigFile == null) return;
        try {
            moduleConfig.save(moduleConfigFile);
        } catch (java.io.IOException ignored) {
        }
    }

    @Override
    public void saveDefaultConfig() {
        java.io.File cfg = new java.io.File(getDataFolder(), "config.yml");
        if (!cfg.exists()) saveResource("config.yml", false);
    }

    @Override
    public org.bukkit.Server getServer() {
        return host.getServer();
    }

    @Override
    public java.util.logging.Logger getLogger() {
        return host.getLogger();
    }

    @Override
    public String getName() {
        return "Npc";
    }

    @Override
    public String namespace() {
        return host.namespace();
    }

    @Override
    public boolean isEnabled() {
        return enabledFlag && host != null && host.isEnabled();
    }

    @Override
    @SuppressWarnings("deprecation")
    public org.bukkit.plugin.PluginDescriptionFile getDescription() {
        return host.getDescription();
    }

    @Override
    public io.papermc.paper.plugin.configuration.PluginMeta getPluginMeta() {
        return host.getPluginMeta();
    }

    @Override
    public org.bukkit.plugin.PluginLoader getPluginLoader() {
        return host.getPluginLoader();
    }

    @Override
    public io.papermc.paper.plugin.lifecycle.event.LifecycleEventManager<org.bukkit.plugin.Plugin> getLifecycleManager() {
        return host.getLifecycleManager();
    }

    @Override
    public boolean isNaggable() {
        return host.isNaggable();
    }

    @Override
    public void setNaggable(boolean canNag) {
        host.setNaggable(canNag);
    }

    @Override
    public org.bukkit.generator.ChunkGenerator getDefaultWorldGenerator(String worldName, String id) {
        return null;
    }

    @Override
    public org.bukkit.generator.BiomeProvider getDefaultBiomeProvider(String worldName, String id) {
        return null;
    }

    @Override
    public void onLoad() {
    }

    @Override
    public void onEnable() {
    }

    @Override
    public void onDisable() {
    }

    @Override
    public boolean onCommand(org.bukkit.command.CommandSender sender, org.bukkit.command.Command command,
                             String label, String[] args) {
        return false;
    }

    @Override
    public java.util.List<String> onTabComplete(org.bukkit.command.CommandSender sender, org.bukkit.command.Command command,
                                                String alias, String[] args) {
        return null;
    }
}
