package me.davidml16.baul.cosmetics.render;

import me.davidml16.baul.Main;
import me.davidml16.baul.cosmetics.Cosmetic;
import me.davidml16.baul.cosmetics.CosmeticCategory;
import me.davidml16.baul.cosmetics.types.Wing;
import me.davidml16.baul.objects.Profile;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.entity.Player;

/**
 * Renderiza las alas cosméticas equipadas por cada jugador online. Cada Wing
 * declara su propio {@code intervalTicks}; el task corre cada tick y emite las
 * formas que toquen en este tick.
 *
 * El método de emisión usa {@code World.spawnParticle(..., force=true)} para
 * que el paquete llegue a TODOS los jugadores en rango extendido (~256 bloques)
 * en vez del rango por defecto de partículas (~32 bloques). Sin force, los
 * espectadores lejanos o con la opción "Particles: Minimal" del cliente no
 * verían el efecto.
 *
 * Inspirado por ButterflyTask de CompletosCore (Pablo B07) — generalizado para
 * múltiples shapes (mariposa, corazón, halo, ángel, etc.) y unificado bajo el
 * sistema de cosméticos de Baul.
 */
public class WingTask {

    private static final double TPS_FLOOR = 18.0;
    private static final double MAX_DISTANCE_SQUARED = 4096.0; // 64-block emit gate

    private final Main main;
    private int taskId = -1;
    private long tickCounter = 0;

    public WingTask(Main main) {
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
    }

    private void tick() {
        tickCounter++;

        // Drop wing rendering when the server is struggling — gameplay before glitter.
        double[] tps = Bukkit.getServer().getTPS();
        if (tps.length > 0 && tps[0] < TPS_FLOOR) return;

        for (Player player : Bukkit.getOnlinePlayers()) {
            Profile profile = main.getPlayerDataHandler().getData(player);
            if (profile == null) continue;

            String equippedId = profile.getEquipped(CosmeticCategory.WING.getId());
            if (equippedId == null) continue;

            Cosmetic c = main.getCosmeticRegistry().getById(equippedId);
            if (!(c instanceof Wing wing)) continue;

            if (tickCounter % wing.getIntervalTicks() != 0) continue;

            renderWing(player, wing);
        }
    }

    private void renderWing(Player player, Wing wing) {
        Location anchor = player.getLocation().clone();
        // Detrás del jugador, a la altura del pecho. Mantenemos el pitch a 0 para
        // que la forma no se incline cuando el jugador mire arriba o abajo.
        anchor.add(anchor.getDirection().normalize().multiply(-0.3));
        anchor.add(0, 0.85, 0);
        anchor.setPitch(0F);

        final float dustSize = wing.getDustSize();
        wing.getShape().emit(player, anchor, wing.getScale(), wing.getDensity(), (at, phase) -> {
            if (at.getWorld() == null) return;
            if (at.distanceSquared(anchor) > MAX_DISTANCE_SQUARED) return;
            Particle.DustOptions dust = new Particle.DustOptions(wing.colorAt(phase, tickCounter), dustSize);
            try {
                at.getWorld().spawnParticle(
                        Particle.DUST,
                        at.getX(), at.getY(), at.getZ(),
                        0,                  // count=0 → cada llamada un solo punto, el offset lo controla la forma
                        0, 0, 0,
                        0,
                        dust,
                        true                // force=true: broadcast garantizado
                );
            } catch (Exception ignored) {
                // Algunos servidores antiguos pueden no aceptar DUST con esta sobrecarga;
                // si pasa, simplemente saltamos el punto.
            }
        });
    }
}
