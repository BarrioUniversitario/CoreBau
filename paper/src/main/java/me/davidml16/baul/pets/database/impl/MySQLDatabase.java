package me.davidml16.baul.pets.database.impl;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import me.davidml16.baul.pets.data.PlayerData;
import me.davidml16.baul.pets.database.Database;
import me.davidml16.baul.pets.utils.Messages;

import java.lang.reflect.Type;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public class MySQLDatabase implements Database {

    private HikariDataSource dataSource;
    private final String host;
    private final int port;
    private final String database;
    private final String username;
    private final String password;
    private final Gson gson;
    private final Type MAP_TYPE = new TypeToken<Map<String, Object>>() {
    }.getType();

    public MySQLDatabase(String host, int port, String database, String username, String password) {
        this.host = host;
        this.port = port;
        this.database = database;
        this.username = username;
        this.password = password;
        this.gson = new Gson();
    }

    @Override
    public void init() {
        HikariConfig config = new HikariConfig();
        config.setJdbcUrl("jdbc:mysql://" + host + ":" + port + "/" + database);
        config.setUsername(username);
        config.setPassword(password);
        config.setMaximumPoolSize(10);

        dataSource = new HikariDataSource(config);

        try (Connection connection = dataSource.getConnection();
             PreparedStatement statement = connection.prepareStatement(
                     "CREATE TABLE IF NOT EXISTS pet_data (" +
                             "uuid VARCHAR(36) PRIMARY KEY, " +
                             "data TEXT NOT NULL" +
                             ")"
             )) {
            statement.executeUpdate();
        } catch (Exception e) {
            Messages.debug("Error initializing MySQL database: " + e.getMessage());
        }
    }

    @Override
    public void close() {
        if (dataSource != null && !dataSource.isClosed()) {
            dataSource.close();
        }
    }

    @Override
    public CompletableFuture<Void> savePlayerData(UUID uuid, PlayerData data) {
        return CompletableFuture.runAsync(() -> {
            String json = gson.toJson(data.serialize());
            try (Connection connection = dataSource.getConnection();
                 PreparedStatement statement = connection.prepareStatement(
                         "INSERT INTO pet_data (uuid, data) VALUES (?, ?) " +
                                 "ON DUPLICATE KEY UPDATE data = ?")) {
                statement.setString(1, uuid.toString());
                statement.setString(2, json);
                statement.setString(3, json);
                statement.executeUpdate();
            } catch (Exception e) {
                Messages.debug("Error saving player data to MySQL: " + e.getMessage());
            }
        });
    }

    @Override
    public CompletableFuture<PlayerData> loadPlayerData(UUID uuid, PlayerData playerData) {
        return CompletableFuture.supplyAsync(() -> {
            try (Connection connection = dataSource.getConnection();
                 PreparedStatement statement = connection.prepareStatement(
                         "SELECT data FROM pet_data WHERE uuid = ?")) {
                statement.setString(1, uuid.toString());
                ResultSet resultSet = statement.executeQuery();

                if (resultSet.next()) {
                    String json = resultSet.getString("data");
                    Map<String, Object> data = gson.fromJson(json, MAP_TYPE);
                    return playerData.deserialize(data);
                }
            } catch (Exception e) {
                Messages.debug("Error loading player data from MySQL: " + e.getMessage());
            }
            return null;
        });
    }

    @Override
    public CompletableFuture<Boolean> hasData(UUID uuid) {
        return CompletableFuture.supplyAsync(() -> {
            try (Connection connection = dataSource.getConnection();
                 PreparedStatement statement = connection.prepareStatement(
                         "SELECT 1 FROM pet_data WHERE uuid = ?")) {
                statement.setString(1, uuid.toString());
                ResultSet resultSet = statement.executeQuery();
                return resultSet.next();
            } catch (Exception e) {
                Messages.debug("Error checking if player data exists from MySQL: " + e.getMessage());
                return false;
            }
        });
    }

    @Override
    public void deletePlayerData(UUID uuid) {
        CompletableFuture.runAsync(() -> {
            try (Connection connection = dataSource.getConnection();
                 PreparedStatement statement = connection.prepareStatement(
                         "DELETE FROM pet_data WHERE uuid = ?")) {
                statement.setString(1, uuid.toString());
                statement.executeUpdate();
            } catch (Exception e) {
                Messages.debug("Error deleting player data from MySQL: " + e.getMessage());
            }
        });
    }
}
