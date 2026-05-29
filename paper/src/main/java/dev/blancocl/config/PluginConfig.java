package dev.blancocl.config;

import org.bukkit.configuration.file.FileConfiguration;

/**
 * Typed, immutable snapshot of {@code config.yml}. Recreated on reload —
 * never edited in place — so consumers can hold a reference safely.
 */
public final class PluginConfig {

    private final int configVersion;

    // npc-defaults
    private final String npcDefaultType;
    private final int    npcDefaultViewDistance;
    private final boolean npcDefaultLookAtPlayer;
    private final int    npcDefaultLookRadius;
    private final String npcDefaultSkin;

    // view-distance
    private final long    workerIntervalMs;
    private final double  movementEpsilon;
    private final int     maxVisiblePerPlayer;

    // update-intervals
    private final long hologramTickMs;
    private final long velocityStateRefreshMs;

    // packet
    private final boolean packetBatchingEnabled;
    private final int     maxBatchSize;
    private final long    batchFlushMs;

    // async
    private final int     visibilityParallelism;
    private final boolean skinVirtualThreads;

    // persistence
    private final String  persistenceBackend;
    private final String  mysqlHost;
    private final int     mysqlPort;
    private final String  mysqlDatabase;
    private final String  mysqlUser;
    private final String  mysqlPassword;
    private final int     mysqlPoolSize;
    private final boolean persistenceSyncEnabled;

    // velocity
    private final boolean velocityEnabled;
    private final String  velocityChannel;
    private final long    velocityPingTtlMs;
    private final String  velocityDefaultServer;

    // cache
    private final int     skinMemoryMax;
    private final String  skinDiskDir;
    private final long    skinDiskTtlHours;
    private final int     velocityStateMax;

    // hologram
    private final double  hologramLineSpacing;
    private final int     hologramRenderDistance;
    private final boolean useTextDisplays;

    // skin
    private final String      skinFallback;
    private final java.util.List<String> skinPreload;
    private final String      mojangProfileBase;
    private final String      mojangUsernameBase;
    private final int         requestTimeoutMs;
    private final int         maxRetries;

    // debug
    private final boolean debugEnabled;
    private final boolean logPacketCounts;
    private final boolean debugProfilerEnabled;

    // performance
    private final int  maxNpcsPerChunk;
    private final int  chunkIndexShards;
    private final long shutdownTimeoutMs;

    // integrations
    private final boolean papiEnabled;
    private final boolean luckPermsEnabled;
    private final boolean metricsEnabled;
    private final boolean updateCheckerEnabled;

    PluginConfig(FileConfiguration c) {
        configVersion           = c.getInt("config-version", 1);

        npcDefaultType          = c.getString("npc-defaults.type", "PLAYER");
        npcDefaultViewDistance  = c.getInt("npc-defaults.view-distance", 48);
        npcDefaultLookAtPlayer  = c.getBoolean("npc-defaults.look-at-player", true);
        npcDefaultLookRadius    = c.getInt("npc-defaults.look-radius", 12);
        npcDefaultSkin          = c.getString("npc-defaults.default-skin", "MHF_Steve");

        workerIntervalMs        = c.getLong("view-distance.worker-interval-ms", 250);
        movementEpsilon         = c.getDouble("view-distance.movement-epsilon-blocks", 0.75);
        maxVisiblePerPlayer     = c.getInt("view-distance.max-visible-per-player", 200);

        hologramTickMs          = c.getLong("update-intervals.hologram-tick-ms", 50);
        velocityStateRefreshMs  = c.getLong("update-intervals.velocity-state-refresh-ms", 5000);

        packetBatchingEnabled   = c.getBoolean("packet.batching-enabled", true);
        maxBatchSize            = c.getInt("packet.max-batch-size", 64);
        batchFlushMs            = c.getLong("packet.batch-flush-ms", 50);

        visibilityParallelism   = c.getInt("async.visibility-parallelism", 0);
        skinVirtualThreads      = c.getBoolean("async.skin-virtual-threads", true);

        persistenceBackend      = c.getString("persistence.backend", "yaml").toLowerCase();
        mysqlHost               = c.getString("persistence.mysql.host", "localhost");
        mysqlPort               = c.getInt("persistence.mysql.port", 3306);
        mysqlDatabase           = c.getString("persistence.mysql.database", "npcs");
        mysqlUser               = c.getString("persistence.mysql.user", "npc");
        mysqlPassword           = c.getString("persistence.mysql.password", "");
        mysqlPoolSize           = c.getInt("persistence.mysql.pool-size", 4);
        persistenceSyncEnabled  = c.getBoolean("persistence.sync-enabled", true);

        velocityEnabled         = c.getBoolean("velocity.enabled", true);
        velocityChannel         = c.getString("velocity.channel", "bungeecord:main");
        velocityPingTtlMs       = c.getLong("velocity.ping-ttl-seconds", 5) * 1000L;
        velocityDefaultServer   = c.getString("velocity.default-server", "");

        skinMemoryMax           = c.getInt("cache.skin-memory-max", 5000);
        skinDiskDir             = c.getString("cache.skin-disk-dir", "cache/skins");
        skinDiskTtlHours        = c.getLong("cache.skin-disk-ttl-hours", 168);
        velocityStateMax        = c.getInt("cache.velocity-state-max", 256);

        hologramLineSpacing     = c.getDouble("hologram.default-line-spacing", 0.28);
        hologramRenderDistance  = c.getInt("hologram.default-render-distance", 48);
        useTextDisplays         = c.getBoolean("hologram.use-text-displays", true);

        skinFallback            = c.getString("skin.fallback", "MHF_Steve");
        skinPreload             = c.getStringList("skin.preload");
        mojangProfileBase       = c.getString("skin.mojang-base", "https://sessionserver.mojang.com/session/minecraft/profile");
        mojangUsernameBase      = c.getString("skin.username-base", "https://api.mojang.com/users/profiles/minecraft");
        requestTimeoutMs        = c.getInt("skin.request-timeout-ms", 4000);
        maxRetries              = c.getInt("skin.max-retries", 2);

        debugEnabled            = c.getBoolean("debug.enabled", false);
        logPacketCounts         = c.getBoolean("debug.log-packet-counts", false);
        debugProfilerEnabled    = c.getBoolean("debug.profiler-enabled", false);

        maxNpcsPerChunk         = c.getInt("performance.max-npcs-per-chunk", 64);
        chunkIndexShards        = c.getInt("performance.chunk-index-shards", 16);
        shutdownTimeoutMs       = c.getLong("performance.graceful-shutdown-timeout-ms", 2000);

        papiEnabled             = c.getBoolean("integrations.placeholderapi", true);
        luckPermsEnabled        = c.getBoolean("integrations.luckperms", true);
        metricsEnabled          = c.getBoolean("integrations.metrics", true);
        updateCheckerEnabled    = c.getBoolean("integrations.update-checker", true);
    }

