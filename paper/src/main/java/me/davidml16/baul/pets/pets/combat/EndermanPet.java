package me.davidml16.baul.pets.pets.combat;

import me.davidml16.baul.pets.abilities.AbilityBuilder;
import me.davidml16.baul.pets.abilities.PetAbility;
import me.davidml16.baul.pets.abilities.enderman.Enderian;
import me.davidml16.baul.pets.abilities.enderman.EndermanSlayer;
import me.davidml16.baul.pets.abilities.enderman.TeleportSavvy;
import me.davidml16.baul.pets.abilities.enderman.ZealotMadness;
import me.davidml16.baul.pets.pet.Pet;
import me.davidml16.baul.pets.pet.PetAttribute;
import me.davidml16.baul.pets.pet.PetUpgrade;
import me.davidml16.baul.pets.utils.enums.PetType;
import me.davidml16.baul.pets.utils.enums.Rarity;
import me.davidml16.baul.pets.utils.enums.Stats;
import org.bukkit.Material;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class EndermanPet extends Pet {

    private static final String PET_ID = "enderman";
    private static final String PET_NAME = "Enderman";
    private static final String TEXTURE_URL = "https://textures.minecraft.net/texture/96c0b36d53fff69a49c7d6f3932f2b0fe948e032226d5e8045ec58408a36e951";

    public EndermanPet(Rarity rarity) {
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
                .name("Enderian")
                .description(Collections.singletonList("Take <green><stat> less damage from end monsters"))
                .implementation(new Enderian())
                .build());

        if (getRarity().ordinal() >= Rarity.RARE.ordinal()) {
            abilities.add(new AbilityBuilder()
                    .name("Teleport Savvy")
                    .description(List.of("Buffs the Transmission abilities granting <green><stat>",
                            "weapon damage for 5s on use"))
                    .implementation(new TeleportSavvy())
                    .build());
        }

        if (getRarity().ordinal() >= Rarity.LEGENDARY.ordinal()) {
            abilities.add(new AbilityBuilder()
                    .name("Zealot Madness")
                    .description(List.of("Increases your odds to find a special Zealot by <green><stat>%"))
                    .implementation(new ZealotMadness())
                    .build());
        }
        if (getRarity().ordinal() >= Rarity.MYTHIC.ordinal()) {
            abilities.add(new AbilityBuilder()
                    .name("Enderman Slayer")
                    .description(List.of("Gain <green><stat>% Combat XP against",
                            "<green>Enderman"))
                    .implementation(new EndermanSlayer())
                    .build());
        }

        return abilities;
    }

    @Override
    public PetAttribute[] initializeAttributes() {
        List<PetAttribute> attributes = new ArrayList<>();

        attributes.add(new PetAttribute(0.75, 0.75, Stats.CRIT_DAMAGE));

        return attributes.toArray(new PetAttribute[0]);
    }

    @Override
    protected PetUpgrade initializeUpgrade() {
        Map<Material, Integer> upgradeMaterials = new HashMap<>();

        return switch (getRarity()) {
            case COMMON -> {
                upgradeMaterials.put(Material.ENDER_PEARL, 8);
                upgradeMaterials.put(Material.OBSIDIAN, 16);
                yield new PetUpgrade(Rarity.UNCOMMON, 500, 24 * 60 * 60 * 1000L, upgradeMaterials);
            }
            case UNCOMMON -> {
                upgradeMaterials.put(Material.ENDER_PEARL, 16);
                upgradeMaterials.put(Material.OBSIDIAN, 32);
                yield new PetUpgrade(Rarity.RARE, 1500, 2 * 24 * 60 * 60 * 1000L, upgradeMaterials);
            }
            case RARE -> {
                upgradeMaterials.put(Material.ENDER_EYE, 8);
                upgradeMaterials.put(Material.END_STONE, 64);
                yield new PetUpgrade(Rarity.EPIC, 3000, 6 * 24 * 60 * 60 * 1000L, upgradeMaterials);
            }
            case EPIC -> {
                upgradeMaterials.put(Material.ELYTRA, 1);
                upgradeMaterials.put(Material.ENDER_PEARL, 64);
                yield new PetUpgrade(Rarity.LEGENDARY, 6000, 12 * 24 * 60 * 60 * 1000L, upgradeMaterials);
            }
            case LEGENDARY -> {
                upgradeMaterials.put(Material.ENDER_PEARL, 128);
                yield new PetUpgrade(Rarity.MYTHIC, 12000, 60 * 60 * 1000L, upgradeMaterials);
            }
            default -> null;
        };
    }
}
