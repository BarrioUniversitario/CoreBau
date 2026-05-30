package me.davidml16.baul.pets.database.impl;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import me.davidml16.baul.pets.data.PlayerData;
import me.davidml16.baul.pets.database.Database;
import me.davidml16.baul.pets.utils.Messages;

import java.io.File;
import java.lang.reflect.Type;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public class SQLiteDatabase implements Database {
    private final File dataFolder;
    private final Gson gson;
    private Connection connection;
    private final Type MAP_TYPE = new TypeToken<Map<String, Object>>() {
    }.getType();

    public SQLiteDatabase(File dataFolder) {
        this.dataFolder = dataFolder;
        this.gson = new GsonBuilder().create();
    }

    @Override
    public void init() {
        try {
            if (!dataFolder.exists()) {
                dataFolder.mkdirs();
            }

            Class.forName("org.sqlite.JDBC");
            connection = DriverManager.getConnection("jdbc:sqlite:" + new File(dataFolder, "pets.db"));

            try (PreparedStatement statement = connection.prepareStatement(
                    "CREATE TABLE IF NOT EXISTS pet_data (" +
                            "uuid TEXT PRIMARY KEY, " +
                            "data TEXT NOT NULL" +
                            ")"
            )) {
                statement.executeUpdate();
            }
        } catch (Exception e) {
            Messages.debug("Error initializing SQLite database: " + e.getMessage());
        }
    }

    @Override
    public void close() {
        try {
            if (connection != null && !connection.isClosed()) {
                connection.close();
            }
        } catch (Exception e) {
            Messages.debug("Error closing SQLite database: " + e.getMessage());
        }
    }

    @Override
    public CompletableFuture<Void> savePlayerData(UUID uuid, PlayerData data) {
        return CompletableFuture.runAsync(() -> {
            synchronized (this) {
                try {
                    Map<String, Object> serializedData = data.serialize();
                    String json = gson.toJson(serializedData);

                    String sql = "INSERT OR REPLACE INTO pet_data (uuid, data) VALUES (?, ?)";
                    try (PreparedStatement stmt = connection.prepareStatement(sql)) {
                        stmt.setString(1, uuid.toString());
                        stmt.setString(2, json);
                        stmt.executeUpdate();
                        Messages.debug("Successfully saved playerdata to database for UUID: " + uuid);
                    }
                } catch (Exception e) {
                    Messages.debug("Error saving playerdata for UUID " + uuid.toString() + ": " + e.getMessage());
                    e.printStackTrace();
                }
            }
        });
    }

    @Override
    public CompletableFuture<PlayerData> loadPlayerData(UUID uuid, PlayerData playerData) {
        return CompletableFuture.supplyAsync(() -> {
            synchronized (this) {
                String sql = "SELECT data FROM pet_data WHERE uuid = ?";
                try (PreparedStatement stmt = connection.prepareStatement(sql)) {
                    stmt.setString(1, uuid.toString());
                    ResultSet rs = stmt.executeQuery();
                    if (rs.next()) {
                        String json = rs.getString("data");
                        Map<String, Object> data = gson.fromJson(json, MAP_TYPE);

                        // Convert Double values to Integer before deserializing
                        convertDoubleToInteger(data);

                        Messages.debug("Successfully loaded playerdata from database for UUID: " + uuid);
                        return playerData.deserialize(data);
                    }
                } catch (Exception e) {
                    Messages.debug("Error loading playerdata for UUID " + uuid.toString() + ": " + e.getMessage());
                    e.printStackTrace();
                }
                return playerData; // Return the empty playerData instead of null
            }
        });
    }

    @SuppressWarnings("unchecked")
    private void convertDoubleToInteger(Map<String, Object> data) {
        if (data == null) return;

        for (Map.Entry<String, Object> entry : new HashMap<>(data).entrySet()) {
            Object value = entry.getValue();
            if (value instanceof Double doubleValue) {
                // Convert Double to Integer if it's a whole number
                if (doubleValue % 1 == 0) {
                    data.put(entry.getKey(), doubleValue.intValue());
                }
            } else if (value instanceof Map) {
                convertDoubleToInteger((Map<String, Object>) value);
            } else if (value instanceof List<?> list) {
                for (int i = 0; i < list.size(); i++) {
                    Object listValue = list.get(i);
                    if (listValue instanceof Double doubleValue && doubleValue % 1 == 0) {
                        ((List<Object>) list).set(i, doubleValue.intValue());
                    } else if (listValue instanceof Map) {
                        convertDoubleToInteger((Map<String, Object>) listValue);
                    }
                }
            }
        }
    }

    @Override
    public CompletableFuture<Boolean> hasData(UUID uuid) {
        return CompletableFuture.supplyAsync(() -> {
            synchronized (this) {
                String sql = "SELECT EXISTS(SELECT 1 FROM pet_data WHERE uuid = ?)";
                try (PreparedStatement stmt = connection.prepareStatement(sql)) {
                    stmt.setString(1, uuid.toString());
                    ResultSet rs = stmt.executeQuery();

                    return rs.next() && rs.getBoolean(1);
                } catch (Exception e) {
                    Messages.debug("Error checking playerdata existence: " + e.getMessage());
                    return false;
                }
            }
        });
    }

    @Override
    public void deletePlayerData(UUID uuid) {
        synchronized (this) {
            try (PreparedStatement stmt = connection.prepareStatement("DELETE FROM pet_data WHERE uuid = ?")) {
                stmt.setString(1, uuid.toString());
                stmt.executeUpdate();
            } catch (Exception e) {
                Messages.debug("Error deleting playerdata: " + e.getMessage());
            }
        }
    }
}
