package me.davidml16.baul.pets.listeners.player;

import me.davidml16.baul.Main;
import me.davidml16.baul.pets.utils.PetUtils;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.inventory.ItemStack;

public class BlockPlaceListener implements Listener {

    public BlockPlaceListener(Main plugin) {
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
    }

    @EventHandler
    public void onPlace(BlockPlaceEvent event) {
        ItemStack item = event.getItemInHand();
        if (PetUtils.isPet(item) || PetUtils.isSkinItem(item)) event.setCancelled(true);
    }

}
