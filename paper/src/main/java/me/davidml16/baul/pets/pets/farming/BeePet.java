package me.davidml16.baul.pets.pets.farming;

import me.davidml16.baul.pets.abilities.AbilityBuilder;
import me.davidml16.baul.pets.abilities.PetAbility;
import me.davidml16.baul.pets.abilities.bee.BusyBuzz;
import me.davidml16.baul.pets.abilities.bee.Hive;
import me.davidml16.baul.pets.abilities.bee.WeaponizedHoney;
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

public class BeePet extends Pet {

    private static final String PET_ID = "bee";
    private static final String PET_NAME = "Bee";
    private static final String TEXTURE_URL = "https://textures.minecraft.net/texture/7e941987e825a24ea7baafab9819344b6c247c75c54a691987cd296bc163c263";

    public BeePet(Rarity rarity) {
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
                .name("Hive")
                .description("For each player within <green>25</green> blocks:",
                        "<aqua>+<stat> Intelligence",
                        "<red>+<stat> Strength",
                        "<green>+<stat> Defense",
                        "Max 15 players")
                .implementation(new Hive()).build());

        if (getRarity().ordinal() >= Rarity.RARE.ordinal()) {
            abilities.add(new AbilityBuilder()
                    .name("Busy Buzz Buzz")
                    .description("Grants <green>+<stat></green> of each to your pet",
                            "<gold>Farming Fortune",
                            "<gold>Foraging Fortune",
                            "<gold>Mining Fortune")
                    .implementation(new BusyBuzz()).build());
        }

        if (getRarity().ordinal() >= Rarity.LEGENDARY.ordinal()) {
            abilities.add(new AbilityBuilder()
                    .name("Weaponized Honey")
                    .description("Gain <green><stat>%</green> of received damage",
                            "as <gold>Absorption")
                    .implementation(new WeaponizedHoney()).build());
        }

        return abilities;
    }

    @Override
    public PetAttribute[] initializeAttributes() {
        List<PetAttribute> attributes = new ArrayList<>();

        attributes.add(new PetAttribute(0.1, 0.1, Stats.SPEED));
        attributes.add(new PetAttribute(5.25, 0.25, Stats.STRENGTH));
        attributes.add(new PetAttribute(0.5, 0.5, Stats.INTELLIGENCE));

        return attributes.toArray(new PetAttribute[0]);
    }

    @Override
    protected PetUpgrade initializeUpgrade() {
        Map<Material, Integer> upgradeMaterials = new HashMap<>();

        return switch (getRarity()) {
            case COMMON -> {
                upgradeMaterials.put(Material.HONEYCOMB, 16);
                upgradeMaterials.put(Material.WHEAT_SEEDS, 32);
                yield new PetUpgrade(Rarity.UNCOMMON, 500, 20 * 60 * 1000L, upgradeMaterials); // 20 min
            }
            case UNCOMMON -> {
                upgradeMaterials.put(Material.HONEY_BLOCK, 8);
                upgradeMaterials.put(Material.WHEAT, 64);
                yield new PetUpgrade(Rarity.RARE, 1000, 45 * 60 * 1000L, upgradeMaterials); // 45 min
            }
            case RARE -> {
                upgradeMaterials.put(Material.HONEY_BOTTLE, 32);
                upgradeMaterials.put(Material.GOLDEN_CARROT, 16);
                yield new PetUpgrade(Rarity.EPIC, 2500, 90 * 60 * 1000L, upgradeMaterials); // 90 min
            }
            case EPIC -> {
                upgradeMaterials.put(Material.ENCHANTED_GOLDEN_APPLE, 1);
                upgradeMaterials.put(Material.HONEY_BLOCK, 64);
                yield new PetUpgrade(Rarity.LEGENDARY, 5000, 3 * 60 * 60 * 1000L, upgradeMaterials); // 3 hours
            }
            default -> null;
        };
    }
}
