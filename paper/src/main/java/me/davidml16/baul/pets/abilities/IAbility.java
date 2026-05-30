package me.davidml16.baul.pets.abilities;

import me.davidml16.baul.Main;
import me.davidml16.baul.pets.PlayerPetManager;
import me.davidml16.baul.pets.hooks.WorldGuardHook;
import me.davidml16.baul.pets.pet.Pet;
import me.davidml16.baul.pets.utils.PetUtils;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;

public interface IAbility {

    PlayerPetManager playerPetManager = Main.getInstance().getPlayerPetManager();
    MountManager mountManager = Main.getInstance().getMountManager();
    WorldGuardHook worldGuard = Main.getInstance().getWorldGuardHook();

    void onEquip(Player paramPlayer);
    void onUnequip(Player paramPlayer);
    default void handleEvent(Event event, Player owner) {}
    AbilityStats getAbilityStat();

    default void applyAbilityStats(Player player) {
        // EcoSkills integration removed - ability stats no longer applied
    }

    default void removeAbilityStats(Player player) {
        // EcoSkills integration removed - ability stats no longer removed
    }
}
