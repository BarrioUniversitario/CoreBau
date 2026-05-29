package me.davidml16.baul.animations.animation.animation25;

import me.davidml16.baul.Main;
import me.davidml16.baul.animations.ASSpawner;
import me.davidml16.baul.animations.Animation;
import me.davidml16.baul.animations.AnimationSettings;
import me.davidml16.baul.utils.ParticlesAPI.Particles;
import me.davidml16.baul.utils.ParticlesAPI.UtilParticles;
import me.davidml16.baul.utils.SkullUtils;
import me.davidml16.baul.utils.Sounds;
import org.bukkit.Color;
import org.bukkit.FireworkEffect;
import org.bukkit.Location;
import org.bukkit.entity.ArmorStand;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.util.EulerAngle;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

public class Animation25_Task extends Animation {

    private final Set<ArmorStand> souls = new HashSet<>();
    private final Set<ArmorStand> witches = new HashSet<>();
    private ArmorStand rewardStand;
    private Location rewardLocation;
    private double spiralAngle = 0;
    private double witchAngle = 0;

    public Animation25_Task(Main main, AnimationSettings animationSettings) {
        super(main, animationSettings);
    }

    @Override
    public void onTick(int time) {

        if (time >= 10 && time <= 30 && time % 3 == 0) {
            Location smokeLoc = getCubeletBox().getLocation()
                .clone()
                .add(0.5, 0.5, 0.5);
            UtilParticles.display(Particles.SMOKE_LARGE, 0.3f, 0.1f, 0.3f, smokeLoc, 5);
            UtilParticles.display(Particles.PORTAL, 0.3f, 0.3f, 0.3f, smokeLoc, 10);
        }

        if (time >= 25 && time <= 85 && time % 12 == 0) {
            spawnSoul();
        }

        if (time >= 25 && time <= 165) {
            animateSouls();
        }

        if (time == 35) spawnWitch(2.5, 0, 0);
        if (time == 45) spawnWitch(-2.5, 0, 0);
        if (time == 55) spawnWitch(0, 0, 2.5);
        if (time == 65) spawnWitch(0, 0, -2.5);

        if (time >= 35 && time <= 155) {
            animateWitches();
        }

        if (time >= 40 && time <= 150 && time % 8 == 0) {
            for (ArmorStand witch : witches) {
                if (witch != null && witch.isValid()) {
                    UtilParticles.display(Particles.SPELL_WITCH, 0.2f, 0.4f, 0.2f, witch.getLocation(), 5);
                    UtilParticles.display(Particles.SMOKE_NORMAL, 0.1f, 0.2f, 0.1f, witch.getLocation(), 2);
                }
            }
        }

        if (time == 95) {
            rewardStand = ASSpawner.spawn(getMain(), getCubeletBox(), getCubeletType(), false);
            rewardLocation = rewardStand.getLocation();
            getMain().getAnimationHandler()
                .getEntities()
                .add(rewardStand);

            getCubeletBox().getLocation()
                .getWorld()
                .strikeLightningEffect(getCubeletBox().getLocation()
                    .clone()
                    .add(0.5, 1, 0.5));

            Sounds.playSound(rewardLocation, Sounds.MySound.WITCH_AMBIENT, 1F, 0.6F);
            Sounds.playSound(rewardLocation, Sounds.MySound.ENDERDRAGON_GROWL, 0.7F, 1.5F);
            Sounds.playSound(rewardLocation, Sounds.MySound.GHAST_SCREAM2, 0.5F, 0.8F);

            UtilParticles.display(Particles.SPELL_WITCH, 1.5f, 1.5f, 1.5f, rewardLocation.clone()
                .add(0, 1, 0), 80);
            UtilParticles.display(Particles.SMOKE_LARGE, 1f, 1f, 1f, rewardLocation.clone()
                .add(0, 1, 0), 30);
            UtilParticles.display(Particles.PORTAL, 1f, 1f, 1f, rewardLocation.clone()
                .add(0, 1, 0), 40);
        }

        if (time > 95 && time < 165) {
            if (rewardStand != null) {
                if (time <= 135) {
                    rewardLocation.add(0, 0.022, 0);
                }
                rewardStand.teleport(rewardLocation);
                rewardStand.setHeadPose(rewardStand.getHeadPose()
                    .add(0, 0.18, 0));

                if (time % 4 == 0) {
                    UtilParticles.display(Particles.SPELL_WITCH, 0.4f, 0.6f, 0.4f, rewardLocation, 3);
                }
            }
        }

        if (time >= 30 && time <= 155 && time % 2 == 0) {
            Location cauldronLoc = getCubeletBox().getLocation()
                .clone()
                .add(0.5, 0.5, 0.5);
            UtilParticles.display(Particles.SPELL_WITCH, 0.25f, 0.1f, 0.25f, cauldronLoc, 4);
            UtilParticles.display(Particles.SMOKE_LARGE, 0.2f, 0.1f, 0.2f, cauldronLoc, 3);

            if (time % 10 == 0) {
                UtilParticles.display(Particles.PORTAL, 0.3f, 0.5f, 0.3f, cauldronLoc, 8);
                Sounds.playSound(cauldronLoc, Sounds.MySound.GHAST_MOAN, 0.3F, 1.8F);
            }
        }

        if (time >= 25 && time <= 165 && time % 4 == 0) {
            for (int i = 0; i < 5; i++) {
                double offsetX = (Math.random() - 0.5) * 5;
                double offsetZ = (Math.random() - 0.5) * 5;
                double offsetY = Math.random() * 2;
                Location particleLoc = getCubeletBox().getLocation()
                    .clone()
                    .add(offsetX + 0.5, offsetY, offsetZ + 0.5);
                UtilParticles.display(Particles.SPELL_WITCH, 0.05f, 0.05f, 0.05f, particleLoc, 1);

                // Occasional dark smoke
                if (i % 2 == 0) {
                    UtilParticles.display(Particles.SMOKE_NORMAL, 0.05f, 0.05f, 0.05f, particleLoc, 1);
                }
            }
        }

        if (time == 50 || time == 80 || time == 120) {
            Sounds.playSound(getCubeletBox().getLocation(), Sounds.MySound.GHAST_MOAN, 0.4F, 0.8F);
        }

        if (time == 135) removeSoul();
        if (time == 140) removeSoul();
        if (time == 145) removeSoul();
        if (time == 150) removeSoul();
        if (time == 155) removeSoul();

        if (time == 163) {
            doPreRewardReveal();
        }

    }

