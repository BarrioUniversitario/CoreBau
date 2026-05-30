package me.davidml16.baul.pets.database;

import me.davidml16.baul.pets.data.PlayerData;

import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public interface Database {
    void init();

    void close();

    CompletableFuture<Void> savePlayerData(UUID uuid, PlayerData data);

    CompletableFuture<PlayerData> loadPlayerData(UUID uuid, PlayerData data);

    CompletableFuture<Boolean> hasData(UUID uuid);

    void deletePlayerData(UUID uuid);
}
