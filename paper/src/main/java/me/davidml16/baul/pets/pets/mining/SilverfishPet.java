package me.davidml16.baul.pets.pets.mining;

import me.davidml16.baul.pets.abilities.AbilityBuilder;
import me.davidml16.baul.pets.abilities.PetAbility;
import me.davidml16.baul.pets.abilities.silverfish.Dexterity;
import me.davidml16.baul.pets.abilities.silverfish.ExperiencedBurrower;
import me.davidml16.baul.pets.abilities.silverfish.Magnetic;
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

public class SilverfishPet extends Pet {

    private static final String PET_ID = "silverfish";
    private static final String PET_NAME = "Silverfish";
    private static final String TEXTURE_URL = "https://textures.minecraft.net/texture/da91dab8391af5fda54acd2c0b18fbd819b865e1a8f1d623813fa761e924540";

    public SilverfishPet(Rarity rarity) {
        super(PET_ID + "_" + rarity.name().toLowerCase(),
                PET_NAME,
                PetType.MINING,
                rarity,
                TEXTURE_URL);
    }

    @Override
    protected List<PetAbility> initializeAbilities() {
        List<PetAbility> abilities = new ArrayList<>();

        abilities.add(new AbilityBuilder()
                .name("Magnetic")
                .description("Earn <green>+<stat>%</green> more Exp when mining.")
                .implementation(new Magnetic())
                .build());

        if (getRarity().ordinal() >= Rarity.RARE.ordinal()) {
            abilities.add(new AbilityBuilder()
                    .name("Experienced Burrower")
                    .description("Grants <aqua>+<stat> Mining Wisdom</aqua>.")
                    .implementation(new ExperiencedBurrower())
                    .build());
        }

        if (getRarity().ordinal() >= Rarity.LEGENDARY.ordinal()) {
            abilities.add(new AbilityBuilder()
                    .name("Dexterity")
                    .description("Grants <gold>+<stat> Mining Speed</gold> and",
                            "permanent Haste.")
                    .implementation(new Dexterity())
                    .build());
        }

        return abilities;
    }

    @Override
    public PetAttribute[] initializeAttributes() {
        List<PetAttribute> attributes = new ArrayList<>();

        attributes.add(new PetAttribute(1, 1, Stats.DEFENSE));
        attributes.add(new PetAttribute(0.2, 0.2, Stats.MINING_FORTUNE));

        return attributes.toArray(new PetAttribute[0]);
    }

    @Override
    protected PetUpgrade initializeUpgrade() {
        Map<Material, Integer> upgradeMaterials = new HashMap<>();

        return switch (getRarity()) {
            case COMMON -> {
                upgradeMaterials.put(Material.STONE, 128);
                upgradeMaterials.put(Material.COBBLESTONE, 64);
                yield new PetUpgrade(Rarity.UNCOMMON, 300, 10 * 60 * 1000L, upgradeMaterials);
            }
            case UNCOMMON -> {
                upgradeMaterials.put(Material.IRON_ORE, 64);
                upgradeMaterials.put(Material.COAL, 128);
                yield new PetUpgrade(Rarity.RARE, 600, 25 * 60 * 1000L, upgradeMaterials);
            }
            case RARE -> {
                upgradeMaterials.put(Material.GOLD_ORE, 32);
                upgradeMaterials.put(Material.DIAMOND, 16);
                yield new PetUpgrade(Rarity.EPIC, 1200, 60 * 60 * 1000L, upgradeMaterials);
            }
            case EPIC -> {
                upgradeMaterials.put(Material.NETHERITE_INGOT, 2);
                upgradeMaterials.put(Material.DIAMOND_BLOCK, 4);
                yield new PetUpgrade(Rarity.LEGENDARY, 2400, 2 * 60 * 60 * 1000L, upgradeMaterials);
            }
            default -> null;
        };
    }
}
