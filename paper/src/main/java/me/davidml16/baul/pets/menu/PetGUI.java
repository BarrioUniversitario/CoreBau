package me.davidml16.baul.pets.menu;

import me.davidml16.baul.Main;
import me.davidml16.baul.pets.data.PlayerData;
import me.davidml16.baul.pets.pet.Pet;
import me.davidml16.baul.pets.utils.InventoryUtils;
import me.davidml16.baul.pets.utils.Messages;
import me.davidml16.baul.pets.utils.PetUtils;
import me.davidml16.baul.pets.utils.enums.SortType;
import me.davidml16.baul.pets.utils.enums.VisibilityType;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class PetGUI implements InventoryHolder {

    private Inventory inventory;
    private final EXPShareGUI expShareGUI;


    public PetGUI() {
        this.inventory = Bukkit.createInventory(this, 54,
                Component.text("Your Pets").color(NamedTextColor.GOLD));
        this.expShareGUI = new EXPShareGUI();

    }

    private ItemStack createSortButton(SortType currentSort) {
        ItemStack sortButton = InventoryUtils.SORT.clone();
        ItemMeta meta = sortButton.getItemMeta();

        List<Component> lore = List.of(
                Component.empty(),
                formatSortType(SortType.DEFAULT, currentSort),
                formatSortType(SortType.ALPHABETICAL, currentSort),
                formatSortType(SortType.PET_EXP, currentSort),
                formatSortType(SortType.SKILL, currentSort),
                Component.empty(),
                Messages.deserialize("<aqua>Right Click to go backwards</aqua>"),
                Messages.deserialize("<yellow>Left Click to switch sort</yellow>")
        );

        meta.lore(lore);
        sortButton.setItemMeta(meta);
        return sortButton;
    }

    private Component formatSortType(SortType type, SortType current) {
        return Messages.deserialize((type == current ? "<color:#0b5f8a><bold>â–º</bold></color> " : "  ") + "<gray>" + type.getDisplayName() + "</gray>");
    }

    private ItemStack createVisibilityButton(VisibilityType currentVisibility) {
        ItemStack visibilityButton = InventoryUtils.VISIBILITY.clone();
        ItemMeta meta = visibilityButton.getItemMeta();

        List<Component> lore = List.of(
                Component.empty(),
                formatVisibilityType(VisibilityType.ALL, currentVisibility),
                formatVisibilityType(VisibilityType.NONE, currentVisibility),
                formatVisibilityType(VisibilityType.OWN, currentVisibility),
                Component.empty(),
                Messages.deserialize("<aqua>Right Click to go backwards</aqua>"),
                Messages.deserialize("<yellow>Left Click to switch visibility</yellow>")
        );

        meta.lore(lore);
        visibilityButton.setItemMeta(meta);
        return visibilityButton;
    }

    private Component formatVisibilityType(VisibilityType type, VisibilityType current) {
        return Messages.deserialize((type == current ? "<color:#0b5f8a><bold>â–º</bold></color> " : "  ") + "<gray>" + type.getDisplayName() + "</gray>");
    }

    public void openPetsMenu(Player player, int page) {
        PlayerData playerData = Main.getInstance().getPlayerPetManager().getPlayerData(player);

        Set<Pet> playerPets = playerData.getOwnedPets();
        List<Pet> petsList = new ArrayList<>(playerPets);

        sortPets(petsList, playerData.getSortType());

        int maxPages = (int) Math.ceil(petsList.size() / 45.0D);

        if (page < 0) page = 0;
        if (page >= maxPages) page = maxPages - 1;

        playerData.setInventoryPage(page);
        this.inventory.clear();

        for (int paginationSlot : InventoryUtils.PAGINATION_SLOTS)
            this.inventory.setItem(paginationSlot, InventoryUtils.BLACK_FILLER);

        ItemStack info = InventoryUtils.INFO.clone();
        info.editMeta(meta -> {
            List<Component> lore = meta.lore();
            if (lore != null) {
                Pet pet = playerData.getActivePet();
                String replacement = pet == null ? "<red>None</red>" : pet.getColoredName();
                lore.set(lore.size() - 1, Messages.deserialize("<gray>Selected Pet: " + replacement + "</gray>"));
                meta.lore(lore);
            }
        });
        this.inventory.setItem(4, info);

        this.inventory.setItem(48, InventoryUtils.PREVIOUS_PAGE);
        this.inventory.setItem(53, InventoryUtils.NEXT_PAGE);
        this.inventory.setItem(7, InventoryUtils.EXP_SHARE);
        this.inventory.setItem(46, InventoryUtils.AUTO_PET_RULE);
        this.inventory.setItem(49, InventoryUtils.CLOSE);
        this.inventory.setItem(50, InventoryUtils.SEARCH);
        this.inventory.setItem(51, createVisibilityButton(playerData.getVisibilityType()));
        this.inventory.setItem(52, createSortButton(playerData.getSortType()));

        int startIndex = page * 45;
        int endIndex = Math.min(startIndex + 45, petsList.size());

        if (!petsList.isEmpty())
            for (int i = startIndex; i < endIndex; i++) {
                Pet pet = petsList.get(i);
                this.inventory.addItem(PetUtils.petToItem(pet));
            }
        playerData.openInventory();
    }

    private void sortPets(List<Pet> pets, SortType sortType) {
        switch (sortType) {
            case ALPHABETICAL -> pets.sort((p1, p2) ->
                    p1.getName().compareToIgnoreCase(p2.getName()));
            case PET_EXP -> pets.sort((p1, p2) ->
                    Integer.compare(p2.getCurrentExp(), p1.getCurrentExp()));
            case SKILL -> pets.sort((p1, p2) ->
                    p1.getType().name().compareToIgnoreCase(p2.getType().name()));
            default -> pets.sort((p1, p2) ->
                    p2.getRarity().compareTo(p1.getRarity()));
        }
    }

    @NotNull
    public Inventory getInventory() {
        return this.inventory;
    }

    public EXPShareGUI getExpShareGUI() {
        return expShareGUI;
    }


    public void setInventory(Inventory inventory) {
        this.inventory = inventory;
    }
}
