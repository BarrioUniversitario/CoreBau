package me.davidml16.baul.pets.pets.alchemy;

import me.davidml16.baul.pets.abilities.AbilityBuilder;
import me.davidml16.baul.pets.abilities.PetAbility;
import me.davidml16.baul.pets.abilities.sheep.DungeonWizard;
import me.davidml16.baul.pets.abilities.sheep.ManaSaver;
import me.davidml16.baul.pets.abilities.sheep.Overheal;
import me.davidml16.baul.pets.pet.Pet;
import me.davidml16.baul.pets.pet.PetAttribute;
import me.davidml16.baul.pets.pet.PetUpgrade;
import me.davidml16.baul.pets.utils.enums.PetType;
import me.davidml16.baul.pets.utils.enums.Rarity;
import me.davidml16.baul.pets.utils.enums.Stats;
import org.bukkit.Material;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SheepPet extends Pet {

    private static final String PET_ID = "sheep";
    private static final String PET_NAME = "Sheep";
    private static final String TEXTURE_URL = "https://textures.minecraft.net/texture/f31f9ccc6b3e32ecf13b8a11ac29cd33d18c95fc73db8a66c5d657ccb8be70";

    public SheepPet(Rarity rarity) {
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
                .name("Mana saver")
                .description(Collections.singletonList("Reduces the mana cost of abilities by <green><stat>%"))
                .implementation(new ManaSaver())
                .build());

        if (getRarity().ordinal() >= Rarity.RARE.ordinal()) {
            abilities.add(new AbilityBuilder()
                    .name("Overheal")
                    .description(List.of("Gives a <green><stats>% shield",
                            "after not taking damage ",
                            "for 10 seconds"))
                    .implementation(new Overheal())
                    .build());
        }
        if (getRarity().ordinal() >= Rarity.LEGENDARY.ordinal()) {
            abilities.add(new AbilityBuilder()
                    .name("Dungeon Wizard")
                    .description(List.of("Increase your total mana by <green><stat>% while ",
                            "in dungeons"))
                    .implementation(new DungeonWizard())
                    .build());
        }
        return abilities;
    }

    @Override
    public PetAttribute[] initializeAttributes() {
        List<PetAttribute> attributes = new ArrayList<>();

        attributes.add(new PetAttribute(1, 1, Stats.INTELLIGENCE));
        attributes.add(new PetAttribute(0.2, 0.2, Stats.ABILITY_DAMAGE));
        return attributes.toArray(new PetAttribute[0]);
    }

    @Override
    protected PetUpgrade initializeUpgrade() {
        Map<Material, Integer> upgradeMaterials = new HashMap<>();

        return switch (getRarity()) {
            case COMMON -> {
                upgradeMaterials.put(Material.WHITE_WOOL, 64);
                upgradeMaterials.put(Material.WHEAT, 32);
                yield new PetUpgrade(Rarity.UNCOMMON, 400, 5 * 60 * 1000L, upgradeMaterials);
            }
            case UNCOMMON -> {
                upgradeMaterials.put(Material.LIGHT_GRAY_WOOL, 32);
                upgradeMaterials.put(Material.HAY_BLOCK, 16);
                yield new PetUpgrade(Rarity.RARE, 800, 10 * 60 * 1000L, upgradeMaterials);
            }
            case RARE -> {
                upgradeMaterials.put(Material.GRAY_WOOL, 64);
                upgradeMaterials.put(Material.GOLDEN_APPLE, 4);
                yield new PetUpgrade(Rarity.EPIC, 1600, 30 * 60 * 1000L, upgradeMaterials);
            }
            case EPIC -> {
                upgradeMaterials.put(Material.BLACK_WOOL, 64);
                upgradeMaterials.put(Material.ENCHANTED_GOLDEN_APPLE, 1);
                yield new PetUpgrade(Rarity.LEGENDARY, 3200, 60 * 60 * 1000L, upgradeMaterials);
            }
            default -> null;
        };
    }
}
