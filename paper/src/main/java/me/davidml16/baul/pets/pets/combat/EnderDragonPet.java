package me.davidml16.baul.pets.pets.combat;

import me.davidml16.baul.pets.abilities.AbilityBuilder;
import me.davidml16.baul.pets.abilities.PetAbility;
import me.davidml16.baul.pets.abilities.ender_dragon.EndStrike;
import me.davidml16.baul.pets.abilities.ender_dragon.OneWithTheDragon;
import me.davidml16.baul.pets.abilities.ender_dragon.Superior;
import me.davidml16.baul.pets.pet.Pet;
import me.davidml16.baul.pets.pet.PetAttribute;
import me.davidml16.baul.pets.pet.PetUpgrade;
import me.davidml16.baul.pets.utils.enums.PetType;
import me.davidml16.baul.pets.utils.enums.Rarity;
import me.davidml16.baul.pets.utils.enums.Stats;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class EnderDragonPet extends Pet {

    private static final String PET_ID = "ender_dragon";
    private static final String PET_NAME = "Ender Dragon";
    private static final String TEXTURE_URL = "https://textures.minecraft.net/texture/aec3ff563290b13ff3bcc36898af7eaa988b6cc18dc254147f58374afe9b21b9";

    public EnderDragonPet(Rarity rarity) {
        super(PET_ID + "_" + rarity.name().toLowerCase(),
                PET_NAME,
                PetType.COMBAT,
                rarity,
                TEXTURE_URL);
    }

    @Override
    protected List<PetAbility> initializeAbilities() {
        List<PetAbility> abilities = new ArrayList<>();

        abilities.add(new AbilityBuilder()
                .name("End Strike")
                .description(Collections.singletonList("Deal <green><stat>%</green> more damage to end mobs"))
                .implementation(new EndStrike())
                .build());

        abilities.add(new AbilityBuilder()
                .name("One With The Dragon")
                .description(List.of("Buffs the Aspect of the Dragons and Dragon Armor",
                        "Increases damage against dragons and end mobs"))
                .implementation(new OneWithTheDragon())
                .build());

        if (getRarity().ordinal() >= Rarity.LEGENDARY.ordinal()) {
            abilities.add(new AbilityBuilder()
                    .name("Superior")
                    .description(Collections.singletonList("Increases all stats by <green><stat>%</green>."))
                    .implementation(new Superior())
                    .build());

        }
        return abilities;
    }

    @Override
    public PetAttribute[] initializeAttributes() {
        List<PetAttribute> attributes = new ArrayList<>();

        attributes.add(new PetAttribute(0.5, 0.5, Stats.STRENGTH));
        attributes.add(new PetAttribute(0.1, 0.1, Stats.CRIT_CHANCE));
        attributes.add(new PetAttribute(0.5, 0.5, Stats.CRIT_DAMAGE));

        return attributes.toArray(new PetAttribute[0]);
    }

    @Override
    protected PetUpgrade initializeUpgrade() {
        return null;
    }
}
