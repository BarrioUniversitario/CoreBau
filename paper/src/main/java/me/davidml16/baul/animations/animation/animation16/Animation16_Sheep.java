package me.davidml16.baul.animations.animation.animation16;

import me.davidml16.baul.Main;
import me.davidml16.baul.animations.ASSpawner;
import me.davidml16.baul.utils.LocationUtils;
import me.davidml16.baul.utils.ParticlesAPI.UtilParticles;
import org.bukkit.DyeColor;
import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Sheep;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class Animation16_Sheep extends BukkitRunnable {

	private final Entity sheep;

	private final List<Location> locations;

	private int step;
	private double yDif;

	private final Random random = new Random();

	private final List<RGBColor> colors = new ArrayList<RGBColor>() {{
		add(new RGBColor(255, 0, 0));
		add(new RGBColor(255, 165, 0));
		add(new RGBColor(255, 255, 0));
		add(new RGBColor(154, 205, 50));
		add(new RGBColor(30, 144, 255));
		add(new RGBColor(148, 0, 211));
	}};

	public Animation16_Sheep(Main main, Location center, float radius, int points, int step) {
		this.locations = LocationUtils.getCircle(center, radius, points);
		this.step = step;
		this.yDif = 0.0;

		Sheep sheep = center.getWorld().spawn(locations.get(step), Sheep.class);

		sheep.setSilent(true);
		sheep.setInvulnerable(true);
		sheep.setAI(false);
		sheep.setSilent(true);
		sheep.setBaby();
		sheep.setCollidable(false);
		sheep.setMetadata("BAUL", new FixedMetadataValue(main, Boolean.TRUE));
		sheep.teleport(locations.get(step));

		ASSpawner.setEntityNoclip(sheep);

		main.getAnimationHandler().getEntities().add(sheep);

		this.sheep = sheep;
	}

	public Entity getEntity() {
		return sheep;
	}

	public int getStep() {
		return step;
	}

	public void setStep(int step) {
		this.step = step;
	}

	public void run() {

		Location loc = locations.get(step);
		if (step + 1 >= locations.size())
			loc.setDirection(locations.get(0).clone().add(0, yDif, 0).toVector().subtract(loc.toVector()));
		else loc.setDirection(locations.get(step + 1).clone().add(0, yDif, 0).toVector().subtract(loc.toVector()));

		Location l = loc.clone().add(0, yDif, 0);
		l.setPitch(0);
		sheep.teleport(l);

		float y = 0.5f;
		for (RGBColor rgbColor : colors) {
			for (int i = 0; i < 10; i++)
				UtilParticles.display(rgbColor.getRed(), rgbColor.getGreen(), rgbColor.getBlue(), sheep.getLocation()
						.add(sheep.getLocation().getDirection().normalize().multiply(-1.5).multiply(1))
						.add(0, y, 0));
			y -= 0.075;
		}

		if (step % 2 == 0) ((Sheep) sheep).setColor(DyeColor.values()[random.nextInt(15)]);

		this.step++;
		if (step >= locations.size()) step = 0;

		if (yDif < 5) yDif += 0.095;

	}

	private class RGBColor {
		int red;
		int green;
		int blue;

		public RGBColor(int red, int green, int blue) {
			this.red = red;
			this.green = green;
			this.blue = blue;
		}

		public int getBlue() {
			return blue;
		}

		public int getGreen() {
			return green;
		}

		public int getRed() {
			return red;
		}
	}

}
