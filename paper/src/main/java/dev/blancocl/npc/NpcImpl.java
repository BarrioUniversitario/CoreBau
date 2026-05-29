package dev.blancocl.npc;

import dev.blancocl.api.hologram.Hologram;
import dev.blancocl.api.npc.ClickType;
import dev.blancocl.api.npc.Npc;
import dev.blancocl.api.npc.NpcAction;
import dev.blancocl.api.npc.NpcType;
import dev.blancocl.api.skin.Skin;
import dev.blancocl.npc.action.ActionList;
import dev.blancocl.npc.engine.NpcEngine;
import org.bukkit.Location;
import org.bukkit.entity.Player;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicReference;

/**
 * Internal NPC state. Public API surface is {@link Npc}; consumers should
 * not depend on this concrete type.
 */
public final class NpcImpl implements Npc {

    private final String id;
    private final AtomicReference<String> name;
    private final NpcType type;
    private final NpcEngine.Handle engineHandle;

    private final AtomicReference<Location> location;
    private final AtomicReference<Skin> skin = new AtomicReference<>();
    private final java.util.concurrent.atomic.AtomicBoolean lookAtPlayer = new java.util.concurrent.atomic.AtomicBoolean(true);
    private final java.util.concurrent.atomic.AtomicInteger viewDistance = new java.util.concurrent.atomic.AtomicInteger(48);

    private final ActionList leftClick  = new ActionList();
    private final ActionList rightClick = new ActionList();
    private final java.util.Map<String, java.util.List<java.util.Map<String, Object>>> actionsRaw =
            new java.util.concurrent.ConcurrentHashMap<>();

    private final List<Hologram> holograms = new CopyOnWriteArrayList<>();

    private volatile String skinName;
    private volatile String animationIdle = "NONE";
    private volatile int    animationIntervalTicks = 200;
    private volatile String visibilityPermission;
    private volatile String visibilityWorld;
    private volatile String visibilityServerOnline;

    public NpcImpl(String id, String name, NpcType type, Location location, NpcEngine.Handle engineHandle) {
        this.id = id;
        this.name = new AtomicReference<>(name != null ? name : id);
        this.type = type;
        this.location = new AtomicReference<>(location.clone());
        this.engineHandle = engineHandle;
    }

    public NpcEngine.Handle handle() { return engineHandle; }

    public ActionList actions(ClickType type) {
        return type == ClickType.LEFT ? leftClick : rightClick;
    }

    @Override public String id() { return id; }
    @Override public String name() { return name.get(); }
    @Override public void setName(String newName) {
        name.set(newName);
        engineHandle.setName(newName);
    }
    @Override public NpcType type() { return type; }
    @Override public Location location() { return location.get().clone(); }
    @Override public Skin skin() { return skin.get(); }
    @Override public boolean lookAtPlayer() { return lookAtPlayer.get(); }
    @Override public int viewDistance() { return viewDistance.get(); }

    @Override public void setLookAtPlayer(boolean enabled) {
        lookAtPlayer.set(enabled);
        engineHandle.setLookAtPlayer(enabled);
    }

    @Override public void setViewDistance(int blocks) { viewDistance.set(blocks); }

    @Override
    public CompletableFuture<Void> teleport(Location to) {
        Location target = to.clone();
        location.set(target);
        engineHandle.teleport(target);
        return CompletableFuture.completedFuture(null);
    }

    @Override
    public CompletableFuture<Void> applySkin(Skin newSkin) {
        skin.set(newSkin);
        engineHandle.applySkin(newSkin);
        return CompletableFuture.completedFuture(null);
    }

    @Override
    public void registerAction(ClickType type, NpcAction action) {
        actions(type).add(action);
    }

    @Override
    public void clearActions(ClickType type) {
        actions(type).clear();
    }

    @Override public void showTo(Player player) { engineHandle.spawn(player); }
    @Override public void hideFrom(Player player) { engineHandle.despawn(player); }

    @Override public List<Hologram> holograms() { return java.util.Collections.unmodifiableList(holograms); }

    public List<Hologram> hologramsMutable() { return holograms; }

    public java.util.Map<String, java.util.List<java.util.Map<String, Object>>> actionsRaw() { return actionsRaw; }

    public String  skinName()              { return skinName; }
    public void    setSkinName(String s)   { this.skinName = s; }
    public String  animationIdle()         { return animationIdle; }
    public int     animationInterval()     { return animationIntervalTicks; }
    public void    setAnimation(String idle, int intervalTicks) {
        this.animationIdle = idle == null ? "NONE" : idle;
        this.animationIntervalTicks = intervalTicks;
    }
    public String  visibilityPermission()   { return visibilityPermission; }
    public String  visibilityWorld()        { return visibilityWorld; }
    public String  visibilityServerOnline() { return visibilityServerOnline; }
    public void    setVisibility(String permission, String world, String serverOnline) {
        this.visibilityPermission   = permission;
        this.visibilityWorld        = world;
        this.visibilityServerOnline = serverOnline;
    }
}
