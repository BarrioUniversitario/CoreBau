package me.davidml16.baul;

import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.ProtocolManager;
import com.cryptomorin.xseries.XMaterial;
import lombok.Getter;
import lombok.Setter;
import me.davidml16.baul.animations.Animation;
import me.davidml16.baul.animations.AnimationHandler;
import me.davidml16.baul.api.CubeletsAPI;
import me.davidml16.baul.api.PointsAPI;
import me.davidml16.baul.cosmetics.CosmeticCraftingHandler;
import me.davidml16.baul.cosmetics.CosmeticRegistry;
import me.davidml16.baul.cosmetics.EmoteCooldowns;
import me.davidml16.baul.cosmetics.PreviewManager;
import me.davidml16.baul.cosmetics.render.HatApplier;
import me.davidml16.baul.cosmetics.render.HatGuardListener;
import me.davidml16.baul.cosmetics.render.PetManager;
import me.davidml16.baul.cosmetics.render.PlayerTrailTask;
import me.davidml16.baul.cosmetics.render.WingTask;
import me.davidml16.baul.database.DatabaseHandler;
import me.davidml16.baul.events.*;
import me.davidml16.baul.handlers.*;
import me.davidml16.baul.pets.PlayerPetManager;
import me.davidml16.baul.pets.abilities.MountManager;
import me.davidml16.baul.pets.api.PetAPI;
import me.davidml16.baul.pets.autopet.AutoPetRuleService;
import me.davidml16.baul.pets.config.PetItemLoader;
import me.davidml16.baul.pets.config.PetSkinLoader;
import me.davidml16.baul.pets.database.DatabaseManager;
import me.davidml16.baul.pets.hooks.WorldGuardHook;
import me.davidml16.baul.pets.listeners.actions.RulesListener;
import me.davidml16.baul.pets.listeners.actions.SkillsListener;
import me.davidml16.baul.pets.listeners.gui.AutoPetListener;
import me.davidml16.baul.pets.listeners.gui.EXPShareListener;
import me.davidml16.baul.pets.listeners.gui.MenuListener;
import me.davidml16.baul.pets.listeners.gui.PetSelectionListener;
import me.davidml16.baul.pets.listeners.gui.UpgradeMenuListener;
import me.davidml16.baul.pets.listeners.pet.PetItemProtectionListener;
import me.davidml16.baul.pets.listeners.pet.PetRightClickListener;
import me.davidml16.baul.pets.listeners.player.AbilityListener;
import me.davidml16.baul.pets.listeners.player.BlockPlaceListener;
import me.davidml16.baul.pets.listeners.player.PetInteractItemListener;
import me.davidml16.baul.pets.listeners.player.PlayerListener;
import me.davidml16.baul.pets.utils.Messages;
import me.davidml16.baul.holograms.HologramHandler;
import me.davidml16.baul.holograms.HologramImplementation;
import me.davidml16.baul.sync.SyncManager;
import me.davidml16.baul.tasks.*;
import me.davidml16.baul.utils.ConfigUpdater;
import me.davidml16.baul.utils.FireworkUtil;
import me.davidml16.baul.utils.Utils;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandMap;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.plugin.PluginDescriptionFile;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Field;
import java.util.*;

public class Main implements cl.xgamers.corebau.module.Module, org.bukkit.plugin.Plugin {

    public static ConsoleCommandSender log;
    private static Main main;

