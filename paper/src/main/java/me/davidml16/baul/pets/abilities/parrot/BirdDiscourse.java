package me.davidml16.baul.pets.abilities.parrot;

import me.davidml16.baul.Main;
import me.davidml16.baul.pets.abilities.AbilityStats;
import me.davidml16.baul.pets.abilities.IAbility;
import me.davidml16.baul.pets.pet.Pet;
import me.davidml16.baul.pets.utils.enums.Stats;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

public class BirdDiscourse implements IAbility {

    private final Map<UUID, Set<UUID>> buffedPlayers = new HashMap<>();
    private final Map<UUID, BukkitTask> updateTasks = new HashMap<>();

    @Override
    public void onEquip(Player owner) {
        Pet pet = playerPetManager.getActivePet(owner);
        if (pet == null) return;

        buffedPlayers.put(owner.getUniqueId(), new HashSet<>());

        BukkitTask task = new BukkitRunnable() {
            @Override
            public void run() {
                if (!owner.isOnline()) {
                    cancel();
                    updateTasks.remove(owner.getUniqueId());
                    return;
                }

                Pet currentPet = playerPetManager.getActivePet(owner);
                if (currentPet == null) {
                    cancel();
                    updateTasks.remove(owner.getUniqueId());
                    return;
                }

                updateNearbyPlayers(owner, currentPet);
            }
        }.runTaskTimer(Main.getInstance(), 0L, 20L); // Update every second

        updateTasks.put(owner.getUniqueId(), task);
    }

    @Override
    public void onUnequip(Player owner) {
        BukkitTask task = updateTasks.remove(owner.getUniqueId());
        if (task != null && !task.isCancelled()) {
            task.cancel();
        }

        Set<UUID> playersToUnbuff = buffedPlayers.remove(owner.getUniqueId());
        if (playersToUnbuff != null) {
            // EcoSkills API has been removed - no stat modifiers to remove
            for (UUID playerUUID : playersToUnbuff) {
                Player player = Bukkit.getPlayer(playerUUID);
                if (player != null && player.isOnline()) {
                    // Stat modifier removal disabled
                }
            }
        }
    }

    private void updateNearbyPlayers(Player owner, Pet pet) {
        Set<UUID> currentBuffed = buffedPlayers.get(owner.getUniqueId());
        if (currentBuffed == null) return;

        Set<UUID> nearbyPlayers = new HashSet<>();

        owner.getNearbyEntities(20, 20, 20).stream()
                .filter(entity -> entity instanceof Player)
                .map(entity -> (Player) entity)
                .filter(player -> !player.equals(owner))
                .forEach(player -> {
                    nearbyPlayers.add(player.getUniqueId());

                    if (!currentBuffed.contains(player.getUniqueId())) {
                        if (!hasParrotStrengthBuff(player)) {
                            addStrengthBuff(player, pet, owner.getUniqueId());
                            currentBuffed.add(player.getUniqueId());
                        }
                    }
                });

        for (UUID playerUUID : currentBuffed) {
            if (!nearbyPlayers.contains(playerUUID)) {
                Player player = Bukkit.getPlayer(playerUUID);
                if (player != null && player.isOnline()) {
                    removeStrengthBuff(player, owner.getUniqueId());
                    currentBuffed.remove(playerUUID);
                }
            }
        }
    }

    private void addStrengthBuff(Player player, Pet pet, UUID ownerUUID) {
        // EcoSkills API has been removed - stat modifications disabled
        // Strength buff calculation: getAbilityStat().getStatAmplifier(Stats.STRENGTH).getStatAtLevel(pet.getLevel())
    }

    private void removeStrengthBuff(Player player, UUID ownerUUID) {
        // EcoSkills API has been removed - no modifiers to remove
    }

    private boolean hasParrotStrengthBuff(Player player) {
        return buffedPlayers.values().stream()
                .anyMatch(buffedSet -> buffedSet.contains(player.getUniqueId()));
    }

    @Override
    public AbilityStats getAbilityStat() {
        return new AbilityStats().addStatAmplifier(Stats.STRENGTH, 5, 0.25);
    }
}
