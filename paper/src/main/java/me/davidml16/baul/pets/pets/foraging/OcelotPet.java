package me.davidml16.baul.pets.pets.foraging;

import me.davidml16.baul.pets.abilities.AbilityBuilder;
import me.davidml16.baul.pets.abilities.PetAbility;
import me.davidml16.baul.pets.abilities.ocelot.ForagingWisdomBoost;
import me.davidml16.baul.pets.abilities.ocelot.TreeEssence;
import me.davidml16.baul.pets.abilities.ocelot.TreeHugger;
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

public class OcelotPet extends Pet {

    private static final String PET_ID = "ocelot";
    private static final String PET_NAME = "Ocelot";
    private static final String TEXTURE_URL = "https://textures.minecraft.net/texture/5657cd5c2989ff97570fec4ddcdc6926a68a3393250c1be1f0b114a1db1";

    public OcelotPet(Rarity rarity) {
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
                .name("Foraging Wisdom Boost")
                .description("Grants <aqua>+<stat> Foraging Wisdom</aqua>.")
                .implementation(new ForagingWisdomBoost())
                .build());

        if (getRarity().ordinal() >= Rarity.RARE.ordinal()) {
            abilities.add(new AbilityBuilder()
                    .name("Tree Hugger")
                    .description("Foraging minions work <green><stat>%</green>faster",
                            "while on your island")
                    .implementation(new TreeHugger())
                    .build());
        }

        if (getRarity().ordinal() >= Rarity.EPIC.ordinal()) {
            abilities.add(new AbilityBuilder()
                    .name("Tree Essence")
                    .description("Gain a <green><stat>%</green> chance to get exp from",
                            "breaking a log")
                    .implementation(new TreeEssence())
                    .build());
        }

        return abilities;
    }

    @Override
    public PetAttribute[] initializeAttributes() {
        List<PetAttribute> attributes = new ArrayList<>();

        attributes.add(new PetAttribute(0.5, 0.5, Stats.SPEED));
        attributes.add(new PetAttribute(0.1, 0.1, Stats.FEROCITY));

        return attributes.toArray(new PetAttribute[0]);
    }

    @Override
    protected PetUpgrade initializeUpgrade() {
        Map<Material, Integer> upgradeMaterials = new HashMap<>();

        return switch (getRarity()) {
            case COMMON -> {
                upgradeMaterials.put(Material.JUNGLE_LOG, 32);
                upgradeMaterials.put(Material.TROPICAL_FISH, 16);
                yield new PetUpgrade(Rarity.UNCOMMON, 500, 20 * 60 * 1000L, upgradeMaterials);
            }
            case UNCOMMON -> {
                upgradeMaterials.put(Material.ACACIA_LOG, 64);
                upgradeMaterials.put(Material.COD, 32);
                yield new PetUpgrade(Rarity.RARE, 1000, 40 * 60 * 1000L, upgradeMaterials);
            }
            case RARE -> {
                upgradeMaterials.put(Material.DARK_OAK_LOG, 64);
                upgradeMaterials.put(Material.SALMON, 32);
                yield new PetUpgrade(Rarity.EPIC, 2000, 90 * 60 * 1000L, upgradeMaterials);
            }
            case EPIC -> {
                upgradeMaterials.put(Material.JUNGLE_LEAVES, 128);
                upgradeMaterials.put(Material.GOLDEN_APPLE, 4);
                yield new PetUpgrade(Rarity.LEGENDARY, 4000, 3 * 60 * 60 * 1000L, upgradeMaterials);
            }
            default -> null;
        };
    }
}
