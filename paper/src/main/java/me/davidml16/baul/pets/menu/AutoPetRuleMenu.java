package me.davidml16.baul.pets.menu;

import me.davidml16.baul.pets.autopet.AutoPetRule;
import me.davidml16.baul.pets.autopet.TriggerType;
import me.davidml16.baul.pets.data.PlayerData;
import me.davidml16.baul.pets.utils.InventoryUtils;
import me.davidml16.baul.pets.utils.Keys;
import me.davidml16.baul.pets.utils.Messages;
import me.davidml16.baul.pets.utils.enums.PetSelectionCause;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataType;
import org.jetbrains.annotations.NotNull;

import java.util.Collections;


public class AutoPetRuleMenu implements InventoryHolder {

    private final Inventory inventory;
    private MenuType menuType;
    private final PlayerData playerData;

    public AutoPetRuleMenu(MenuType menuType, PlayerData playerData) {
        this.menuType = menuType;
        this.playerData = playerData;
        this.inventory = Bukkit.createInventory(this, 54, Messages.deserialize("Auto Pet Rules"));
    }

    public void openMenu() {
        Player player = Bukkit.getPlayer(playerData.getPlayerUUID());

        switch (menuType) {
            case RULE_MANAGEMENT -> decorateRuleManagement();
            case TRIGGER_SELECTION -> decorateTriggerSelection();
            case PET_SELECTION -> {
                PetSelectionGUI petSelectionGUI = new PetSelectionGUI(PetSelectionCause.PET_RULE);
                petSelectionGUI.openSelectionMenu(player);
                return;
            }
            case EXCEPTION_SELECTION -> {
            }

        }
        player.openInventory(this.inventory);
    }

    public void openMenu(MenuType menuType) {
        this.menuType = menuType;
        openMenu();
    }

    public void decorateRuleManagement() {
        inventory.clear();
        for (int slot : InventoryUtils.PAGINATION_SLOTS) inventory.setItem(slot, InventoryUtils.BLACK_FILLER);
        inventory.setItem(4, petRules());
        inventory.setItem(48, InventoryUtils.PREVIOUS_PAGE);
        inventory.setItem(49, InventoryUtils.CLOSE);

        for (AutoPetRule autoPetRule : playerData.getAutoPetRules()) {
            inventory.addItem(autoPetRule.getIcon());
        }
        inventory.setItem(50, createRule());
    }

    public void decorateTriggerSelection() {
        inventory.clear();
        for (int slot : InventoryUtils.PAGINATION_SLOTS) inventory.setItem(slot, InventoryUtils.BLACK_FILLER);
        inventory.setItem(49, InventoryUtils.CLOSE);

        for (TriggerType triggerType : TriggerType.values()) {
            ItemStack triggerItem = InventoryUtils.createGuiItem(triggerType.getTriggerIcon(),
                    Messages.deserialize(triggerType.getName()),
                    Messages.deserialize(triggerType.getDescription()));
            triggerItem.editMeta(itemMeta -> {
                itemMeta.getPersistentDataContainer().set(
                        Keys.TRIGGER_KEY, PersistentDataType.STRING, triggerType.name());
                itemMeta.addItemFlags(ItemFlag.HIDE_ADDITIONAL_TOOLTIP);
            });
            inventory.addItem(triggerItem);
        }
    }

    public ItemStack createRule() {
        ItemStack ruleItem = new ItemStack(Material.GREEN_CONCRETE);
        ruleItem.editMeta(meta -> {
            meta.displayName(Messages.deserialize("<green>Create New Rule</green>"));
            meta.lore(Collections.singletonList(Messages.deserialize("<gray>Click to create a new autopet rule.</gray>")));
        });
        return ruleItem;
    }

    public ItemStack petRules() {
        ItemStack petRules = InventoryUtils.AUTO_PET_RULE.clone();
        petRules.editMeta(meta -> {
            meta.displayName(Messages.deserialize("<red>Autopet</red>"));
            meta.lore(Messages.deserialize(
                    "<gray>Define custom <red>rules</red> to</gray>",
                    "<gray>automatically equip your pets.</gray>",
                    "",
                    "<gray>Rules used:</gray> <red>" + playerData.getAutoPetRules().size() + "/4</red>"
            ));
        });
        return petRules;
    }

    @Override
    public @NotNull Inventory getInventory() {
        return inventory;
    }

    public MenuType getMenuType() {
        return menuType;
    }

    public enum MenuType {
        TRIGGER_SELECTION,
        PET_SELECTION,
        EXCEPTION_SELECTION,
        RULE_MANAGEMENT
    }
}
