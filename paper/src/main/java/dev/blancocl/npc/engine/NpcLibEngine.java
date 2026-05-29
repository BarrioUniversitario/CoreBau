package dev.blancocl.npc.engine;

import com.github.juliarn.npclib.api.Npc;
import com.github.juliarn.npclib.api.NpcActionController;
import com.github.juliarn.npclib.api.Platform;
import com.github.juliarn.npclib.api.Position;
import com.github.juliarn.npclib.api.profile.Profile;
import com.github.juliarn.npclib.api.profile.ProfileProperty;
import com.github.juliarn.npclib.api.protocol.enums.EntityAnimation;
import com.github.juliarn.npclib.bukkit.BukkitPlatform;
import com.github.juliarn.npclib.bukkit.BukkitWorldAccessor;
import com.github.juliarn.npclib.bukkit.protocol.BukkitProtocolAdapter;
import com.github.juliarn.npclib.bukkit.util.BukkitPlatformUtil;
import com.github.retrooper.packetevents.PacketEvents;
import com.github.retrooper.packetevents.event.PacketListenerPriority;
import com.github.retrooper.packetevents.event.SimplePacketListenerAbstract;
import com.github.retrooper.packetevents.event.simple.PacketPlayReceiveEvent;
import com.github.retrooper.packetevents.protocol.packettype.PacketType;
import com.github.retrooper.packetevents.wrapper.play.client.WrapperPlayClientInteractEntity;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerEntityHeadLook;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerEntityRotation;
import dev.blancocl.api.npc.ClickType;
import dev.blancocl.api.npc.NpcType;
import dev.blancocl.api.skin.Skin;
import dev.blancocl.config.ConfigManager;
import dev.blancocl.util.Threading;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;

import java.nio.charset.StandardCharsets;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.function.BiConsumer;

/**
 * {@link NpcEngine} implementation backed by juliarn/npc-lib for NPC lifecycle
 * (spawn, despawn, profile, skin, animation) and {@link PacketEvents} for click
 * detection.
 *
 * <p>We don't rely on npc-lib's {@code InteractNpcEvent} / {@code AttackNpcEvent}
 * because those have shown to silently drop on some PacketEvents builds. Reading
 * {@code Use Entity} packets directly from PacketEvents and matching the entity id
 * against our handle map is more reliable and avoids the abstraction.</p>
 */
public final class NpcLibEngine implements NpcEngine {

    private final Plugin plugin;
    private final Threading threading;
    private final ConfigManager configManager;
    private final ConcurrentHashMap<Npc<World, Player, ItemStack, Plugin>, NpcLibHandle> handles =
            new ConcurrentHashMap<>();

    private volatile Platform<World, Player, ItemStack, Plugin> platform;
    private volatile boolean started;
    private volatile boolean debug;
    private volatile ScheduledFuture<?> headLookTask;

    public NpcLibEngine(Plugin plugin, Threading threading, ConfigManager configManager) {
        this.plugin = plugin;
        this.threading = threading;
        this.configManager = configManager;
    }

    /** Enables verbose click / interaction logging when {@code true}. */
    public void setDebug(boolean enabled) { this.debug = enabled; }

    @Override
    public synchronized void start() {
        if (started) return;
        platform = BukkitPlatform.bukkitNpcPlatformBuilder()
                .extension(plugin)
                .packetFactory(BukkitProtocolAdapter.packetEvents())
                .worldAccessor(BukkitWorldAccessor.nameBasedAccessor())
                .actionController(builder -> builder
                        .flag(NpcActionController.SPAWN_DISTANCE, 1)
                        .flag(NpcActionController.IMITATE_DISTANCE, 20))
                .build();
        registerClickListener();
        startHeadLookTask();
        started = true;
        plugin.getLogger().info("npc-lib engine started (click via PacketEvents, head-only look-at).");
    }

    @Override
    public synchronized void stop() {
        if (headLookTask != null) {
            headLookTask.cancel(false);
            headLookTask = null;
        }
        for (NpcLibHandle handle : handles.values()) {
            handle.destroy();
        }
        handles.clear();
        started = false;
    }

    @Override
    public Handle create(String name, NpcType type, Location location) {
        if (!started) throw new IllegalStateException("engine not started");
        return new NpcLibHandle(name, type, location);
    }

    /* ============================================================
     *  Click detection — directo desde PacketEvents
     * ============================================================ */

