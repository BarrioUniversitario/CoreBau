package me.davidml16.baul.pets.abilities.parrot;

import me.davidml16.baul.Main;
import me.davidml16.baul.pets.abilities.AbilityStats;
import me.davidml16.baul.pets.abilities.IAbility;
import me.davidml16.baul.pets.pet.Pet;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.entity.EntityPotionEffectEvent;
import org.bukkit.potion.PotionEffect;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class Repeat implements IAbility {

    // Track original durations to avoid stacking boosts
    private final Map<UUID, Map<String, Integer>> originalDurations = new HashMap<>();

    @Override
    public void onEquip(Player owner) {
        Pet pet = playerPetManager.getActivePet(owner);
        if (pet == null) return;

        originalDurations.put(owner.getUniqueId(), new HashMap<>());

        Collection<PotionEffect> currentEffects = new ArrayList<>(owner.getActivePotionEffects());
        for (PotionEffect effect : currentEffects) {
            if (effect.getDuration() > 0) {
                originalDurations.get(owner.getUniqueId()).put(effect.getType().getName(), effect.getDuration());

                double boostPercentage = 5 + (0.3 * pet.getLevel());
                int newDuration = (int) (effect.getDuration() * (1.0 + boostPercentage / 100.0));

                owner.removePotionEffect(effect.getType());
                owner.addPotionEffect(new PotionEffect(
                    effect.getType(),
                    newDuration,
                    effect.getAmplifier(),
                    effect.isAmbient(),
                    effect.hasParticles(),
                    effect.hasIcon()
                ));
            }
        }
    }

    @Override
    public void onUnequip(Player owner) {
        originalDurations.remove(owner.getUniqueId());
    }

    @Override
    public void handleEvent(Event event, Player owner) {
        if (!(event instanceof EntityPotionEffectEvent potionEvent)) return;
        if (!potionEvent.getEntity().equals(owner)) return;
        if (potionEvent.getAction() != EntityPotionEffectEvent.Action.ADDED) return;

        Pet pet = playerPetManager.getActivePet(owner);
        if (pet == null) return;

        PotionEffect newEffect = potionEvent.getNewEffect();
        if (newEffect == null || newEffect.getDuration() <= 0) return;

        Map<String, Integer> playerOriginalDurations = originalDurations.get(owner.getUniqueId());
        if (playerOriginalDurations == null) return;

        String effectName = newEffect.getType().getName();

        if (!playerOriginalDurations.containsKey(effectName)) {
            playerOriginalDurations.put(effectName, newEffect.getDuration());

            double boostPercentage = 5 + (0.3 * pet.getLevel());
            int boostedDuration = (int) (newEffect.getDuration() * (1.0 + boostPercentage / 100.0));

            potionEvent.setCancelled(true);

            owner.getScheduler().run(Main.getInstance(),
                    (task) -> owner.addPotionEffect(new PotionEffect(
                            newEffect.getType(),
                            boostedDuration,
                            newEffect.getAmplifier(),
                            newEffect.isAmbient(),
                            newEffect.hasParticles(),
                            newEffect.hasIcon()
                    )), null);
        }
    }

    @Override
    public AbilityStats getAbilityStat() {
        return null;
    }
}
