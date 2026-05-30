package me.davidml16.baul.pets.pets.combat;

import me.davidml16.baul.pets.abilities.AbilityBuilder;
import me.davidml16.baul.pets.abilities.PetAbility;
import me.davidml16.baul.pets.abilities.spider.WebBattlefield;
import me.davidml16.baul.pets.abilities.tarantula.ArachnidSlayer;
import me.davidml16.baul.pets.abilities.tarantula.EightLegs;
import me.davidml16.baul.pets.abilities.tarantula.WebbedCells;
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

public class TarantulaPet extends Pet {

    private static final String PET_ID = "tarantula";
    private static final String PET_NAME = "Tarantula";
    private static final String TEXTURE_URL = "https://textures.minecraft.net/texture/8300986ed0a04ea79904f6ae53f49ed3a0ff5b1df62bba622ecbd3777f156df8";

    public TarantulaPet(Rarity rarity) {
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
                .name("Webbed Cells")
                .description("Anti-healing is <green><stat>%</green> less effective against you")
                .implementation(new WebbedCells()).build());

        abilities.add(new AbilityBuilder()
                .name("Eight Legs")
                .description("Decrease the mana cost of Spider, Tarantula and Spirit",
                        "boots by <green><stat>%</green>")
                .implementation(new EightLegs()).build());

        if (getRarity().ordinal() >= Rarity.LEGENDARY.ordinal()) {
            abilities.add(new AbilityBuilder()
                    .name("Arachnid Slayer")
                    .description("Gain <aqua><stat>x</aqua> Combat XP against Spiders")
                    .implementation(new ArachnidSlayer()).build());
        }
        if (getRarity().ordinal() >= Rarity.MYTHIC.ordinal()) {
            abilities.add(new AbilityBuilder()
                    .name("Web Battlefield")
                    .description("Killing mobs grants <red>+<stat> Strength</red> and <aqua>+<stat> Magic Find</aqua>",
                            "for <green>40s</green> to all players staying within <green>20</green> blocks of where",
                            "they died.")
                    .implementation(new WebBattlefield()).build());
        }

        return abilities;
    }

    @Override
    public PetAttribute[] initializeAttributes() {
        List<PetAttribute> attributes = new ArrayList<>();
        attributes.add(new PetAttribute(0.1, 0.1, Stats.STRENGTH));
        attributes.add(new PetAttribute(0.1, 0.1, Stats.CRIT_CHANCE));
        attributes.add(new PetAttribute(0.1, 0.1, Stats.CRIT_DAMAGE));

        return attributes.toArray(new PetAttribute[0]);
    }

    @Override
    protected PetUpgrade initializeUpgrade() {
        Map<Material, Integer> upgradeMaterials = new HashMap<>();

        return switch (getRarity()) {
            case EPIC -> {
                upgradeMaterials.put(Material.SPIDER_EYE, 128);
                upgradeMaterials.put(Material.COBWEB, 64);
                yield new PetUpgrade(Rarity.LEGENDARY, 15000, 8 * 60 * 60 * 1000L, upgradeMaterials);
            }
            case LEGENDARY -> {
                upgradeMaterials.put(Material.SPIDER_EYE, 256);
                upgradeMaterials.put(Material.COBWEB, 128);
                yield new PetUpgrade(Rarity.MYTHIC, 30000, 14 * 24 * 60 * 60 * 1000L, upgradeMaterials);
            }
            default -> null;
        };
    }
}
