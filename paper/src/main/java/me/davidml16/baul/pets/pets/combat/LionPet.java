package me.davidml16.baul.pets.pets.combat;

import me.davidml16.baul.pets.abilities.AbilityBuilder;
import me.davidml16.baul.pets.abilities.PetAbility;
import me.davidml16.baul.pets.abilities.lion.FirstPounce;
import me.davidml16.baul.pets.abilities.lion.KingOfTheJungle;
import me.davidml16.baul.pets.abilities.lion.PrimalForce;
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

public class LionPet extends Pet {

    private static final String PET_ID = "lion";
    private static final String PET_NAME = "Lion";
    private static final String TEXTURE_URL = "https://textures.minecraft.net/texture/38ff473bd52b4db2c06f1ac87fe1367bce7574fac330ffac7956229f82efba1";

    public LionPet(Rarity rarity) {
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
                .name("Primal Force")
                .description("Adds <red>+<stat> Damage</red> and <red>+<stat> Strength</red>",
                        "to your weapons")
                .implementation(new PrimalForce()).build());

        if (getRarity().ordinal() >= Rarity.RARE.ordinal()) {
            abilities.add(new AbilityBuilder()
                    .name("First Pounce")
                    .description("First Strike, Triple-Strike, and Combo are <green><stat>%",
                            "more effective.")
                    .implementation(new FirstPounce()).build());
        }

        if (getRarity().ordinal() >= Rarity.LEGENDARY.ordinal()) {
            abilities.add(new AbilityBuilder()
                    .name("King of the Jungle")
                    .description("Deal <red>+<stat>% Damage</red> against mobs",
                            "that have attacked you.")
                    .implementation(new KingOfTheJungle()).build());
        }

        return abilities;
    }

    @Override
    public PetAttribute[] initializeAttributes() {
        List<PetAttribute> attributes = new ArrayList<>();

        attributes.add(new PetAttribute(0.25, 0.25, Stats.SPEED));
        attributes.add(new PetAttribute(0.5, 0.5, Stats.STRENGTH));
        attributes.add(new PetAttribute(0.05, 0.05, Stats.FEROCITY));

        return attributes.toArray(new PetAttribute[0]);
    }

    @Override
    protected PetUpgrade initializeUpgrade() {
        Map<Material, Integer> upgradeMaterials = new HashMap<>();

        return switch (getRarity()) {
            case COMMON -> {
                upgradeMaterials.put(Material.JUNGLE_LOG, 64);
                upgradeMaterials.put(Material.BONE, 32);
                yield new PetUpgrade(Rarity.UNCOMMON, 800, 25 * 60 * 1000L, upgradeMaterials);
            }
            case UNCOMMON -> {
                upgradeMaterials.put(Material.ACACIA_LOG, 64);
                upgradeMaterials.put(Material.COOKED_BEEF, 32);
                yield new PetUpgrade(Rarity.RARE, 1600, 50 * 60 * 1000L, upgradeMaterials);
            }
            case RARE -> {
                upgradeMaterials.put(Material.GOLDEN_APPLE, 8);
                upgradeMaterials.put(Material.JUNGLE_LEAVES, 128);
                yield new PetUpgrade(Rarity.EPIC, 3200, 100 * 60 * 1000L, upgradeMaterials);
            }
            case EPIC -> {
                upgradeMaterials.put(Material.ENCHANTED_GOLDEN_APPLE, 2);
                upgradeMaterials.put(Material.TOTEM_OF_UNDYING, 1);
                yield new PetUpgrade(Rarity.LEGENDARY, 6400, 4 * 60 * 60 * 1000L, upgradeMaterials);
            }
            default -> null;
        };
    }
}