    private void registerClickListener() {
        // MONITOR priority: corremos último para no interferir con la pipeline
        // de PacketEvents/npc-lib. Si nuestra lógica falla, el server sigue
        // sirviendo packets sin que el cliente vea un Network Protocol Error.
        PacketEvents.getAPI().getEventManager().registerListener(
                new SimplePacketListenerAbstract(PacketListenerPriority.MONITOR) {
                    @Override
                    public void onPacketPlayReceive(PacketPlayReceiveEvent event) {
                        if (event.getPacketType() != PacketType.Play.Client.INTERACT_ENTITY) return;
                        // Nunca mutamos el packet ni cancelamos — solo observamos.
                        try {
                            handleInteract(event);
                        } catch (Throwable t) {
                            plugin.getLogger().warning("[Npc] click listener failed: " + t);
                        }
                    }
                });
    }

    private void handleInteract(PacketPlayReceiveEvent event) {
        WrapperPlayClientInteractEntity wrapper = new WrapperPlayClientInteractEntity(event);
        int entityId = wrapper.getEntityId();

        NpcLibHandle handle = findByEntityId(entityId);
        if (handle == null) return;     // no es un NPC nuestro — ignorar

        WrapperPlayClientInteractEntity.InteractAction action = wrapper.getAction();
        boolean isAttack = action == WrapperPlayClientInteractEntity.InteractAction.ATTACK;

        // INTERACT y INTERACT_AT son el mismo click derecho desde el cliente — Minecraft envía ambos.
        // Solo procesamos INTERACT y descartamos INTERACT_AT para evitar disparar la acción dos veces.
        if (action == WrapperPlayClientInteractEntity.InteractAction.INTERACT_AT) return;

        // Filtra clicks de off-hand para evitar duplicados (el cliente envía ambos).
        if (!isAttack && wrapper.getHand() != null
                && "OFF_HAND".equals(wrapper.getHand().name())) return;

        UUID playerId = event.getUser().getUUID();
        Player player = Bukkit.getPlayer(playerId);
        if (player == null) return;

        ClickType type = isAttack ? ClickType.LEFT : ClickType.RIGHT;
        if (debug) {
            plugin.getLogger().info("[click] " + type + " on entity " + entityId
                    + " by " + player.getName());
        }
        threading.onMain(() -> handle.fire(player, type));
    }

    private NpcLibHandle findByEntityId(int entityId) {
        for (var entry : handles.entrySet()) {
            if (entry.getKey().entityId() == entityId) return entry.getValue();
        }
        return null;
    }

    /* ============================================================
     *  Head-only look-at — replaces npc-lib's full-body LOOK_AT_PLAYER
     * ============================================================ */

    private void startHeadLookTask() {
        if (headLookTask != null) headLookTask.cancel(false);
        // 200 ms (~4 ticks): smooth enough that the head feels alive, cheap enough
        // that we don't burn CPU on a server-wide head-tracking pass.
        long ms = 200L;
        headLookTask = threading.scheduled().scheduleAtFixedRate(
                this::headLookTickSafely, ms, ms, TimeUnit.MILLISECONDS);
    }

    private void headLookTickSafely() {
        try { headLookTick(); }
        catch (Throwable t) { plugin.getLogger().warning("[Npc] head-look tick failed: " + t); }
    }

    private void headLookTick() {
        int radius = Math.max(1, configManager.config().npcDefaultLookRadius());
        int radiusSq = radius * radius;

        for (var entry : handles.entrySet()) {
            var npcLibObj = entry.getKey();
            NpcLibHandle handle = entry.getValue();
            if (!handle.lookAtPlayer) continue;

            Location npcLoc = handle.location;
            if (npcLoc == null || npcLoc.getWorld() == null) continue;

            int entityId = npcLibObj.entityId();
            float bodyYaw = npcLoc.getYaw();
            double npcEyeY = npcLoc.getY() + eyeHeight(handle.type);

            for (UUID viewerId : handle.visibleTo) {
                Player viewer = Bukkit.getPlayer(viewerId);
                if (viewer == null || !viewer.isOnline()) continue;
                if (!viewer.getWorld().getUID().equals(npcLoc.getWorld().getUID())) continue;

                Location vEye = viewer.getEyeLocation();
                double dx = vEye.getX() - npcLoc.getX();
                double dy = vEye.getY() - npcEyeY;
                double dz = vEye.getZ() - npcLoc.getZ();
                double horizSq = dx * dx + dz * dz;
                if (horizSq > radiusSq) continue;     // out of look range
                if (horizSq < 0.0001) continue;       // viewer on top of NPC

                float headYaw = (float) (Math.toDegrees(Math.atan2(dz, dx)) - 90);
                float pitch = (float) -Math.toDegrees(Math.atan2(dy, Math.sqrt(horizSq)));
                if (pitch > 90f)  pitch = 90f;
                if (pitch < -90f) pitch = -90f;

                try {
                    // HeadLook turns ONLY the head's yaw. Body yaw is preserved by sending
                    // EntityRotation with the unchanged bodyYaw; we only update pitch so the
                    // head can also tilt up/down to track tall or short viewers.
                    WrapperPlayServerEntityHeadLook hl =
                            new WrapperPlayServerEntityHeadLook(entityId, headYaw);
                    WrapperPlayServerEntityRotation er =
                            new WrapperPlayServerEntityRotation(entityId, bodyYaw, pitch, true);
                    PacketEvents.getAPI().getPlayerManager().sendPacket(viewer, hl);
                    PacketEvents.getAPI().getPlayerManager().sendPacket(viewer, er);
                } catch (Throwable ignored) {}
            }
        }
    }

