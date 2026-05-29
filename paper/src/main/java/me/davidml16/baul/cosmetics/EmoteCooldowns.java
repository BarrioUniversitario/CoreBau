package me.davidml16.baul.cosmetics;

import me.davidml16.baul.Main;
import org.bukkit.Bukkit;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Per-player per-emote cooldown tracking, persisted across lobbies.
 *
 * In-memory cache is loaded from {@code ac_emote_cooldowns} when the player
 * joins. On {@link #mark}, the timestamp is written async to the database AND
 * a {@code COSMETIC_COOLDOWN} sync packet is broadcast so other lobby servers
 * refresh their copy. This prevents emote-spam across lobby hops.
 */
public class EmoteCooldowns {

    private final Map<UUID, Map<String, Long>> cooldowns = new HashMap<>();
    private final Main main;

    public EmoteCooldowns(Main main) {
        this.main = main;
    }

    /** Async-loads cooldowns for a player from the database into the cache. */
    public void loadForPlayer(UUID uuid) {
        main.getDatabaseHandler().getEmoteCooldowns(uuid).thenAccept(loaded ->
                Bukkit.getScheduler().runTask(main, () -> cooldowns.put(uuid, new HashMap<>(loaded))));
    }

    /** @return remaining cooldown in millis, or 0 if ready to fire. */
    public long remaining(UUID uuid, String emoteId, long cooldownMillis) {
        Map<String, Long> byEmote = cooldowns.get(uuid);
        if (byEmote == null) return 0;
        Long last = byEmote.get(emoteId);
        if (last == null) return 0;
        long elapsed = System.currentTimeMillis() - last;
        return elapsed >= cooldownMillis ? 0 : (cooldownMillis - elapsed);
    }

    /** Marks the emote used now, persists to DB, and broadcasts cross-lobby. */
    public void mark(UUID uuid, String emoteId) {
        long now = System.currentTimeMillis();
        cooldowns.computeIfAbsent(uuid, k -> new HashMap<>()).put(emoteId, now);
        main.getDatabaseHandler().setEmoteCooldown(uuid, emoteId, now);
        if (main.getSyncManager() != null) main.getSyncManager().syncCosmeticCooldown(uuid);
    }

    public void forgetPlayer(UUID uuid) {
        cooldowns.remove(uuid);
    }
}
