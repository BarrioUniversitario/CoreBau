package me.davidml16.baul.pets.menu;

import me.davidml16.baul.Main;
import me.davidml16.baul.pets.data.PlayerData;
import me.davidml16.baul.pets.pet.Pet;
import me.davidml16.baul.pets.utils.InventoryUtils;
import me.davidml16.baul.pets.utils.Messages;
import me.davidml16.baul.pets.utils.PetUtils;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

public class EXPShareGUI implements InventoryHolder {

    private static final int[] RED_GLASS_SLOTS = {21, 22, 23};
    private static final int[] SHARE_SLOTS = {30, 31, 32};

    private Inventory inventory;

    public EXPShareGUI() {
        inventory = Bukkit.createInventory(this, 54, Messages.getGuiExpShareTitle());
    }

    public void openExpMenu(Player player) {
        PlayerData playerData = Main.getInstance().getPlayerPetManager().getPlayerData(player);

        // Fill with black stained glass
        for (int i = 0; i < inventory.getSize(); i++) {
            inventory.setItem(i, InventoryUtils.BLACK_FILLER);
        }

        // Add red glass slots with sharing status
        ItemStack redGlass = InventoryUtils.createGuiItem(Material.RED_STAINED_GLASS_PANE,
                Messages.getGuiShareStatusTitle(),
                playerData.getSharedPets().isEmpty() ? Messages.getGuiShareStatusInactive() :
                        Messages.getGuiShareStatusActive(),
                Messages.getGuiShareStatusCount(playerData.getSharedPets().size())
        );

        for (int slot : RED_GLASS_SLOTS) {
            inventory.setItem(slot, redGlass);
        }

        // Add shared pets to slots or empty slots
        int slotIndex = 0;
        for (int slot : SHARE_SLOTS) {
            if (slotIndex < playerData.getSharedPets().size()) {
                Pet sharedPet = playerData.getSharedPets().stream().toList().get(slotIndex);
                inventory.setItem(slot, PetUtils.petToItem(sharedPet));
            } else {
                inventory.setItem(slot, InventoryUtils.createGuiItem(Material.LIGHT_GRAY_STAINED_GLASS_PANE,
                        Messages.getGuiSharePetClick(),
                        Messages.getGuiSharePetLore()));
            }
            slotIndex++;
        }

        // Add control buttons
        inventory.setItem(48, InventoryUtils.PREVIOUS_PAGE);
        inventory.setItem(49, InventoryUtils.CLOSE);

        player.openInventory(inventory);
    }

    @NotNull
    public Inventory getInventory() {
        return inventory;
    }

    public void setInventory(Inventory inventory) {
        this.inventory = inventory;
    }
}