    // Host JavaPlugin real (CoreBau). Main delega en él toda la API de Plugin
    // (scheduler, eventos, logger, etc.) salvo getDataFolder/getResource/config,
    // que se redirigen a la subcarpeta/recursos "baul/" del módulo.
    private cl.xgamers.corebau.CoreBauPlugin host;
    private boolean enabledFlag = true;
    private org.bukkit.configuration.file.FileConfiguration moduleConfig;
    private File moduleConfigFile;
    @Getter
    private final List<String> templates = Collections.singletonList("example");
    @Getter
    private MetricsLite metrics;
    private CubeletsAPI cubeletsAPI;
    private PointsAPI pointsAPI;
    @Getter
    private ProtocolManager protocolManager;
    @Getter
    private HologramTask hologramTask;
    private DataSaveTask dataSaveTask;
    @Getter
    private LiveGuiTask liveGuiTask;
    private DataCacheTask dataCacheTask;
    @Getter
    private MachineEffectsTask machineEffectsTask;
    @Getter
    private LanguageHandler languageHandler;
    @Getter
    private DatabaseHandler databaseHandler;
    @Getter
    private PlayerDataHandler playerDataHandler;
    @Getter
    private CubeletTypesHandler cubeletTypesHandler;
    @Getter
    private CubeletRarityHandler cubeletRarityHandler;
    @Getter
    private CubeletRewardHandler cubeletRewardHandler;
    @Getter
    private CubeletMachineHandler cubeletMachineHandler;
    @Getter
    private HologramHandler hologramHandler;
    @Getter
    private CubeletOpenHandler cubeletOpenHandler;
    @Getter
    private AnimationHandler animationHandler;
    @Getter
    private CubeletCraftingHandler cubeletCraftingHandler;
    @Getter
    private EconomyHandler economyHandler;
    @Getter
    private LayoutHandler layoutHandler;
    @Getter
    private ConversationHandler conversationHandler;
    @Getter
    private TransactionHandler transactionHandler;
    @Getter
    private MenuHandler menuHandler;
    @Getter
    private FireworkUtil fireworkUtil;
    @Getter
    private PluginHandler pluginHandler;
    @Getter
    @Setter
    private int playerCount;
    @Getter
    private SyncManager syncManager;
    @Getter
    private CosmeticRegistry cosmeticRegistry;
    @Getter
    private PlayerTrailTask playerTrailTask;
    @Getter
    private HatApplier hatApplier;
    @Getter
    private EmoteCooldowns emoteCooldowns;
    @Getter
    private PetManager petManager;
    @Getter
    private WingTask wingTask;
    @Getter
    private PreviewManager previewManager;
    @Getter
    private CosmeticCraftingHandler cosmeticCraftingHandler;
    @Getter
    private me.davidml16.baul.pets.registry.PetManager registryPetManager;
    @Getter
    private PlayerPetManager playerPetManager;
    @Getter
    private PetSkinLoader petSkinLoader;
    @Getter
    private PetItemLoader petItemLoader;
    @Getter
    private DatabaseManager databaseManager;
    @Getter
    private AutoPetRuleService autoPetRuleService;
    @Getter
    private MountManager mountManager;
    @Getter
    private WorldGuardHook worldGuardHook;
    private Map<String, Object> settings;
    private CommandMap commandMap;

    public static Main get() {
        return main;
    }

    public static Main getInstance() {
        return main;
    }

    @Override
    public String id() {
        return "baul";
    }