    private static double eyeHeight(NpcType type) {
        return switch (type) {
            case PLAYER, VILLAGER -> 1.62;
            case ZOMBIE, SKELETON -> 1.74;
            case ARMOR_STAND      -> 1.70;
        };
    }

    /* ============================================================
     *  Handle: una NPC viva
     * ============================================================ */

    private final class NpcLibHandle implements Handle {
        private final NpcType type;
        private final Set<UUID> visibleTo = ConcurrentHashMap.newKeySet();

        private volatile Location location;
        private volatile Profile.Resolved profile;
        private volatile Npc<World, Player, ItemStack, Plugin> npc;
        private volatile boolean lookAtPlayer;
        private volatile BiConsumer<Player, ClickType> clickHandler = (p, c) -> {};

        NpcLibHandle(String name, NpcType type, Location location) {
            this.type = type;
            this.location = location.clone();
            String profileName = name != null ? dev.blancocl.util.Mini.toLegacy(name) : defaultName(type);
            String sanitized = sanitizeProfileName(profileName);
            this.profile = Profile.resolved(sanitized, stableUuid(sanitized));
            this.npc = buildNpc();
            hideNametag(sanitized);
        }

        @Override public Object engineNpc() { return npc; }

        @Override
        public void spawn(Player viewer) {
            visibleTo.add(viewer.getUniqueId());
            threading.onMain(() -> npc.forceTrackPlayer(viewer));
        }

        @Override
        public void despawn(Player viewer) {
            visibleTo.remove(viewer.getUniqueId());
            threading.onMain(() -> npc.stopTrackingPlayer(viewer));
        }

        @Override
        public void teleport(Location to) {
            location = to.clone();
            recreate();
        }

        @Override
        public void applySkin(Skin skin) {
            if (skin == null || skin.value() == null || skin.signature() == null) return;
            profile = Profile.resolved(
                    profile.name(),
                    profile.uniqueId(),
                    Set.of(ProfileProperty.property("textures", skin.value(), skin.signature())));
            recreate();
        }

        @Override
        public void setLookAtPlayer(boolean enabled) {
            // No longer triggers recreate(): the look-at is handled by headLookTick(),
            // which reads this field every ~200 ms. Recreating would respawn the NPC
            // and cause a visible "snap" — exactly what we removed by going head-only.
            lookAtPlayer = enabled;
        }

        @Override
        public void setName(String newName) {
            if (newName == null || newName.isEmpty()) return;
            String legacy = dev.blancocl.util.Mini.toLegacy(newName);
            String sanitized = sanitizeProfileName(legacy);
            this.profile = Profile.resolved(
                    sanitized,
                    stableUuid(sanitized),
                    profile.properties()
            );
            hideNametag(sanitized);
            recreate();
        }

        @Override
        public void playAnimation(EntityAnimation animation) {
            if (animation != null) {
                npc.playAnimation(animation).schedule(npc.trackedPlayers());
            }
        }

        @Override
        public void destroy() {
            String releasedName = profile.name();
            threading.onMain(() -> {
                Npc<World, Player, ItemStack, Plugin> current = npc;
                handles.remove(current);
                current.unlink();
                unhideNametag(releasedName);
            });
        }

        @Override
        public void onClick(BiConsumer<Player, ClickType> handler) {
            clickHandler = handler == null ? (p, c) -> {} : handler;
        }

        void fire(Player player, ClickType clickType) {
            try {
                clickHandler.accept(player, clickType);
            } catch (Throwable t) {
                plugin.getLogger().warning("[Npc] click handler failed for " + player.getName()
                        + " (" + clickType + "): " + t);
            }
        }

