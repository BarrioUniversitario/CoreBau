package dev.blancocl.util;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.configuration.ConfigurationSection;

import java.util.LinkedHashMap;
import java.util.Map;

public final class Locations {

    private Locations() {}

    /** Pack a location into the YAML schema used in {@code npcs.yml}. */
    public static Map<String, Object> toMap(Location loc) {
        Map<String, Object> m = new LinkedHashMap<>(6);
        m.put("world", loc.getWorld() == null ? "world" : loc.getWorld().getName());
        m.put("x", round(loc.getX()));
        m.put("y", round(loc.getY()));
        m.put("z", round(loc.getZ()));
        m.put("yaw",   round(loc.getYaw()));
        m.put("pitch", round(loc.getPitch()));
        return m;
    }

    public static Location fromSection(ConfigurationSection s) {
        if (s == null) return null;
        String worldName = s.getString("world", "world");
        World w = Bukkit.getWorld(worldName);
        return new Location(
                w,
                s.getDouble("x"),
                s.getDouble("y"),
                s.getDouble("z"),
                (float) s.getDouble("yaw", 0d),
                (float) s.getDouble("pitch", 0d));
    }

    public static long chunkKey(int chunkX, int chunkZ) {
        return (((long) chunkX) << 32) | (chunkZ & 0xffffffffL);
    }

    public static long chunkKeyFor(Location loc) {
        return chunkKey(loc.getBlockX() >> 4, loc.getBlockZ() >> 4);
    }

    private static double round(double v) { return Math.round(v * 1000d) / 1000d; }
    private static double round(float v)  { return Math.round(v * 1000d) / 1000d; }
}