    @Override
    public void enable(cl.xgamers.corebau.CoreBauPlugin host) {
        this.host = host;
        this.enabledFlag = true;
        main = this;
        log = Bukkit.getConsoleSender();
        metrics = new MetricsLite(this, 7349);

        settings = new HashMap<>();

        migrateOldConfig();

        saveDefaultConfig();
        try {
            ConfigUpdater.update(this, "config.yml", new File(main.getDataFolder(), "config.yml"), Collections.emptyList());
        } catch (IOException e) {
            e.printStackTrace();
        }
        reloadConfig();

        migrateCraftingFiles();

        if (!XMaterial.supports(1, 16)) {
            getLogger().severe("***   Plugin only supports 1.16+ versions.");
            getLogger().severe("***   If you are using 1.8+ versions, please use the latest plugin version 2.1.8.");
            getLogger().severe("***   Or if you are using 1.13+ versions, please use the latest plugin version 2.4.7.");
            setEnabled(false);
            return;
        }

        if (!Bukkit.getPluginManager().isPluginEnabled("ProtocolLib")) {
            getLogger().severe("*** ProtocolLib is not installed or not enabled. ***");
            getLogger().severe("*** This plugin will be disabled. ***");
            setEnabled(false);
            return;
        }

        protocolManager = ProtocolLibrary.getProtocolManager();

        registerSettings();

        pluginHandler = new PluginHandler(this);

        transactionHandler = new TransactionHandler(this);

        languageHandler = new LanguageHandler(this, getConfig().getString("Language").toLowerCase());
        languageHandler.pushMessages();

        databaseHandler = new DatabaseHandler(this);
        databaseHandler.openConnection();
        databaseHandler.loadTables();

        animationHandler = new AnimationHandler(this);
        animationHandler.loadAnimations();

        cubeletMachineHandler = new CubeletMachineHandler(this);
        cubeletMachineHandler.loadMachines();
        cubeletMachineHandler.setClickType(getConfig().getString("CubeletMachine.ClickType"));

        cubeletTypesHandler = new CubeletTypesHandler(this);
        cubeletTypesHandler.loadTypes();

        cubeletRarityHandler = new CubeletRarityHandler(this);
        cubeletRarityHandler.loadRarities();

        cubeletRewardHandler = new CubeletRewardHandler(this);
        cubeletRewardHandler.loadRewards();

        cubeletTypesHandler.printLog();

        economyHandler = new EconomyHandler();
        economyHandler.load();

        cubeletCraftingHandler = new CubeletCraftingHandler(this);
        cubeletCraftingHandler.loadCrafting();

        cosmeticRegistry = new CosmeticRegistry(this);
        cosmeticRegistry.load();

        cosmeticCraftingHandler = new CosmeticCraftingHandler(this);
        if (cosmeticRegistry.isEnabled()) cosmeticCraftingHandler.load();

        playerDataHandler = new PlayerDataHandler(this);

        hologramHandler = new HologramHandler(this);

        if (hologramHandler.getImplementation() == null) {
            getLogger().severe("*** HolographicDisplays or Decent Holograms is not installed or not enabled. ***");
            getLogger().severe("*** Now the plugin will be disabled. ***");
            setEnabled(false);
            return;
        }

        int distance = getConfig().getInt("Holograms.VisibilityDistance");
        hologramHandler.setVisibilityDistance(distance * distance);

        hologramHandler.getColorAnimation().setColors(getConfig().getStringList("Holograms.ColorAnimation"));
        hologramHandler.getImplementation().loadHolograms();

        playerDataHandler.loadAllPlayerData();

        hologramTask = new HologramTask(this);
        hologramTask.start();

        dataSaveTask = new DataSaveTask(this);
        dataSaveTask.start();

        dataCacheTask = new DataCacheTask(this);
        dataCacheTask.start();

        machineEffectsTask = new MachineEffectsTask(this);
        machineEffectsTask.start();

        playerTrailTask = new PlayerTrailTask(this);
        if (cosmeticRegistry.isEnabled()) playerTrailTask.start();

        hatApplier = new HatApplier();
        emoteCooldowns = new EmoteCooldowns(this);
        petManager = new PetManager(this);
        if (cosmeticRegistry.isEnabled()) petManager.start();
        wingTask = new WingTask(this);
        if (cosmeticRegistry.isEnabled()) wingTask.start();
        previewManager = new PreviewManager(this);

        cubeletOpenHandler = new CubeletOpenHandler(this);

        liveGuiTask = new LiveGuiTask(this);
        if (isSetting("LiveGuiUpdates")) liveGuiTask.start();

        layoutHandler = new LayoutHandler(this);

        menuHandler = new MenuHandler(this);

        menuHandler.setClickType(getConfig().getString("Rewards.Preview.ClickType"));

        conversationHandler = new ConversationHandler(this);

        fireworkUtil = new FireworkUtil(this);

        cubeletsAPI = new CubeletsAPI(this);
        pointsAPI = new PointsAPI(this);

        initPetsSubsystem();

        registerCommands();
        registerEvents();

        syncManager = new SyncManager(this);

        playerCount = getServer().getOnlinePlayers().size();

        String authors = String.join(", ", getDescription().getAuthors());

        PluginDescriptionFile pdf = getDescription();
        log.sendMessage(me.davidml16.baul.utils.Colorize.format("  &eBaul Enabled!"));
        log.sendMessage(me.davidml16.baul.utils.Colorize.format("    &aVersion: &b" + pdf.getVersion()));
        log.sendMessage(me.davidml16.baul.utils.Colorize.format("    &aAuthor: &b" + authors));
        log.sendMessage("");

        if (Bukkit.getPluginManager().isPluginEnabled("PlaceholderAPI")) {
            new PlaceholderHook(this).register();
            settings.put("placeholderapi", true);
        } else {
            settings.put("placeholderapi", false);
        }

    }

