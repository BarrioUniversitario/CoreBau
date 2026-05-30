package me.davidml16.baul.pets.listeners.gui;

import me.davidml16.baul.Main;
import me.davidml16.baul.pets.autopet.AutoPetRuleCreationSession;
import me.davidml16.baul.pets.data.PlayerData;
import me.davidml16.baul.pets.menu.EXPShareGUI;
import me.davidml16.baul.pets.menu.PetSelectionGUI;
import me.davidml16.baul.pets.pet.Pet;
import me.davidml16.baul.pets.utils.PetUtils;
import me.davidml16.baul.pets.utils.enums.PetSelectionCause;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;

public class PetSelectionListener implements Listener {

    private final Main plugin;

    public PetSelectionListener(Main plugin) {
        this.plugin = plugin;
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
    }

    @EventHandler
    public void onClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof org.bukkit.entity.Player player)) return;

        PlayerData playerData = plugin.getPlayerPetManager().getPlayerData(player);
        ItemStack clicked = event.getCurrentItem();

        if (clicked == null || clicked.getType().isAir()) return;

        if (event.getInventory().getHolder() instanceof PetSelectionGUI petSelectionGUI) {
            event.setCancelled(true);
            int slot = event.getRawSlot();

            if (petSelectionGUI.getCause().equals(PetSelectionCause.EXP_SHARE)) {
                if (slot == 49) {
                    player.closeInventory();
                    playerData.getPetsInventory().getExpShareGUI().openExpMenu(player);
                } else if (PetUtils.isPet(clicked)) {
                    Pet pet = playerData.getPetByUUID(PetUtils.extractUUID(clicked));
                    if (pet != null && pet.getLevel() < 100 && !playerData.isSharedPet(pet)) {
                        playerData.addSharedPet(pet);
                        player.closeInventory();
                        new EXPShareGUI().openExpMenu(player);
                    }
                }
            } else if (petSelectionGUI.getCause().equals(PetSelectionCause.PET_RULE)) {
                if (PetUtils.isPet(clicked)) {
                    Pet pet = playerData.getPetByUUID(PetUtils.extractUUID(clicked));

                    AutoPetRuleCreationSession session = plugin.getAutoPetRuleService().getSession(player);
                    session.setSelectedPet(pet);

                    plugin.getAutoPetRuleService().createRule(player);
                    player.closeInventory();
                }
            }
        }
    }
}
