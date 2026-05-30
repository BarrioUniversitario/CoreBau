package me.davidml16.baul.pets.pets.combat;

import me.davidml16.baul.pets.abilities.AbilityBuilder;
import me.davidml16.baul.pets.abilities.PetAbility;
import me.davidml16.baul.pets.abilities.ghoul.ArmyOfTheDead;
import me.davidml16.baul.pets.abilities.ghoul.ReaperSoul;
import me.davidml16.baul.pets.abilities.ghoul.UndeadSlayer;
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

public class GhoulPet extends Pet {

    private static final String PET_ID = "ghoul";
    private static final String PET_NAME = "Ghoul";
    private static final String TEXTURE_URL = "https://textures.minecraft.net/texture/87934565bf522f6f4726cdfe127137be11d37c310db34d8c70253392b5ff5b";

    public GhoulPet(Rarity rarity) {
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
                .name("Undead Slayer")
                .description(List.of("Gain bonus Combat XP vs. Zombies: <green><stat>x</green>"))
                .implementation(new UndeadSlayer())
                .build());

        abilities.add(new AbilityBuilder()
                .name("Army of the Dead")
                .description(List.of("+2 soul capacity, <green><stat>%</green> bonus soul chance"))
                .implementation(new ArmyOfTheDead())
                .build());

        if (getRarity().ordinal() >= Rarity.LEGENDARY.ordinal()) {
            abilities.add(new AbilityBuilder()
                    .name("Reaper Soul")
                    .description(List.of(
                            "Reduces mob summoning cost by <green>30%</green>",
                            "Summoned mobs deal <green>+20%</green> damage",
                            "Summoned mobs gain <green>+100%</green> health"))
                    .implementation(new ReaperSoul())
                    .build());
        }

        return abilities;
    }

    @Override
    public PetAttribute[] initializeAttributes() {
        List<PetAttribute> attributes = new ArrayList<>();
        attributes.add(new PetAttribute(1, 1, Stats.HEALTH));
        attributes.add(new PetAttribute(0.75, 0.75, Stats.INTELLIGENCE));
        attributes.add(new PetAttribute(0.05, 0.05, Stats.FEROCITY));
        return attributes.toArray(new PetAttribute[0]);
    }

    @Override
    protected PetUpgrade initializeUpgrade() {
        Map<Material, Integer> upgradeMaterials = new HashMap<>();

        return switch (getRarity()) {
            case EPIC -> {
                upgradeMaterials.put(Material.ROTTEN_FLESH, 512); // proxy for Revenant Flesh
                yield new PetUpgrade(Rarity.LEGENDARY, 3_500_000, 10L * 24 * 60 * 60 * 1000L, upgradeMaterials);
            }
            default -> null;
        };
    }
}
