package me.davidml16.baul.pets.pets.combat;

import me.davidml16.baul.pets.abilities.AbilityBuilder;
import me.davidml16.baul.pets.abilities.PetAbility;
import me.davidml16.baul.pets.abilities.horse.Ridable;
import me.davidml16.baul.pets.abilities.horse.RideIntoBattle;
import me.davidml16.baul.pets.abilities.horse.Run;
import me.davidml16.baul.pets.pet.Pet;
import me.davidml16.baul.pets.pet.PetAttribute;
import me.davidml16.baul.pets.pet.PetUpgrade;
import me.davidml16.baul.pets.utils.enums.PetType;
import me.davidml16.baul.pets.utils.enums.Rarity;
import me.davidml16.baul.pets.utils.enums.Stats;

import java.util.ArrayList;
import java.util.List;

public class SkeletonHorsePet extends Pet {

    private static final String PET_ID = "skeleton_horse";
    private static final String PET_NAME = "Skeleton Horse";
    private static final String TEXTURE_URL = "https://textures.minecraft.net/texture/47effce35132c86ff72bcae77dfbb1d22587e94df3cbc2570ed17cf8973a";

    public SkeletonHorsePet(Rarity rarity) {
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
                .name("Ridable")
                .description("Right-click your summoned pet to ride it!")
                .implementation(new Ridable()).build());

        abilities.add(new AbilityBuilder()
                .name("Run")
                .description("Increases the speed of your mount by",
                        "<green><stat>%</green>")
                .implementation(new Run())
                .build());

        abilities.add(new AbilityBuilder()
                .name("Ride Into Battle")
                .description("While riding your horse, gain <green>+<stat>%</green>",
                        "bow damage")
                .implementation(new RideIntoBattle()).build());

        return abilities;
    }

    @Override
    public PetAttribute[] initializeAttributes() {
        List<PetAttribute> attributes = new ArrayList<>();

        attributes.add(new PetAttribute(0.5, 0.5, Stats.SPEED));
        attributes.add(new PetAttribute(1, 1, Stats.INTELLIGENCE));

        return attributes.toArray(new PetAttribute[0]);
    }

    @Override
    protected PetUpgrade initializeUpgrade() {
        return null;
    }
}
