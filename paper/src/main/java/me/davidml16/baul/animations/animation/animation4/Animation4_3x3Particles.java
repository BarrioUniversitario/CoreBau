package me.davidml16.baul.animations.animation.animation4;

import me.davidml16.baul.utils.ParticlesAPI.Particles;
import me.davidml16.baul.utils.ParticlesAPI.UtilParticles;
import org.bukkit.entity.LivingEntity;
import org.bukkit.scheduler.BukkitRunnable;

public class Animation4_3x3Particles extends BukkitRunnable {

    private final LivingEntity entity;


    public Animation4_3x3Particles(LivingEntity entity) {
        this.entity = entity;
    }

    public void run() {
        UtilParticles.display(Particles.FLAME, 1D, 1D, 1D, this.entity.getLocation(), 5);
    }

}
