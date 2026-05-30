package me.davidml16.baul.pets.database;

import me.davidml16.baul.Main;
import me.davidml16.baul.pets.data.PlayerData;
import me.davidml16.baul.pets.database.impl.MySQLDatabase;
import me.davidml16.baul.pets.database.impl.SQLiteDatabase;
import me.davidml16.baul.pets.utils.Messages;
import org.bukkit.configuration.ConfigurationSection;

import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public class DatabaseManager {

    private final ConfigurationSection config;
    private Database database;

    public DatabaseManager(Main plugin) {
        this.config = plugin.getConfig().getConfigurationSection("database");
    }

    public void initialize() {
        String type = config.getString("type", "sqlite").toLowerCase();
        Messages.debug("Initializing database with type: " + type.toUpperCase());

        try {
            if (type.equals("mysql")) {
                String host = config.getString("mysql.host", "localhost");
                int port = config.getInt("mysql.port", 3306);
                String database = config.getString("mysql.database", "betterpets");
                String username = config.getString("mysql.username", "root");
                String password = config.getString("mysql.password", "");

                Messages.debug("Connecting to MySQL database at " + host + ":" + port);
                this.database = new MySQLDatabase(host, port, database, username, password);
            } else {
                Messages.debug("Using SQLite database in plugin data folder");
                this.database = new SQLiteDatabase(Main.getInstance().getDataFolder());
            }

            this.database.init();
            Messages.debug("Database successfully initialized: " + type.toUpperCase());
        } catch (Exception e) {
            Messages.debug("Failed to initialize database: " + e.getMessage());
            Messages.debug("Falling back to SQLite database");
            this.database = new SQLiteDatabase(Main.getInstance().getDataFolder());
            try {
                this.database.init();
                Messages.debug("SQLite fallback database initialized successfully");
            } catch (Exception fallbackException) {
                Messages.debug("Critical error: Could not initialize any database: " + fallbackException.getMessage());
                throw new RuntimeException("Database initialization failed completely", fallbackException);
            }
        }
    }

    public Database getDatabase() {
        return database;
    }

    /**
     * Saves player data to the database
     *
     * @param uuid Player UUID
     * @param data PlayerData to save
     */
    public CompletableFuture<Void> savePlayerData(UUID uuid, PlayerData data) {
        return database.savePlayerData(uuid, data);
    }

    /**
     * Loads player data from the database
     *
     * @param uuid Player UUID
     * @return CompletableFuture with the loaded PlayerData
     */
    public CompletableFuture<PlayerData> loadPlayerData(UUID uuid, PlayerData data) {
        return database.loadPlayerData(uuid, data);
    }

    /**
     * Checks if player has data in the database
     *
     * @param uuid Player UUID
     * @return CompletableFuture with boolean result
     */
    public CompletableFuture<Boolean> hasPlayerData(UUID uuid) {
        return database.hasData(uuid);
    }

    /**
     * Deletes player data from the database
     *
     * @param uuid Player UUID
     */
    public void deletePlayerData(UUID uuid) {
        database.deletePlayerData(uuid);
    }

    public void shutdown() {
        if (database != null) {
            database.close();
        }
    }
}
