package me.davidml16.baul.pets.pets.fishing;

import me.davidml16.baul.pets.abilities.AbilityBuilder;
import me.davidml16.baul.pets.abilities.PetAbility;
import me.davidml16.baul.pets.abilities.flying_fish.LavaBender;
import me.davidml16.baul.pets.abilities.flying_fish.MagmaticDiver;
import me.davidml16.baul.pets.abilities.flying_fish.QuickReel;
import me.davidml16.baul.pets.abilities.flying_fish.RapidDecay;
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

public class FlyingFishPet extends Pet {

    private static final String PET_ID = "flying_fish";
    private static final String PET_NAME = "Flying Fish";
    private static final String TEXTURE_URL = "https://textures.minecraft.net/texture/40cd71fbbbbb66c7baf7881f415c64fa84f6504958a57ccdb8589252647ea";

    public FlyingFishPet(Rarity rarity) {
        super(PET_ID + "_" + rarity.name().toLowerCase(),
                PET_NAME,
                PetType.FISHING,
                rarity,
                TEXTURE_URL);
    }

    @Override
    protected List<PetAbility> initializeAbilities() {
        List<PetAbility> abilities = new ArrayList<>();

        abilities.add(new AbilityBuilder()
                .name("Quick Reel")
                .description("Grants <aqua>+0.8 Fishing Speed</aqua>.")
                .implementation(new QuickReel())
                .build());

            abilities.add(new AbilityBuilder()
                    .name("Lava Bender")
                    .description("Gives <green>1</green> <red>Strength</red> and <defense>Defense</defense> when",
                            "near water or lava")
                    .implementation(new LavaBender())
                    .build());

        if (getRarity().ordinal() >= Rarity.LEGENDARY.ordinal()) {
            abilities.add(new AbilityBuilder()
                    .name("Magmatic Diver")
                    .description("Increases the stats of Magma Lord Armor by <green><stat>%</green>.")
                    .implementation(new MagmaticDiver())
                    .build());
        }

        if (getRarity().ordinal() >= Rarity.MYTHIC.ordinal()) {
            abilities.add(new AbilityBuilder()
                    .name("Rapid Decay")
                    .description("Increases the chance to activate the Flash Enchantment by <green><stat>%")
                    .implementation(new RapidDecay())
                    .build());
        }

        return abilities;
    }

    @Override
    public PetAttribute[] initializeAttributes() {
        List<PetAttribute> attributes = new ArrayList<>();

        attributes.add(new PetAttribute(0.5, 0.5, Stats.DEFENSE));
        attributes.add(new PetAttribute(0.5, 0.5, Stats.STRENGTH));

        return attributes.toArray(new PetAttribute[0]);
    }

    @Override
    protected PetUpgrade initializeUpgrade() {
        Map<Material, Integer> upgradeMaterials = new HashMap<>();

        return switch (getRarity()) {
            case RARE -> {
                upgradeMaterials.put(Material.TROPICAL_FISH, 64);
                upgradeMaterials.put(Material.FEATHER, 32);
                yield new PetUpgrade(Rarity.EPIC, 3000, 90 * 60 * 1000L, upgradeMaterials);
            }
            case EPIC -> {
                upgradeMaterials.put(Material.PHANTOM_MEMBRANE, 8);
                upgradeMaterials.put(Material.ELYTRA, 1);
                yield new PetUpgrade(Rarity.LEGENDARY, 6000, 4 * 60 * 60 * 1000L, upgradeMaterials);
            }
            case LEGENDARY -> {
                upgradeMaterials.put(Material.PRISMARINE_CRYSTALS, 16);
                upgradeMaterials.put(Material.MAGMA_CREAM, 1);
                yield new PetUpgrade(Rarity.MYTHIC, 9600, 2 * 60 * 60 * 1000L, upgradeMaterials);
            }
            default -> null;
        };
    }
}
