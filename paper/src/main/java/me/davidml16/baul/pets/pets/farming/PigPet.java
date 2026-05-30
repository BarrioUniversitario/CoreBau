package me.davidml16.baul.pets.pets.farming;

import me.davidml16.baul.pets.abilities.AbilityBuilder;
import me.davidml16.baul.pets.abilities.PetAbility;
import me.davidml16.baul.pets.abilities.horse.Ridable;
import me.davidml16.baul.pets.abilities.horse.Run;
import me.davidml16.baul.pets.abilities.pig.Sprint;
import me.davidml16.baul.pets.abilities.pig.Trample;
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

public class PigPet extends Pet {

    private static final String PET_ID = "pig";
    private static final String PET_NAME = "Pig";
    private static final String TEXTURE_URL = "https://textures.minecraft.net/texture/621668ef7cb79dd9c22ce3d1f3f4cb6e2559893b6df4a469514e667c16aa4";

    public PigPet(Rarity rarity) {
        super(PET_ID + "_" + rarity.name().toLowerCase(),
                PET_NAME,
                PetType.FARMING,
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
                .description("ncreases the speed of your mount by <green><stat>%")
                .implementation(new Run()).build());

        if (getRarity().ordinal() >= Rarity.RARE.ordinal()) {
            abilities.add(new AbilityBuilder()
                    .name("Sprint")
                    .description("While holding Enchanted Carrot on a",
                            "Stick, increases the speed of your mount by <green><stat>%")
                    .implementation(new Sprint()).build());
        }

        if (getRarity().ordinal() >= Rarity.LEGENDARY.ordinal()) {
            abilities.add(new AbilityBuilder()
                    .name("Trample")
                    .description("Your pig will break all crops that it walks over",
                            "while on your private island or Garden.",
                            "While riding, <gold>Farming Fortune</gold> and Farming Exp",
                            "gain is reduced by <green><stat>%</green>.")
                    .implementation(new Trample()).build());
        }

        return abilities;
    }

    @Override
    public PetAttribute[] initializeAttributes() {
        List<PetAttribute> attributes = new ArrayList<>();

        attributes.add(new PetAttribute(0.25, 0.25, Stats.SPEED));

        return attributes.toArray(new PetAttribute[0]);
    }

    @Override
    protected PetUpgrade initializeUpgrade() {
        Map<Material, Integer> upgradeMaterials = new HashMap<>();

        return switch (getRarity()) {
            case COMMON -> {
                upgradeMaterials.put(Material.CARROT, 64);
                upgradeMaterials.put(Material.POTATO, 64);
                yield new PetUpgrade(Rarity.UNCOMMON, 400, 15 * 60 * 1000L, upgradeMaterials);
            }
            case UNCOMMON -> {
                upgradeMaterials.put(Material.GOLDEN_CARROT, 16);
                upgradeMaterials.put(Material.BAKED_POTATO, 32);
                yield new PetUpgrade(Rarity.RARE, 800, 35 * 60 * 1000L, upgradeMaterials);
            }
            case RARE -> {
                upgradeMaterials.put(Material.GOLDEN_APPLE, 4);
                upgradeMaterials.put(Material.CAKE, 2);
                yield new PetUpgrade(Rarity.EPIC, 1600, 75 * 60 * 1000L, upgradeMaterials);
            }
            case EPIC -> {
                upgradeMaterials.put(Material.ENCHANTED_GOLDEN_APPLE, 1);
                upgradeMaterials.put(Material.DIAMOND, 16);
                yield new PetUpgrade(Rarity.LEGENDARY, 3200, 2 * 60 * 60 * 1000L, upgradeMaterials);
            }
            default -> null;
        };
    }
}
