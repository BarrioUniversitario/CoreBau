package dev.blancocl.skin;

import dev.blancocl.api.skin.Skin;
import dev.blancocl.api.skin.SkinSource;
import dev.blancocl.cache.ExpiringCache;
import dev.blancocl.config.PluginConfig;
import org.bukkit.plugin.Plugin;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.util.Optional;

/** Two-tier cache: in-memory (Caffeine) on top of an on-disk JSON cache. */
public final class SkinCache {

    private final ExpiringCache<String, Skin> memory;
    private final Path diskDir;
    private final Duration diskTtl;

    public SkinCache(Plugin plugin, PluginConfig cfg) {
        this.memory  = new ExpiringCache<>(cfg.skinMemoryMax(), Duration.ofHours(cfg.skinDiskTtlHours()));
        this.diskDir = plugin.getDataFolder().toPath().resolve(cfg.skinDiskDir());
        this.diskTtl = Duration.ofHours(cfg.skinDiskTtlHours());
        try { Files.createDirectories(diskDir); }
        catch (IOException e) { plugin.getLogger().warning("Could not create skin cache dir: " + e); }
    }

    public Optional<Skin> get(String username) {
        Optional<Skin> mem = memory.get(username.toLowerCase());
        if (mem.isPresent()) return mem;
        Optional<Skin> disk = readDisk(username);
        disk.ifPresent(s -> memory.put(username.toLowerCase(), s));
        return disk;
    }

    public void put(String username, Skin skin) {
        memory.put(username.toLowerCase(), skin);
        writeDisk(username, skin);
    }

    public void invalidate(String username) {
        memory.invalidate(username.toLowerCase());
        try { Files.deleteIfExists(fileFor(username)); }
        catch (IOException ignored) {}
    }

    public void clear() {
        memory.invalidateAll();
        // Don't wipe disk cache on reload — it's idempotent and pre-populates restarts.
    }

    private Path fileFor(String username) {
        return diskDir.resolve(username.toLowerCase().replaceAll("[^a-zA-Z0-9_-]", "_") + ".json");
    }

    private Optional<Skin> readDisk(String username) {
        Path f = fileFor(username);
        if (!Files.exists(f)) return Optional.empty();
        try {
            long ageMs = System.currentTimeMillis() - Files.getLastModifiedTime(f).toMillis();
            if (ageMs > diskTtl.toMillis()) {
                Files.deleteIfExists(f);
                return Optional.empty();
            }
            String json = Files.readString(f, StandardCharsets.UTF_8);
            String value     = extract(json, "\"value\":\"", "\"");
            String signature = extract(json, "\"signature\":\"", "\"");
            if (value == null || signature == null) return Optional.empty();
            return Optional.of(new Skin(username, value, signature,
                    SkinSource.DISK_CACHE, Files.getLastModifiedTime(f).toMillis()));
        } catch (IOException ignored) {
            return Optional.empty();
        }
    }

    private void writeDisk(String username, Skin skin) {
        Path f = fileFor(username);
        String json = "{\"name\":\"" + escape(username) + "\","
                    + "\"value\":\"" + escape(skin.value()) + "\","
                    + "\"signature\":\"" + escape(skin.signature()) + "\"}";
        try { Files.writeString(f, json, StandardCharsets.UTF_8); }
        catch (IOException ignored) {}
    }

    private static String escape(String s) { return s.replace("\\", "\\\\").replace("\"", "\\\""); }

    private static String extract(String src, String open, String close) {
        int a = src.indexOf(open);
        if (a < 0) return null;
        a += open.length();
        int b = src.indexOf(close, a);
        return b < 0 ? null : src.substring(a, b);
    }
}
