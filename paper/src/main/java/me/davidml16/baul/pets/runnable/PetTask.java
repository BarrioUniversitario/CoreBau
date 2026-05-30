package me.davidml16.baul.pets.runnable;

import me.davidml16.baul.pets.pet.Pet;
import me.davidml16.baul.pets.utils.Messages;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

public class PetTask extends BukkitRunnable {

    private final Pet pet;
    private final Player owner;

    private Location lastOwnerLocation;
    private long bobTime = 0;

    private static final double FOLLOW_DISTANCE = 3.0; // Distance to start following
    private static final double TELEPORT_DISTANCE = 15.0; // Distance to teleport
    private static final double BOB_AMPLITUDE = 0.2; // Height of bobbing motion
    private static final double BOB_SPEED = 0.17; // Speed of bobbing (increased)

    public PetTask(Pet pet, Player owner) {
        this.pet = pet;
        this.owner = owner;
        this.lastOwnerLocation = owner.getLocation().clone();
    }

    @Override
    public void run() {
        // Safety checks
        if (!owner.isOnline() || pet.getDisplay() == null || pet.getDisplay().isDead()) {
            cancel();
            return;
        }

        Location ownerLocation = owner.getEyeLocation();
        Location petLocation = pet.getDisplay().getLocation();
        double distance = petLocation.distance(ownerLocation);

        boolean ownerMoved = lastOwnerLocation.distance(ownerLocation) > 0.2;

        if (ownerMoved) {
            lastOwnerLocation = ownerLocation.clone();
        }

        if (distance > TELEPORT_DISTANCE || !petLocation.getWorld().equals(ownerLocation.getWorld())) {
            Location teleportLocation = ownerLocation.clone().add(0, 1, 0);
            pet.getDisplay().setTeleportDuration(1);
            pet.teleport(teleportLocation);
            return;
        }

        updatePetRotation(petLocation, ownerLocation);

        if (distance > FOLLOW_DISTANCE) {
            Location targetLocation = ownerLocation.clone().add(0, 1, 0);

            Vector direction = targetLocation.toVector().subtract(petLocation.toVector());
            direction.normalize().multiply(0.5);

            Location newLocation = petLocation.clone().add(direction);

            int teleportDuration = Math.max(1, Math.min(3, (int) (distance * 1)));
            pet.getDisplay().setTeleportDuration(teleportDuration);
            pet.teleport(newLocation);
            updatePetRotation(petLocation, ownerLocation);

        } else {
            bobTime++;
            double bobOffset = Math.sin(bobTime * BOB_SPEED) * BOB_AMPLITUDE;

            Location bobLocation = petLocation.clone();
            bobLocation.setY(ownerLocation.getY() + 1 + bobOffset);

            pet.getDisplay().setTeleportDuration(2);
            pet.teleport(bobLocation);
        }
    }

    private void updatePetRotation(Location petLoc, Location ownerLoc) {
        double deltaX = ownerLoc.getX() - petLoc.getX();
        double deltaZ = ownerLoc.getZ() - petLoc.getZ();

        if (deltaX != 0 || deltaZ != 0) {
            float yaw = (float) (Math.atan2(deltaZ, deltaX) * 180.0 / Math.PI) - 90.0f;
            if (yaw < 0) yaw += 360;

            pet.getDisplay().setRotation(yaw * -1, 0);
        }
    }

    @Override
    public synchronized void cancel() throws IllegalStateException {
        Messages.debug("Pet following task cancelled for " + pet.getName());
        super.cancel();
    }
}