    @Override
    public void disable() {

        PluginDescriptionFile pdf = getDescription();
        String authors = String.join(", ", pdf.getAuthors());
        log.sendMessage("");
        log.sendMessage(me.davidml16.baul.utils.Colorize.format("  &eBaul Disabled!"));
        log.sendMessage(me.davidml16.baul.utils.Colorize.format("    &aVersion: &b" + pdf.getVersion()));
        log.sendMessage(me.davidml16.baul.utils.Colorize.format("    &aAuthor: &b" + authors));
        log.sendMessage("");

        if (hologramHandler != null && hologramHandler.getImplementation() != null)
            hologramHandler.getImplementation().removeHolograms();

        if (hatApplier != null) {
            for (Player p : Bukkit.getOnlinePlayers()) hatApplier.restore(p);
        }

        main.getPlayerDataHandler().saveAllPlayerDataSync();

        for (Animation task : new ArrayList<>(main.getAnimationHandler().getTasks())) {
            task.stop();
        }
        main.getAnimationHandler().getTasks().clear();

        for (Entity entity : main.getAnimationHandler().getEntities()) {
            entity.remove();
        }
        main.getAnimationHandler().getEntities().clear();

        if (hologramTask != null) hologramTask.stop();
        if (dataSaveTask != null) dataSaveTask.stop();
        if (machineEffectsTask != null) machineEffectsTask.stop();
        if (playerTrailTask != null) playerTrailTask.stop();
        if (petManager != null) petManager.stop();
        if (wingTask != null) wingTask.stop();
        shutdownPetsSubsystem();
        if (syncManager != null) syncManager.shutdown();
        if (databaseHandler != null) databaseHandler.getDatabaseConnection().stop();
    }

