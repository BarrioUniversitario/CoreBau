package me.davidml16.baul.pets.abilities.dolphin;

import me.davidml16.baul.Main;
import me.davidml16.baul.pets.abilities.AbilityStats;
import me.davidml16.baul.pets.abilities.IAbility;
import me.davidml16.baul.pets.pet.Pet;
import me.davidml16.baul.pets.utils.enums.Stats;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitTask;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class PodTactics implements IAbility {

    private static final Map<Player, BukkitTask> activeTasks = new HashMap<>();
    private static final Map<Player, Set<Player>> buffedPlayers = new HashMap<>();
    private static final double RANGE = 30;
    private static final int MAX_PLAYERS = 5;

    @Override
    public void onEquip(Player owner) {
        Pet pet = playerPetManager.getActivePet(owner);
        if (pet == null) return;

        buffedPlayers.put(owner, new HashSet<>());

        BukkitTask task = Bukkit.getScheduler().runTaskTimer(Main.getInstance(),
                () -> updateNearbyPlayerBuffs(owner, pet), 0L, 20L);

        activeTasks.put(owner, task);
    }

    @Override
    public void onUnequip(Player owner) {
        BukkitTask task = activeTasks.remove(owner);
        if (task != null) {
            task.cancel();
        }

        Set<Player> previouslyBuffed = buffedPlayers.remove(owner);
        if (previouslyBuffed != null) {
            // EcoSkills API has been removed - no stat modifiers to remove
            for (Player buffedPlayer : previouslyBuffed) {
                // Stat modifier removal disabled
            }
        }
    }

    private void updateNearbyPlayerBuffs(Player owner, Pet pet) {
        if (owner == null || !owner.isOnline()) return;

        Set<Player> currentlyBuffed = buffedPlayers.get(owner);
        if (currentlyBuffed == null) return;

        Set<Player> nearbyPlayers = new HashSet<>();
        for (Player player : Bukkit.getOnlinePlayers()) {
            if (player.getWorld().equals(owner.getWorld()) &&
                player.getLocation().distance(owner.getLocation()) <= RANGE) {
                nearbyPlayers.add(player);
                if (nearbyPlayers.size() >= MAX_PLAYERS) break;
            }
        }

        double fishingSpeedPerPlayer = 0.1 + (0.1 * pet.getLevel());
        double totalFishingSpeed = fishingSpeedPerPlayer * nearbyPlayers.size();

        Set<Player> playersToUnbuff = new HashSet<>(currentlyBuffed);
        playersToUnbuff.removeAll(nearbyPlayers);
        for (Player player : playersToUnbuff) {
            // EcoSkills API has been removed - no stat modifiers to remove
        }

        for (Player nearbyPlayer : nearbyPlayers) {
            if (currentlyBuffed.contains(nearbyPlayer)) {
                // EcoSkills API has been removed - no stat modifiers to remove
            }

            // EcoSkills API has been removed - stat modifications disabled
            // Fishing speed buff calculation: fishingSpeedPerPlayer * nearbyPlayers.size()
        }

        currentlyBuffed.clear();
        currentlyBuffed.addAll(nearbyPlayers);
    }

    @Override
    public AbilityStats getAbilityStat() {
        return null;
    }
}
