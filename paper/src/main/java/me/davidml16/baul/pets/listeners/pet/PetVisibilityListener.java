package me.davidml16.baul.pets.listeners.pet;

import me.davidml16.baul.Main;
import me.davidml16.baul.pets.PlayerPetManager;
import me.davidml16.baul.pets.data.PlayerData;
import me.davidml16.baul.pets.pet.Pet;
import me.davidml16.baul.pets.utils.enums.VisibilityType;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

public class PetVisibilityListener implements Listener {

    private final PlayerPetManager manager;

    public PetVisibilityListener(Main plugin) {
        this.manager = plugin.getPlayerPetManager();
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        PlayerData playerData = manager.getPlayerData(player);

        for (Player onlinePlayer : Bukkit.getOnlinePlayers()) {
            PlayerData otherData = manager.getPlayerData(onlinePlayer);
            Pet otherPet = otherData.getActivePet();

            if (otherPet == null) continue;

            applyVisibility(player, otherPet, playerData.getVisibilityType());

            if (onlinePlayer != player) {
                applyVisibility(onlinePlayer, otherPet, otherData.getVisibilityType());
            }
        }
    }

    public static void applyVisibility(Player player, Pet pet, VisibilityType visibilityType) {
        if (pet == null || pet.getDisplay() == null) return;

        PlayerData petOwnerData = Main.getInstance().getPlayerPetManager().getPlayerData(player);

        switch (visibilityType) {
            case ALL -> player.showEntity(Main.getInstance(), pet.getDisplay());
            case NONE -> player.hideEntity(Main.getInstance(), pet.getDisplay());
            case OWN -> {
                if (petOwnerData.getPlayerUUID().equals(player.getUniqueId())) {
                    player.showEntity(Main.getInstance(), pet.getDisplay());
                } else {
                    player.hideEntity(Main.getInstance(), pet.getDisplay());
                }
            }
        }
    }
}
