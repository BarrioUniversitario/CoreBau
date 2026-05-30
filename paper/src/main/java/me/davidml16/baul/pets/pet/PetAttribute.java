package me.davidml16.baul.pets.pet;

import me.davidml16.baul.pets.utils.Messages;
import me.davidml16.baul.pets.utils.enums.Stats;

public class PetAttribute {

    private double baseValue;
    private final double perLevelIncrease;
    private final Stats stat;

    public PetAttribute(double baseValue, double perLevelIncrease, Stats stat) {
        this.baseValue = Messages.round(baseValue);
        this.perLevelIncrease = Messages.round(perLevelIncrease);
        this.stat = stat;
    }

    public Stats getStat() {
        return stat;
    }

    public double getBaseValue() {
        return baseValue;
    }

    public double getValue(int level) {
        return Messages.round(baseValue + (perLevelIncrease * (level - 1)));
    }

    public void addBoost(double amount) {
        baseValue = Messages.round(baseValue + amount);
    }

    public void removeBoost(double amount) {
        baseValue = Messages.round(baseValue - amount);
    }
}
