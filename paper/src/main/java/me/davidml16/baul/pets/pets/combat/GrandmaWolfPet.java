package me.davidml16.baul.pets.pets.combat;

import me.davidml16.baul.pets.abilities.AbilityBuilder;
import me.davidml16.baul.pets.abilities.PetAbility;
import me.davidml16.baul.pets.abilities.grandma_wolf.KillCombo;
import me.davidml16.baul.pets.pet.Pet;
import me.davidml16.baul.pets.pet.PetAttribute;
import me.davidml16.baul.pets.pet.PetUpgrade;
import me.davidml16.baul.pets.utils.enums.PetType;
import me.davidml16.baul.pets.utils.enums.Rarity;
import me.davidml16.baul.pets.utils.enums.Stats;

import java.util.ArrayList;
import java.util.List;

public class GrandmaWolfPet extends Pet {

    private static final String PET_ID = "grandma_wolf";
    private static final String PET_NAME = "Grandma Wolf";
    private static final String TEXTURE_URL = "https://textures.minecraft.net/texture/dc3dd984bb659849bd52994046964c22725f717e986b12d548fd169367e5c";

    public GrandmaWolfPet(Rarity rarity) {
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
                .name("Kill Combo")
                .description(List.of("Gain buffs for combo kills. Effects stack as you",
                        "increase your combo.",
                        "5 Combo (lasts <stat>s)",
                        "+3% Magic Find",
                        "10 Combo (lasts <stat>s)",
                        "+10 coins per kill",
                        "15 Combo (lasts <stat>s)",
                        "+3% Magic Find",
                        "20 Combo (lasts <stat>s)",
                        "+15 Combat Wisdom",
                        "25 Combo (lasts <stat>s)",
                        "+3% Magic Find",
                        "30 Combo (lasts <stat>s)",
                        "+20 coins per kill",
                        "The pet's perk are active even when the pet in not",
                        "equipped."
                ))
                .implementation(new KillCombo())
                .build());

        return abilities;
    }

    @Override
    public PetAttribute[] initializeAttributes() {
        List<PetAttribute> attributes = new ArrayList<>();

        attributes.add(new PetAttribute(1, 1, Stats.STRENGTH));
        attributes.add(new PetAttribute(0.25, 0.25, Stats.HEALTH));

        return attributes.toArray(new PetAttribute[0]);
    }

    @Override
    protected PetUpgrade initializeUpgrade() {
        return null;
    }
}
