package me.davidml16.baul.pets.pets.combat;

import me.davidml16.baul.pets.abilities.AbilityBuilder;
import me.davidml16.baul.pets.abilities.PetAbility;
import me.davidml16.baul.pets.abilities.skeleton.BoneArrows;
import me.davidml16.baul.pets.abilities.skeleton.Combo;
import me.davidml16.baul.pets.abilities.skeleton.SkeletalDefense;
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

public class SkeletonPet extends Pet {

    private static final String PET_ID = "skeleton";
    private static final String PET_NAME = "Skeleton";
    private static final String TEXTURE_URL = "https://textures.minecraft.net/texture/fca445749251bdd898fb83f667844e38a1dff79a1529f79a42447a0599310ea4";

    public SkeletonPet(Rarity rarity) {
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
                .name("Bone Arrows")
                .description("Increase arrow damage by <green><stat>%</green> which",
                        "is doubled while in dungeons")
                .implementation(new BoneArrows()).build());

        if (getRarity().ordinal() >= Rarity.RARE.ordinal()) {
            abilities.add(new AbilityBuilder()
                    .name("Combo")
                    .description("Gain a combo stack for every bow hit granting",
                            "<red>+3 Strength</red>. Max <green><stat></green> stacks.",
                            "stacks disappear after 8 seconds.")
                    .implementation(new Combo()).build());
        }

        if (getRarity().ordinal() >= Rarity.LEGENDARY.ordinal()) {
            abilities.add(new AbilityBuilder()
                    .name("Skeletal Defense")
                    .description("Your skeleton shoots an arrow dealing <green>30x",
                            "your <blue>Crit Damage</blue> when a mob gets close",
                            "to you. (5s cooldown)")
                    .implementation(new SkeletalDefense()).build());
        }

        return abilities;
    }

    @Override
    public PetAttribute[] initializeAttributes() {
        List<PetAttribute> attributes = new ArrayList<>();
        attributes.add(new PetAttribute(0.15, 0.15, Stats.CRIT_CHANCE));
        attributes.add(new PetAttribute(0.3, 0.3, Stats.CRIT_DAMAGE));
        return attributes.toArray(new PetAttribute[0]);
    }

    @Override
    protected PetUpgrade initializeUpgrade() {
        Map<Material, Integer> upgradeMaterials = new HashMap<>();
        return switch (getRarity()) {
            case COMMON -> {
                upgradeMaterials.put(Material.BONE, 64);
                upgradeMaterials.put(Material.ARROW, 128);
                yield new PetUpgrade(Rarity.UNCOMMON, 500, 20 * 60 * 1000L, upgradeMaterials);
            }
            case UNCOMMON -> {
                upgradeMaterials.put(Material.BOW, 1);
                upgradeMaterials.put(Material.BONE_BLOCK, 16);
                yield new PetUpgrade(Rarity.RARE, 1000, 40 * 60 * 1000L, upgradeMaterials);
            }
            case RARE -> {
                upgradeMaterials.put(Material.CROSSBOW, 1);
                upgradeMaterials.put(Material.BONE_MEAL, 128);
                yield new PetUpgrade(Rarity.EPIC, 2000, 90 * 60 * 1000L, upgradeMaterials);
            }
            case EPIC -> {
                upgradeMaterials.put(Material.WITHER_SKELETON_SKULL, 1);
                upgradeMaterials.put(Material.BONE_BLOCK, 64);
                yield new PetUpgrade(Rarity.LEGENDARY, 4000, 3 * 60 * 60 * 1000L, upgradeMaterials);
            }
            default -> null;
        };
    }
}
