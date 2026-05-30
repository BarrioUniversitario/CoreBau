package me.davidml16.baul.pets.abilities.bee;

import me.davidml16.baul.pets.abilities.AbilityStats;
import me.davidml16.baul.pets.abilities.IAbility;
import me.davidml16.baul.pets.pet.Pet;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

public class WeaponizedHoney implements IAbility {

    @Override
    public void onEquip(Player paramPlayer) {}

    @Override
    public void onUnequip(Player paramPlayer) {}

    @Override
    public void handleEvent(Event event, Player owner) {
        if (event instanceof EntityDamageEvent damageEvent) {
            if (damageEvent.getEntity().equals(owner)) {
                Pet pet = playerPetManager.getActivePet(owner);
                if (pet != null) {

                    int absorptionLevel = getAbsorptionLevel(damageEvent, pet);

                    PotionEffect absorptionEffect = new PotionEffect(
                        PotionEffectType.ABSORPTION,
                        1200,
                        absorptionLevel - 1,
                        false,
                        true,
                        true
                    );

                    owner.addPotionEffect(absorptionEffect);
                }
            }
        }
    }

    private static int getAbsorptionLevel(EntityDamageEvent damageEvent, Pet pet) {
        double absorptionPercentage = 5.2 + (0.2 * pet.getLevel());

        double damageReceived = damageEvent.getFinalDamage();
        double absorptionHearts = (damageReceived * absorptionPercentage) / 100.0;

        return Math.max(1, (int) Math.ceil(absorptionHearts / 2.0));
    }

    @Override
    public AbilityStats getAbilityStat() {
        return null;
    }
}