    @Override
    public void onStart() {
        setAnimationBlocks(new Animation25_Blocks(getCubeletBox().getLocation()));
        startAnimationBlocks(0L);
        setColors(Arrays.asList(Color.PURPLE, Color.ORANGE, Color.BLACK));
    }

    @Override
    public void onStop() {
        cancelRunnables();

        try {
            for (ArmorStand soul : souls) {
                if (soul != null) soul.remove();
                getMain().getAnimationHandler()
                    .getEntities()
                    .remove(soul);
            }
            souls.clear();
        } catch (IllegalStateException |
                 NullPointerException ignored) {}

        try {
            for (ArmorStand witch : witches) {
                if (witch != null) witch.remove();
                getMain().getAnimationHandler()
                    .getEntities()
                    .remove(witch);
            }
            witches.clear();
        } catch (IllegalStateException |
                 NullPointerException ignored) {}

        stopAnimationBlocks();

        if (getMain().getAnimationHandler()
            .getEntities()
            .contains(rewardStand)) {
            if (rewardStand != null)
                rewardStand.remove();
            getMain().getAnimationHandler()
                .getEntities()
                .remove(rewardStand);
        }
    }

    @Override
    public void onPreRewardReveal() {
        getMain().getFireworkUtil()
            .spawn(getCubeletBox().getLocation()
                    .clone()
                    .add(0.5, 1.5, 0.5),
                FireworkEffect.Type.BALL_LARGE,
                getColors().get(0),
                getColors().get(1));

        for (ArmorStand witch : witches) {
            if (witch != null) {
                UtilParticles.display(Particles.SPELL_WITCH, 0.3f, 0.5f, 0.3f, witch.getLocation(), 20);
                witch.remove();
            }
            getMain().getAnimationHandler()
                .getEntities()
                .remove(witch);
        }
        witches.clear();

        Sounds.playSound(getCubeletBox().getLocation(), Sounds.MySound.LEVEL_UP, 1F, 1.5F);
    }

    @Override
    public void onRewardReveal() {
        if (rewardStand != null) {
            rewardStand.remove();
            rewardStand = null;
        }
    }

    @Override
    public void onRewardDuplication() {}

    private void spawnSoul() {
        Location soulLoc = getCubeletBox().getLocation()
            .clone()
            .add(
                (Math.random() - 0.5) * 3,
                0.5,
                (Math.random() - 0.5) * 3
            );

        ArmorStand soul = soulLoc.getWorld()
            .spawn(soulLoc, ArmorStand.class);
        soul.setSmall(true);
        soul.setGravity(false);
        soul.setVisible(false);
        soul.setMarker(false);
        soul.setHelmet(SkullUtils.itemFromBase64("eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvNjhkMjE4MzY0MDIxOGFiMzMwYWM1NmQyYWFiN2UyOWE5NzkwYTU0NWY2OTE2MTllMzg1NzhlYTRhNjlhZTBiNiJ9fX0="));
        soul.setMetadata("BAUL", new FixedMetadataValue(getMain(), Boolean.TRUE));
        soul.setMetadata("HALLOWEEN_SOUL", new FixedMetadataValue(getMain(), System.currentTimeMillis()));

        souls.add(soul);
        getMain().getAnimationHandler()
            .getEntities()
            .add(soul);

        UtilParticles.display(Particles.SPELL_WITCH, 0.2f, 0.2f, 0.2f, soulLoc, 10);
        Sounds.playSound(soulLoc, Sounds.MySound.GHAST_MOAN, 0.5F, 1.5F);
    }

