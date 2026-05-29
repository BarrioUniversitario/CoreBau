package me.davidml16.baul.cosmetics.types;

import me.davidml16.baul.cosmetics.Cosmetic;
import me.davidml16.baul.cosmetics.CosmeticCategory;
import me.davidml16.baul.effects.SimpleParticle;
import org.bukkit.Color;
import org.bukkit.Particle;

import java.util.List;

public class Trail extends Cosmetic {

    private final Particle particle;
    private final String particleData;
    private final int intervalTicks;
    private final int amount;
    private final double speed;
    private final List<Color> colorCycle;
    private final int colorCycleTicks;

    public Trail(String key, String displayName, String rarity, String iconMaterial, String permission,
                 Particle particle, String particleData, int intervalTicks, int amount, double speed,
                 List<Color> colorCycle, int colorCycleTicks, long price) {
        super(key, CosmeticCategory.TRAIL, displayName, rarity, iconMaterial, permission, price);
        this.particle = particle;
        this.particleData = particleData;
        this.intervalTicks = Math.max(1, intervalTicks);
        this.amount = Math.max(1, amount);
        this.speed = speed;
        this.colorCycle = colorCycle;
        this.colorCycleTicks = Math.max(1, colorCycleTicks);
    }

    public Particle getParticle() { return particle; }
    public String getParticleData() { return particleData; }
    public int getIntervalTicks() { return intervalTicks; }
    public int getAmount() { return amount; }
    public double getSpeed() { return speed; }
    public boolean isAnimated() { return colorCycle != null && !colorCycle.isEmpty(); }

    /**
     * Returns the SimpleParticle to emit at the given server tick.
     * For trails with a non-empty colorCycle, builds a DUST particle whose color
     * advances through the cycle every {@code colorCycleTicks} ticks.
     */
    public SimpleParticle particleForTick(long tick) {
        if (isAnimated()) {
            int idx = (int) ((tick / colorCycleTicks) % colorCycle.size());
            return SimpleParticle.redstone(colorCycle.get(idx), 1f);
        }
        SimpleParticle base = SimpleParticle.of(particle);
        if (particleData == null || particleData.isEmpty()) return base;
        return base.parseData(particleData);
    }

    /** @deprecated kept for backwards compat; prefer {@link #particleForTick(long)}. */
    @Deprecated
    public SimpleParticle toSimpleParticle() {
        return particleForTick(0L);
    }
}