    private void initPetsSubsystem() {
        try {
            registryPetManager = new me.davidml16.baul.pets.registry.PetManager();
            petSkinLoader = new PetSkinLoader(this);
            petItemLoader = new PetItemLoader(this);
            databaseManager = new DatabaseManager(this);
            databaseManager.initialize();
            playerPetManager = new PlayerPetManager(this);
            autoPetRuleService = new AutoPetRuleService(this);
            mountManager = new MountManager();

            PetAPI.init(this);
            Messages.load(this);

            registryPetManager.initializePets();
            getLogger().info(registryPetManager.getRegistryInfo());

            petSkinLoader.loadSkins();
            petItemLoader.loadItems();

            if (Bukkit.getPluginManager().isPluginEnabled("WorldGuard")) {
                try {
                    worldGuardHook = new WorldGuardHook();
                    worldGuardHook.setupWorldGuard(this);
                    getLogger().info("Pets subsystem hooked into WorldGuard");
                } catch (Exception ex) {
                    getLogger().warning("Pets WorldGuard hook failed: " + ex.getMessage());
                }
            }

            new SkillsListener(this);
            new MenuListener(this);
            new PlayerListener(this);
            new PetInteractItemListener(this);
            new AbilityListener(this);
            new PetRightClickListener(this);
            new BlockPlaceListener(this);
            new EXPShareListener(this);
            new PetItemProtectionListener(this);
            new UpgradeMenuListener(this);
            new PetSelectionListener(this);
            new AutoPetListener(this);
            new RulesListener(this);

            getLogger().info("Pets subsystem ready");
        } catch (Exception e) {
            getLogger().severe("Failed to initialize pets subsystem: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void shutdownPetsSubsystem() {
        try {
            if (playerPetManager != null) {
                for (Player p : Bukkit.getOnlinePlayers()) {
                    if (playerPetManager.getActivePet(p) != null) {
                        playerPetManager.deactivatePet(p);
                    }
                }
                playerPetManager.cleanup();
            }
        } catch (Exception e) {
            getLogger().warning("Pets subsystem shutdown error: " + e.getMessage());
        }
    }

    public void registerSettings() {
        settings.put("Crafting", getConfig().getBoolean("Crafting"));

        settings.put("Rewards.Broadcast", getConfig().getBoolean("Rewards.Broadcast"));

        settings.put("LoginReminder", getConfig().getBoolean("LoginReminder"));

        settings.put("CubeletsCommand", getConfig().getBoolean("NoCubelets.ExecuteCommand"));
        settings.put("NoCubelets.ExecuteCommand", getConfig().getBoolean("NoCubelets.ExecuteCommand"));
        settings.put("NoCubelets.Command", getConfig().getString("NoCubelets.Command"));
        settings.put("NoCubelets.Executor", getConfig().getString("NoCubelets.Executor"));

        settings.put("Rewards.Duplication.Enabled", getConfig().getBoolean("Rewards.Duplication.Enabled"));
        settings.put("Rewards.Duplication.PointsCommand", getConfig().getString("Rewards.Duplication.PointsCommand"));
        settings.put("Rewards.PermissionCommand", getConfig().getString("Rewards.PermissionCommand"));

        settings.put("NoGuiMode", getConfig().getBoolean("NoGuiMode"));

        settings.put("AnimationsByPlayer", getConfig().getBoolean("AnimationsByPlayer"));

        settings.put("SerializeBase64", getConfig().getBoolean("SerializeBase64"));
        settings.put("Rewards.AutoSorting", getConfig().getBoolean("Rewards.AutoSorting"));
        settings.put("UseKeys", getConfig().getBoolean("UseKeys"));

        settings.put("HDVisibleToAllPlayers", getConfig().getBoolean("Holograms.Duplication.VisibleToAllPlayers"));

        settings.put("LiveGuiUpdates", getConfig().getBoolean("LiveGuiUpdates"));

        settings.put("Rewards.Preview.Enabled", getConfig().getBoolean("Rewards.Preview.Enabled"));

        settings.put("GiftCubeletsCommand", getConfig().getBoolean("GiftCubeletsCommand"));

        settings.put("GiftMenuSpareHeadType", getConfig().getString("GiftMenuSpareHeadType"));
        settings.put("GiftMenuSpareHeadValue", getConfig().getString("GiftMenuSpareHeadValue"));
    }

    public boolean isSetting(String key) {
        return settings.containsKey(key) && (boolean) settings.get(key);
    }

    public String getSetting(String key) {
        return settings.containsKey(key) ? (String) settings.get(key) : "";
    }

    public DatabaseHandler getDatabase() {
        return databaseHandler;
    }

    public CubeletMachineHandler getCubeletBoxHandler() {
        return cubeletMachineHandler;
    }

    public HologramImplementation getHologramImplementation() {
        return hologramHandler.getImplementation();
    }

    public boolean playerHasPermission(Player p, String permission) {
        return p.hasPermission(permission) || p.isOp();
    }

    /**
     * Pre-handler migration: merges legacy {@code cubelet-crafting.yml} and
     * {@code cosmetic-crafting.yml} into a single {@code crafting.yml} on first
     * run. Old files are renamed to {@code .bak} for safety, not deleted.
     */
    private void migrateCraftingFiles() {
        File dataFolder = getDataFolder();
        File unified = new File(dataFolder, "crafting.yml");
        if (unified.exists()) return;

        File oldCubelet = new File(dataFolder, "cubelet-crafting.yml");
        File oldCosmetic = new File(dataFolder, "cosmetic-crafting.yml");
        if (!oldCubelet.exists() && !oldCosmetic.exists()) return;

        org.bukkit.configuration.file.YamlConfiguration combined = new org.bukkit.configuration.file.YamlConfiguration();
        if (oldCubelet.exists()) {
            org.bukkit.configuration.file.YamlConfiguration cub = org.bukkit.configuration.file.YamlConfiguration.loadConfiguration(oldCubelet);
            if (cub.contains("cubelet-crafting")) combined.set("cubelet-crafting", cub.get("cubelet-crafting"));
        }
        if (oldCosmetic.exists()) {
            org.bukkit.configuration.file.YamlConfiguration cos = org.bukkit.configuration.file.YamlConfiguration.loadConfiguration(oldCosmetic);
            if (cos.contains("cosmetic-crafting")) combined.set("cosmetic-crafting", cos.get("cosmetic-crafting"));
        }
        try {
            combined.save(unified);
            if (oldCubelet.exists()) oldCubelet.renameTo(new File(dataFolder, "cubelet-crafting.yml.bak"));
            if (oldCosmetic.exists()) oldCosmetic.renameTo(new File(dataFolder, "cosmetic-crafting.yml.bak"));
            log.sendMessage(me.davidml16.baul.utils.Colorize.format("  &aMerged cubelet-crafting.yml + cosmetic-crafting.yml into crafting.yml (old files saved as .bak)"));
        } catch (IOException e) {
            getLogger().warning("Failed to merge crafting files: " + e.getMessage());
        }
    }

    private void migrateOldConfig() {
        File oldFolder = new File("plugins/ACubelets");
        File newFolder = getDataFolder();
        if (!oldFolder.exists() || newFolder.exists()) return;

        log.sendMessage(me.davidml16.baul.utils.Colorize.format(""));
        log.sendMessage(me.davidml16.baul.utils.Colorize.format("  <gold>Detectada instalación previa de ACubelets.</gold>"));
        log.sendMessage(me.davidml16.baul.utils.Colorize.format("  <gold>Migrando archivos a plugins/Baul/...</gold>"));

        File[] files = oldFolder.listFiles();
        if (files == null) return;
        for (File file : files) {
            try {
                if (file.isDirectory()) {
                    copyDirectory(file, new File(newFolder, file.getName()));
                } else {
                    java.nio.file.Files.copy(file.toPath(), new File(newFolder, file.getName()).toPath(), java.nio.file.StandardCopyOption.REPLACE_EXISTING);
                }
            } catch (IOException e) {
                log.sendMessage(me.davidml16.baul.utils.Colorize.format("  <red>Error migrando " + file.getName() + ": " + e.getMessage() + "</red>"));
            }
        }

        log.sendMessage(me.davidml16.baul.utils.Colorize.format("  <green>Migración completada!</green>"));
        log.sendMessage(me.davidml16.baul.utils.Colorize.format(""));
    }

    private void copyDirectory(File source, File target) throws IOException {
        if (!target.exists()) target.mkdirs();
        File[] files = source.listFiles();
        if (files == null) return;
        for (File file : files) {
            File dest = new File(target, file.getName());
            if (file.isDirectory()) {
                copyDirectory(file, dest);
            } else {
                java.nio.file.Files.copy(file.toPath(), dest.toPath(), java.nio.file.StandardCopyOption.REPLACE_EXISTING);
            }
        }
    }

    private void registerCommands() {
        Field bukkitCommandMap;
        try {
            bukkitCommandMap = Bukkit.getServer().getClass().getDeclaredField("commandMap");
            bukkitCommandMap.setAccessible(true);
            commandMap = (CommandMap) bukkitCommandMap.get(Bukkit.getServer());
            commandMap.register("baul", new me.davidml16.baul.commands.baul.CoreCommand(getConfig().getString("Commands.Main")
                    .toLowerCase()));
        } catch (NoSuchFieldException | IllegalAccessException e) {
            e.printStackTrace();
        }
    }

    private void registerEvents() {
        Bukkit.getPluginManager().registerEvents(new Event_Interact(this), this);
        Bukkit.getPluginManager().registerEvents(new Event_JoinQuit(this), this);
        Bukkit.getPluginManager().registerEvents(new Event_Damage(), this);
        Bukkit.getPluginManager().registerEvents(new Event_Menus(this), this);
        Bukkit.getPluginManager().registerEvents(new Event_Block(this), this);
        if (cosmeticRegistry != null && cosmeticRegistry.isEnabled()) {
            Bukkit.getPluginManager().registerEvents(new HatGuardListener(this), this);
        }
    }

    // ============================================================
    //  Implementación de org.bukkit.plugin.Plugin delegando al host
    //  (Main no es un JavaPlugin real; el host CoreBau lo es).
    // ============================================================

    public cl.xgamers.corebau.CoreBauPlugin getHost() {
        return host;
    }

    // ---- Archivos / config: redirigidos a la subcarpeta y recursos "baul/" ----

    @Override
    public File getDataFolder() {
        return host.getModuleFolder("baul");
    }

    @Override
    public java.io.InputStream getResource(String filename) {
        return host.getResource("baul/" + filename);
    }

    @Override
    public void saveResource(String resourcePath, boolean replace) {
        if (resourcePath == null || resourcePath.isEmpty()) return;
        String path = resourcePath.replace('\\', '/');
        java.io.InputStream in = getResource(path);
        if (in == null) return;
        File out = new File(getDataFolder(), path);
        File outDir = out.getParentFile();
        if (!outDir.exists()) outDir.mkdirs();
        try {
            if (!out.exists() || replace) {
                java.nio.file.Files.copy(in, out.toPath(),
                        java.nio.file.StandardCopyOption.REPLACE_EXISTING);
            }
            in.close();
        } catch (IOException e) {
            getLogger().warning("No se pudo guardar el recurso " + path + ": " + e.getMessage());
        }
    }

    @Override
    public org.bukkit.configuration.file.FileConfiguration getConfig() {
        if (moduleConfig == null) reloadConfig();
        return moduleConfig;
    }

    @Override
    public void reloadConfig() {
        moduleConfigFile = new File(getDataFolder(), "config.yml");
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
        } catch (IOException e) {
            getLogger().warning("No se pudo guardar config.yml de baul: " + e.getMessage());
        }
    }

    @Override
    public void saveDefaultConfig() {
        File cfg = new File(getDataFolder(), "config.yml");
        if (!cfg.exists()) saveResource("config.yml", false);
    }

    // ---- Delegaciones al host ----

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
        return "Baul";
    }

    @Override
    public String namespace() {
        return host.namespace();
    }

    @Override
    public boolean isEnabled() {
        return enabledFlag && host != null && host.isEnabled();
    }

    /** Sustituye al setEnabled de JavaPlugin: marca el módulo como abortado. */
    public void setEnabled(boolean enabled) {
        this.enabledFlag = enabled;
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

    // ---- Métodos de ciclo de vida de Plugin: inertes (el host gobierna) ----

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
