package me.davidml16.baul.pets.pets.combat;

import me.davidml16.baul.pets.abilities.AbilityBuilder;
import me.davidml16.baul.pets.abilities.PetAbility;
import me.davidml16.baul.pets.abilities.horse.Ridable;
import me.davidml16.baul.pets.abilities.horse.RideIntoBattle;
import me.davidml16.baul.pets.abilities.horse.Run;
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

public class HorsePet extends Pet {

    private static final String PET_ID = "horse";
    private static final String PET_NAME = "Horse";
    private static final String TEXTURE_URL = "https://textures.minecraft.net/texture/36fcd3ec3bc84bafb4123ea479471f9d2f42d8fb9c5f11cf5f4e0d93226";

    public HorsePet(Rarity rarity) {
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
                .name("Ridable")
                .description("Right-click to mount your pet")
                .implementation(new Ridable()).build());

        if (getRarity().ordinal() >= Rarity.RARE.ordinal()) {
            abilities.add(new AbilityBuilder()
                    .name("Run")
                    .description("Increases the speed of your mount by <green><stat>%")
                    .implementation(new Run()).build());
        }

        if (getRarity().ordinal() >= Rarity.LEGENDARY.ordinal()) {
            abilities.add(new AbilityBuilder()
                    .name("Ride Into Battle")
                    .description("While riding your horse, gain <green>+<stat>%</green>", "bow damage")
                    .implementation(new RideIntoBattle()).build());
        }

        return abilities;
    }

    @Override
    public PetAttribute[] initializeAttributes() {
        List<PetAttribute> attributes = new ArrayList<>();

        attributes.add(new PetAttribute(0.25, 0.25, Stats.SPEED));
        attributes.add(new PetAttribute(0.5, 0.5, Stats.INTELLIGENCE));
        return attributes.toArray(new PetAttribute[0]);
    }

    @Override
    protected PetUpgrade initializeUpgrade() {
        Map<Material, Integer> upgradeMaterials = new HashMap<>();
        return switch (getRarity()) {
            case COMMON -> {
                upgradeMaterials.put(Material.HAY_BLOCK, 16);
                upgradeMaterials.put(Material.APPLE, 32);
                yield new PetUpgrade(Rarity.UNCOMMON, 700, 25 * 60 * 1000L, upgradeMaterials);
            }
            case UNCOMMON -> {
                upgradeMaterials.put(Material.GOLDEN_APPLE, 4);
                upgradeMaterials.put(Material.SADDLE, 1);
                yield new PetUpgrade(Rarity.RARE, 1400, 50 * 60 * 1000L, upgradeMaterials);
            }
            case RARE -> {
                upgradeMaterials.put(Material.GOLDEN_CARROT, 32);
                upgradeMaterials.put(Material.DIAMOND_HORSE_ARMOR, 1);
                yield new PetUpgrade(Rarity.EPIC, 2800, 100 * 60 * 1000L, upgradeMaterials);
            }
            case EPIC -> {
                upgradeMaterials.put(Material.ENCHANTED_GOLDEN_APPLE, 2);
                upgradeMaterials.put(Material.NETHERITE_INGOT, 1);
                yield new PetUpgrade(Rarity.LEGENDARY, 5600, 4 * 60 * 60 * 1000L, upgradeMaterials);
            }
            default -> null;
        };
    }
}
