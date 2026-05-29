package dev.blancocl.npc.persistence;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import dev.blancocl.config.PluginConfig;
import dev.blancocl.util.Threading;
import org.bukkit.Bukkit;
import org.bukkit.plugin.Plugin;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

/** MySQL-backed repository. JSON-blob-per-NPC schema. */
public final class MySqlNpcRepository implements NpcRepository, AutoCloseable {

    private final Plugin plugin;
    private final Threading threading;
    private final HikariDataSource ds;

    public MySqlNpcRepository(Plugin plugin, Threading threading, PluginConfig cfg) {
        this.plugin = plugin;
        this.threading = threading;

        HikariConfig hc = new HikariConfig();
        hc.setJdbcUrl("jdbc:mysql://" + cfg.mysqlHost() + ":" + cfg.mysqlPort()
                + "/" + cfg.mysqlDatabase()
                + "?useSSL=false&serverTimezone=UTC&useUnicode=true&characterEncoding=utf8");
        hc.setUsername(cfg.mysqlUser());
        hc.setPassword(cfg.mysqlPassword());
        hc.setMaximumPoolSize(cfg.mysqlPoolSize());
        hc.setPoolName("Npc-MySQL");
        hc.setConnectionTimeout(10_000);
        this.ds = new HikariDataSource(hc);

        runSchema();
    }

    @Override
    public void close() {
        if (ds != null && !ds.isClosed()) ds.close();
    }

    private void runSchema() {
        String sql;
        try (InputStream in = plugin.getResource("schema.sql")) {
            if (in == null) {
                plugin.getLogger().warning("schema.sql not in plugin jar — skipping bootstrap");
                return;
            }
            sql = new BufferedReader(new InputStreamReader(in, StandardCharsets.UTF_8))
                    .lines().collect(Collectors.joining("\n"));
        } catch (Exception e) {
            plugin.getLogger().severe("Failed to read schema.sql: " + e);
            return;
        }
        try (Connection c = ds.getConnection();
             Statement st = c.createStatement()) {
            for (String stmt : sql.split(";")) {
                String s = stmt.trim();
                if (!s.isEmpty()) st.execute(s);
            }
        } catch (SQLException e) {
            plugin.getLogger().severe("Failed to apply schema.sql: " + e);
        }
    }

    @Override
    public CompletableFuture<List<NpcSnapshot>> loadAll() {
        return CompletableFuture.supplyAsync(this::loadSync, threading.skinIo());
    }

    @Override
    public CompletableFuture<Void> saveAll(List<NpcSnapshot> snapshots) {
        return CompletableFuture.runAsync(() -> saveSync(snapshots), threading.skinIo());
    }

    private List<NpcSnapshot> loadSync() {
        List<NpcSnapshot> out = new ArrayList<>();
        try (Connection c = ds.getConnection();
             Statement st = c.createStatement();
             ResultSet rs = st.executeQuery("SELECT id, data FROM npc")) {
            while (rs.next()) {
                String id = rs.getString("id");
                String json = rs.getString("data");
                try { out.add(NpcSnapshotJson.fromJson(id, json)); }
                catch (Throwable t) {
                    Bukkit.getLogger().warning("[Npc] Failed to parse MySQL NPC '" + id + "': " + t.getMessage());
                }
            }
        } catch (SQLException e) {
            plugin.getLogger().severe("Failed to load NPCs from MySQL: " + e);
        }
        return out;
    }

    private void saveSync(List<NpcSnapshot> snapshots) {
        try (Connection c = ds.getConnection()) {
            c.setAutoCommit(false);
            try {
                if (snapshots.isEmpty()) {
                    try (Statement st = c.createStatement()) {
                        st.execute("DELETE FROM npc");
                    }
                } else {
                    StringBuilder placeholders = new StringBuilder();
                    for (int i = 0; i < snapshots.size(); i++) {
                        if (i > 0) placeholders.append(',');
                        placeholders.append('?');
                    }
                    try (PreparedStatement ps = c.prepareStatement(
                            "DELETE FROM npc WHERE id NOT IN (" + placeholders + ")")) {
                        for (int i = 0; i < snapshots.size(); i++) {
                            ps.setString(i + 1, snapshots.get(i).id());
                        }
                        ps.executeUpdate();
                    }
                    try (PreparedStatement ps = c.prepareStatement(
                            "INSERT INTO npc (id, data) VALUES (?, ?) "
                          + "ON DUPLICATE KEY UPDATE data = VALUES(data)")) {
                        for (NpcSnapshot s : snapshots) {
                            ps.setString(1, s.id());
                            ps.setString(2, NpcSnapshotJson.toJson(s));
                            ps.addBatch();
                        }
                        ps.executeBatch();
                    }
                }
                c.commit();
            } catch (SQLException e) {
                c.rollback();
                throw e;
            }
        } catch (SQLException e) {
            plugin.getLogger().severe("Failed to save NPCs to MySQL: " + e);
        }
    }
}
