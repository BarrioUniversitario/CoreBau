package me.davidml16.baul.cosmetics;

import me.davidml16.baul.Main;
import me.davidml16.baul.cosmetics.types.Emote;
import me.davidml16.baul.cosmetics.types.Hat;
import me.davidml16.baul.cosmetics.types.JoinEffect;
import me.davidml16.baul.cosmetics.types.Pet;
import me.davidml16.baul.cosmetics.types.Trail;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.entity.EntityType;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CosmeticRegistry {

    private final Main main;
    private final Map<String, Cosmetic> byId = new HashMap<>();
    private final Map<CosmeticCategory, List<Cosmetic>> byCategory = new EnumMap<>(CosmeticCategory.class);
    private boolean enabled;

    public CosmeticRegistry(Main main) {
        this.main = main;
        for (CosmeticCategory c : CosmeticCategory.values()) {
            byCategory.put(c, new ArrayList<>());
        }
    }

    public boolean isEnabled() {
        return enabled;
    }

    public Cosmetic getById(String id) {
        if (id == null) return null;
        return byId.get(id.toLowerCase());
    }

    public List<Cosmetic> getByCategory(CosmeticCategory category) {
        return Collections.unmodifiableList(byCategory.getOrDefault(category, Collections.emptyList()));
    }

    public Map<String, Cosmetic> getAll() {
        return Collections.unmodifiableMap(byId);
    }

    public void load() {
        enabled = main.getConfig().getBoolean("Cosmetics.Enabled", false);
        byId.clear();
        for (List<Cosmetic> list : byCategory.values()) list.clear();

        if (!enabled) {
            main.getLogger().info("Cosmetics disabled in config; registry empty.");
            return;
        }

        File folder = new File(main.getDataFolder(), "cosmetics");
        if (!folder.exists()) folder.mkdirs();
        String[] defaults = {"trails.yml", "hats.yml", "join_effects.yml", "emotes.yml", "pets.yml"};
        for (String f : defaults) {
            if (!new File(folder, f).exists()) main.saveResource("cosmetics/" + f, false);
        }

        loadTrails(new File(folder, "trails.yml"));
        loadHats(new File(folder, "hats.yml"));
        loadJoinEffects(new File(folder, "join_effects.yml"));
        loadEmotes(new File(folder, "emotes.yml"));
        loadPets(new File(folder, "pets.yml"));

        printLog();
    }

    private void loadPets(File file) {
        if (!file.exists()) return;
        FileConfiguration config = YamlConfiguration.loadConfiguration(file);
        for (String key : config.getKeys(false)) {
            ConfigurationSection s = config.getConfigurationSection(key);
            if (s == null) continue;
            String entityName = s.getString("entity", "CHICKEN");
            EntityType type;
            try {
                type = EntityType.valueOf(entityName.toUpperCase());
            } catch (IllegalArgumentException ex) {
                main.getLogger().warning("Unknown entity '" + entityName + "' for pet '" + key + "', using CHICKEN");
                type = EntityType.CHICKEN;
            }
            Pet pet = new Pet(
                    key.toLowerCase(),
                    s.getString("display", key),
                    s.getString("rarity", "COMMON"),
                    s.getString("icon", "BONE"),
                    s.getString("permission", ""),
                    type,
                    s.getString("name", ""),
                    s.getLong("price", 0L)
            );
            register(pet);
        }
    }

    private void loadTrails(File file) {
        if (!file.exists()) return;
        FileConfiguration config = YamlConfiguration.loadConfiguration(file);
        for (String key : config.getKeys(false)) {
            ConfigurationSection section = config.getConfigurationSection(key);
            if (section == null) continue;

            String display = section.getString("display", key);
            String rarity = section.getString("rarity", "COMMON");
            String icon = section.getString("icon", "FEATHER");
            String permission = section.getString("permission", "");

            Particle particle = parseParticle(section.getString("particle", "FLAME"), key);
            String particleData = section.getString("particleData", "");
            int interval = section.getInt("interval", 2);
            int amount = section.getInt("amount", 3);
            double speed = section.getDouble("speed", 0.05);

            java.util.List<org.bukkit.Color> colors = new java.util.ArrayList<>();
            if (section.isList("colors")) {
                for (String c : section.getStringList("colors")) {
                    try {
                        colors.add(me.davidml16.baul.utils.StringUtils.parseColor(c));
                    } catch (Exception ex) {
                        main.getLogger().warning("Bad color '" + c + "' for trail '" + key + "'; skipping");
                    }
                }
            }
            int cycleTicks = section.getInt("colorCycleTicks", 4);

            Trail trail = new Trail(key.toLowerCase(), display, rarity, icon, permission,
                    particle, particleData, interval, amount, speed,
                    colors.isEmpty() ? null : colors, cycleTicks,
                    section.getLong("price", 0L));
            register(trail);
        }
    }

    private void loadHats(File file) {
        if (!file.exists()) return;
        FileConfiguration config = YamlConfiguration.loadConfiguration(file);
        for (String key : config.getKeys(false)) {
            ConfigurationSection section = config.getConfigurationSection(key);
            if (section == null) continue;

            String display = section.getString("display", key);
            String rarity = section.getString("rarity", "COMMON");
            String icon = section.getString("icon", "PUMPKIN");
            String permission = section.getString("permission", "");

            String materialName = section.getString("material", "PUMPKIN");
            Material material;
            try {
                material = Material.valueOf(materialName.toUpperCase());
            } catch (IllegalArgumentException e) {
                main.getLogger().warning("Unknown material '" + materialName + "' for hat '" + key + "', falling back to PUMPKIN");
                material = Material.PUMPKIN;
            }

            Hat hat = new Hat(key.toLowerCase(), display, rarity, icon, permission, material,
                    section.getLong("price", 0L));
            register(hat);
        }
    }

    private void loadJoinEffects(File file) {
        if (!file.exists()) return;
        FileConfiguration config = YamlConfiguration.loadConfiguration(file);
        for (String key : config.getKeys(false)) {
            ConfigurationSection s = config.getConfigurationSection(key);
            if (s == null) continue;
            Particle particle = parseParticle(s.getString("particle", "CLOUD"), key);
            Sound sound = parseSound(s.getString("sound", ""), key);
            JoinEffect je = new JoinEffect(
                    key.toLowerCase(),
                    s.getString("display", key),
                    s.getString("rarity", "COMMON"),
                    s.getString("icon", "WHITE_WOOL"),
                    s.getString("permission", ""),
                    particle,
                    s.getString("particleData", ""),
                    s.getInt("amount", 30),
                    s.getDouble("spread", 0.5),
                    sound,
                    (float) s.getDouble("pitch", 1.0),
                    s.getBoolean("broadcast", true),
                    s.getLong("price", 0L)
            );
            register(je);
        }
    }

    private void loadEmotes(File file) {
        if (!file.exists()) return;
        FileConfiguration config = YamlConfiguration.loadConfiguration(file);
        for (String key : config.getKeys(false)) {
            ConfigurationSection s = config.getConfigurationSection(key);
            if (s == null) continue;
            Particle particle = parseParticle(s.getString("particle", "HAPPY_VILLAGER"), key);
            Sound sound = parseSound(s.getString("sound", ""), key);
            Emote e = new Emote(
                    key.toLowerCase(),
                    s.getString("display", key),
                    s.getString("rarity", "COMMON"),
                    s.getString("icon", "PAPER"),
                    s.getString("permission", ""),
                    particle,
                    s.getString("particleData", ""),
                    s.getInt("amount", 20),
                    s.getDouble("spread", 0.5),
                    sound,
                    (float) s.getDouble("pitch", 1.0),
                    s.getLong("cooldown", 3000L),
                    s.getLong("price", 0L)
            );
            register(e);
        }
    }

    private Particle parseParticle(String name, String key) {
        try { return Particle.valueOf(name.toUpperCase()); }
        catch (IllegalArgumentException ex) {
            main.getLogger().warning("Unknown particle '" + name + "' for cosmetic '" + key + "', using FLAME");
            return Particle.FLAME;
        }
    }

    private Sound parseSound(String name, String key) {
        if (name == null || name.isEmpty()) return null;
        try { return Sound.valueOf(name.toUpperCase()); }
        catch (IllegalArgumentException ex) {
            main.getLogger().warning("Unknown sound '" + name + "' for cosmetic '" + key + "', omitting");
            return null;
        }
    }

    private void register(Cosmetic cosmetic) {
        if (byId.containsKey(cosmetic.getId())) {
            main.getLogger().warning("Duplicate cosmetic id, skipping: " + cosmetic.getId());
            return;
        }
        byId.put(cosmetic.getId(), cosmetic);
        byCategory.get(cosmetic.getCategory()).add(cosmetic);
    }

    public void printLog() {
        main.getLogger().info("Cosmetics registry:");
        for (CosmeticCategory c : CosmeticCategory.values()) {
            main.getLogger().info("  " + c.getId() + ": " + byCategory.get(c).size());
        }
    }
}
