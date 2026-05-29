package dev.blancocl.services;

import dev.blancocl.api.manager.NpcManager;
import dev.blancocl.api.npc.ClickType;
import dev.blancocl.api.npc.Npc;
import dev.blancocl.api.npc.NpcType;
import dev.blancocl.config.ConfigManager;
import dev.blancocl.api.skin.Skin;
import dev.blancocl.npc.NpcImpl;
import dev.blancocl.npc.NpcRegistry;
import dev.blancocl.npc.action.ActionExecutor;
import dev.blancocl.npc.action.ActionParser;
import dev.blancocl.npc.animation.AnimationScheduler;
import dev.blancocl.npc.animation.NpcAnimation;
import dev.blancocl.npc.engine.NpcEngine;
import dev.blancocl.npc.persistence.NpcSnapshot;
import dev.blancocl.npc.persistence.NpcRepository;
import dev.blancocl.velocity.NpcSyncMessenger;
import dev.blancocl.npc.visibility.VisibilityWorker;
import dev.blancocl.util.Threading;
import dev.blancocl.velocity.PluginMessenger;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * Coordinates {@link NpcImpl} lifecycle: creation, persistence, registration
 * into the visibility worker, and disposal.
 */
public final class NpcManagerImpl implements NpcManager, Service {

    private final Plugin plugin;
    private final Threading threading;
    private final NpcEngine engine;
    private final NpcRepository repository;
    private final VisibilityWorker visibility;
    private final ConfigManager configManager;
    private volatile NpcSyncMessenger sync;

    /**
     * Becomes {@code true} only after the initial async load from persistence
     * completes successfully. {@link #save()} and {@link #disable()} MUST gate on
     * this — otherwise a shutdown during the load window snapshots an empty
     * registry and (with MySQL) executes {@code DELETE FROM npc}, wiping
     * persistence across the entire network.
     */
    private volatile boolean initialLoadComplete = false;

    private final NpcRegistry registry = new NpcRegistry();
    private final AnimationScheduler animations;
    private final ActionExecutor executor = new ActionExecutor();
    private final ConcurrentHashMap<String, NpcActionFactory> actionTypes = new ConcurrentHashMap<>();
    private final ActionParser actionParser;

    // Injected lazily to break a cycle: holograms attach during snapshot install,
    // but HologramManagerImpl depends on NpcManagerImpl. Set by ServiceContainer.boot().
    private volatile dev.blancocl.services.HologramManagerImpl hologramManager;
    private volatile dev.blancocl.services.SkinManagerImpl skinManager;

    public NpcManagerImpl(Plugin plugin, Threading threading, NpcEngine engine,
                          NpcRepository repository, VisibilityWorker visibility,
                          ConfigManager configManager, PluginMessenger messenger) {
        this.plugin = plugin;
        this.threading = threading;
        this.engine = engine;
        this.repository = repository;
        this.visibility = visibility;
        this.configManager = configManager;
        this.animations = new AnimationScheduler(threading);
        this.actionParser = new ActionParser(this, messenger);
    }

    public void setHologramManager(dev.blancocl.services.HologramManagerImpl m) { this.hologramManager = m; }
    public void setSkinManager(dev.blancocl.services.SkinManagerImpl m)         { this.skinManager = m; }
    public void setSyncMessenger(NpcSyncMessenger m)                            { this.sync = m; }

    public ActionExecutor executor() { return executor; }

    /* ---------------- lifecycle ---------------- */

    @Override
    public void enable() {
        engine.start();
        visibility.start();
        // NPCs load async; spawning is lazy via visibility worker so the main thread is free.
        // We only flip initialLoadComplete = true AFTER the install succeeds, so any save()
        // (including disable()'s shutdown save) that fires before the load is done will be
        // a no-op instead of wiping persistence with an empty snapshot.
        repository.loadAll()
                .thenAcceptAsync(snaps -> {
                    installAll(snaps);
                    initialLoadComplete = true;
                }, threading.visibility())
                .exceptionally(t -> {
                    plugin.getLogger().severe("[Npc] Failed to load NPCs from persistence: " + t);
                    return null;
                });
    }

