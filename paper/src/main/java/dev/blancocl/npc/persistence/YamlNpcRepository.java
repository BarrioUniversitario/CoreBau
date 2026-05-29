package dev.blancocl.npc.persistence;

import dev.blancocl.api.npc.NpcType;
import dev.blancocl.util.Locations;
import dev.blancocl.util.Threading;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.Plugin;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

/** YAML-backed repository. All disk I/O runs on {@link Threading#skinIo()}. */
public final class YamlNpcRepository implements NpcRepository {

    private final File file;
    private final Threading threading;

    public YamlNpcRepository(Plugin plugin, Threading threading) {
        this.file = new File(plugin.getDataFolder(), "npcs.yml");
        this.threading = threading;
    }

    @Override
    public CompletableFuture<List<NpcSnapshot>> loadAll() {
        return CompletableFuture.supplyAsync(this::loadSync, threading.skinIo());
    }

    @Override
    public CompletableFuture<Void> saveAll(List<NpcSnapshot> snapshots) {
        return CompletableFuture.runAsync(() -> saveSync(snapshots), threading.skinIo());
    }

    /* ---------------- load ---------------- */

    private List<NpcSnapshot> loadSync() {
        if (!file.exists()) return List.of();
        YamlConfiguration y = YamlConfiguration.loadConfiguration(file);
        ConfigurationSection root = y.getConfigurationSection("npcs");
        if (root == null) return List.of();

        List<NpcSnapshot> out = new ArrayList<>(root.getKeys(false).size());
        for (String id : root.getKeys(false)) {
            ConfigurationSection s = root.getConfigurationSection(id);
            if (s == null) continue;
            try { out.add(parse(id, s)); }
            catch (Throwable t) {
                Bukkit.getLogger().warning("[Npc] Failed to parse NPC '" + id + "': " + t.getMessage());
            }
        }
        return out;
    }

    private NpcSnapshot parse(String id, ConfigurationSection s) {
        String name = s.getString("name", id);
        NpcType type = parseType(s.getString("type", "PLAYER"));
        String skin = s.getString("skin");
        Location loc = Locations.fromSection(s.getConfigurationSection("location"));
        if (loc == null || loc.getWorld() == null) {
            String worldName = s.getString("location.world", "?");
            throw new IllegalStateException("Unknown world '" + worldName + "' for NPC " + id);
        }
        boolean lookAtPlayer = s.getBoolean("look-at-player", true);
        int viewDistance = s.getInt("view-distance", 48);

        List<NpcSnapshot.HologramSnapshot> holos = new ArrayList<>();
        for (Map<?, ?> h : s.getMapList("holograms")) {
            double offY    = number(h.get("offset-y"), 2.3);
            double spacing = number(h.get("line-spacing"), 0.28);
            boolean anim   = bool(h.get("animated"), false);
            List<String> lines = stringList(h.get("lines"));
            holos.add(new NpcSnapshot.HologramSnapshot(offY, spacing, anim, lines));
        }

        Map<String, List<Map<String, Object>>> actions = new LinkedHashMap<>();
        ConfigurationSection actionsSection = s.getConfigurationSection("actions");
        if (actionsSection != null) {
            for (String click : actionsSection.getKeys(false)) {
                List<Map<String, Object>> list = new ArrayList<>();
                for (Map<?, ?> m : actionsSection.getMapList(click)) {
                    Map<String, Object> dict = new LinkedHashMap<>();
                    m.forEach((k, v) -> dict.put(String.valueOf(k), v));
                    list.add(dict);
                }
                actions.put(click, list);
            }
        }

        var anim = new NpcSnapshot.AnimationSnapshot(
                s.getString("animation.idle", "NONE"),
                s.getInt("animation.interval-ticks", 200));

        var vis = new NpcSnapshot.VisibilitySnapshot(
                s.getString("visibility.permission"),
                s.getString("visibility.world"),
                s.getString("visibility.server-online"));

        return new NpcSnapshot(id, name, type, skin, loc, lookAtPlayer, viewDistance, holos, actions, anim, vis);
    }

    /* ---------------- save ---------------- */

    private void saveSync(List<NpcSnapshot> snapshots) {
        YamlConfiguration y = new YamlConfiguration();
        y.set("schema-version", 1);
        for (NpcSnapshot snap : snapshots) {
            String base = "npcs." + snap.id() + ".";
            y.set(base + "name", snap.name());
            y.set(base + "type", snap.type().name());
            if (snap.skin() != null) y.set(base + "skin", snap.skin());
            y.set(base + "location", Locations.toMap(snap.location()));
            y.set(base + "look-at-player", snap.lookAtPlayer());
            y.set(base + "view-distance", snap.viewDistance());

            List<Map<String, Object>> holos = new ArrayList<>();
            for (var h : snap.holograms()) {
                Map<String, Object> hm = new LinkedHashMap<>();
                hm.put("offset-y",     h.offsetY());
                hm.put("line-spacing", h.lineSpacing());
                hm.put("animated",     h.animated());
                hm.put("lines",        h.lines());
                holos.add(hm);
            }
            y.set(base + "holograms", holos);
            y.set(base + "actions", snap.actions());
            y.set(base + "animation.idle",           snap.animation().idle());
            y.set(base + "animation.interval-ticks", snap.animation().intervalTicks());
            y.set(base + "visibility.permission",    snap.visibility().permission());
            y.set(base + "visibility.world",         snap.visibility().world());
            y.set(base + "visibility.server-online", snap.visibility().serverOnline());
        }

        File tmp = new File(file.getParentFile(), file.getName() + ".tmp");
        try {
            y.save(tmp);
            Files.move(tmp.toPath(), file.toPath(),
                    StandardCopyOption.REPLACE_EXISTING, StandardCopyOption.ATOMIC_MOVE);
        } catch (IOException e) {
            Bukkit.getLogger().warning("[Npc] Failed to save npcs.yml: " + e);
        }
    }

    private static NpcType parseType(String raw) {
        try { return NpcType.valueOf(raw.toUpperCase()); }
        catch (Exception e) { return NpcType.PLAYER; }
    }

    private static double number(Object o, double dflt) {
        return o instanceof Number n ? n.doubleValue() : dflt;
    }

    private static boolean bool(Object o, boolean dflt) {
        return o instanceof Boolean b ? b : dflt;
    }

    @SuppressWarnings("unchecked")
    private static List<String> stringList(Object o) {
        if (o instanceof List<?> raw) {
            List<String> out = new ArrayList<>(raw.size());
            for (Object x : raw) out.add(String.valueOf(x));
            return out;
        }
        return List.of();
    }
}
