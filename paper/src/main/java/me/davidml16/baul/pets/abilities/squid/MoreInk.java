package me.davidml16.baul.pets.abilities.squid;

import me.davidml16.baul.pets.abilities.AbilityStats;
import me.davidml16.baul.pets.abilities.IAbility;
import me.davidml16.baul.pets.pet.Pet;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.inventory.ItemStack;

import java.util.Random;

public class MoreInk implements IAbility {

    private final Random random = new Random();

    @Override
    public void handleEvent(Event event, Player owner) {
        if (!(event instanceof EntityDeathEvent deathEvent)) return;
        if (deathEvent.getEntity().getType() != EntityType.SQUID) return;
        if (!(deathEvent.getEntity().getKiller() instanceof Player player)) return;

        Pet pet = playerPetManager.getActivePet(player);
        if (pet == null) return;

        double doubleDropChance = getAbilityStat().getStatAmplifiers().getFirst().getStatAtLevel(pet.getLevel());

        if (random.nextDouble() * 100 < doubleDropChance) {
            for (ItemStack drop : deathEvent.getDrops()) {
                deathEvent.getDrops().add(drop.clone());
            }
        }
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
        return new AbilityStats().addStatAmplifier(1, 1);
    }
}
