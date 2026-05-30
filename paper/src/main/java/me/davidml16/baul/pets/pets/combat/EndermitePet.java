package me.davidml16.baul.pets.pets.combat;

import me.davidml16.baul.pets.abilities.AbilityBuilder;
import me.davidml16.baul.pets.abilities.PetAbility;
import me.davidml16.baul.pets.abilities.endermite.DailyCommuter;
import me.davidml16.baul.pets.abilities.endermite.MiteBait;
import me.davidml16.baul.pets.abilities.endermite.MoreStonks;
import me.davidml16.baul.pets.abilities.endermite.Sacrificer;
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

public class EndermitePet extends Pet {

    private static final String PET_ID = "endermite";
    private static final String PET_NAME = "Endermite";
    private static final String TEXTURE_URL = "https://textures.minecraft.net/texture/5a1a0831aa03afb4212adcbb24e5dfaa7f476a1173fce259ef75a85855";

    public EndermitePet(Rarity rarity) {
        super(PET_ID + "_" + rarity.name().toLowerCase(),
                PET_NAME,
                PetType.MINING,
                rarity,
                TEXTURE_URL);
    }

    @Override
    protected List<PetAbility> initializeAbilities() {
        List<PetAbility> abilities = new ArrayList<>();

        abilities.add(new AbilityBuilder()
                .name("More Stonks")
                .description(List.of("Extra End Stone drop chance: <green>+<stat>%</green>"))
                .implementation(new MoreStonks())
                .build());

        if (getRarity().ordinal() >= Rarity.RARE.ordinal()) {
            abilities.add(new AbilityBuilder()
                    .name("Daily Commuter")
                    .description(List.of("Less mana cost for Transmission Abilities: <green><stat>%</green>"))
                    .implementation(new DailyCommuter())
                    .build());
        }

        if (getRarity().ordinal() >= Rarity.LEGENDARY.ordinal()) {
            abilities.add(new AbilityBuilder()
                    .name("Mite Bait")
                    .description(List.of("Bonus Nest Endermite chance per +1 Pet Luck: <green><stat>%</green>"))
                    .implementation(new MiteBait()) // ~3.0 at L100
                    .build());
        }

        if (getRarity().ordinal() >= Rarity.MYTHIC.ordinal()) {
            abilities.add(new AbilityBuilder()
                    .name("Sacrificer")
                    .description(List.of("Bonus Draconic Altar roll chance: <green>+<stat>%</green>"))
                    .implementation(new Sacrificer())
                    .build());
        }

        return abilities;
    }

    @Override
    public PetAttribute[] initializeAttributes() {
        List<PetAttribute> attributes = new ArrayList<>();
        attributes.add(new PetAttribute(1.5, 1.5, Stats.INTELLIGENCE));
        return attributes.toArray(new PetAttribute[0]);
    }

    @Override
    protected PetUpgrade initializeUpgrade() {
        Map<Material, Integer> upgradeMaterials = new HashMap<>();

        return switch (getRarity()) {
            case COMMON -> {
                upgradeMaterials.put(Material.END_STONE, 512);
                yield new PetUpgrade(Rarity.UNCOMMON, 5_000, 5 * 60 * 1000L, upgradeMaterials);
            }
            case UNCOMMON -> {
                upgradeMaterials.put(Material.END_STONE, 1280);
                yield new PetUpgrade(Rarity.RARE, 10_000, 10 * 60 * 1000L, upgradeMaterials);
            }
            case RARE -> {
                upgradeMaterials.put(Material.END_STONE, 2560);
                upgradeMaterials.put(Material.SLIME_BALL, 32); // proxy for Mite Gel
                yield new PetUpgrade(Rarity.EPIC, 25_000, 15 * 60 * 1000L, upgradeMaterials);
            }
            case EPIC -> {
                upgradeMaterials.put(Material.END_STONE, 5120);
                upgradeMaterials.put(Material.SLIME_BALL, 64); // proxy for Mite Gel
                yield new PetUpgrade(Rarity.LEGENDARY, 50_000, 30 * 60 * 1000L, upgradeMaterials);
            }
            case LEGENDARY -> {
                upgradeMaterials.put(Material.END_STONE, 10_240);
                upgradeMaterials.put(Material.SLIME_BLOCK, 1); // proxy for Mixed Mite Gel
                yield new PetUpgrade(Rarity.MYTHIC, 100_000, 60 * 60 * 1000L, upgradeMaterials);
            }
            default -> null;
        };
    }
}
