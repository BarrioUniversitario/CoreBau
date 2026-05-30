package me.davidml16.baul.pets.listeners.player;

import me.davidml16.baul.Main;
import me.davidml16.baul.pets.PlayerPetManager;
import me.davidml16.baul.pets.data.PlayerData;
import me.davidml16.baul.pets.pet.Pet;
import me.davidml16.baul.pets.utils.Keys;
import me.davidml16.baul.pets.utils.PetUtils;
import net.kyori.adventure.text.Component;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;

public class PetInteractItemListener implements Listener {

    private final PlayerPetManager petManager;

    public PetInteractItemListener(Main plugin) {
        this.petManager = plugin.getPlayerPetManager();
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
    }

    @EventHandler
    public void onPetInteract(PlayerInteractEvent event) {
        if (event.getClickedBlock() != null) return;
        Player player = event.getPlayer();
        ItemStack item = event.getItem();

        if (item == null || item.getItemMeta() == null) return;
        if (!item.getItemMeta().getPersistentDataContainer().has(Keys.PET_ID_KEY)) return;

        Pet pet = PetUtils.itemToPet(item);
        if (pet == null) {
            player.sendMessage(Component.text("Failed to add pet. Please try again."));
            return;
        }

        PlayerData playerData = petManager.getPlayerData(player);
        if (playerData == null) {
            player.sendMessage(Component.text("Please wait while your data is loading..."));
            return;
        }

        playerData.addPet(pet);

        player.sendMessage(Component.text("You successfully added your pet to your pet menu!"));
        player.getInventory().remove(item);
    }
}