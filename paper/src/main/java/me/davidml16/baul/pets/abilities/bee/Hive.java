package me.davidml16.baul.pets.abilities.bee;

import me.davidml16.baul.pets.abilities.AbilityStats;
import me.davidml16.baul.pets.abilities.IAbility;
import me.davidml16.baul.pets.utils.enums.Stats;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

public class Hive implements IAbility {

    private final Map<UUID, Set<UUID>> buffedPlayers = new HashMap<>();

    @Override
    public void onEquip(Player owner) {
        List<Player> nearbyPlayers = owner.getWorld().getNearbyEntities(owner.getLocation(), 25, 25, 25).stream()
                .filter(entity -> entity instanceof Player)
                .filter(entity -> !entity.equals(owner)) // Exclude the owner
                .map(entity -> (Player) entity)
                .limit(15)
                .toList();

        Set<UUID> playerBuffs = buffedPlayers.computeIfAbsent(owner.getUniqueId(), k -> new HashSet<>());

        applyAbilityStats(owner);
        playerBuffs.add(owner.getUniqueId());

        for (Player nearbyPlayer : nearbyPlayers) {
            applyAbilityStats(nearbyPlayer);
            playerBuffs.add(nearbyPlayer.getUniqueId());
        }
    }

    @Override
    public void onUnequip(Player owner) {
        Set<UUID> playerBuffs = buffedPlayers.get(owner.getUniqueId());

        if (playerBuffs != null) {
            for (UUID playerId : playerBuffs) {
                Player buffedPlayer = owner.getServer().getPlayer(playerId);
                if (buffedPlayer != null && buffedPlayer.isOnline()) {
                    removeAbilityStats(buffedPlayer);
                }
            }

            buffedPlayers.remove(owner.getUniqueId());
        }
    }

    @Override
    public AbilityStats getAbilityStat() {
        AbilityStats abilityStats = new AbilityStats();
        abilityStats.addStatAmplifier(Stats.INTELLIGENCE, 1.09, 0.09);
        abilityStats.addStatAmplifier(Stats.STRENGTH, 1.07, 0.07);
        abilityStats.addStatAmplifier(Stats.DEFENSE, 1.04, 0.04);
        return abilityStats;
    }
}
