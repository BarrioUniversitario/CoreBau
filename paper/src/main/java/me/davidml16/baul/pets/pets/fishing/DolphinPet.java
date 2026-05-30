package me.davidml16.baul.pets.pets.fishing;

import me.davidml16.baul.pets.abilities.AbilityBuilder;
import me.davidml16.baul.pets.abilities.PetAbility;
import me.davidml16.baul.pets.abilities.dolphin.Echolocation;
import me.davidml16.baul.pets.abilities.dolphin.PodTactics;
import me.davidml16.baul.pets.abilities.dolphin.SplashSurprise;
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

public class DolphinPet extends Pet {

    private static final String PET_ID = "dolphin";
    private static final String PET_NAME = "Dolphin";
    private static final String TEXTURE_URL = "https://textures.minecraft.net/texture/8e9688b950d880b55b7aa2cfcd76e5a0fa94aac6d16f78e833f7443ea29fed3";

    public DolphinPet(Rarity rarity) {
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
                .name("Pod Tactics")
                .description("Grants <aqua>+<stat> Fishing Speed</aqua> for",
                        "each player within <green>30</green> blocks, up to",
                        "<green>5</green> players.")
                .implementation(new PodTactics())
                .build());

        if (getRarity().ordinal() >= Rarity.RARE.ordinal()) {
            abilities.add(new AbilityBuilder()
                    .name("Echolocation")
                    .description("Grants <aqua>+<stat> Sea Creature Chance</aqua>.")
                    .implementation(new Echolocation())
                    .build());
        }

        if (getRarity().ordinal() >= Rarity.LEGENDARY.ordinal()) {
            abilities.add(new AbilityBuilder()
                    .name("Splash Surprise")
                    .description("Stun sea creatures for 5s after fishing",
                            "them up")
                    .implementation(new SplashSurprise())
                    .build());
        }

        return abilities;
    }

    @Override
    public PetAttribute[] initializeAttributes() {
        List<PetAttribute> attributes = new ArrayList<>();

        attributes.add(new PetAttribute(1, 1, Stats.INTELLIGENCE));

        attributes.add(new PetAttribute(0.05, 0.05, Stats.SEA_CREATURE_CHANCE));

        return attributes.toArray(new PetAttribute[0]);
    }

    @Override
    protected PetUpgrade initializeUpgrade() {
        Map<Material, Integer> upgradeMaterials = new HashMap<>();

        return switch (getRarity()) {
            case COMMON -> {
                upgradeMaterials.put(Material.COD, 64);
                upgradeMaterials.put(Material.KELP, 32);
                yield new PetUpgrade(Rarity.UNCOMMON, 600, 20 * 60 * 1000L, upgradeMaterials);
            }
            case UNCOMMON -> {
                upgradeMaterials.put(Material.SALMON, 32);
                upgradeMaterials.put(Material.PRISMARINE_SHARD, 16);
                yield new PetUpgrade(Rarity.RARE, 1200, 40 * 60 * 1000L, upgradeMaterials);
            }
            case RARE -> {
                upgradeMaterials.put(Material.TROPICAL_FISH, 64);
                upgradeMaterials.put(Material.PRISMARINE_CRYSTALS, 8);
                yield new PetUpgrade(Rarity.EPIC, 2400, 90 * 60 * 1000L, upgradeMaterials);
            }
            case EPIC -> {
                upgradeMaterials.put(Material.HEART_OF_THE_SEA, 1);
                upgradeMaterials.put(Material.NAUTILUS_SHELL, 8);
                yield new PetUpgrade(Rarity.LEGENDARY, 4800, 3 * 60 * 60 * 1000L, upgradeMaterials);
            }
            default -> null;
        };
    }
}
