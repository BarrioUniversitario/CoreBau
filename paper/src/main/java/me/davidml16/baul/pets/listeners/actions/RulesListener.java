package me.davidml16.baul.pets.listeners.actions;

import me.davidml16.baul.Main;
import me.davidml16.baul.pets.PlayerPetManager;
import me.davidml16.baul.pets.autopet.AutoPetRule;
import me.davidml16.baul.pets.autopet.TriggerType;
import me.davidml16.baul.pets.data.PlayerData;
import me.davidml16.baul.pets.pet.Pet;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.player.PlayerAnimationEvent;

public class RulesListener implements Listener {

    private final PlayerPetManager manager;

    public RulesListener(Main plugin) {
        this.manager = plugin.getPlayerPetManager();
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
    }

    @EventHandler(priority = org.bukkit.event.EventPriority.MONITOR)
    public void onBlockBreak(BlockBreakEvent event) {
        Player player = event.getPlayer();
        PlayerData playerData = manager.getPlayerData(player);
        if (playerData.hasAutoPetRule(TriggerType.BREAK_BLOCK)) {
            AutoPetRule rule = playerData.getAutoPetRule(TriggerType.BREAK_BLOCK);
            Pet newPet = rule.getSelectedPet();
            manager.deactivatePet(player);
            manager.activatePet(player, newPet);
        }
    }

    @EventHandler(priority = org.bukkit.event.EventPriority.MONITOR)
    public void onBlockPlace(BlockPlaceEvent event) {
        Player player = event.getPlayer();
        PlayerData playerData = manager.getPlayerData(player);
        if (playerData.hasAutoPetRule(TriggerType.PLACE_BLOCK)) {
            AutoPetRule rule = playerData.getAutoPetRule(TriggerType.PLACE_BLOCK);
            Pet newPet = rule.getSelectedPet();
            manager.deactivatePet(player);
            manager.activatePet(player, newPet);
        }
    }

    @EventHandler(priority = org.bukkit.event.EventPriority.MONITOR)
    public void onSwing(PlayerAnimationEvent event) {
        Player player = event.getPlayer();
        PlayerData playerData = manager.getPlayerData(player);
        if (playerData.hasAutoPetRule(TriggerType.SWING_SWORD)) {
            AutoPetRule rule = playerData.getAutoPetRule(TriggerType.SWING_SWORD);
            Pet newPet = rule.getSelectedPet();
            manager.deactivatePet(player);
            manager.activatePet(player, newPet);
        }
    }
}
