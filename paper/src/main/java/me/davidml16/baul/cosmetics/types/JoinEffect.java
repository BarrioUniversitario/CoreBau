package me.davidml16.baul.cosmetics.types;

import me.davidml16.baul.cosmetics.Cosmetic;
import me.davidml16.baul.cosmetics.CosmeticCategory;
import me.davidml16.baul.effects.SimpleParticle;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.entity.Player;

public class JoinEffect extends Cosmetic {

    private final Particle particle;
    private final String particleData;
    private final int amount;
    private final double spread;
    private final Sound sound;
    private final float pitch;
    private final boolean broadcast;

    public JoinEffect(String key, String displayName, String rarity, String iconMaterial, String permission,
                      Particle particle, String particleData, int amount, double spread,
                      Sound sound, float pitch, boolean broadcast, long price) {
        super(key, CosmeticCategory.JOIN_EFFECT, displayName, rarity, iconMaterial, permission, price);
        this.particle = particle;
        this.particleData = particleData;
        this.amount = Math.max(1, amount);
        this.spread = spread;
        this.sound = sound;
        this.pitch = pitch;
        this.broadcast = broadcast;
    }

    public void play(Player player) {
        SimpleParticle p = particleData == null || particleData.isEmpty()
                ? SimpleParticle.of(particle)
                : SimpleParticle.of(particle).parseData(particleData);
        p.play(player.getLocation().add(0, 1, 0), spread, 0.05, amount);
        if (sound != null) {
            if (broadcast) player.getWorld().playSound(player.getLocation(), sound, 1f, pitch);
            else player.playSound(player.getLocation(), sound, 1f, pitch);
        }
    }
}
