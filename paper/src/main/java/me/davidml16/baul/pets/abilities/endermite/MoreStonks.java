package me.davidml16.baul.pets.abilities.endermite;

import me.davidml16.baul.pets.abilities.AbilityStats;
import me.davidml16.baul.pets.abilities.IAbility;
import me.davidml16.baul.pets.pet.Pet;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.inventory.ItemStack;

import java.util.Random;

public class MoreStonks implements IAbility {

    private final Random random = new Random();

    @Override
    public void onEquip(Player owner) {

    }

    @Override
    public void onUnequip(Player owner) {

    }

    @Override
    public void handleEvent(Event event, Player owner) {
        if (event instanceof BlockBreakEvent breakEvent) {
            if (breakEvent.getBlock().getType() != Material.END_STONE) return;
            Pet pet = playerPetManager.getActivePet(owner);
            double chance = (double) (1 + pet.getLevel()) / 100;

            if (random.nextDouble() < chance) {
                ItemStack drop = new ItemStack(Material.END_STONE, 1);
                breakEvent.getBlock().getDrops().add(drop);
            }
            breakEvent.setExpToDrop((int) (breakEvent.getExpToDrop() * 1.2));
        }
    }

    @Override
    public AbilityStats getAbilityStat() {
        return null;
    }
}

