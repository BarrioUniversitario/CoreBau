package me.davidml16.baul.pets.pets.mining;

import me.davidml16.baul.pets.abilities.AbilityBuilder;
import me.davidml16.baul.pets.abilities.PetAbility;
import me.davidml16.baul.pets.abilities.wither_skeleton.DeathsTouch;
import me.davidml16.baul.pets.abilities.wither_skeleton.StrongerBones;
import me.davidml16.baul.pets.abilities.wither_skeleton.WitherBlood;
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

public class WitherSkeletonPet extends Pet {

    private static final String PET_ID = "wither_skeleton";
    private static final String PET_NAME = "Wither Skeleton";
    private static final String TEXTURE_URL = "https://textures.minecraft.net/texture/7953b6c68448e7e6b6bf8fb273d7203acd8e1be19e81481ead51f45de59a8";

    public WitherSkeletonPet(Rarity rarity) {
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
                .name("Stronger Bones")
                .description("Take <green><stat>%</green> less damage from skeletons")
                .implementation(new StrongerBones())
                .build());

        if (getRarity().ordinal() >= Rarity.RARE.ordinal()) {
            abilities.add(new AbilityBuilder()
                    .name("Wither Blood")
                    .description("Deal <green><stat>%<green> more damage to wither mobs")
                    .implementation(new WitherBlood())
                    .build());
        }

        if (getRarity().ordinal() >= Rarity.LEGENDARY.ordinal()) {
            abilities.add(new AbilityBuilder()
                    .name("Death's Touch")
                    .description("Upon hitting an enemy inflict the wither",
                            "effect for <green><stat>%</green> damage over",
                            "3 seconds. Does not stack")
                    .implementation(new DeathsTouch()).build());
        }

        return abilities;
    }

    @Override
    public PetAttribute[] initializeAttributes() {
        List<PetAttribute> attributes = new ArrayList<>();

        attributes.add(new PetAttribute(0.25, 0.25, Stats.DEFENSE));
        attributes.add(new PetAttribute(0.25, 0.25, Stats.STRENGTH));
        attributes.add(new PetAttribute(0.25, 0.25, Stats.INTELLIGENCE));
        attributes.add(new PetAttribute(0.05, 0.05, Stats.CRIT_CHANCE));
        attributes.add(new PetAttribute(0.25, 0.25, Stats.CRIT_DAMAGE));

        return attributes.toArray(new PetAttribute[0]);
    }

    @Override
    protected PetUpgrade initializeUpgrade() {
        Map<Material, Integer> upgradeMaterials = new HashMap<>();

        return switch (getRarity()) {
            case EPIC -> {
                upgradeMaterials.put(Material.WITHER_SKELETON_SKULL, 3);
                upgradeMaterials.put(Material.SOUL_SAND, 64);
                yield new PetUpgrade(Rarity.LEGENDARY, 25000, 10 * 60 * 60 * 1000L, upgradeMaterials);
            }
            default -> null;
        };
    }
}
