package me.davidml16.baul.pets.abilities.horse;

import me.davidml16.baul.Main;
import me.davidml16.baul.pets.abilities.AbilityStats;
import me.davidml16.baul.pets.abilities.IAbility;
import me.davidml16.baul.pets.utils.Messages;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Horse;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.inventory.InventoryOpenEvent;
import org.bukkit.event.vehicle.VehicleExitEvent;
import org.bukkit.inventory.HorseInventory;
import org.bukkit.scheduler.BukkitRunnable;

public class Ridable implements IAbility {

    @Override
    public void onEquip(Player player) {
        spawnRideableHorse(player);
    }

    @Override
    public void onUnequip(Player player) {
        removeHorse(player);
    }

    @Override
    public void handleEvent(Event event, Player owner) {
        if (event instanceof InventoryOpenEvent inventoryEvent) {
            if (inventoryEvent.getInventory().getHolder() instanceof HorseInventory horse) {
                if (mountManager.isMounted(owner.getUniqueId())) {
                    inventoryEvent.setCancelled(true);
                }
            }
        } else if (event instanceof VehicleExitEvent exitEvent) {
            if (exitEvent.getVehicle() instanceof Horse horse) {
                if (mountManager.isMounted(owner.getUniqueId())) {
                    new BukkitRunnable() {
                        @Override
                        public void run() {
                            if (horse.isValid() && owner.isOnline()) {
                                Location playerLoc = owner.getLocation();
                                horse.teleport(playerLoc.clone().add(1, 0, 1));
                            }
                        }
                    }.runTaskLater(Main.getInstance(), 20L);
                }
            }
        }
    }

    private void spawnRideableHorse(Player player) {
        removeHorse(player); // Remove any existing horse

        Location spawnLoc = player.getLocation().clone().add(1, 0, 1);
        Horse horse = (Horse) player.getWorld().spawnEntity(spawnLoc, EntityType.HORSE);

        // Configure the horse
        horse.setTamed(true);
        horse.setOwner(player);
        horse.setAdult();
        horse.setAgeLock(true);
        horse.customName(Messages.deserialize("<gray>"+ player.getName() + "'s Mount"));
        horse.setCustomNameVisible(true);

        mountManager.mount(player.getUniqueId(), horse.getUniqueId());

        horse.getInventory().clear();

        new BukkitRunnable() {
            @Override
            public void run() {
                if (horse.isValid() && player.isOnline()) {
                    horse.addPassenger(player);
                }
            }
        }.runTaskLater(Main.getInstance(), 10L);
    }

    private void removeHorse(Player player) {
        if (!mountManager.isMounted(player.getUniqueId())) return;
        Horse horse = (Horse) Bukkit.getEntity(mountManager.getMount(player.getUniqueId()));
        if (horse != null && horse.isValid()) {
            horse.eject(); // Remove any passengers
            horse.remove();
        }
    }

    @Override
    public AbilityStats getAbilityStat() {
        return null;
    }
}
