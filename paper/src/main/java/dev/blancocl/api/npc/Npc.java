package dev.blancocl.api.npc;

import dev.blancocl.api.hologram.Hologram;
import dev.blancocl.api.skin.Skin;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.concurrent.CompletableFuture;

/**
 * A packet-only NPC. Never backed by a Bukkit entity; cannot be killed,
 * pushed, or queried via the entity scoreboard.
 *
 * <p>Mutators that touch packet state ({@link #teleport(Location)},
 * {@link #applySkin(Skin)}, {@link #setLookAtPlayer(boolean)}) are safe
 * from any thread — internal implementations marshal back to main as needed.</p>
 */
public interface Npc {

    String   id();
    String   name();
    NpcType  type();
    Location location();

    void setName(String name);

    /** Currently applied skin, or {@code null} if none / not a {@link NpcType#PLAYER}. */
    @Nullable Skin skin();

    boolean lookAtPlayer();
    int     viewDistance();

    void setLookAtPlayer(boolean enabled);
    void setViewDistance(int blocks);

    CompletableFuture<Void> teleport(Location to);
    CompletableFuture<Void> applySkin(Skin skin);

    void registerAction(ClickType type, NpcAction action);
    void clearActions(ClickType type);

    /** Force a spawn packet to the given player even if they fail visibility checks. */
    void showTo(Player player);
    /** Force a despawn packet, ignoring distance. */
    void hideFrom(Player player);

    /** Live (immutable) view of the holograms attached to this NPC. */
    List<Hologram> holograms();
}
