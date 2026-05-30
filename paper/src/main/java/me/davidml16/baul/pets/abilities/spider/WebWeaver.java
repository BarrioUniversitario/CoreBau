package me.davidml16.baul.pets.abilities.spider;

import me.davidml16.baul.pets.abilities.AbilityStats;
import me.davidml16.baul.pets.abilities.IAbility;
import me.davidml16.baul.pets.pet.Pet;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

public class WebWeaver implements IAbility {
    @Override
    public void onEquip(Player paramPlayer) {

    }

    @Override
    public void onUnequip(Player paramPlayer) {

    }

    @Override
    public void handleEvent(Event event, Player owner) {
        if (!(event instanceof EntityDamageByEntityEvent damageEvent)) return;
        if (!damageEvent.getDamager().equals(owner)) return;
        if (!(damageEvent.getEntity() instanceof LivingEntity target)) return;
        if (target instanceof Player) return; // Don't affect other players

        Pet pet = playerPetManager.getActivePet(owner);
        if (pet == null) return;

        double slownessPercent = getAbilityStat().getStatAmplifiers().getFirst().getStatAtLevel(pet.getLevel());

        int amplifier = Math.max(0, (int) Math.ceil(slownessPercent / 15.0) - 1);

        target.addPotionEffect(new PotionEffect(PotionEffectType.SLOWNESS, 60, amplifier, false, true));
    }

    @Override
    public AbilityStats getAbilityStat() {
        return new AbilityStats().addStatAmplifier(0.4, 0.4);
    }
}
