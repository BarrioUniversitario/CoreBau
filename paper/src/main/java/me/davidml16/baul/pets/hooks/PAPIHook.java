package me.davidml16.baul.pets.hooks;

import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import me.davidml16.baul.Main;
import me.davidml16.baul.pets.data.PlayerData;
import me.davidml16.baul.pets.pet.Pet;
import me.davidml16.baul.pets.utils.Messages;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class PAPIHook extends PlaceholderExpansion {
    private final Main plugin;

    public PAPIHook(Main plugin) {
        this.plugin = plugin;
    }

    @Override
    public @NotNull String getIdentifier() {
        return "BetterPets";
    }

    @Override
    public @NotNull String getAuthor() {
        return "Psikuvit";
    }

    @Override
    public @NotNull String getVersion() {
        return plugin.getDescription().getVersion();
    }

    @Override
    public String onPlaceholderRequest(Player player, @NotNull String params) {
        if (player == null) return "";

        PlayerData data = plugin.getPlayerPetManager().getPlayerData(player);
        if (data == null) return "";

        Pet activePet = data.getActivePet();

        return switch (params.toLowerCase()) {
            case "is_active_pet" -> activePet != null ? "true" : "false";
            case "pet_name" -> activePet != null ? Messages.serialize(Messages.getPetName(activePet)) : "None";
            case "pet_level" -> activePet != null ? String.valueOf(activePet.getLevel()) : "0";
            case "pet_rarity" -> activePet != null ? activePet.getRarity().name() : "None";
            case "total_pets" -> String.valueOf(data.getOwnedPets().size());
            case "pet_xp" -> activePet != null ? String.valueOf(activePet.getCurrentExp()) : "0";
            case "pet_xp_required" -> activePet != null ? String.valueOf(activePet.getRequiredExp()) : "0";
            default -> null;
        };
    }
}
