package me.davidml16.baul.pets.menu;

import me.davidml16.baul.Main;
import me.davidml16.baul.pets.data.PlayerData;
import me.davidml16.baul.pets.pet.Pet;
import me.davidml16.baul.pets.utils.InventoryUtils;
import me.davidml16.baul.pets.utils.Messages;
import me.davidml16.baul.pets.utils.PetUtils;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

public class UpgradeMenu implements InventoryHolder {

    private final Inventory inventory;

    public UpgradeMenu() {
        this.inventory = Bukkit.createInventory(this, 45,
                Component.text("Your Pets").color(NamedTextColor.GOLD));
    }

    public void openUpgradeMenu(Player player) {
        inventory.clear();
        PlayerData playerData = Main.getInstance().getPlayerPetManager().getPlayerData(player);

        for (int i = 0; i < inventory.getSize(); i++) {
            if (i == 13) continue;
            inventory.setItem(i, InventoryUtils.BLACK_FILLER);
        }

        inventory.setItem(40, InventoryUtils.CLOSE);

        if (playerData.hasUpgradingPet()) {
            Pet upgradingPet = playerData.getUpgradingPet();
            if (upgradingPet.getUpgrade().getEndTime() - System.currentTimeMillis() <= 0) {
                completeUpgrade(upgradingPet);
            } else {
                petUpgrading(upgradingPet);
            }
            inventory.setItem(13, PetUtils.petToItem(upgradingPet));
        } else {
            noPet();
        }

        player.openInventory(inventory);
    }

    public void noPet() {
        ItemStack itemStack = new ItemStack(Material.RED_CONCRETE);
        itemStack.editMeta(meta -> {
            meta.displayName(Messages.deserialize("<red>No Pet Selected"));
            meta.lore(Messages.deserialize("<gray>You need to select a pet to upgrade.", "<gray>Please place a pet above to upgrade."));
        });
        inventory.setItem(22, itemStack);
    }

    public void noUpgradesAvailable() {
        ItemStack itemStack = new ItemStack(Material.YELLOW_CONCRETE);
        itemStack.editMeta(meta -> {
            meta.displayName(Messages.deserialize("<yellow>No Upgrades Available"));
            meta.lore(Messages.deserialize("<gray>Your pet is already at maximum level.", "<gray>No upgrades are available at this time."));
        });
        inventory.setItem(22, itemStack);
    }

    public void petUpgradeInfo(Pet pet) {
        ItemStack itemStack = new ItemStack(Material.GREEN_CONCRETE);
        itemStack.editMeta(meta -> {
            meta.displayName(Messages.deserialize("<green>Upgrade " + pet.getColoredName()));
            List<Component> lore = new ArrayList<>(Messages.deserialize("<gray>Your " + pet.getColoredName() + " pet is ready for an upgrade!",
                    "<gray>This process will take <blue>" + pet.getUpgrade().getDurationString() + ".</gray>",
                    "",
                    "<gray>Cost: <gold>" + pet.getUpgrade().getCost() + " coins</gray>",
                    "<gray>Required Items: .</gray>"));
            List<Component> requiredItems = new ArrayList<>();
            pet.getUpgrade().getMaterials().forEach((material, amount) ->
                    requiredItems.add(Messages.deserialize("<gray> - " + amount + "x " + material.name().toLowerCase().replace("_", " ") + "</gray>")));
            lore.addAll(requiredItems);
            meta.lore(lore);
        });
        inventory.setItem(22, itemStack);
    }

    public void petUpgrading(Pet pet) {
        ItemStack itemStack = new ItemStack(Material.ORANGE_CONCRETE);
        itemStack.editMeta(meta -> {
            meta.displayName(Messages.deserialize("<green>Upgrade " + pet.getColoredName()));
            meta.lore(Messages.deserialize("<gray>Your " + pet.getColoredName() + " pet is upgrading!",
                    "<gray>Time Left: " + "<green>" + pet.getUpgrade().getRemainingTime() + "</green>" + ".</gray>")
            );
        });
        inventory.setItem(22, itemStack);
    }

    public void completeUpgrade(Pet pet) {
        ItemStack itemStack = new ItemStack(Material.LIME_CONCRETE);
        itemStack.editMeta(meta -> {
            meta.displayName(Messages.deserialize("<green>Upgrade Complete: " + pet.getColoredName()));
            meta.lore(Messages.deserialize("<gray>Your pet has been successfully upgraded!", "<gray>It is now stronger and ready for action!"));
        });
        inventory.setItem(22, itemStack);
    }

    @NotNull
    public Inventory getInventory() {
        return this.inventory;
    }

}
