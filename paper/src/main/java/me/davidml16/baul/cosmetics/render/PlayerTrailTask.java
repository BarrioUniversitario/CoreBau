package me.davidml16.baul.cosmetics.render;

import me.davidml16.baul.Main;
import me.davidml16.baul.cosmetics.Cosmetic;
import me.davidml16.baul.cosmetics.CosmeticCategory;
import me.davidml16.baul.cosmetics.types.Trail;
import me.davidml16.baul.effects.SimpleParticle;
import me.davidml16.baul.objects.Profile;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class PlayerTrailTask {

    private static final double MIN_MOVE_SQUARED = 0.0025;
    private static final double TPS_FLOOR = 18.0; // skip render below this

    private final Main main;
    private final Map<UUID, Long> lastEmitTick = new HashMap<>();
    private final Map<UUID, Location> lastEmitLocation = new HashMap<>();
    private int taskId = -1;
    private long tickCounter = 0;

    public PlayerTrailTask(Main main) {
        this.main = main;
    }

    public void start() {
        if (taskId != -1) return;
        taskId = Bukkit.getScheduler().scheduleSyncRepeatingTask(main, this::tick, 20L, 1L);
    }

    public void stop() {
        if (taskId != -1) {
            Bukkit.getScheduler().cancelTask(taskId);
            taskId = -1;
        }
        lastEmitTick.clear();
        lastEmitLocation.clear();
    }

    public void forgetPlayer(UUID uuid) {
        lastEmitTick.remove(uuid);
        lastEmitLocation.remove(uuid);
    }

    private void tick() {
        tickCounter++;

        // Drop trail rendering when the server is struggling. The 1-minute TPS average
        // is the most reactive; below 18 we sacrifice cosmetic frames to give gameplay
        // headroom. (Available natively on Paper — wouldn't compile on pure Spigot.)
        double[] tps = Bukkit.getServer().getTPS();
        if (tps.length > 0 && tps[0] < TPS_FLOOR) return;

        for (Player player : Bukkit.getOnlinePlayers()) {
            Profile profile = main.getPlayerDataHandler().getData(player);
            if (profile == null) continue;

            String equippedId = profile.getEquipped(CosmeticCategory.TRAIL.getId());
            if (equippedId == null) continue;

            Cosmetic cosmetic = main.getCosmeticRegistry().getById(equippedId);
            if (!(cosmetic instanceof Trail)) continue;
            Trail trail = (Trail) cosmetic;

            UUID uuid = player.getUniqueId();
            long last = lastEmitTick.getOrDefault(uuid, 0L);
            if (tickCounter - last < trail.getIntervalTicks()) continue;

            Location current = player.getLocation();
            Location previous = lastEmitLocation.get(uuid);
            if (previous != null
                    && previous.getWorld() == current.getWorld()
                    && previous.distanceSquared(current) < MIN_MOVE_SQUARED) {
                continue;
            }

            SimpleParticle particle = trail.particleForTick(tickCounter);
            Location emit = current.clone().add(0, 0.1, 0);

            // Emisión única vía world.spawnParticle(force=true): todos los jugadores
            // del mundo dentro del rango extendido ven el trail. Antes se hacía un
            // bucle por espectador con player.spawnParticle, lo cual provocaba que
            // sólo el dueño viera su trail si los demás tenían cosmeticsVisible=false
            // o si quedaban fuera del rango de partículas por defecto del cliente.
            particle.play(emit, 0.15, trail.getSpeed(), trail.getAmount());

            lastEmitTick.put(uuid, tickCounter);
            lastEmitLocation.put(uuid, current);
        }
    }
}
