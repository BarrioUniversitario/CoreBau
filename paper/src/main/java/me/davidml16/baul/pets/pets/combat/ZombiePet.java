package me.davidml16.baul.pets.pets.combat;

import me.davidml16.baul.pets.abilities.AbilityBuilder;
import me.davidml16.baul.pets.abilities.PetAbility;
import me.davidml16.baul.pets.abilities.zombie.BiteShield;
import me.davidml16.baul.pets.abilities.zombie.LivingDead;
import me.davidml16.baul.pets.abilities.zombie.RottenBlade;
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

public class ZombiePet extends Pet {

    private static final String PET_ID = "zombie";
    private static final String PET_NAME = "Zombie";
    private static final String TEXTURE_URL = "https://textures.minecraft.net/texture/56fc854bb84cf4b7697297973e02b79bc10698460b51a639c60e5e417734e11";

    public ZombiePet(Rarity rarity) {
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
                .name("Bite Shield")
                .description("Reduce the damage taken from zombies by",
                        "<green><stat>%")
                .implementation(new BiteShield()).build());

        if (getRarity().ordinal() >= Rarity.RARE.ordinal()) {
            abilities.add(new AbilityBuilder()
                    .name("Rotten Blade")
                    .description("Deal <green>+<stat>%</green> more damage against zombies")
                    .implementation(new RottenBlade()).build());
        }

        if (getRarity().ordinal() >= Rarity.LEGENDARY.ordinal()) {
            abilities.add(new AbilityBuilder()
                    .name("Living Dead")
                    .description("Increases the stats of all undead armor",
                            "by <green><stat>%</green>")
                    .implementation(new LivingDead()).build());
        }

        return abilities;
    }

    @Override
    public PetAttribute[] initializeAttributes() {
        List<PetAttribute> attributes = new ArrayList<>();

        attributes.add(new PetAttribute(1.0, 1.0, Stats.HEALTH));
        attributes.add(new PetAttribute(0.3, 0.3, Stats.CRIT_DAMAGE));

        return attributes.toArray(new PetAttribute[0]);
    }

    @Override
    protected PetUpgrade initializeUpgrade() {
        Map<Material, Integer> upgradeMaterials = new HashMap<>();

        return switch (getRarity()) {
            case COMMON -> {
                upgradeMaterials.put(Material.ROTTEN_FLESH, 64);
                upgradeMaterials.put(Material.BONE, 32);
                yield new PetUpgrade(Rarity.UNCOMMON, 400, 15 * 60 * 1000L, upgradeMaterials);
            }
            case UNCOMMON -> {
                upgradeMaterials.put(Material.LEATHER, 32);
                upgradeMaterials.put(Material.IRON_INGOT, 16);
                yield new PetUpgrade(Rarity.RARE, 800, 35 * 60 * 1000L, upgradeMaterials);
            }
            case RARE -> {
                upgradeMaterials.put(Material.GOLDEN_APPLE, 4);
                upgradeMaterials.put(Material.ZOMBIE_HEAD, 1);
                yield new PetUpgrade(Rarity.EPIC, 1600, 75 * 60 * 1000L, upgradeMaterials);
            }
            case EPIC -> {
                upgradeMaterials.put(Material.ENCHANTED_GOLDEN_APPLE, 1);
                upgradeMaterials.put(Material.NETHERITE_INGOT, 1);
                yield new PetUpgrade(Rarity.LEGENDARY, 3200, 2 * 60 * 60 * 1000L, upgradeMaterials);
            }
            default -> null;
        };
    }
}
