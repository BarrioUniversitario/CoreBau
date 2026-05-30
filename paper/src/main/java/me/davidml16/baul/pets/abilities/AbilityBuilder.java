package me.davidml16.baul.pets.abilities;

import java.util.Arrays;
import java.util.List;

public class AbilityBuilder {

    private String name;
    private List<String> description;
    private IAbility implementation;

    public AbilityBuilder name(String name) {
        this.name = name;
        return this;
    }

    public AbilityBuilder description(List<String> description) {
        this.description = description;
        return this;
    }

    public AbilityBuilder description(String... description) {
        this.description = Arrays.asList(description);
        return this;
    }

    public AbilityBuilder implementation(IAbility ability) {
        this.implementation = ability;
        return this;
    }

    public PetAbility build() {
        return new PetAbility(this.name, this.description, this.implementation);
    }
}