    // Accessors (named, not record-style, to keep call sites stable across schema rewrites)
    public int configVersion()                 { return configVersion; }
    public String npcDefaultType()             { return npcDefaultType; }
    public int npcDefaultViewDistance()        { return npcDefaultViewDistance; }
    public boolean npcDefaultLookAtPlayer()    { return npcDefaultLookAtPlayer; }
    public int npcDefaultLookRadius()          { return npcDefaultLookRadius; }
    public String npcDefaultSkin()             { return npcDefaultSkin; }
    public long workerIntervalMs()             { return workerIntervalMs; }
    public double movementEpsilon()            { return movementEpsilon; }
    public int maxVisiblePerPlayer()           { return maxVisiblePerPlayer; }
    public long hologramTickMs()               { return hologramTickMs; }
    public long velocityStateRefreshMs()       { return velocityStateRefreshMs; }
    public boolean packetBatchingEnabled()     { return packetBatchingEnabled; }
    public int maxBatchSize()                  { return maxBatchSize; }
    public long batchFlushMs()                 { return batchFlushMs; }
    public int visibilityParallelism()         { return visibilityParallelism; }
    public boolean skinVirtualThreads()        { return skinVirtualThreads; }
    public String persistenceBackend()         { return persistenceBackend; }
    public String mysqlHost()                  { return mysqlHost; }
    public int mysqlPort()                     { return mysqlPort; }
    public String mysqlDatabase()              { return mysqlDatabase; }
    public String mysqlUser()                  { return mysqlUser; }
    public String mysqlPassword()              { return mysqlPassword; }
    public int mysqlPoolSize()                 { return mysqlPoolSize; }
    public boolean persistenceSyncEnabled()    { return persistenceSyncEnabled; }
    public boolean velocityEnabled()           { return velocityEnabled; }
    public String velocityChannel()            { return velocityChannel; }
    public long velocityPingTtlMs()            { return velocityPingTtlMs; }
    public String velocityDefaultServer()      { return velocityDefaultServer; }
    public int skinMemoryMax()                 { return skinMemoryMax; }
    public String skinDiskDir()                { return skinDiskDir; }
    public long skinDiskTtlHours()             { return skinDiskTtlHours; }
    public int velocityStateMax()              { return velocityStateMax; }
    public double hologramLineSpacing()        { return hologramLineSpacing; }
    public int hologramRenderDistance()        { return hologramRenderDistance; }
    public boolean useTextDisplays()           { return useTextDisplays; }
    public String skinFallback()               { return skinFallback; }
    public java.util.List<String> skinPreload(){ return skinPreload; }
    public String mojangProfileBase()          { return mojangProfileBase; }
    public String mojangUsernameBase()         { return mojangUsernameBase; }
    public int requestTimeoutMs()              { return requestTimeoutMs; }
    public int maxRetries()                    { return maxRetries; }
    public boolean debugEnabled()              { return debugEnabled; }
    public boolean logPacketCounts()           { return logPacketCounts; }
    public boolean debugProfilerEnabled()      { return debugProfilerEnabled; }
    public int maxNpcsPerChunk()               { return maxNpcsPerChunk; }
    public int chunkIndexShards()              { return chunkIndexShards; }
    public long shutdownTimeoutMs()            { return shutdownTimeoutMs; }
    public boolean papiEnabled()               { return papiEnabled; }
    public boolean luckPermsEnabled()          { return luckPermsEnabled; }
    public boolean metricsEnabled()            { return metricsEnabled; }
    public boolean updateCheckerEnabled()      { return updateCheckerEnabled; }
}
