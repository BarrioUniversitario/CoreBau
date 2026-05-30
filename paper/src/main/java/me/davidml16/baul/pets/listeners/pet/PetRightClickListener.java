package me.davidml16.baul.pets.listeners.pet;

import me.davidml16.baul.Main;
import me.davidml16.baul.pets.PlayerPetManager;
import me.davidml16.baul.pets.api.events.PetInteractEvent;
import me.davidml16.baul.pets.items.PetItem;
import me.davidml16.baul.pets.pet.Pet;
import me.davidml16.baul.pets.pet.PetSkin;
import me.davidml16.baul.pets.utils.Messages;
import me.davidml16.baul.pets.utils.PetUtils;
import org.bukkit.Bukkit;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Interaction;
import org.bukkit.entity.ItemDisplay;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.inventory.ItemStack;

import java.util.UUID;

public class PetRightClickListener implements Listener {

    private final Main plugin;
    private final PlayerPetManager manager;

    public PetRightClickListener(Main plugin) {
        this.plugin = plugin;
        this.manager = plugin.getPlayerPetManager();
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
    }

    @EventHandler
    public void onPetRightClick(PlayerInteractEntityEvent event) {
        Player player = event.getPlayer();
        Entity clicked = event.getRightClicked();
        ItemStack item = player.getInventory().getItemInMainHand();

        // Handle interaction with either the ItemDisplay or Interaction entity
        if (clicked instanceof ItemDisplay || clicked instanceof Interaction) {
            UUID petUUID = PetUtils.extractUUID(clicked);
            if (petUUID == null) return;

            Pet pet = manager.getPetByUUID(player, petUUID);
            if (pet == null) return;

            PetInteractEvent petInteractEvent = new PetInteractEvent(player, pet);
            Bukkit.getPluginManager().callEvent(petInteractEvent);
            if (petInteractEvent.isCancelled()) return;

            if (PetUtils.isSkinItem(item)) {
                PetSkin skin = plugin.getPetSkinLoader().getSkinById(PetUtils.getSkinId(item));
                if (skin == null) {
                    player.sendMessage(Messages.getSkinNotFound());
                    return;
                }
                String petId = pet.getId().substring(0, pet.getId().lastIndexOf('_'));
                String skinPetId = skin.petId();

                if (!petId.equals(skinPetId)) {
                    player.sendMessage(Messages.getSkinIncompatible());
                    return;
                }

                pet.setPetSkin(skin);
                item.setAmount(item.getAmount() - 1);
                player.sendMessage(Messages.getSkinEquipped(skin, pet));
            } else if (PetUtils.isPetItem(item)) {
                PetItem petItem = plugin.getPetItemLoader().getItem(PetUtils.getPetItemId(item));
                if (petItem == null) {
                    player.sendMessage("error");
                    return;
                }
                pet.equipItem(petItem);
                item.setAmount(item.getAmount() - 1);
            }
        }
    }
}
