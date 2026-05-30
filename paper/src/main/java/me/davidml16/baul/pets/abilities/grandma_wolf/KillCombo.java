package me.davidml16.baul.pets.abilities.grandma_wolf;

import me.davidml16.baul.Main;
import me.davidml16.baul.pets.abilities.AbilityStats;
import me.davidml16.baul.pets.abilities.IAbility;
import me.davidml16.baul.pets.pet.Pet;
import me.davidml16.baul.pets.utils.enums.Stats;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.scheduler.BukkitTask;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class KillCombo implements IAbility {

    private static final Map<Player, Integer> playerCombos = new ConcurrentHashMap<>();
    private static final Map<Player, BukkitTask> comboResetTasks = new ConcurrentHashMap<>();
    private static final Map<Player, Map<Integer, BukkitTask>> activeBuffTasks = new ConcurrentHashMap<>();

    private static final int[] COMBO_THRESHOLDS = {5, 10, 15, 20, 25, 30};

    @Override
    public void handleEvent(Event event, Player owner) {
        if (!(event instanceof EntityDeathEvent deathEvent)) return;
        if (!(deathEvent.getEntity().getKiller() instanceof Player player)) return;
        if (!player.equals(owner)) return;

        Pet pet = playerPetManager.getActivePet(player);

        int currentCombo = playerCombos.getOrDefault(player, 0) + 1;
        playerCombos.put(player, currentCombo);

        BukkitTask existingTask = comboResetTasks.get(player);
        if (existingTask != null) {
            existingTask.cancel();
        }

        for (int threshold : COMBO_THRESHOLDS) {
            if (currentCombo == threshold) {
                applyComboBuffs(player, pet, threshold);
                break;
            }
        }

        BukkitTask resetTask = Bukkit.getScheduler().runTaskLater(Main.getInstance(),
                () -> resetCombo(player), 100L); // 5 seconds

        comboResetTasks.put(player, resetTask);
    }

    private void applyComboBuffs(Player player, Pet pet, int comboLevel) {
        double durationBonus = getDurationBonus(pet.getLevel(), comboLevel);

        switch (comboLevel) {
            case 5:
                applyMagicFindBuff(player, 3.0, 8.0 + durationBonus, comboLevel);
                break;
            case 10:
                applyCoinsPerKillBuff(player, 10, 6.0 + durationBonus, comboLevel);
                break;
            case 15:
                applyMagicFindBuff(player, 3.0, 4.0 + durationBonus, comboLevel);
                break;
            case 20:
                applyCombatWisdomBuff(player, 15.0, 3.0 + durationBonus, comboLevel);
                break;
            case 25:
                applyMagicFindBuff(player, 3.0, 3.0 + (pet.getLevel() - 1) * 0.01, comboLevel);
                break;
            case 30:
                applyCoinsPerKillBuff(player, 10, 2.0 + (pet.getLevel() - 1) * 0.01, comboLevel);
                break;
        }
    }

    private double getDurationBonus(int petLevel, int comboLevel) {
        if (comboLevel >= 25) {
            return (petLevel - 1) * 0.01;
        } else {
            return (petLevel - 1) * 0.02;
        }
    }

    private void applyMagicFindBuff(Player player, double percentage, double duration, int comboLevel) {
        cancelExistingBuff(player, comboLevel);

        player.sendMessage("Â§6Kill Combo " + comboLevel + "! Â§a+" + percentage + "% Magic Find for " + String.format("%.1f", duration) + "s");

        // Schedule removal of the buff
        BukkitTask removeTask = Bukkit.getScheduler().runTaskLater(Main.getInstance(),
                () -> removeMagicFindBuff(player), (long) (duration * 20)); // Convert seconds to ticks

        // Store the task for potential cancellation
        storeBuffTask(player, comboLevel, removeTask);
    }

    private void applyCoinsPerKillBuff(Player player, int coins, double duration, int comboLevel) {
        cancelExistingBuff(player, comboLevel);

        player.sendMessage("Â§6Kill Combo " + comboLevel + "! Â§e+" + coins + " coins per kill for " + String.format("%.1f", duration) + "s");

        BukkitTask removeTask = Bukkit.getScheduler().runTaskLater(Main.getInstance(),
                () -> removeCoinsPerKillBuff(player), (long) (duration * 20));

        storeBuffTask(player, comboLevel, removeTask);
    }

    private void applyCombatWisdomBuff(Player player, double wisdom, double duration, int comboLevel) {
        cancelExistingBuff(player, comboLevel);

        // EcoSkills API has been removed - stat modifications disabled
        // Wisdom modifier value would be: wisdom

        player.sendMessage("Â§6Kill Combo " + comboLevel + "! Â§b+" + wisdom + " Combat Wisdom for " + String.format("%.1f", duration) + "s");

        BukkitTask removeTask = Bukkit.getScheduler().runTaskLater(Main.getInstance(),
                () -> {
                    // EcoSkills API has been removed - no stat modifiers to remove
                }, (long) (duration * 20));

        storeBuffTask(player, comboLevel, removeTask);
    }

    private void cancelExistingBuff(Player player, int comboLevel) {
        Map<Integer, BukkitTask> playerBuffs = activeBuffTasks.get(player);
        if (playerBuffs != null) {
            BukkitTask existingBuff = playerBuffs.get(comboLevel);
            if (existingBuff != null) {
                existingBuff.cancel();
                playerBuffs.remove(comboLevel);
            }
        }
    }

    private void storeBuffTask(Player player, int comboLevel, BukkitTask task) {
        activeBuffTasks.computeIfAbsent(player, k -> new HashMap<>()).put(comboLevel, task);
    }

    private void removeMagicFindBuff(Player player) {
        // Remove Magic Find modifier - placeholder implementation
        // This would remove the actual Magic Find modifier when implemented
    }

    private void removeCoinsPerKillBuff(Player player) {
        // Remove coins per kill buff - placeholder implementation
        // This would remove the actual coins buff when implemented
    }

    private void resetCombo(Player player) {
        playerCombos.remove(player);
        comboResetTasks.remove(player);
        player.sendMessage("Â§cKill combo reset!");
    }

    @Override
    public void onEquip(Player paramPlayer) {
        activeBuffTasks.put(paramPlayer, new HashMap<>());
    }

    @Override
    public void onUnequip(Player paramPlayer) {
        resetCombo(paramPlayer);

        Map<Integer, BukkitTask> playerBuffs = activeBuffTasks.remove(paramPlayer);
        if (playerBuffs != null) {
            for (BukkitTask task : playerBuffs.values()) {
                task.cancel();
            }
        }

        // EcoSkills API has been removed - no stat modifiers to remove
    }

    @Override
    public AbilityStats getAbilityStat() {
        return null; // Empty stats since we're using custom duration calculations
    }
}
