package me.davidml16.baul.pets.autopet;

import me.davidml16.baul.Main;
import me.davidml16.baul.pets.data.PlayerData;
import me.davidml16.baul.pets.pet.Pet;
import me.davidml16.baul.pets.utils.Messages;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Service class that handles AutoPet rule business logic.
 * Manages rule creation, validation, and persistence.
 */
public class AutoPetRuleService {

    private final Main plugin;
    private final Map<UUID, AutoPetRuleCreationSession> sessions;


    public AutoPetRuleService(Main plugin) {
        this.plugin = plugin;
        this.sessions = new HashMap<>();
    }

    public void createRule(Player player) {
        AutoPetRuleCreationSession session = getSession(player);
        if (!session.isComplete()) {
            player.sendMessage(Messages.deserialize("<red>Rule creation incomplete! Please select a pet.</red>"));
            return;
        }

        PlayerData playerData = plugin.getPlayerPetManager().getPlayerData(player);

        if (playerData.hasAutoPetRule(session.getTriggerType())) {
            player.sendMessage(Messages.deserialize("<yellow>You already have a rule for this trigger! The old rule will be replaced.</yellow>"));
            AutoPetRule existingRule = playerData.getAutoPetRule(session.getTriggerType());
            if (existingRule != null) {
                playerData.removeAutoPetRule(existingRule);
            }
        }

        AutoPetRule newRule = new AutoPetRule(session.getTriggerType(), session.getSelectedPet());
        for (AutoPetException exception : session.getExceptions()) {
            newRule.addException(exception);
        }

        playerData.addAutoPetRule(newRule);

        player.sendMessage(Messages.deserialize("<green>AutoPet rule created successfully!</green>"));
        completeSession(player);
    }


    public void validatePetSelection(Player player, UUID petUuid) {
        PlayerData playerData = plugin.getPlayerPetManager().getPlayerData(player);
        Pet selectedPet = playerData.getPetByUUID(petUuid);

        if (selectedPet == null) {
            player.sendMessage(Messages.deserialize("<red>Pet not found!</red>"));
        }

    }

    /**
     * Gets a pet by UUID for a player.
     */
    public Pet getPet(Player player, UUID petUuid) {
        PlayerData playerData = plugin.getPlayerPetManager().getPlayerData(player);
        return playerData.getPetByUUID(petUuid);
    }

    /**
     * Removes an AutoPet rule for a player.
     */
    public boolean removeRule(Player player, TriggerType triggerType) {
        PlayerData playerData = plugin.getPlayerPetManager().getPlayerData(player);

        if (!playerData.hasAutoPetRule(triggerType)) {
            player.sendMessage(Messages.deserialize("<red>No rule found for this trigger!</red>"));
            return false;
        }

        AutoPetRule rule = playerData.getAutoPetRule(triggerType);
        playerData.removeAutoPetRule(rule);

        player.sendMessage(Messages.deserialize("<green>AutoPet rule removed successfully!</green>"));
        return true;
    }

    public AutoPetRuleCreationSession getSession(Player player) {
        return sessions.computeIfAbsent(player.getUniqueId(), k -> new AutoPetRuleCreationSession());
    }

    public void completeSession(Player player) {
        sessions.remove(player.getUniqueId());
    }
}
