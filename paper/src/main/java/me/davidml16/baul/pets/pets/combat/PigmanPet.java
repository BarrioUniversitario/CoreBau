package me.davidml16.baul.pets.pets.combat;

import me.davidml16.baul.pets.abilities.AbilityBuilder;
import me.davidml16.baul.pets.abilities.PetAbility;
import me.davidml16.baul.pets.abilities.pigman.BaconFarmer;
import me.davidml16.baul.pets.abilities.pigman.GiantSlayer;
import me.davidml16.baul.pets.abilities.pigman.PorkMaster;
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

public class PigmanPet extends Pet {

    private static final String PET_ID = "pigman";
    private static final String PET_NAME = "Pigman";
    private static final String TEXTURE_URL = "https://textures.minecraft.net/texture/74e9c6e98582ffd8ff8feb3322cd1849c43fb16b158abb11ca7b42eda7743eb";

    public PigmanPet(Rarity rarity) {
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
                .name("Bacon Farmer")
                .description("Pig minions work <green><stat>%</green> faster",
                        "while on your island")
                .implementation(new BaconFarmer())
                .build());

        if (getRarity().ordinal() >= Rarity.RARE.ordinal()) {
            abilities.add(new AbilityBuilder()
                    .name("Pork Master")
                    .description("Buffs the <gold>Pigman Sword</gold> by <green><stat>",
                            "<red>Damage</red> and <red>Strength</red>")
                    .implementation(new PorkMaster())
                    .build());
        }

        if (getRarity().ordinal() >= Rarity.LEGENDARY.ordinal()) {
            abilities.add(new AbilityBuilder()
                    .name("Giant Slayer")
                    .description("Deal <red>+50%</red> damage to monsters Level <green>50+</green>",
                            "<red>+75%</red> damage to monsters Level <green>100+</green>")
                    .implementation(new GiantSlayer())
                    .build());
        }

        return abilities;
    }

    @Override
    public PetAttribute[] initializeAttributes() {
        List<PetAttribute> attributes = new ArrayList<>();

        attributes.add(new PetAttribute(0.5, 0.5, Stats.DEFENSE));
        attributes.add(new PetAttribute(0.5, 0.5, Stats.STRENGTH));
        attributes.add(new PetAttribute(0.05, 0.05, Stats.FEROCITY));

        return attributes.toArray(new PetAttribute[0]);
    }

    @Override
    protected PetUpgrade initializeUpgrade() {
        Map<Material, Integer> upgradeMaterials = new HashMap<>();

        return switch (getRarity()) {
            case COMMON -> {
                upgradeMaterials.put(Material.PORKCHOP, 64);
                yield new PetUpgrade(Rarity.COMMON, 1000, 2 * 60 * 60 * 1000L, upgradeMaterials);
            }
            case UNCOMMON -> {
                upgradeMaterials.put(Material.GOLD_INGOT, 32);
                upgradeMaterials.put(Material.PORKCHOP, 64);
                yield new PetUpgrade(Rarity.RARE, 3000, 4 * 60 * 60 * 1000L, upgradeMaterials);
            }
            case RARE -> {
                upgradeMaterials.put(Material.GOLD_INGOT, 64);
                upgradeMaterials.put(Material.PORKCHOP, 96);
                yield new PetUpgrade(Rarity.EPIC, 6000, 8 * 60 * 60 * 1000L, upgradeMaterials);
            }
            case EPIC -> {
                upgradeMaterials.put(Material.GOLD_INGOT, 64);
                upgradeMaterials.put(Material.PORKCHOP, 128);
                yield new PetUpgrade(Rarity.LEGENDARY, 12000, 6 * 60 * 60 * 1000L, upgradeMaterials);
            }
            default -> null;
        };
    }
}
