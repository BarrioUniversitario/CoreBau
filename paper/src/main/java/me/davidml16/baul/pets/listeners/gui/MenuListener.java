package me.davidml16.baul.pets.listeners.gui;

import me.davidml16.baul.Main;
import me.davidml16.baul.pets.PlayerPetManager;
import me.davidml16.baul.pets.data.PlayerData;
import me.davidml16.baul.pets.menu.AutoPetRuleMenu;
import me.davidml16.baul.pets.pet.Pet;
import me.davidml16.baul.pets.utils.Messages;
import me.davidml16.baul.pets.utils.PetUtils;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.ItemStack;

import java.util.Set;
import java.util.UUID;

public class MenuListener implements Listener {
    private final PlayerPetManager manager;

    public MenuListener(Main plugin) {
        this.manager = plugin.getPlayerPetManager();
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (!(event.getInventory().getHolder() instanceof me.davidml16.baul.pets.menu.PetGUI petGUI)) return;
        event.setCancelled(true);

        if (!(event.getWhoClicked() instanceof Player player)) return;

        PlayerData playerData = this.manager.getPlayerData(player);

        ItemStack clicked = event.getCurrentItem();
        if (clicked == null) return;

        int slot = event.getRawSlot();
        int currentPage = playerData.getSelectedPage();

        switch (slot) {
            case 7:
                petGUI.getExpShareGUI().openExpMenu(player);
                return;
            case 46:
                new AutoPetRuleMenu(AutoPetRuleMenu.MenuType.RULE_MANAGEMENT, playerData).openMenu();
                return;
            case 48:
                if (currentPage > 0) playerData.getPetsInventory().openPetsMenu(player, currentPage - 1);
                return;
            case 49:
                player.closeInventory();
                return;
            case 51:
                if (event.isLeftClick()) {
                    playerData.cycleVisibility();
                } else if (event.isRightClick()) {
                    playerData.setVisibilityType(playerData.getVisibilityType().previous());
                }
                playerData.applyVisibility();
                petGUI.openPetsMenu(player, currentPage);
                return;
            case 52:
                if (event.isLeftClick()) {
                    playerData.cycleSortType();
                } else if (event.isRightClick()) {
                    playerData.setSortType(playerData.getSortType().previous());
                }
                petGUI.openPetsMenu(player, currentPage);
                return;
            case 53:
                Set<Pet> pets = playerData.getOwnedPets();
                int maxPages = (int) Math.ceil(pets.size() / 45.0D);
                if (currentPage < maxPages - 1)
                    playerData.getPetsInventory().openPetsMenu(player, currentPage + 1);
                return;
        }

        if (PetUtils.isPet(clicked)) {
            UUID petUUID = PetUtils.extractUUID(clicked);

            Pet pet = playerData.getPetByUUID(petUUID);
            if (pet == null) return;
            if (event.isLeftClick()) {
                if (this.manager.isActivePet(player, pet)) this.manager.deactivatePet(player);
                else {
                    Bukkit.getScheduler().runTask(Main.getInstance(), () -> this.manager.activatePet(player, pet));
                }
            } else if (event.isRightClick()) {
                if (this.manager.isActivePet(player, pet)) {
                    manager.deactivatePet(player);
                }
                manager.removePetFromPlayer(player, pet);
                player.getInventory().addItem(PetUtils.petToItem(pet));
                player.sendMessage(Messages.deserialize("You have removed " + pet.getColoredName() + " from your pets menu."));
            }

            player.closeInventory();
        }

        if (event.getClickedInventory() != null && event
                .getClickedInventory().getType() == InventoryType.PLAYER && event.getView().getTopInventory().getHolder() instanceof me.davidml16.baul.pets.menu.PetGUI)
            event.setCancelled(true);
    }

    @EventHandler
    public void onInventoryDrag(InventoryDragEvent event) {
        if (!(event.getWhoClicked() instanceof Player))
            return;
        if (event.getInventory().getHolder() instanceof me.davidml16.baul.pets.menu.PetGUI)
            event.setCancelled(true);
    }
}
