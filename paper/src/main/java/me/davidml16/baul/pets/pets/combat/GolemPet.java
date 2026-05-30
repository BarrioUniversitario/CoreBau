package me.davidml16.baul.pets.pets.combat;

import me.davidml16.baul.pets.abilities.AbilityBuilder;
import me.davidml16.baul.pets.abilities.PetAbility;
import me.davidml16.baul.pets.abilities.golem.LastStand;
import me.davidml16.baul.pets.abilities.golem.Ricochet;
import me.davidml16.baul.pets.abilities.golem.Toss;
import me.davidml16.baul.pets.pet.Pet;
import me.davidml16.baul.pets.pet.PetAttribute;
import me.davidml16.baul.pets.pet.PetUpgrade;
import me.davidml16.baul.pets.utils.enums.PetType;
import me.davidml16.baul.pets.utils.enums.Rarity;
import me.davidml16.baul.pets.utils.enums.Stats;
import org.bukkit.Material;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class GolemPet extends Pet {

    private static final String PET_ID = "golem";
    private static final String PET_NAME = "Golem";
    private static final String TEXTURE_URL = "https://textures.minecraft.net/texture/89091d79ea0f59ef7ef94d7bba6e5f17f2f7d4572c44f90f76c4819a714";

    public GolemPet(Rarity rarity) {
        super(PET_ID + "_" + rarity.name().toLowerCase(),
                PET_NAME,
                PetType.MINING,
                rarity,
                TEXTURE_URL);
    }


    @Override
    protected List<PetAbility> initializeAbilities() {
        List<PetAbility> abilities = new ArrayList<>();

        // Last Stand (Epic+ & Legendary): below 20% HP -> DR 20%, 40% max HP shield, +40% damage; 12s, 60s CD
        abilities.add(new AbilityBuilder()
                .name("Last Stand")
                .description(List.of(
                        "When below 20% HP: <green>-20% damage taken</green>",
                        "Gain shield = <green>40% max HP</green>",
                        "Deal <green>+40% damage</green> (12s, 60s CD)"))
                .implementation(new LastStand())
                .build());

        if (getRarity().ordinal() >= Rarity.EPIC.ordinal()) {
            abilities.add(new AbilityBuilder()
                    .name("Ricochet")
                    .description(List.of("Chance to reflect attacks: <green><stat>%</green>"))
                    .implementation(new Ricochet())
                    .build());
        }

        if (getRarity().ordinal() >= Rarity.LEGENDARY.ordinal()) {
            abilities.add(new AbilityBuilder()
                    .name("Toss")
                    .description(List.of("Every 5 hits: launch enemy, deal <green>5x</green> damage (5s CD)"))
                    .implementation(new Toss())
                    .build());
        }

        return abilities;
    }

    @Override
    public PetAttribute[] initializeAttributes() {
        List<PetAttribute> attributes = new ArrayList<>();

        attributes.add(new PetAttribute(0.01, 0.01, Stats.SWING_RANGE));
        attributes.add(new PetAttribute(1.5, 1.5, Stats.HEALTH));
        attributes.add(new PetAttribute(0.5, 0.5, Stats.STRENGTH));

        return attributes.toArray(new PetAttribute[0]);
    }

    @Override
    protected PetUpgrade initializeUpgrade() {
        Map<Material, Integer> upgradeMaterials = new HashMap<>();

        return switch (getRarity()) {
            case EPIC -> {
                upgradeMaterials.put(Material.DIAMOND_BLOCK, 4);
                upgradeMaterials.put(Material.EMERALD_BLOCK, 2);
                yield new PetUpgrade(Rarity.LEGENDARY, 7000, 4 * 60 * 60 * 1000L, upgradeMaterials);
            }
            default -> null;
        };
    }
}
