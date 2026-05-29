package dev.blancocl;

import dev.blancocl.config.ConfigManager;
import dev.blancocl.hologram.HologramRenderer;
import dev.blancocl.hologram.HologramUpdateLoop;
import dev.blancocl.npc.engine.NpcEngine;
import dev.blancocl.npc.engine.NpcLibEngine;
import dev.blancocl.npc.persistence.MySqlNpcRepository;
import dev.blancocl.npc.persistence.NpcRepository;
import dev.blancocl.npc.persistence.RepositoryFactory;
import dev.blancocl.npc.visibility.VisibilityWorker;
import dev.blancocl.packets.PacketBatcher;
import dev.blancocl.services.HologramManagerImpl;
import dev.blancocl.services.NpcManagerImpl;
import dev.blancocl.services.Service;
import dev.blancocl.services.SkinManagerImpl;
import dev.blancocl.services.VelocityManagerImpl;
import dev.blancocl.skin.SkinCache;
import dev.blancocl.skin.SkinLoadQueue;
import dev.blancocl.skin.MojangClient;
import dev.blancocl.util.Profiler;
import dev.blancocl.util.Threading;
import dev.blancocl.velocity.NpcSyncMessenger;
import dev.blancocl.velocity.PluginMessenger;
import dev.blancocl.velocity.ServerStateCache;

import java.util.ArrayDeque;
import java.util.Deque;

/**
 * Tiny purpose-built DI container. Services are constructed lazily in
 * {@link #boot()} so cyclic references are explicit and order is auditable.
 * Disposal happens LIFO in {@link #shutdown()}.
 */
public final class ServiceContainer {

    private final NpcPlugin plugin;
    private final Deque<Service> shutdownOrder = new ArrayDeque<>();

    // Infra
    private Threading       threading;
    private Profiler        profiler;
    private ConfigManager   configManager;

    // Skin
    private MojangClient    mojangClient;
    private SkinCache       skinCache;
    private SkinLoadQueue   skinLoadQueue;
    private SkinManagerImpl skinManager;

    // NPC
    private NpcEngine          npcEngine;
    private NpcRepository      npcRepository;
    private VisibilityWorker   visibilityWorker;
    private PacketBatcher      packetBatcher;
    private NpcManagerImpl     npcManager;
    private NpcSyncMessenger   syncMessenger;

    // Holograms
    private HologramRenderer    hologramRenderer;
    private HologramUpdateLoop  hologramLoop;
    private HologramManagerImpl hologramManager;

    // Velocity
    private PluginMessenger     pluginMessenger;
    private ServerStateCache    serverStateCache;
    private VelocityManagerImpl velocityManager;

    public ServiceContainer(NpcPlugin plugin) { this.plugin = plugin; }

    public void boot() throws Exception {
        configManager = new ConfigManager(plugin);
        configManager.loadAll();

        var cfg = configManager.config();
        profiler  = new Profiler(cfg.debugProfilerEnabled());
        threading = new Threading(plugin, cfg.visibilityParallelism(), cfg.skinVirtualThreads());

        // Skin pipeline
        mojangClient  = new MojangClient(threading, cfg);
        skinCache     = new SkinCache(plugin, cfg);
        skinLoadQueue = new SkinLoadQueue(threading, mojangClient, skinCache);
        skinManager   = new SkinManagerImpl(skinCache, skinLoadQueue);
        startup(skinManager);

        // Velocity
        pluginMessenger  = new PluginMessenger(plugin, cfg);
        serverStateCache = new ServerStateCache(threading, pluginMessenger, cfg);
        velocityManager  = new VelocityManagerImpl(pluginMessenger, serverStateCache);
        startup(velocityManager);

        // NPC core
        npcEngine        = new NpcLibEngine(plugin, threading, configManager);
        ((NpcLibEngine) npcEngine).setDebug(cfg.debugEnabled());
        npcRepository    = RepositoryFactory.create(plugin, threading, cfg);
        packetBatcher    = new PacketBatcher(plugin, cfg);
        visibilityWorker = new VisibilityWorker(plugin, threading, cfg, profiler);
        npcManager       = new NpcManagerImpl(plugin, threading, npcEngine, npcRepository,
                                              visibilityWorker, configManager, pluginMessenger);

        // Cross-server sync: only when persistence is actually on a shared backend.
        if (npcRepository instanceof MySqlNpcRepository && cfg.persistenceSyncEnabled()) {
            syncMessenger = new NpcSyncMessenger(plugin, () -> threading.onMain(npcManager::reload));
            syncMessenger.enable();
            npcManager.setSyncMessenger(syncMessenger);
        }

        // Holograms must be constructed BEFORE npcManager.enable() so snapshot install
        // can call hologramManager.attach(...).
        hologramRenderer = new HologramRenderer(plugin, threading);
        hologramLoop     = new HologramUpdateLoop(threading, cfg, hologramRenderer);
        hologramManager  = new HologramManagerImpl(hologramRenderer, hologramLoop, npcManager);

        npcManager.setHologramManager(hologramManager);
        npcManager.setSkinManager(skinManager);

        startup(npcManager);
        startup(hologramManager);
    }

    public void shutdown() {
        while (!shutdownOrder.isEmpty()) {
            try { shutdownOrder.pop().disable(); }
            catch (Throwable t) { plugin.getLogger().warning("Service disable failed: " + t); }
        }
        if (syncMessenger != null) syncMessenger.disable();
        if (npcRepository instanceof MySqlNpcRepository my) {
            try { my.close(); } catch (Throwable ignored) {}
        }
        if (threading != null) threading.shutdown(configManager.config().shutdownTimeoutMs());
    }

    public void reload() {
        configManager.loadAll();
        Mini_refresh();
        // Reload only the services that hold cached config copies
        velocityManager.reload();
        npcManager.reload();
        hologramManager.reload();
        skinManager.reload();
    }

    private static void Mini_refresh() { dev.blancocl.util.Mini.refreshIntegrations(); }

    private void startup(Service service) throws Exception {
        service.enable();
        shutdownOrder.push(service);
    }

    // ---- accessors used by NpcPlugin / commands / listeners ----

    public NpcPlugin            plugin()           { return plugin; }
    public Threading            threading()        { return threading; }
    public Profiler             profiler()         { return profiler; }
    public ConfigManager        config()           { return configManager; }
    public NpcManagerImpl       npcs()             { return npcManager; }
    public HologramManagerImpl  holograms()        { return hologramManager; }
    public SkinManagerImpl      skins()            { return skinManager; }
    public VelocityManagerImpl  velocity()         { return velocityManager; }
    public NpcEngine            engine()           { return npcEngine; }
    public PacketBatcher        packetBatcher()    { return packetBatcher; }
    public VisibilityWorker     visibilityWorker() { return visibilityWorker; }
}
