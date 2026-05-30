package me.davidml16.baul.pets.pets.combat;

import me.davidml16.baul.pets.abilities.AbilityBuilder;
import me.davidml16.baul.pets.abilities.PetAbility;
import me.davidml16.baul.pets.abilities.tiger.ApexPredator;
import me.davidml16.baul.pets.abilities.tiger.Hemorrhage;
import me.davidml16.baul.pets.abilities.tiger.MercilessSwipe;
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

public class TigerPet extends Pet {

    private static final String PET_ID = "tiger";
    private static final String PET_NAME = "Tiger";
    private static final String TEXTURE_URL = "https://textures.minecraft.net/texture/dc3dd984bb659849bd52994046964c22725f717e986b12d548fd169367d494";

    public TigerPet(Rarity rarity) {
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
                .name("Merciless Swipe")
                .description("Gain <red>+<stat>% Ferocity")
                .implementation(new MercilessSwipe()).build());

        if (getRarity().ordinal() >= Rarity.RARE.ordinal()) {
            abilities.add(new AbilityBuilder()
                    .name("Hemorrhage")
                    .description("Melee attacks reduced healing by <gold><stat>%",
                            "for <green>10s")
                    .implementation(new Hemorrhage()).build());
        }

        if (getRarity().ordinal() >= Rarity.LEGENDARY.ordinal()) {
            abilities.add(new AbilityBuilder()
                    .name("Apex Predator")
                    .description("Deal <red>+<stat>%<red> damage against targets with",
                            "no other mobs within <green>15</green> blocks")
                    .implementation(new ApexPredator()).build());
        }

        return abilities;
    }

    @Override
    public PetAttribute[] initializeAttributes() {
        List<PetAttribute> attributes = new ArrayList<>();

        attributes.add(new PetAttribute(5, 0.1, Stats.STRENGTH));
        attributes.add(new PetAttribute(0.05, 0.05, Stats.CRIT_CHANCE));
        attributes.add(new PetAttribute(0.5, 0.5, Stats.CRIT_DAMAGE));
        attributes.add(new PetAttribute(0.25, 0.25, Stats.FEROCITY));

        return attributes.toArray(new PetAttribute[0]);
    }

    @Override
    protected PetUpgrade initializeUpgrade() {
        Map<Material, Integer> upgradeMaterials = new HashMap<>();

        return switch (getRarity()) {
            case COMMON -> {
                upgradeMaterials.put(Material.JUNGLE_LOG, 64);
                upgradeMaterials.put(Material.COOKED_BEEF, 32);
                yield new PetUpgrade(Rarity.UNCOMMON, 800, 25 * 60 * 1000L, upgradeMaterials);
            }
            case UNCOMMON -> {
                upgradeMaterials.put(Material.ACACIA_LOG, 64);
                upgradeMaterials.put(Material.COOKED_PORKCHOP, 32);
                yield new PetUpgrade(Rarity.RARE, 1600, 50 * 60 * 1000L, upgradeMaterials);
            }
            case RARE -> {
                upgradeMaterials.put(Material.GOLDEN_APPLE, 8);
                upgradeMaterials.put(Material.JUNGLE_LEAVES, 128);
                yield new PetUpgrade(Rarity.EPIC, 3200, 100 * 60 * 1000L, upgradeMaterials);
            }
            case EPIC -> {
                upgradeMaterials.put(Material.ENCHANTED_GOLDEN_APPLE, 2);
                upgradeMaterials.put(Material.NETHERITE_INGOT, 1);
                yield new PetUpgrade(Rarity.LEGENDARY, 6400, 4 * 60 * 60 * 1000L, upgradeMaterials);
            }
            default -> null;
        };
    }
}
