package me.davidml16.baul.pets.api.events;

import me.davidml16.baul.pets.pet.Pet;
import me.davidml16.baul.pets.pet.PetSkin;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

/**
 * Called when a pet skin is being applied to a pet.
 * This event can be cancelled to prevent the skin from being applied.
 *
 * <p>Example usage:</p>
 * <pre>{@code
 * @EventHandler
 * public void onSkinApply(PetSkinApplyEvent event) {
 *     if (!event.getPlayer().hasPermission("betterpets.skin." + event.getSkin().id())) {
 *         event.setCancelled(true);
 *         event.getPlayer().sendMessage("You don't have permission to use this skin!");
 *     }
 * }
 * }</pre>
 *
 * @author psikuvit
 * @since 1.0
 */
public class PetSkinApplyEvent extends Event implements Cancellable {
    private static final HandlerList HANDLERS = new HandlerList();
    private final Player player;
    private final Pet pet;
    private final PetSkin skin;
    private boolean cancelled;

    /**
     * Creates a new PetSkinApplyEvent.
     *
     * @param player The player applying the skin
     * @param pet    The pet receiving the skin
     * @param skin   The skin being applied
     */
    public PetSkinApplyEvent(@NotNull Player player, @NotNull Pet pet, @NotNull PetSkin skin) {
        this.player = player;
        this.pet = pet;
        this.skin = skin;
    }

    /**
     * Gets the player applying the skin.
     *
     * @return The player
     */
    @NotNull
    public Player getPlayer() {
        return player;
    }

    /**
     * Gets the pet receiving the skin.
     *
     * @return The pet
     */
    @NotNull
    public Pet getPet() {
        return pet;
    }

    /**
     * Gets the skin being applied.
     *
     * @return The pet skin
     */
    @NotNull
    public PetSkin getSkin() {
        return skin;
    }

    @Override
    public boolean isCancelled() {
        return cancelled;
    }

    @Override
    public void setCancelled(boolean cancel) {
        this.cancelled = cancel;
    }

    @Override
    public @NotNull HandlerList getHandlers() {
        return HANDLERS;
    }

    public static HandlerList getHandlerList() {
        return HANDLERS;
    }
}
