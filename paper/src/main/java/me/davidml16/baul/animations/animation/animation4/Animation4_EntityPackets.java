package me.davidml16.baul.animations.animation.animation4;

import me.davidml16.baul.objects.CubeletMachine;
import me.davidml16.baul.utils.Sounds;
import org.bukkit.Location;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.LivingEntity;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.Random;

public class Animation4_EntityPackets extends BukkitRunnable {

	private final LivingEntity entity;
	private final ArmorStand armorStand;
	private final CubeletMachine box;

	private final Random random;

	public Animation4_EntityPackets(LivingEntity entity, ArmorStand armorStand, CubeletMachine box) {
		this.entity = entity;
		this.armorStand = armorStand;
		this.random = new Random();
		this.box = box;
	}

	public void run() {

		entity.swingMainHand();
		entity.swingOffHand();

		Sounds.playSound(this.entity.getLocation(), Sounds.MySound.DIG_STONE, 0.5F, 1F);
		Location l = armorStand.getLocation();
		l.setYaw(box.getRotation().value + random.nextInt(15 + 15) - 15);
		armorStand.teleport(l);
	}

}
