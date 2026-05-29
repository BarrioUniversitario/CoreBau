package me.davidml16.baul.cosmetics.render;

import me.davidml16.baul.cosmetics.types.Hat;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

public class HatApplier {

    private final Map<UUID, ItemStack> stash = new HashMap<>();
    private final Set<UUID> applying = new HashSet<>();

    public boolean isApplied(UUID uuid) {
        return stash.containsKey(uuid);
    }

    public boolean isApplying(UUID uuid) {
        return applying.contains(uuid);
    }

    public void apply(Player player, Hat hat) {
        UUID uuid = player.getUniqueId();
        applying.add(uuid);
        try {
            if (!stash.containsKey(uuid)) {
                ItemStack current = player.getInventory().getHelmet();
                stash.put(uuid, current == null ? null : current.clone());
            }
            player.getInventory().setHelmet(hat.toItemStack());
        } finally {
            applying.remove(uuid);
        }
    }

    public void restore(Player player) {
        UUID uuid = player.getUniqueId();
        if (!stash.containsKey(uuid)) return;
        applying.add(uuid);
        try {
            ItemStack original = stash.remove(uuid);
            player.getInventory().setHelmet(original);
        } finally {
            applying.remove(uuid);
        }
    }

    public void clear(UUID uuid) {
        stash.remove(uuid);
        applying.remove(uuid);
    }
}
