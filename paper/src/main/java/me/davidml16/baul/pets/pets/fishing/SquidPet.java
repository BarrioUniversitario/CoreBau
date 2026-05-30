package me.davidml16.baul.pets.pets.fishing;

import me.davidml16.baul.pets.abilities.AbilityBuilder;
import me.davidml16.baul.pets.abilities.PetAbility;
import me.davidml16.baul.pets.abilities.squid.FishingWisdom;
import me.davidml16.baul.pets.abilities.squid.InkSpecialty;
import me.davidml16.baul.pets.abilities.squid.MoreInk;
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

public class SquidPet extends Pet {

    private static final String PET_ID = "squid";
    private static final String PET_NAME = "Squid";
    private static final String TEXTURE_URL = "https://textures.minecraft.net/texture/01433be242366af126da434b8735df1eb5b3cb2cede39145974e9c483607bac";

    public SquidPet(Rarity rarity) {
        super(PET_ID + "_" + rarity.name().toLowerCase(),
                PET_NAME,
                PetType.FISHING,
                rarity,
                TEXTURE_URL);
    }

    @Override
    protected List<PetAbility> initializeAbilities() {
        List<PetAbility> abilities = new ArrayList<>();

        // More Ink - Available from Common rarity
        abilities.add(new AbilityBuilder()
                .name("More Ink")
                .description(List.of("Gain a 1% (+1% per level) chance to get double drops from squids"))
                .implementation(new MoreInk())
                .build());

        // Ink Specialty - Available from Rare rarity
        if (getRarity().ordinal() >= Rarity.RARE.ordinal()) {
            abilities.add(new AbilityBuilder()
                    .name("Ink Specialty")
                    .description(List.of("Buffs the Ink Wand by 0.4 (+0.4 per level) âDamage and 0.2 (+0.2 per level) Strength."))
                    .implementation(new InkSpecialty())
                    .build());
        }

        // Fishing Wisdom Boost - Available from Epic rarity
        if (getRarity().ordinal() >= Rarity.EPIC.ordinal()) {
            abilities.add(new AbilityBuilder()
                    .name("Fishing Wisdom Boost")
                    .description(List.of("Grants +0.3 â˜¯Fishing Wisdom (+0.3 per level)."))
                    .implementation(new FishingWisdom())
                    .build());
        }

        return abilities;
    }

    @Override
    public PetAttribute[] initializeAttributes() {
        List<PetAttribute> attributes = new ArrayList<>();

        attributes.add(new PetAttribute(0.5, 0.5, Stats.HEALTH));

        attributes.add(new PetAttribute(0.5, 0.5, Stats.INTELLIGENCE));

        return attributes.toArray(new PetAttribute[0]);
    }

    @Override
    protected PetUpgrade initializeUpgrade() {
        Map<Material, Integer> upgradeMaterials = new HashMap<>();

        return switch (getRarity()) {
            case COMMON -> {
                upgradeMaterials.put(Material.INK_SAC, 64);
                upgradeMaterials.put(Material.COD, 32);
                yield new PetUpgrade(Rarity.UNCOMMON, 500, 20 * 60 * 1000L, upgradeMaterials);
            }
            case UNCOMMON -> {
                upgradeMaterials.put(Material.GLOW_INK_SAC, 16);
                upgradeMaterials.put(Material.SALMON, 32);
                yield new PetUpgrade(Rarity.RARE, 1000, 40 * 60 * 1000L, upgradeMaterials);
            }
            case RARE -> {
                upgradeMaterials.put(Material.PRISMARINE_SHARD, 32);
                upgradeMaterials.put(Material.TROPICAL_FISH, 64);
                yield new PetUpgrade(Rarity.EPIC, 2000, 90 * 60 * 1000L, upgradeMaterials);
            }
            case EPIC -> {
                upgradeMaterials.put(Material.HEART_OF_THE_SEA, 1);
                upgradeMaterials.put(Material.GLOW_INK_SAC, 64);
                yield new PetUpgrade(Rarity.LEGENDARY, 4000, 3 * 60 * 60 * 1000L, upgradeMaterials);
            }
            default -> null;
        };
    }
}