    @Override
    public void disable() {
        animations.shutdown();
        visibility.stop();
        // Save state synchronously so a crash mid-disable doesn't lose changes.
        // CRITICAL: skip save if the initial load never completed — otherwise we'd snapshot
        // an empty registry and (with MySQL) DELETE FROM npc, wiping every other backend's
        // NPCs across the network. Better to lose nothing than to lose everything.
        if (initialLoadComplete) {
            try {
                repository.saveAll(snapshotAll()).get(2, java.util.concurrent.TimeUnit.SECONDS);
            } catch (Throwable ignored) {}
        } else {
            plugin.getLogger().warning(
                "[Npc] disable() ran before the initial load finished — skipping save to "
              + "avoid wiping persistence. No NPCs were lost.");
        }
        for (NpcImpl npc : registry.all()) {
            try { npc.handle().destroy(); } catch (Throwable ignored) {}
        }
        registry.clear();
        engine.stop();
    }

    @Override
    public void reload() {
        repository.loadAll().thenAcceptAsync(snaps -> {
            // Despawn current set, install fresh.
            for (NpcImpl old : registry.all()) {
                visibility.tracker().releaseAllOf(old);
                visibility.chunks().remove(old);
                // Holograms son TextDisplays separados del engine handle: hay que detacharlos
                // explícitamente o quedan colgando en el mundo y bloquean ediciones futuras.
                if (hologramManager != null) hologramManager.detachAll(old);
                old.handle().destroy();
            }
            registry.clear();
            installAll(snaps);
            // A successful reload counts as a completed load — `/npc reload` is the documented
            // recovery path after a failed boot, so make sure save() / disable() are unblocked.
            initialLoadComplete = true;
        }, threading.visibility());
    }

    /* ---------------- NpcManager API ---------------- */

    @Override
    public CompletableFuture<Npc> create(String id, NpcType type, Location location) {
        if (registry.get(id).isPresent())
            return CompletableFuture.failedFuture(new IllegalArgumentException("NPC '" + id + "' exists"));

        NpcEngine.Handle handle = engine.create(id, type, location);
        NpcImpl npc = new NpcImpl(id, id, type, location, handle);
        npc.setLookAtPlayer(configManager.config().npcDefaultLookAtPlayer());
        npc.setViewDistance(configManager.config().npcDefaultViewDistance());

        handle.onClick((p, ct) -> executor.execute(npc, p, ct));

        registry.put(npc);
        visibility.chunks().add(npc);
        return CompletableFuture.completedFuture(npc);
    }

    @Override
    public Optional<Npc> get(String id) { return registry.get(id).map(n -> n); }

    @Override
    public Collection<Npc> list() { return new ArrayList<>(registry.all()); }

    @Override
    public CompletableFuture<Void> remove(String id) {
        NpcImpl npc = registry.remove(id);
        if (npc == null) return CompletableFuture.completedFuture(null);
        animations.cancel(id);
        visibility.tracker().releaseAllOf(npc);
        visibility.chunks().remove(npc);
        if (hologramManager != null) hologramManager.detachAll(npc);
        npc.handle().destroy();
        return save();
    }

    @Override
    public CompletableFuture<Void> save() {
        // Pre-load saves would persist an empty/partial snapshot and (with MySQL) wipe the
        // shared NPC table for every backend in the network. Reject them with a loud warning
        // — the caller should never have been able to mutate anything before load finished.
        if (!initialLoadComplete) {
            plugin.getLogger().warning(
                "[Npc] save() called before the initial load finished — refused to avoid "
              + "wiping persistence. Retry the operation once the plugin reports it is ready.");
            return CompletableFuture.completedFuture(null);
        }
        return repository.saveAll(snapshotAll()).thenRun(() -> {
            NpcSyncMessenger s = sync;
            if (s != null) threading.onMain(s::broadcastReload);
        });
    }

    @Override
    public void registerActionType(String name, NpcActionFactory factory) {
        actionTypes.put(name.toLowerCase(), factory);
    }

    @Override
    @Nullable
    public NpcActionFactory actionType(String name) {
        return actionTypes.get(name.toLowerCase());
    }

