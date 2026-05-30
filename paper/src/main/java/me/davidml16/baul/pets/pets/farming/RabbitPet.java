package me.davidml16.baul.pets.pets.farming;

import me.davidml16.baul.pets.abilities.AbilityBuilder;
import me.davidml16.baul.pets.abilities.PetAbility;
import me.davidml16.baul.pets.abilities.rabbit.EfficientFarming;
import me.davidml16.baul.pets.abilities.rabbit.FarmingWisdom;
import me.davidml16.baul.pets.abilities.rabbit.HappyFeet;
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

public class RabbitPet extends Pet {

    private static final String PET_ID = "rabbit";
    private static final String PET_NAME = "Rabbit";
    private static final String TEXTURE_URL = "https://textures.minecraft.net/texture/117bffc1972acd7f3b4a8f43b5b6c7534695b8fd62677e0306b2831574b";

    public RabbitPet(Rarity rarity) {
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
                .name("Happy Feet")
                .description("Jump potions also give <green>+<stat></green> speed")
                .implementation(new HappyFeet()).build());

        if (getRarity().ordinal() >= Rarity.RARE.ordinal()) {
            abilities.add(new AbilityBuilder()
                    .name("Farming Wisdom Boost")
                    .description("Grants <aqua>+<stat> Farming Wisdom</aqua>")
                    .implementation(new FarmingWisdom()).build());
        }

        if (getRarity().ordinal() >= Rarity.LEGENDARY.ordinal()) {
            abilities.add(new AbilityBuilder()
                    .name("Efficient Farming")
                    .description("Farming minions work <green><stat>%</green> faster",
                            "while on your island")
                    .implementation(new EfficientFarming()).build());
        }

        return abilities;
    }

    @Override
    public PetAttribute[] initializeAttributes() {
        List<PetAttribute> attributes = new ArrayList<>();

        attributes.add(new PetAttribute(1, 1, Stats.HEALTH));
        attributes.add(new PetAttribute(0.2, 0.2, Stats.SPEED));

        return attributes.toArray(new PetAttribute[0]);
    }

    @Override
    protected PetUpgrade initializeUpgrade() {
        Map<Material, Integer> upgradeMaterials = new HashMap<>();

        return switch (getRarity()) {
            case COMMON -> {
                upgradeMaterials.put(Material.CARROT, 64);
                upgradeMaterials.put(Material.WHEAT, 32);
                yield new PetUpgrade(Rarity.UNCOMMON, 500, 20 * 60 * 1000L, upgradeMaterials);
            }
            case UNCOMMON -> {
                upgradeMaterials.put(Material.GOLDEN_CARROT, 16);
                upgradeMaterials.put(Material.HAY_BLOCK, 8);
                yield new PetUpgrade(Rarity.RARE, 1000, 40 * 60 * 1000L, upgradeMaterials);
            }
            case RARE -> {
                upgradeMaterials.put(Material.ENCHANTED_GOLDEN_APPLE, 1);
                upgradeMaterials.put(Material.CAKE, 4);
                yield new PetUpgrade(Rarity.EPIC, 2000, 90 * 60 * 1000L, upgradeMaterials);
            }
            case EPIC -> {
                upgradeMaterials.put(Material.TOTEM_OF_UNDYING, 1);
                upgradeMaterials.put(Material.DIAMOND_BLOCK, 2);
                yield new PetUpgrade(Rarity.LEGENDARY, 4000, 3 * 60 * 60 * 1000L, upgradeMaterials);
            }
            default -> null;
        };
    }
}
