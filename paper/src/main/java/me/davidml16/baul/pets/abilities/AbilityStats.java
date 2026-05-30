package me.davidml16.baul.pets.abilities;

import me.davidml16.baul.pets.utils.enums.Stats;

import java.util.ArrayList;
import java.util.List;

public class AbilityStats {

    private final List<StatAmplifier> statAmplifiers;

    public AbilityStats() {
        statAmplifiers = new ArrayList<>();
    }

    public void addStatAmplifier(StatAmplifier statAmplifier) {
        statAmplifiers.add(statAmplifier);
    }

    public AbilityStats addStatAmplifier(Stats stat, double base, double increase) {
        statAmplifiers.add(new StatAmplifier(stat, base, increase));
        return this;
    }

    public AbilityStats addStatAmplifier(double base, double increase) {
        statAmplifiers.add(new StatAmplifier(null, base, increase));
        return this;
    }

    public List<StatAmplifier> getStatAmplifiers() {
        return statAmplifiers;
    }

    public StatAmplifier getStatAmplifier(Stats stat) {
        return statAmplifiers.stream()
                .filter(statAmplifier -> statAmplifier.stat().equals(stat))
                .findFirst().orElse(null);
    }


    public record StatAmplifier(Stats stat, double base, double increase) {

        public double getStatAtLevel(int level) {
            return base + (increase * (level - 1));
        }
    }
}
