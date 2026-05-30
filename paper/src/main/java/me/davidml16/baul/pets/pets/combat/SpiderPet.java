package me.davidml16.baul.pets.pets.combat;

import me.davidml16.baul.pets.abilities.AbilityBuilder;
import me.davidml16.baul.pets.abilities.PetAbility;
import me.davidml16.baul.pets.abilities.spider.OneWithTheSpider;
import me.davidml16.baul.pets.abilities.spider.SpiderWhisperer;
import me.davidml16.baul.pets.abilities.spider.WebBattlefield;
import me.davidml16.baul.pets.abilities.spider.WebWeaver;
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

public class SpiderPet extends Pet {

    private static final String PET_ID = "spider";
    private static final String PET_NAME = "Spider";
    private static final String TEXTURE_URL = "https://textures.minecraft.net/texture/cd541541daaff50896cd258bdbdd4cf80c3ba816735726078bfe393927e57f1";

    public SpiderPet(Rarity rarity) {
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
                .name("One With The Spider")
                .description("Applies <red>+<stat> Strength</red> to all <dark_red>Archnal",
                        "weapons, armor, and equipment you have",
                        "equipped")
                .implementation(new OneWithTheSpider()).build());

        if (getRarity().ordinal() >= Rarity.RARE.ordinal()) {
            abilities.add(new AbilityBuilder()
                    .name("Web-weaver")
                    .description("Upon hitting a monster, it becomes slowed",
                            "by <green><stat>%")
                    .implementation(new WebWeaver()).build());
        }

        if (getRarity().ordinal() >= Rarity.LEGENDARY.ordinal()) {
            abilities.add(new AbilityBuilder()
                    .name("Spider Whisperer")
                    .description("Spider, Cave Spider and Tarantula minions",
                            "work <green><stat>%</green> faster while on your island")
                    .implementation(new SpiderWhisperer()).build());
        }

        if (getRarity().ordinal() >= Rarity.MYTHIC.ordinal()) {
            abilities.add(new AbilityBuilder()
                    .name("Web Battlefield")
                    .description("Killing mobs grants <red>+<stat> Strength</red> and",
                            "<aqua>+<stat> Magic Find</aqua> for <green>40s</green> to all players",
                            "staying within <green>20</green> blocks of where they died.",
                            "Stacks up to 10 times")
                    .implementation(new WebBattlefield()).build());
        }

        return abilities;
    }

    @Override
    public PetAttribute[] initializeAttributes() {
        List<PetAttribute> attributes = new ArrayList<>();
        attributes.add(new PetAttribute(0.5, 0.5, Stats.STRENGTH));
        attributes.add(new PetAttribute(0.1, 0.1, Stats.CRIT_CHANCE));
        return attributes.toArray(new PetAttribute[0]);
    }

    @Override
    protected PetUpgrade initializeUpgrade() {
        Map<Material, Integer> upgradeMaterials = new HashMap<>();

        return switch (getRarity()) {
            case COMMON -> {
                upgradeMaterials.put(Material.STRING, 64);
                upgradeMaterials.put(Material.SPIDER_EYE, 32);
                yield new PetUpgrade(Rarity.UNCOMMON, 400, 15 * 60 * 1000L, upgradeMaterials);
            }
            case UNCOMMON -> {
                upgradeMaterials.put(Material.COBWEB, 16);
                upgradeMaterials.put(Material.FERMENTED_SPIDER_EYE, 8);
                yield new PetUpgrade(Rarity.RARE, 800, 35 * 60 * 1000L, upgradeMaterials);
            }
            case RARE -> {
                upgradeMaterials.put(Material.COBWEB, 32);
                yield new PetUpgrade(Rarity.EPIC, 1600, 75 * 60 * 1000L, upgradeMaterials);
            }
            case EPIC -> {
                upgradeMaterials.put(Material.COBWEB, 64);
                upgradeMaterials.put(Material.NETHER_WART, 128);
                yield new PetUpgrade(Rarity.LEGENDARY, 3200, 2 * 60 * 60 * 1000L, upgradeMaterials);
            }
            default -> null;
        };
    }
}