        private Npc<World, Player, ItemStack, Plugin> buildNpc() {
            // NOTE: deliberately NOT setting Npc.LOOK_AT_PLAYER.
            // npc-lib's built-in handler rotates the FULL BODY toward the viewer, which
            // "snaps" the NPC every time it respawns (player rejoin, /npc reload, chunk
            // reload, etc.). We replace it with head-only rotation in headLookTick() so
            // the body stays at its stored yaw and only the head tracks the viewer.
            Npc<World, Player, ItemStack, Plugin> built = platform.newNpcBuilder()
                    .position(position(location))
                    .profile(profile)
                    .build();
            handles.put(built, this);
            return built;
        }

        private void recreate() {
            threading.onMain(() -> {
                Npc<World, Player, ItemStack, Plugin> old = npc;
                handles.remove(old);
                old.unlink();
                npc = buildNpc();
                for (UUID viewerId : visibleTo) {
                    Player viewer = Bukkit.getPlayer(viewerId);
                    if (viewer != null && viewer.isOnline()) {
                        npc.forceTrackPlayer(viewer);
                    }
                }
            });
        }
    }

    /* ============================================================
     *  Helpers
     * ============================================================ */

    private static Position position(Location location) {
        return BukkitPlatformUtil.positionFromBukkitLegacy(location);
    }

    private static String defaultName(NpcType type) {
        return switch (type) {
            case PLAYER -> "Npc";
            case VILLAGER -> "Villager";
            case ZOMBIE -> "Zombie";
            case SKELETON -> "Skeleton";
            case ARMOR_STAND -> "ArmorStand";
        };
    }

    private static UUID stableUuid(String seed) {
        return UUID.nameUUIDFromBytes(("npc-lib:" + seed).getBytes(StandardCharsets.UTF_8));
    }

    /**
     * Profile names en Minecraft 1.20+ están validados por el cliente: solo
     * {@code [a-zA-Z0-9_]} entre 3 y 16 caracteres. Si dejamos pasar §-codes
     * (de un {@code <red>Survival} → {@code §cSurvival}) o acentos, el cliente
     * rechaza el packet PlayerInfoUpdate y desconecta con
     * "Network Protocol Error".
     */
    private static String sanitizeProfileName(String raw) {
        if (raw == null || raw.isEmpty()) return "Npc";
        StringBuilder out = new StringBuilder(16);
        for (int i = 0; i < raw.length() && out.length() < 16; i++) {
            char c = raw.charAt(i);
            if ((c >= 'a' && c <= 'z') || (c >= 'A' && c <= 'Z')
                    || (c >= '0' && c <= '9') || c == '_') {
                out.append(c);
            }
        }
        // Garantiza el mínimo de 3 caracteres rellenando si quedó muy corto.
        while (out.length() < 3) out.append('0');
        return out.toString();
    }

    /**
     * Esconde el nametag (nombre del perfil sobre la cabeza) usando un scoreboard
     * team con {@code NAME_TAG_VISIBILITY=NEVER}. Los profile names de NPCs
     * derivan del id, por lo que el choque con un jugador real es raro.
     */
    private void hideNametag(String profileName) {
        if (profileName == null || profileName.isEmpty()) return;
        threading.onMain(() -> {
            try {
                org.bukkit.scoreboard.Scoreboard sb = Bukkit.getScoreboardManager().getMainScoreboard();
                org.bukkit.scoreboard.Team team = sb.getTeam("npc-hidden");
                if (team == null) {
                    team = sb.registerNewTeam("npc-hidden");
                    team.setOption(
                            org.bukkit.scoreboard.Team.Option.NAME_TAG_VISIBILITY,
                            org.bukkit.scoreboard.Team.OptionStatus.NEVER);
                    team.setOption(
                            org.bukkit.scoreboard.Team.Option.COLLISION_RULE,
                            org.bukkit.scoreboard.Team.OptionStatus.NEVER);
                }
                if (!team.hasEntry(profileName)) team.addEntry(profileName);
            } catch (Throwable t) {
                plugin.getLogger().warning("Failed to hide NPC nametag for '" + profileName + "': " + t);
            }
        });
    }

    private void unhideNametag(String profileName) {
        if (profileName == null || profileName.isEmpty()) return;
        threading.onMain(() -> {
            try {
                org.bukkit.scoreboard.Team team =
                        Bukkit.getScoreboardManager().getMainScoreboard().getTeam("npc-hidden");
                if (team != null && team.hasEntry(profileName)) team.removeEntry(profileName);
            } catch (Throwable ignored) {}
        });
    }
}
