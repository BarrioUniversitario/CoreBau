package dev.blancocl.npc.persistence;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import dev.blancocl.api.npc.NpcType;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

final class NpcSnapshotJson {

    private static final Gson GSON = new GsonBuilder().disableHtmlEscaping().create();

    private NpcSnapshotJson() {}

    static String toJson(NpcSnapshot s) {
        Map<String, Object> root = new LinkedHashMap<>();
        root.put("name", s.name());
        root.put("type", s.type().name());
        root.put("skin", s.skin());
        root.put("location", locMap(s.location()));
        root.put("look-at-player", s.lookAtPlayer());
        root.put("view-distance", s.viewDistance());

        List<Map<String, Object>> holos = new ArrayList<>();
        for (NpcSnapshot.HologramSnapshot h : s.holograms()) {
            Map<String, Object> m = new LinkedHashMap<>();
            m.put("offset-y",     h.offsetY());
            m.put("line-spacing", h.lineSpacing());
            m.put("animated",     h.animated());
            m.put("lines",        h.lines());
            holos.add(m);
        }
        root.put("holograms", holos);
        root.put("actions", s.actions());

        Map<String, Object> anim = new LinkedHashMap<>();
        anim.put("idle",           s.animation().idle());
        anim.put("interval-ticks", s.animation().intervalTicks());
        root.put("animation", anim);

        Map<String, Object> vis = new LinkedHashMap<>();
        vis.put("permission",    s.visibility().permission());
        vis.put("world",         s.visibility().world());
        vis.put("server-online", s.visibility().serverOnline());
        root.put("visibility", vis);

        return GSON.toJson(root);
    }

    @SuppressWarnings("unchecked")
    static NpcSnapshot fromJson(String id, String json) {
        Map<String, Object> root = GSON.fromJson(json, Map.class);
        String name = strOr(root.get("name"), id);
        NpcType type = parseType(strOr(root.get("type"), "PLAYER"));
        String skin = (String) root.get("skin");
        Location loc = locFromMap((Map<String, Object>) root.get("location"));
        if (loc == null) throw new IllegalStateException("NPC '" + id + "' has invalid location");
        boolean lookAtPlayer = bool(root.get("look-at-player"), true);
        int viewDistance = (int) dbl(root.get("view-distance"), 48);

        List<NpcSnapshot.HologramSnapshot> holos = new ArrayList<>();
        Object rawHolos = root.get("holograms");
        if (rawHolos instanceof List<?> hl) {
            for (Object o : hl) {
                if (!(o instanceof Map<?, ?> h)) continue;
                holos.add(new NpcSnapshot.HologramSnapshot(
                        dbl(h.get("offset-y"), 2.3),
                        dbl(h.get("line-spacing"), 0.28),
                        bool(h.get("animated"), false),
                        stringList(h.get("lines"))));
            }
        }

        Map<String, List<Map<String, Object>>> actions = new LinkedHashMap<>();
        Object rawActions = root.get("actions");
        if (rawActions instanceof Map<?, ?> am) {
            for (Map.Entry<?, ?> e : am.entrySet()) {
                List<Map<String, Object>> list = new ArrayList<>();
                if (e.getValue() instanceof List<?> innerList) {
                    for (Object o : innerList) {
                        if (o instanceof Map<?, ?> dict) {
                            Map<String, Object> conv = new LinkedHashMap<>();
                            for (Map.Entry<?, ?> de : dict.entrySet()) {
                                conv.put(String.valueOf(de.getKey()), de.getValue());
                            }
                            list.add(conv);
                        }
                    }
                }
                actions.put(String.valueOf(e.getKey()), list);
            }
        }

        Map<String, Object> a = mapOrEmpty(root.get("animation"));
        var anim = new NpcSnapshot.AnimationSnapshot(
                strOr(a.get("idle"), "NONE"),
                (int) dbl(a.get("interval-ticks"), 200));

        Map<String, Object> v = mapOrEmpty(root.get("visibility"));
        var vis = new NpcSnapshot.VisibilitySnapshot(
                (String) v.get("permission"),
                (String) v.get("world"),
                (String) v.get("server-online"));

        return new NpcSnapshot(id, name, type, skin, loc, lookAtPlayer, viewDistance, holos, actions, anim, vis);
    }

    private static Map<String, Object> locMap(Location loc) {
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("world", loc.getWorld() == null ? "world" : loc.getWorld().getName());
        m.put("x", loc.getX());
        m.put("y", loc.getY());
        m.put("z", loc.getZ());
        m.put("yaw",   (double) loc.getYaw());
        m.put("pitch", (double) loc.getPitch());
        return m;
    }

    private static Location locFromMap(Map<String, Object> m) {
        if (m == null) return null;
        World w = Bukkit.getWorld(strOr(m.get("world"), "world"));
        return new Location(w,
                dbl(m.get("x"), 0),
                dbl(m.get("y"), 0),
                dbl(m.get("z"), 0),
                (float) dbl(m.get("yaw"), 0),
                (float) dbl(m.get("pitch"), 0));
    }

    private static NpcType parseType(String raw) {
        try { return NpcType.valueOf(raw.toUpperCase()); }
        catch (Exception e) { return NpcType.PLAYER; }
    }

    private static double dbl(Object o, double dflt) {
        return o instanceof Number n ? n.doubleValue() : dflt;
    }

    private static boolean bool(Object o, boolean dflt) {
        return o instanceof Boolean b ? b : dflt;
    }

    private static String strOr(Object o, String dflt) {
        return o instanceof String s ? s : dflt;
    }

    @SuppressWarnings("unchecked")
    private static Map<String, Object> mapOrEmpty(Object o) {
        return o instanceof Map<?, ?> m ? (Map<String, Object>) m : new LinkedHashMap<>();
    }

    private static List<String> stringList(Object o) {
        if (o instanceof List<?> raw) {
            List<String> out = new ArrayList<>(raw.size());
            for (Object x : raw) out.add(String.valueOf(x));
            return out;
        }
        return List.of();
    }
}
