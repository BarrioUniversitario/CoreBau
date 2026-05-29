package me.davidml16.baul.animations;

import me.davidml16.baul.Main;
import me.davidml16.baul.enums.Rotation;
import me.davidml16.baul.objects.CubeletMachine;
import me.davidml16.baul.objects.CubeletType;
import org.bukkit.Location;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.LivingEntity;
import org.bukkit.inventory.ItemStack;
import org.bukkit.metadata.FixedMetadataValue;

public class ASSpawner {

	public static ArmorStand spawn(Main main, CubeletMachine box, CubeletType type, boolean opposite) {
		Location loc = box.getLocation().clone().add(0.5, -0.35, 0.5);
		loc.setYaw(getRotation(box, opposite).value);

		ArmorStand armorStand = box.getLocation().getWorld().spawn(loc, ArmorStand.class);

		armorStand.setSilent(true);

		armorStand.setVisible(false);
		armorStand.setGravity(false);
		armorStand.setHelmet(type.getIcon());
		armorStand.setSmall(true);
		armorStand.setMarker(true);
		armorStand.setRemoveWhenFarAway(false);
		armorStand.setCustomNameVisible(false);
		armorStand.setMetadata("BAUL", new FixedMetadataValue(main, Boolean.TRUE));

		armorStand.teleport(loc);

		return armorStand;
	}

	public static ArmorStand spawn(Main main, CubeletMachine box, CubeletType type, boolean opposite, boolean small, Location loc) {
		loc.setYaw(getRotation(box, opposite).value);

		ArmorStand armorStand = box.getLocation().getWorld().spawn(loc, ArmorStand.class);

		armorStand.setSilent(true);

		armorStand.setVisible(false);
		armorStand.setGravity(false);
		armorStand.setHelmet(type.getIcon());
		armorStand.setSmall(small);
		armorStand.setMarker(false);
		armorStand.setRemoveWhenFarAway(false);
		armorStand.setCustomNameVisible(false);
		armorStand.setMetadata("BAUL", new FixedMetadataValue(main, Boolean.TRUE));

		armorStand.teleport(loc);

		return armorStand;
	}

	public static ArmorStand spawn(Main main, CubeletMachine box, ItemStack head, boolean opposite, boolean small, Location loc) {
		loc.setYaw(getRotation(box, opposite).value);

		ArmorStand armorStand = box.getLocation().getWorld().spawn(loc, ArmorStand.class);

		armorStand.setSilent(true);

		armorStand.setVisible(false);
		armorStand.setGravity(false);
		armorStand.setHelmet(head);
		armorStand.setSmall(small);
		armorStand.setMarker(true);
		armorStand.setRemoveWhenFarAway(false);
		armorStand.setCustomNameVisible(false);
		armorStand.setMetadata("BAUL", new FixedMetadataValue(main, Boolean.TRUE));

		armorStand.teleport(loc);

		return armorStand;
	}

	public static ArmorStand spawn(Main main, Location loc, ItemStack itemStack, boolean opposite, boolean gravity) {
		ArmorStand armorStand = loc.getWorld().spawn(loc, ArmorStand.class);

		armorStand.setSilent(true);

		armorStand.setVisible(false);
		armorStand.setGravity(gravity);
		armorStand.setHelmet(itemStack);
		armorStand.setSmall(false);
		armorStand.setRemoveWhenFarAway(false);
		armorStand.setCustomNameVisible(false);
		armorStand.setMetadata("BAUL", new FixedMetadataValue(main, Boolean.TRUE));

		setEntityNoclip(armorStand);

		armorStand.teleport(loc);

		return armorStand;
	}

	public static ArmorStand spawn(Main main, Location loc, ItemStack itemStack, boolean opposite, boolean gravity, boolean small) {
		ArmorStand armorStand = loc.getWorld().spawn(loc, ArmorStand.class);

		armorStand.setSilent(true);

		armorStand.setVisible(false);
		armorStand.setGravity(gravity);
		armorStand.setHelmet(itemStack);
		armorStand.setSmall(small);
		armorStand.setRemoveWhenFarAway(false);
		armorStand.setCustomNameVisible(false);
		armorStand.setMetadata("BAUL", new FixedMetadataValue(main, Boolean.TRUE));

		setEntityNoclip(armorStand);

		armorStand.teleport(loc);

		return armorStand;
	}

	public static Rotation getRotation(CubeletMachine box, boolean opposite) {
		if (box.getRotation() == Rotation.NORTH) return opposite ? Rotation.SOUTH : Rotation.NORTH;
		else if (box.getRotation() == Rotation.SOUTH) return opposite ? Rotation.NORTH : Rotation.SOUTH;
		else if (box.getRotation() == Rotation.EAST) return opposite ? Rotation.WEST : Rotation.EAST;
		else if (box.getRotation() == Rotation.WEST) return opposite ? Rotation.EAST : Rotation.WEST;
		return null;
	}

	public static void setEntityNoclip(LivingEntity entity) {
		entity.setCollidable(false);
	}

}
