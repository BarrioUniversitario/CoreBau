package me.davidml16.baul.pets.pets.foraging;

import me.davidml16.baul.pets.abilities.AbilityBuilder;
import me.davidml16.baul.pets.abilities.PetAbility;
import me.davidml16.baul.pets.abilities.monkey.EvolvedAxes;
import me.davidml16.baul.pets.abilities.monkey.Treeborn;
import me.davidml16.baul.pets.abilities.monkey.VineSwing;
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

public class MonkeyPet extends Pet {

    private static final String PET_ID = "monkey";
    private static final String PET_NAME = "Monkey";
    private static final String TEXTURE_URL = "https://textures.minecraft.net/texture/13cf8db84807c471d7b1dfacb5fc24b4a215d3e6c3f54b9e47b1a1c9b7c7c7";

    public MonkeyPet(Rarity rarity) {
        super(PET_ID + "_" + rarity.name().toLowerCase(),
                PET_NAME,
                PetType.FORAGING,
                rarity,
                TEXTURE_URL);
    }

    @Override
    protected List<PetAbility> initializeAbilities() {
        List<PetAbility> abilities = new ArrayList<>();

        // Treeborn - Available from Common rarity
        abilities.add(new AbilityBuilder()
                .name("Treeborn")
                .description("Grants <green>+<stat></green> <gold>Foraging Fortune,",
                        "which increases your chance at double logs.")
                .implementation(new Treeborn())
                .build());

        if (getRarity().ordinal() >= Rarity.RARE.ordinal()) {
            abilities.add(new AbilityBuilder()
                    .name("Vine Swing")
                    .description("Gain <white>+</white><green><stat></green> <white>Speed</white> while in The Park")
                    .implementation(new VineSwing())
                    .build());
        }

        if (getRarity().ordinal() >= Rarity.LEGENDARY.ordinal()) {
            abilities.add(new AbilityBuilder()
                    .name("Evolved Axes")
                    .description("Reduce the cooldown of Jungle Axe",
                            "and Treecapitator by <green><stat></green>.")
                    .implementation(new EvolvedAxes())
                    .build());
        }

        return abilities;
    }

    @Override
    public PetAttribute[] initializeAttributes() {
        List<PetAttribute> attributes = new ArrayList<>();

        attributes.add(new PetAttribute(0.2, 0.2, Stats.SPEED));

        attributes.add(new PetAttribute(0.5, 0.5, Stats.INTELLIGENCE));

        return attributes.toArray(new PetAttribute[0]);
    }

    @Override
    protected PetUpgrade initializeUpgrade() {
        Map<Material, Integer> upgradeMaterials = new HashMap<>();

        return switch (getRarity()) {
            case COMMON -> {
                upgradeMaterials.put(Material.JUNGLE_LOG, 32);
                upgradeMaterials.put(Material.APPLE, 16);
                yield new PetUpgrade(Rarity.UNCOMMON, 600, 20 * 60 * 1000L, upgradeMaterials);
            }
            case UNCOMMON -> {
                upgradeMaterials.put(Material.ACACIA_LOG, 64);
                upgradeMaterials.put(Material.GOLDEN_APPLE, 2);
                yield new PetUpgrade(Rarity.RARE, 1200, 45 * 60 * 1000L, upgradeMaterials);
            }
            case RARE -> {
                upgradeMaterials.put(Material.DARK_OAK_LOG, 64);
                upgradeMaterials.put(Material.ENCHANTED_GOLDEN_APPLE, 1);
                yield new PetUpgrade(Rarity.EPIC, 2400, 90 * 60 * 1000L, upgradeMaterials);
            }
            case EPIC -> {
                upgradeMaterials.put(Material.JUNGLE_LEAVES, 256);
                upgradeMaterials.put(Material.TOTEM_OF_UNDYING, 1);
                yield new PetUpgrade(Rarity.LEGENDARY, 4800, 3 * 60 * 60 * 1000L, upgradeMaterials);
            }
            default -> null;
        };
    }
}
