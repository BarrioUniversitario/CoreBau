package me.davidml16.baul.pets.pets.enchanting;

import me.davidml16.baul.pets.abilities.AbilityBuilder;
import me.davidml16.baul.pets.abilities.PetAbility;
import me.davidml16.baul.pets.abilities.guardian.EnchantingWisdom;
import me.davidml16.baul.pets.abilities.guardian.LaserBeam;
import me.davidml16.baul.pets.abilities.guardian.LuckySeven;
import me.davidml16.baul.pets.abilities.guardian.ManaPool;
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

public class GuardianPet extends Pet {

    private static final String PET_ID = "guardian";
    private static final String PET_NAME = "Guardian";
    private static final String TEXTURE_URL = "https://textures.minecraft.net/texture/13cf8db84807c471d7c6922302261ac1b5a179f96d1191156ecf3e1b1d3ca";

    public GuardianPet(Rarity rarity) {
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
                .name("Laserbeam")
                .description("Zaps your enemies for <aqua><stat>x</aqya>",
                        "your <aqua>Intelligence</aqua> every 3 seconds")
                .implementation(new LaserBeam()).build());

        if (getRarity().ordinal() >= Rarity.RARE.ordinal()) {
            abilities.add(new AbilityBuilder()
                    .name("Enchanting Wisdom Boost")
                    .description("Grants <aqua>+<stat> Enchanting Wisdom</aqua>")
                    .implementation(new EnchantingWisdom()).build());
        }

        if (getRarity().ordinal() >= Rarity.LEGENDARY.ordinal()) {
            abilities.add(new AbilityBuilder()
                    .name("Mana Pool")
                    .description("Regenerate <aqua><stat>%</aqua> extra mana",
                            "doubled when near or in water")
                    .implementation(new ManaPool()).build());
        }

        if (getRarity().ordinal() >= Rarity.MYTHIC.ordinal()) {
            abilities.add(new AbilityBuilder()
                    .name("Lycky Seven")
                    .description("Gain <aqua>+<stat>%</aqua> chance to find ultra",
                            "rare books in Superpairs")
                    .implementation(new LuckySeven()).build());
        }

        return abilities;
    }

    @Override
    public PetAttribute[] initializeAttributes() {
        List<PetAttribute> attributes = new ArrayList<>();

        attributes.add(new PetAttribute(0.5, 0.5, Stats.DEFENSE));
        attributes.add(new PetAttribute(1, 1, Stats.INTELLIGENCE));

        return attributes.toArray(new PetAttribute[0]);
    }

    @Override
    protected PetUpgrade initializeUpgrade() {
        Map<Material, Integer> upgradeMaterials = new HashMap<>();

        return switch (getRarity()) {
            case UNCOMMON -> {
                upgradeMaterials.put(Material.PRISMARINE_SHARD, 32);
                upgradeMaterials.put(Material.COD, 64);
                yield new PetUpgrade(Rarity.RARE, 1200, 40 * 60 * 1000L, upgradeMaterials);
            }
            case RARE -> {
                upgradeMaterials.put(Material.PRISMARINE_CRYSTALS, 16);
                upgradeMaterials.put(Material.GUARDIAN_SPAWN_EGG, 1);
                yield new PetUpgrade(Rarity.EPIC, 2400, 90 * 60 * 1000L, upgradeMaterials);
            }
            case EPIC -> {
                upgradeMaterials.put(Material.HEART_OF_THE_SEA, 1);
                upgradeMaterials.put(Material.TRIDENT, 1);
                yield new PetUpgrade(Rarity.LEGENDARY, 4800, 3 * 60 * 60 * 1000L, upgradeMaterials);
            }
            case LEGENDARY -> {
                upgradeMaterials.put(Material.PRISMARINE_CRYSTALS, 16);
                upgradeMaterials.put(Material.GUARDIAN_SPAWN_EGG, 1);
                yield new PetUpgrade(Rarity.MYTHIC, 9600, 6 * 60 * 60 * 1000L, upgradeMaterials);
            }
            default -> null;
        };
    }
}
