package me.davidml16.baul.cosmetics.types;

import me.davidml16.baul.cosmetics.Cosmetic;
import me.davidml16.baul.cosmetics.CosmeticCategory;

/**
 * Cosmético de mascota. El renderizado lo gestiona el subsistema embebido
 * {@code me.davidml16.baul.pets} (rescate de BetterPets): cada entrada debe
 * apuntar a una plantilla del registro vía el campo {@code betterPets} con la
 * forma {@code <baseId>_<rareza>} (p. ej. {@code wolf_common},
 * {@code ender_dragon_legendary}).
 */
public class Pet extends Cosmetic {

    /** Id de plantilla BetterPets, p. ej. {@code chicken_common}. */
    private final String betterPetsId;

    public Pet(String key, String displayName, String rarity, String iconMaterial, String permission,
               String betterPetsId, long price) {
        super(key, CosmeticCategory.PET, displayName, rarity, iconMaterial, permission, price);
        this.betterPetsId = betterPetsId == null ? "" : betterPetsId.trim().toLowerCase();
    }

    public String getBetterPetsId() {
        return betterPetsId;
    }
}
