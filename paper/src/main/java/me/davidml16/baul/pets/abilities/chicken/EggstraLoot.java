package me.davidml16.baul.pets.abilities.chicken;

import me.davidml16.baul.pets.abilities.AbilityStats;
import me.davidml16.baul.pets.abilities.IAbility;
import me.davidml16.baul.pets.pet.Pet;
import org.bukkit.Material;
import org.bukkit.entity.Animals;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class EggstraLoot implements IAbility {

    private final Random random = new Random();

    @Override
    public void handleEvent(Event event, Player owner) {
        if (!(event instanceof EntityDeathEvent deathEvent)) return;
        if (!(deathEvent.getEntity().getKiller() instanceof Player player)) return;
        if (!player.equals(owner)) return;

        Pet pet = playerPetManager.getActivePet(player);
        if (pet == null) return;

        if (deathEvent.getEntity().getType() == EntityType.CHICKEN) {
            boolean hasEgg = deathEvent.getDrops().stream()
                    .anyMatch(item -> item.getType() == Material.EGG);

            if (!hasEgg) {
                deathEvent.getDrops().add(new ItemStack(Material.EGG, 1));
            }
        }

        if (deathEvent.getEntity() instanceof Animals) {
            double additionalDropChance = 1 + (0.8 * pet.getLevel());

            if (random.nextDouble() * 100 < additionalDropChance) {
                List<ItemStack> originalDrops = new ArrayList<>(deathEvent.getDrops());
                if (!originalDrops.isEmpty()) {
                    ItemStack itemToDuplicate = originalDrops.get(random.nextInt(originalDrops.size()));
                    deathEvent.getDrops().add(itemToDuplicate.clone());
                }
            }
        }
    }

    @Override
    public void onEquip(Player paramPlayer) {
    }

    @Override
    public void onUnequip(Player paramPlayer) {
    }

    @Override
    public AbilityStats getAbilityStat() {
        return null;
    }
}
