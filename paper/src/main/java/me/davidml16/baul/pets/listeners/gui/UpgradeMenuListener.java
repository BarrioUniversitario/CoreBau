package me.davidml16.baul.pets.listeners.gui;

import me.davidml16.baul.Main;
import me.davidml16.baul.pets.data.PlayerData;
import me.davidml16.baul.pets.menu.UpgradeMenu;
import me.davidml16.baul.pets.pet.Pet;
import me.davidml16.baul.pets.utils.Messages;
import me.davidml16.baul.pets.utils.PetUtils;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryOpenEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class UpgradeMenuListener implements Listener {

    private final Map<UUID, BukkitTask> playerTasks = new HashMap<>();

    private final Main plugin;

    public UpgradeMenuListener(Main plugin) {
        this.plugin = plugin;
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
    }

    @EventHandler
    public void onClose(InventoryCloseEvent event) {
        if (event.getInventory().getHolder() == null || !(event.getInventory().getHolder() instanceof UpgradeMenu upgradeMenu))
            return;

        if (!(event.getPlayer() instanceof Player player)) return;

        PlayerData playerData = plugin.getPlayerPetManager().getPlayerData(player);
        ItemStack petItem = upgradeMenu.getInventory().getItem(13);

        if (PetUtils.isPet(petItem) && !playerData.hasUpgradingPet()) {
            event.getPlayer().getInventory().addItem(petItem);
        }

        if (playerTasks.containsKey(player.getUniqueId())) {
            BukkitTask task = playerTasks.remove(player.getUniqueId());
            if (task != null) task.cancel();

        }
    }

    @EventHandler
    public void onOpen(InventoryOpenEvent event) {
        if (event.getInventory().getHolder() == null || !(event.getInventory().getHolder() instanceof UpgradeMenu upgradeMenu))
            return;

        if (!(event.getPlayer() instanceof Player player)) return;

        PlayerData playerData = plugin.getPlayerPetManager().getPlayerData(player);
        ItemStack petItem = upgradeMenu.getInventory().getItem(13);

        if (PetUtils.isPet(petItem) && playerData.hasUpgradingPet()) {
            BukkitTask task = new BukkitRunnable() {
                @Override
                public void run() {
                    if (!playerData.hasUpgradingPet()) {
                        cancel();
                        playerTasks.remove(player.getUniqueId());
                        return;
                    }
                    if (playerData.getUpgradingPet().getUpgrade().getEndTime() <= System.currentTimeMillis()) {
                        upgradeMenu.completeUpgrade(playerData.getUpgradingPet());

                        playerData.updatePet();
                        Pet newPet = playerData.getUpgradingPet();

                        Messages.debug(newPet.toString());
                        upgradeMenu.getInventory().setItem(13, PetUtils.petToItem(newPet));
                        playerData.setUpgradingPet(null);

                        cancel();
                    } else {
                        upgradeMenu.petUpgrading(playerData.getUpgradingPet());
                    }
                }
            }.runTaskTimerAsynchronously(plugin, 0L, 20L);
            playerTasks.put(player.getUniqueId(), task);
        }
    }

    @EventHandler
    public void onUpgradeMenuClick(InventoryClickEvent event) {
        if (event.getInventory().getHolder() == null || !(event.getInventory().getHolder() instanceof UpgradeMenu upgradeMenu))
            return;

        if (!(event.getWhoClicked() instanceof Player player)) return;

        PlayerData playerData = Main.getInstance().getPlayerPetManager().getPlayerData(player);
        int slot = event.getSlot();

        event.setCancelled(true);

        if (slot == 13 || event.isShiftClick()) {
            handlePetInput(event, playerData, upgradeMenu);
        } else if (slot == 22) {
            handlePetUpgrade(event, playerData, upgradeMenu);
        } else if (slot == 40) {
            player.closeInventory();
        } else {
            if (event.getClickedInventory() == null || event.getClickedInventory().getType() == InventoryType.PLAYER) {
                if (event.getView().getTopInventory().getHolder() instanceof UpgradeMenu) {
                    event.setCancelled(false);
                }
            }
        }
    }

    public void handlePetInput(InventoryClickEvent event, PlayerData playerData, UpgradeMenu upgradeMenu) {
        Player player = (Player) event.getWhoClicked();
        ItemStack clickedItem = event.getCurrentItem();
        ItemStack cursorItem = event.getCursor();

        if (playerData.hasUpgradingPet()) {
            player.sendMessage("Â§cYou cannot remove a pet that is currently being upgraded!");
            return;
        }

        if (event.isShiftClick()) {
            event.setCancelled(false);
            if (PetUtils.isPet(clickedItem)) {
                Pet pet = PetUtils.itemToPet(clickedItem);
                if (pet != null) {
                    if (pet.getUpgrade() != null) {
                        upgradeMenu.petUpgradeInfo(pet);
                    } else {
                        upgradeMenu.noUpgradesAvailable();
                    }
                }
            }
        } else {
            if (!cursorItem.getType().isAir()) {
                if (!PetUtils.isPet(cursorItem)) {
                    Messages.debug("Invalid item clicked in upgrade menu: " + cursorItem.getType());
                    return;
                }
                Pet pet = PetUtils.itemToPet(cursorItem);
                if (pet != null) {
                    upgradeMenu.getInventory().setItem(13, cursorItem);
                    event.setCursor(null);
                    if (pet.getUpgrade() != null) {
                        upgradeMenu.petUpgradeInfo(pet);
                    } else {
                        upgradeMenu.noUpgradesAvailable();
                    }
                }
            } else {
                if (PetUtils.isPet(clickedItem)) {
                    upgradeMenu.getInventory().setItem(13, null);
                    player.getInventory().addItem(clickedItem);
                    upgradeMenu.noPet();
                }
            }
        }
    }

    public void handlePetUpgrade(InventoryClickEvent event, PlayerData playerData, UpgradeMenu upgradeMenu) {
        Player player = (Player) event.getWhoClicked();

        ItemStack petItem = upgradeMenu.getInventory().getItem(13);
        if (PetUtils.isPet(petItem)) {
            Pet pet = PetUtils.itemToPet(petItem);
            if (playerData.hasUpgradingPet()) {
                player.sendMessage(Messages.deserialize("<red>You already have a pet being upgraded!</red>"));
                return;
            }
            if (pet != null && pet.getUpgrade() != null) {
                for (Map.Entry<Material, Integer> entry : pet.getUpgrade().getMaterials().entrySet()) {
                    if (player.getInventory().contains(entry.getKey(), entry.getValue())) {
                        player.getInventory().removeItem(new ItemStack(entry.getKey(), entry.getValue()));
                    } else {
                        player.sendMessage(Messages.deserialize("<red>You do not have enough " +
                                entry.getKey().name().toLowerCase().replace("_", " ") + "!</red>"));
                        return;
                    }
                }

                playerData.setUpgradingPet(pet);
                long ticks = pet.getUpgrade().getDuration() / 50L;
                Messages.debug(String.valueOf(ticks));
                player.sendMessage("Â§aUpgrade started! Your pet will be ready in " + pet.getUpgrade().getDurationString());
                player.closeInventory();
            }
        }
    }
}