    private void animateSouls() {
        spiralAngle += 0.15;

        for (ArmorStand soul : souls) {
            if (soul != null && soul.isValid()) {
                Location soulLoc = soul.getLocation();

                double radius = 0.5;
                double x = Math.cos(spiralAngle + soul.getLocation()
                    .getX()) * radius * 0.1;
                double z = Math.sin(spiralAngle + soul.getLocation()
                    .getZ()) * radius * 0.1;
                soulLoc.add(x, 0.02, z);

                soul.teleport(soulLoc);
                soul.setHeadPose(new EulerAngle(0, spiralAngle * 0.5, 0));

                UtilParticles.display(Particles.SPELL_WITCH, 0.1f, 0.1f, 0.1f, soulLoc, 1);
            }
        }
    }

    private void spawnWitch(double offsetX, double offsetY, double offsetZ) {
        Location witchLoc = getCubeletBox().getLocation()
            .clone()
            .add(offsetX, 1 + offsetY, offsetZ);

        ArmorStand witch = witchLoc.getWorld()
            .spawn(witchLoc, ArmorStand.class);
        witch.setSmall(true);
        witch.setGravity(false);
        witch.setVisible(false);
        witch.setMarker(false);
        witch.setHelmet(SkullUtils.itemFromBase64("eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvN2U3MWE2ZWIzMDNhYjdlNmY3MGVkNTRkZjkxNDZhODBlYWRmMzk2NDE3Y2VlOTQ5NTc3M2ZmYmViZmFkODg3YyJ9fX0="));
        witch.setMetadata("BAUL", new FixedMetadataValue(getMain(), Boolean.TRUE));
        witch.setMetadata("HALLOWEEN_WITCH", new FixedMetadataValue(getMain(), offsetX + "," + offsetZ));

        witches.add(witch);
        getMain().getAnimationHandler()
            .getEntities()
            .add(witch);

        UtilParticles.display(Particles.SMOKE_LARGE, 0.4f, 0.5f, 0.4f, witchLoc, 20);
        UtilParticles.display(Particles.SPELL_WITCH, 0.3f, 0.4f, 0.3f, witchLoc, 15);
        UtilParticles.display(Particles.PORTAL, 0.3f, 0.3f, 0.3f, witchLoc, 10);
        Sounds.playSound(witchLoc, Sounds.MySound.WITCH_AMBIENT, 0.8F, 0.9F);
        Sounds.playSound(witchLoc, Sounds.MySound.GHAST_MOAN, 0.3F, 1.2F);
    }

    private void animateWitches() {
        witchAngle += 0.08;
        Location center = getCubeletBox().getLocation()
            .clone()
            .add(0.5, 1, 0.5);

        for (ArmorStand witch : witches) {
            if (witch != null && witch.isValid()) {
                String offsetData = witch.getMetadata("HALLOWEEN_WITCH")
                    .get(0)
                    .asString();
                String[] offsets = offsetData.split(",");
                double initialX = Double.parseDouble(offsets[0]);
                double initialZ = Double.parseDouble(offsets[1]);

                double angle = witchAngle + Math.atan2(initialZ, initialX);
                double radius = 2.5;
                double x = center.getX() + Math.cos(angle) * radius;
                double z = center.getZ() + Math.sin(angle) * radius;

                // Add bobbing motion (up and down)
                double bobHeight = Math.sin(witchAngle * 2) * 0.15;

                Location newLoc = new Location(center.getWorld(), x, center.getY() + bobHeight, z);
                newLoc.setYaw((float) Math.toDegrees(-angle + Math.PI / 2));

                witch.teleport(newLoc);

                UtilParticles.display(Particles.SPELL_WITCH, 0.15f, 0.15f, 0.15f, newLoc, 3);
                if (witchAngle % 1.0 < 0.1) {
                    UtilParticles.display(Particles.SMOKE_NORMAL, 0.1f, 0.1f, 0.1f, newLoc, 1);
                }
            }
        }
    }

    private void removeSoul() {
        if (souls.isEmpty()) return;

        ArmorStand soul = souls.iterator().next();
        if (soul != null) {
            UtilParticles.display(Particles.SMOKE_LARGE, 0.3f, 0.3f, 0.3f, soul.getLocation(), 15);
            UtilParticles.display(Particles.SPELL_WITCH, 0.2f, 0.2f, 0.2f, soul.getLocation(), 10);
            UtilParticles.display(Particles.PORTAL, 0.2f, 0.2f, 0.2f, soul.getLocation(), 5);
            Sounds.playSound(soul.getLocation(), Sounds.MySound.GHAST_MOAN, 0.3F, 2F);
            soul.remove();
            getMain().getAnimationHandler()
                .getEntities()
                .remove(soul);
        }
        souls.remove(soul);
    }

}
