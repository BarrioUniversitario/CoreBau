package me.davidml16.baul.pets.pets.foraging;

import me.davidml16.baul.pets.abilities.AbilityBuilder;
import me.davidml16.baul.pets.abilities.PetAbility;
import me.davidml16.baul.pets.abilities.giraffe.GoodHeart;
import me.davidml16.baul.pets.abilities.giraffe.HigherGround;
import me.davidml16.baul.pets.abilities.giraffe.LongNeck;
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

public class GiraffePet extends Pet {

    private static final String PET_ID = "giraffe";
    private static final String PET_NAME = "Giraffe";
    private static final String TEXTURE_URL = "https://textures.minecraft.net/texture/221025434045bda7025b3e514b316a4b770c6faa4ba9adb4be3809526db77f9d";

    public GiraffePet(Rarity rarity) {
        super(PET_ID + "_" + rarity.name().toLowerCase(),
                PET_NAME,
                PetType.FORAGING,
                rarity,
                TEXTURE_URL);
    }

    @Override
    protected List<PetAbility> initializeAbilities() {
        List<PetAbility> abilities = new ArrayList<>();

        abilities.add(new AbilityBuilder()
                .name("Good Heart")
                .description("Grants <red>+<stat> Health Regen")
                .implementation(new GoodHeart())
                .build());

        if (getRarity().ordinal() >= Rarity.RARE.ordinal()) {
            abilities.add(new AbilityBuilder()
                    .name("Higher Ground")
                    .description("Increases your <blue>Crit Damage</blue> and <red>Strength</red>",
                            "by <red>0.0015%</red> for every <yellow>0.1",
                            "<yellow>Swing Range</yellow> over 3 (up to 6)")
                    .implementation(new HigherGround())
                    .build());
        }

        if (getRarity().ordinal() >= Rarity.LEGENDARY.ordinal()) {
            abilities.add(new AbilityBuilder()
                    .name("Long Neck")
                    .description("Increases your melee damage by <red><stat>%",
                            "if you are more than 3 blocks away from",
                            "the target")
                    .implementation(new LongNeck())
                    .build());
        }

        return abilities;
    }

    @Override
    public PetAttribute[] initializeAttributes() {
        List<PetAttribute> attributes = new ArrayList<>();

        attributes.add(new PetAttribute(0.01, 0.01, Stats.SWING_RANGE));
        attributes.add(new PetAttribute(1, 1, Stats.HEALTH));
        attributes.add(new PetAttribute(0.05, 0.05, Stats.CRIT_CHANCE));

        return attributes.toArray(new PetAttribute[0]);
    }

    @Override
    protected PetUpgrade initializeUpgrade() {
        Map<Material, Integer> upgradeMaterials = new HashMap<>();

        return switch (getRarity()) {
            case COMMON -> {
                upgradeMaterials.put(Material.OAK_LOG, 64);
                upgradeMaterials.put(Material.APPLE, 32);
                yield new PetUpgrade(Rarity.UNCOMMON, 600, 20 * 60 * 1000L, upgradeMaterials);
            }
            case UNCOMMON -> {
                upgradeMaterials.put(Material.ACACIA_LOG, 64);
                upgradeMaterials.put(Material.GOLDEN_APPLE, 4);
                yield new PetUpgrade(Rarity.RARE, 1200, 45 * 60 * 1000L, upgradeMaterials);
            }
            case RARE -> {
                upgradeMaterials.put(Material.JUNGLE_LOG, 128);
                upgradeMaterials.put(Material.ENCHANTED_GOLDEN_APPLE, 1);
                yield new PetUpgrade(Rarity.EPIC, 2400, 90 * 60 * 1000L, upgradeMaterials);
            }
            case EPIC -> {
                upgradeMaterials.put(Material.DARK_OAK_LOG, 128);
                upgradeMaterials.put(Material.TOTEM_OF_UNDYING, 1);
                yield new PetUpgrade(Rarity.LEGENDARY, 4800, 3 * 60 * 60 * 1000L, upgradeMaterials);
            }
            default -> null;
        };
    }
}
