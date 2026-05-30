package me.davidml16.baul.pets.abilities.silverfish;

import me.davidml16.baul.pets.abilities.AbilityStats;
import me.davidml16.baul.pets.abilities.IAbility;
import me.davidml16.baul.pets.pet.Pet;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.block.BlockBreakEvent;

public class Magnetic implements IAbility {

    @Override
    public void handleEvent(Event event, Player owner) {
        if (!(event instanceof BlockBreakEvent breakEvent)) return;
        Player player = breakEvent.getPlayer();
        Pet pet = playerPetManager.getActivePet(player);

        if (pet == null) return;

        if (isMiningBlock(breakEvent.getBlock().getType())) {
            double expBonus = getAbilityStat().getStatAmplifiers().getFirst().getStatAtLevel(pet.getLevel());
            breakEvent.setExpToDrop((int) (breakEvent.getExpToDrop() * (1.0 + expBonus / 100.0)));
        }
    }

    private boolean isMiningBlock(Material material) {
        return material.name().contains("_ORE") ||
               material == Material.STONE ||
               material == Material.COBBLESTONE ||
               material == Material.DEEPSLATE ||
               material == Material.COBBLED_DEEPSLATE ||
               material.name().contains("STONE");
    }

    @Override
    public void onEquip(Player owner) {
        // Event-based ability - no equip action needed
    }

    @Override
    public void onUnequip(Player owner) {
        // Event-based ability - no unequip action needed
    }

    @Override
    public AbilityStats getAbilityStat() {
        return new AbilityStats().addStatAmplifier(0.5, 0.5);
    }
}
