package me.davidml16.baul.pets.pets.combat;

import me.davidml16.baul.pets.abilities.AbilityBuilder;
import me.davidml16.baul.pets.abilities.PetAbility;
import me.davidml16.baul.pets.abilities.wolf.AlphaDog;
import me.davidml16.baul.pets.abilities.wolf.CombatWisdom;
import me.davidml16.baul.pets.abilities.wolf.PackLeader;
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

public class WolfPet extends Pet {

    private static final String PET_ID = "wolf";
    private static final String PET_NAME = "Wolf";
    private static final String TEXTURE_URL = "https://textures.minecraft.net/texture/dc3dd984bb659849bd52996ccb95569ba5de87b4b5b2a48b67a1fc28cfb8dd";

    public WolfPet(Rarity rarity) {
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
                .name("Alpha Dog")
                .description("Take <green><stat>%</green> less damage from wolves")
                .implementation(new AlphaDog()).build());

        abilities.add(new AbilityBuilder()
                .name("Pack Leader")
                .description("Gain <blue>+<stat>% Crit Damage</blue> for every nearby",
                        "wolf (Max 10 wolves)")
                .implementation(new PackLeader()).build());

        if (getRarity().ordinal() >= Rarity.LEGENDARY.ordinal()) {
            abilities.add(new AbilityBuilder()
                    .name("Combat Wisdom Boost")
                    .description("Grants <aqua>+<stat> Combat Wisdom</aqua>")
                    .implementation(new CombatWisdom()).build());
        }

        return abilities;
    }

    @Override
    public PetAttribute[] initializeAttributes() {
        List<PetAttribute> attributes = new ArrayList<>();

        attributes.add(new PetAttribute(0.5, 0.5, Stats.HEALTH));
        attributes.add(new PetAttribute(0.2, 0.2, Stats.SPEED));
        attributes.add(new PetAttribute(0.1, 0.1, Stats.CRIT_DAMAGE));

        return attributes.toArray(new PetAttribute[0]);
    }

    @Override
    protected PetUpgrade initializeUpgrade() {
        Map<Material, Integer> upgradeMaterials = new HashMap<>();

        return switch (getRarity()) {
            case COMMON -> {
                upgradeMaterials.put(Material.BONE, 64);
                upgradeMaterials.put(Material.COOKED_BEEF, 32);
                yield new PetUpgrade(Rarity.UNCOMMON, 600, 20 * 60 * 1000L, upgradeMaterials);
            }
            case UNCOMMON -> {
                upgradeMaterials.put(Material.BONE_BLOCK, 16);
                upgradeMaterials.put(Material.COOKED_PORKCHOP, 32);
                yield new PetUpgrade(Rarity.RARE, 1200, 45 * 60 * 1000L, upgradeMaterials);
            }
            case RARE -> {
                upgradeMaterials.put(Material.GOLDEN_APPLE, 8);
                upgradeMaterials.put(Material.BONE_MEAL, 64);
                yield new PetUpgrade(Rarity.EPIC, 2400, 90 * 60 * 1000L, upgradeMaterials);
            }
            case EPIC -> {
                upgradeMaterials.put(Material.ENCHANTED_GOLDEN_APPLE, 2);
                upgradeMaterials.put(Material.NETHERITE_INGOT, 1);
                yield new PetUpgrade(Rarity.LEGENDARY, 4800, 3 * 60 * 60 * 1000L, upgradeMaterials);
            }
            default -> null;
        };
    }
}
