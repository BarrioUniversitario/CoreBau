package me.davidml16.baul.pets.abilities.ender_dragon;

import me.davidml16.baul.pets.abilities.AbilityStats;
import me.davidml16.baul.pets.abilities.IAbility;
import me.davidml16.baul.pets.pet.Pet;
import me.davidml16.baul.pets.utils.enums.Stats;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.entity.EntityDamageByEntityEvent;

public class EndStrike implements IAbility {

    @Override
    public void onEquip(Player paramPlayer) {
    }

    @Override
    public void onUnequip(Player paramPlayer) {
    }

    @Override
    public void handleEvent(Event event, Player owner) {
        if (event instanceof EntityDamageByEntityEvent entityEvent) {
            World world = owner.getWorld();
            Pet pet = playerPetManager.getActivePet(owner);

            if (world.getEnvironment() == World.Environment.THE_END) {

                double damageMultiplier = getAbilityStat().getStatAmplifier(Stats.STRENGTH).getStatAtLevel(pet.getLevel());
                entityEvent.setDamage(entityEvent.getDamage() * (1 + damageMultiplier));
            }
        }
    }

    @Override
    public AbilityStats getAbilityStat() {
        return new AbilityStats().addStatAmplifier(Stats.STRENGTH, 0.02, 0.02);
    }
}
