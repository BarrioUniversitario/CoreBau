package dev.blancocl.services;

import dev.blancocl.api.manager.VelocityManager;
import dev.blancocl.velocity.PluginMessenger;
import dev.blancocl.velocity.ServerStateCache;
import org.bukkit.entity.Player;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

public final class VelocityManagerImpl implements VelocityManager, Service {

    private final PluginMessenger messenger;
    private final ServerStateCache stateCache;

    public VelocityManagerImpl(PluginMessenger messenger, ServerStateCache stateCache) {
        this.messenger = messenger;
        this.stateCache = stateCache;
    }

    public PluginMessenger messenger() { return messenger; }

    @Override
    public void enable() {
        messenger.enable();
        stateCache.enable();
    }

    @Override
    public void disable() {
        stateCache.disable();
        messenger.disable();
    }

    @Override
    public boolean proxyDetected() { return messenger.proxyDetected(); }

    @Override
    public void connect(Player player, String serverName) {
        messenger.connect(player, serverName);
    }

    @Override
    public CompletableFuture<Integer> playerCount(String serverName) {
        return messenger.playerCount(serverName);
    }

    @Override
    public CompletableFuture<Integer> globalPlayerCount() {
        return messenger.playerCount("ALL");
    }

    @Override
    public CompletableFuture<List<String>> servers() { return messenger.servers(); }

    @Override
    public Optional<Integer> cachedPlayerCount(String serverName) {
        return stateCache.cachedCount(serverName);
    }
}
