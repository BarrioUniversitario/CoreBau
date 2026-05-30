package me.davidml16.baul.pets.listeners.actions;

import me.davidml16.baul.Main;
import me.davidml16.baul.pets.pet.Pet;
import me.davidml16.baul.pets.utils.Messages;
import me.davidml16.baul.pets.utils.enums.XPSource;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

import java.util.Set;

public class SkillsListener implements Listener {

    private final Main plugin;

    public SkillsListener(Main plugin) {
        this.plugin = plugin;
        // EcoSkills integration removed - listener disabled
        // Bukkit.getPluginManager().registerEvents(this, plugin);
    }

    // EcoSkills integration removed - event handler disabled
    /*
    @EventHandler
    public void onXpGain(PlayerSkillXPGainEvent event) {
        Player player = event.getPlayer();
        Pet activePet = plugin.getPlayerPetManager().getActivePet(player);
        Set<Pet> sharedPets = plugin.getPlayerPetManager().getPlayerSharedPets(player);

        if (activePet == null) return;

        try {
            XPSource xpSource = XPSource.valueOf(event.getSkill().getId().toUpperCase());

            activePet.gainExp((int) event.getGainedXP(), xpSource);
            sharedPets.forEach(pet -> pet.gainExp((int) event.getGainedXP(), xpSource));

        } catch (IllegalArgumentException e) {
            Messages.debug("Unknown skill ID: " + event.getSkill().getId() + ". Cannot process XP gain.");
        }
    }
    */
}
