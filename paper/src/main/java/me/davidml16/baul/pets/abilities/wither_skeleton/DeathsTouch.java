package me.davidml16.baul.pets.abilities.wither_skeleton;

import me.davidml16.baul.pets.abilities.AbilityStats;
import me.davidml16.baul.pets.abilities.IAbility;
import me.davidml16.baul.pets.pet.Pet;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

public class DeathsTouch implements IAbility {

    @Override
    public void handleEvent(Event event, Player owner) {
        if (!(event instanceof EntityDamageByEntityEvent damageEvent)) return;
        if (!(damageEvent.getDamager() instanceof Player player)) return;
        if (!player.equals(owner)) return;
        if (!(damageEvent.getEntity() instanceof LivingEntity target)) return;

        Pet pet = playerPetManager.getActivePet(player);
        if (pet == null) return;

        if (target.hasPotionEffect(PotionEffectType.WITHER)) return;

        double witherDamagePercent = getAbilityStat().getStatAmplifiers().getFirst().getStatAtLevel(pet.getLevel());
        int amplifier = Math.max(0, (int) (witherDamagePercent / 2.0) - 1);

        target.addPotionEffect(new PotionEffect(PotionEffectType.WITHER, 60, amplifier, false, true));
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
        return new AbilityStats().addStatAmplifier(2, 2);
    }
}
