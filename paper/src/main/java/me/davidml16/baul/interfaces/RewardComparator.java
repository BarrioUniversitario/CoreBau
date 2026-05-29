package me.davidml16.baul.interfaces;

import me.davidml16.baul.objects.rewards.Reward;

import java.util.Comparator;

public class RewardComparator implements Comparator<Reward> {

    @Override
    public int compare(Reward o1, Reward o2) {
        try {
            return o1.getName().compareTo(o2.getName());
        } catch (Exception e) {
            return -1;
        }
    }

}
