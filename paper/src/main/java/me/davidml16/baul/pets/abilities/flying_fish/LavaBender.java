package me.davidml16.baul.pets.abilities.flying_fish;

import me.davidml16.baul.Main;
import me.davidml16.baul.pets.abilities.AbilityStats;
import me.davidml16.baul.pets.abilities.IAbility;
import me.davidml16.baul.pets.pet.Pet;
import me.davidml16.baul.pets.utils.enums.Stats;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitTask;

import java.util.HashMap;
import java.util.Map;

public class LavaBender implements IAbility {

    private static final Map<Player, BukkitTask> activeTasks = new HashMap<>();
    private static final Map<Player, Boolean> buffedPlayers = new HashMap<>();

    @Override
    public void onEquip(Player owner) {
        Pet pet = playerPetManager.getActivePet(owner);
        if (pet == null) return;

        buffedPlayers.put(owner, false);

        BukkitTask task = Bukkit.getScheduler().runTaskTimer(Main.getInstance(),
                () -> updateNearWaterLavaBuffs(owner, pet), 0L, 20L);

        activeTasks.put(owner, task);
    }

    @Override
    public void onUnequip(Player owner) {
        BukkitTask task = activeTasks.remove(owner);
        if (task != null) {
            task.cancel();
        }

        Boolean wasBuffed = buffedPlayers.remove(owner);
        if (wasBuffed != null && wasBuffed) {
            removeAbilityStats(owner);
        }
    }

    private void updateNearWaterLavaBuffs(Player owner, Pet pet) {
        if (owner == null || !owner.isOnline()) return;

        boolean nearWaterOrLava = isNearWaterOrLava(owner);
        Boolean currentlyBuffed = buffedPlayers.get(owner);

        if (currentlyBuffed == null) return;

        if (nearWaterOrLava && !currentlyBuffed) {
            applyAbilityStats(owner);
            buffedPlayers.put(owner, true);
        } else if (!nearWaterOrLava && currentlyBuffed) {
            removeAbilityStats(owner);
            buffedPlayers.put(owner, false);
        }
    }


    private boolean isNearWaterOrLava(Player player) {
        Location loc = player.getLocation();

        for (int x = -1; x <= 1; x++) {
            for (int y = -1; y <= 1; y++) {
                for (int z = -1; z <= 1; z++) {
                    Material blockType = loc.clone().add(x, y, z).getBlock().getType();
                    if (blockType == Material.WATER || blockType == Material.LAVA) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    @Override
    public AbilityStats getAbilityStat() {
        return new AbilityStats()
                .addStatAmplifier(Stats.STRENGTH, 1, 1)
                .addStatAmplifier(Stats.DEFENSE, 1, 1);
    }
}