    /* ---------------- internals ---------------- */

    public NpcRegistry registry() { return registry; }
    public AnimationScheduler animations() { return animations; }

    private void installAll(List<NpcSnapshot> snapshots) {
        for (NpcSnapshot s : snapshots) installFromSnapshot(s);
    }

    /** Visible for tests / SnapshotBuilder. */
    public NpcImpl installFromSnapshot(NpcSnapshot s) {
        if (s.location().getWorld() == null) {
            plugin.getLogger().warning("Skipping NPC '" + s.id() + "' — world not loaded");
            return null;
        }
        NpcEngine.Handle handle = engine.create(s.name(), s.type(), s.location());
        NpcImpl npc = new NpcImpl(s.id(), s.name(), s.type(), s.location(), handle);
        npc.setLookAtPlayer(s.lookAtPlayer());
        npc.setViewDistance(s.viewDistance());
        npc.setVisibility(s.visibility().permission(), s.visibility().world(), s.visibility().serverOnline());
        handle.onClick((p, ct) -> executor.execute(npc, p, ct));
        registry.put(npc);
        visibility.chunks().add(npc);

        // Restore skin (async, off-thread)
        if (s.skin() != null && !s.skin().isEmpty() && skinManager != null) {
            npc.setSkinName(s.skin());
            if (s.skin().contains("texture:") || s.skin().contains("|signature:")) {
                String[] parts = s.skin().split("\\|");
                String tex = "", sig = "";
                for (String p : parts) {
                    if (p.startsWith("texture:")) tex = p.substring("texture:".length());
                    if (p.startsWith("signature:")) sig = p.substring("signature:".length());
                }
                Skin inline = skinManager.fromInline(s.id(), tex, sig);
                npc.applySkin(inline);
            } else {
                skinManager.fetchByName(s.skin()).thenAccept(npc::applySkin);
            }
        }

        // Restore holograms
        if (hologramManager != null) {
            for (NpcSnapshot.HologramSnapshot h : s.holograms()) {
                var holo = (dev.blancocl.hologram.HologramImpl)
                        hologramManager.attach(npc, h.offsetY(), h.lines());
                holo.setLineSpacing(h.lineSpacing());
            }
        }

        // Restore actions
        if (s.actions() != null) {
            for (var entry : s.actions().entrySet()) {
                dev.blancocl.api.npc.ClickType type =
                        entry.getKey().toLowerCase().startsWith("right")
                                ? dev.blancocl.api.npc.ClickType.RIGHT
                                : dev.blancocl.api.npc.ClickType.LEFT;
                actionParser.install(npc, type, entry.getValue());
                npc.actionsRaw().put(entry.getKey(), entry.getValue());
            }
        }

        // Restore animation
        if (s.animation() != null) {
            try {
                NpcAnimation a = NpcAnimation.valueOf(s.animation().idle().toUpperCase());
                npc.setAnimation(a.name(), s.animation().intervalTicks());
                if (a != NpcAnimation.NONE) animations.install(npc, a, s.animation().intervalTicks());
            } catch (Exception ignored) {}
        }
        return npc;
    }

    public List<NpcSnapshot> snapshotAll() {
        List<NpcSnapshot> out = new ArrayList<>(registry.all().size());
        for (NpcImpl n : registry.all()) {
            out.add(new NpcSnapshot(
                    n.id(),
                    n.name(),
                    n.type(),
                    n.skinName(),
                    n.location(),
                    n.lookAtPlayer(),
                    n.viewDistance(),
                    n.holograms().stream().map(h -> new NpcSnapshot.HologramSnapshot(
                            h.offsetY(), h.lineSpacing(), false,
                            h.lines().stream().map(l -> l.miniMessage()).collect(Collectors.toList())
                    )).collect(Collectors.toList()),
                    new java.util.LinkedHashMap<>(n.actionsRaw()),
                    new NpcSnapshot.AnimationSnapshot(n.animationIdle(), n.animationInterval()),
                    new NpcSnapshot.VisibilitySnapshot(
                            n.visibilityPermission(),
                            n.visibilityWorld(),
                            n.visibilityServerOnline())));
        }
        return out;
    }
}
