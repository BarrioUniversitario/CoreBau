package me.davidml16.baul.pets.pets.fishing;

import me.davidml16.baul.pets.abilities.AbilityBuilder;
import me.davidml16.baul.pets.abilities.PetAbility;
import me.davidml16.baul.pets.abilities.blue_whale.Archimedes;
import me.davidml16.baul.pets.abilities.blue_whale.Bulk;
import me.davidml16.baul.pets.abilities.blue_whale.Ingest;
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

public class BlueWhalePet extends Pet {

    private static final String PET_ID = "blue_whale";
    private static final String PET_NAME = "Blue Whale";
    private static final String TEXTURE_URL = "https://textures.minecraft.net/texture/dab779bbccc849f88273d844e8ca2f3a67a1699cb216c0a11b44326ce2cc20";

    public BlueWhalePet(Rarity rarity) {
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
                .name("Ingest")
                .description("All potions heal <red>+<stat></red>.")
                .implementation(new Ingest())
                .build());

        if (getRarity().ordinal() >= Rarity.RARE.ordinal()) {
            abilities.add(new AbilityBuilder()
                    .name("Bulk")
                    .description("Gain <green>+<stat> Defense</green> per <red>20 Max â¤ Health.")
                    .implementation(new Bulk())
                    .build());
        }

        if (getRarity().ordinal() >= Rarity.LEGENDARY.ordinal()) {
            abilities.add(new AbilityBuilder()
                    .name("Archimedes")
                    .description("Gain <red>+<stat>% Max Health.")
                    .implementation(new Archimedes())
                    .build());
        }

        return abilities;
    }

    @Override
    public PetAttribute[] initializeAttributes() {
        List<PetAttribute> attributes = new ArrayList<>();

        attributes.add(new PetAttribute(2, 2, Stats.HEALTH));

        return attributes.toArray(new PetAttribute[0]);
    }

    @Override
    protected PetUpgrade initializeUpgrade() {
        Map<Material, Integer> upgradeMaterials = new HashMap<>();

        return switch (getRarity()) {
            case RARE -> {
                upgradeMaterials.put(Material.PRISMARINE_CRYSTALS, 32);
                upgradeMaterials.put(Material.HEART_OF_THE_SEA, 1);
                yield new PetUpgrade(Rarity.EPIC, 5000, 3 * 60 * 60 * 1000L, upgradeMaterials);
            }
            case EPIC -> {
                upgradeMaterials.put(Material.NAUTILUS_SHELL, 16);
                upgradeMaterials.put(Material.CONDUIT, 1);
                yield new PetUpgrade(Rarity.LEGENDARY, 10000, 6 * 60 * 60 * 1000L, upgradeMaterials);
            }
            default -> null;
        };
    }
}