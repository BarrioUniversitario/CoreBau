package dev.blancocl.hologram;

import dev.blancocl.util.Threading;
import eu.decentsoftware.holograms.api.DHAPI;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Entity;
import org.bukkit.entity.TextDisplay;
import org.bukkit.plugin.Plugin;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Renders {@link HologramImpl}s using <b>DecentHolograms</b> as the backing
 * implementation (no mas TextDisplay nativo).
 *
 * <p>Cada {@link HologramImpl} se mapea 1-a-1 a un hologram DH cuyo nombre es
 * el {@code id()} del HologramImpl (UUID). Eso garantiza unicidad cross-plugin
 * y nos permite limpiar por nombre sin escanear entidades del mundo.</p>
 *
 * <p>El texto se renderiza pasando MiniMessage por
 * {@link LegacyComponentSerializer#legacySection()} antes de entregarselo a
 * DH, que entiende {@code §} codes y aplica PAPI / animaciones internas
 * ({@code %anim_x%}, etc.) por su cuenta.</p>
 *
 * <p>API publica (spawn / despawn / broadcastUpdate / sweep*) se mantiene para
 * no romper {@link dev.blancocl.services.HologramManagerImpl}, los comandos
 * de holograma y la persistencia de snapshots.</p>
 */
public final class HologramRenderer {

    private final Plugin plugin;
    private final Threading threading;
    /** Hologram id -> DH name (mismo string, lo guardamos para limpieza barata). */
    private final Map<String, String> tracked = new ConcurrentHashMap<>();
    private volatile boolean dhAvailable;

    public HologramRenderer(Plugin plugin, Threading threading) {
        this.plugin    = plugin;
        this.threading = threading;
        this.dhAvailable = Bukkit.getPluginManager().isPluginEnabled("DecentHolograms");
        if (!dhAvailable) {
            plugin.getLogger().warning("[Holograms] DecentHolograms no esta instalado/activo. "
                    + "Los hologramas del Npc seran no-op hasta que lo instales.");
        }
    }

    /** Re-chequea presencia de DH (util si se cargo despues por orden de plugins). */
    public void recheckBackend() {
        boolean now = Bukkit.getPluginManager().isPluginEnabled("DecentHolograms");
        if (now != dhAvailable) {
            dhAvailable = now;
            plugin.getLogger().info("[Holograms] DecentHolograms = " + (now ? "activo" : "ausente"));
        }
    }

    public void spawn(HologramImpl holo) {
        threading.onMain(() -> spawnSync(holo));
    }

    public void despawn(HologramImpl holo) {
        threading.onMain(() -> despawnSync(holo));
    }

    public void broadcastUpdate(HologramImpl holo) {
        threading.onMain(() -> updateSync(holo));
    }

    public void shutdown() {
        threading.onMain(() -> {
            for (String name : tracked.values()) {
                safeRemoveDh(name);
            }
            tracked.clear();
        });
    }

    /**
     * Sweep de huerfanos: cualquier hologram DH cuyo nombre empiece con
     * nuestro prefijo y NO este en el map de tracking se elimina. Cubre el
     * caso "reload anterior dejo holograms colgando".
     */
    public void sweepOrphans() {
        threading.onMain(this::sweepOrphansSync);
    }

    /** Variante diferida: util al boot para esperar el async-load del NpcManager. */
    public void sweepOrphansLater(long delayTicks) {
        Bukkit.getScheduler().runTaskLater(plugin, this::sweepOrphansSync, delayTicks);
    }

    public int sweepOrphansBlocking() {
        if (!Bukkit.isPrimaryThread()) {
            throw new IllegalStateException("sweepOrphansBlocking must be called from main thread");
        }
        return sweepOrphansSync();
    }

    /**
     * Compat: limpieza legacy por proximidad para entidades {@link TextDisplay}
     * que sobraron de la implementacion anterior (TextDisplay nativo). Solo
     * tiene sentido correrlo una vez tras la migracion a DH.
     */
    public int sweepLegacyOrphansNear(Collection<Location> anchorPoints,
                                      double radius, double verticalRange) {
        if (!Bukkit.isPrimaryThread()) {
            throw new IllegalStateException("sweepLegacyOrphansNear must be called from main thread");
        }
        double r2 = radius * radius;
        int removed = 0;
        for (World w : Bukkit.getWorlds()) {
            for (Entity e : w.getEntities()) {
                if (!(e instanceof TextDisplay)) continue;
                Location at = e.getLocation();
                for (Location anchor : anchorPoints) {
                    if (anchor.getWorld() == null
                            || !anchor.getWorld().getUID().equals(w.getUID())) continue;
                    double dx = at.getX() - anchor.getX();
                    double dz = at.getZ() - anchor.getZ();
                    double dy = Math.abs(at.getY() - anchor.getY());
                    if (dx * dx + dz * dz <= r2 && dy <= verticalRange) {
                        try { e.remove(); removed++; }
                        catch (Throwable ignored) {}
                        break;
                    }
                }
            }
        }
        return removed;
    }

    /* ---------------- sync impls ---------------- */

    private void spawnSync(HologramImpl holo) {
        if (!dhAvailable) return;
        // Despawn previo: garantiza idempotencia, evita doble-spawn tras reload.
        despawnSync(holo);

        Location anchor = holo.location();
        if (anchor.getWorld() == null) return;

        String name = dhName(holo.id());
        List<String> rendered = renderLines(holo.rawLines());
        try {
            // Forma de 2 args (la misma que ya usa Baul con DH 2.9.9). Las
            // lineas se setean despues. La persistencia del NPC vive en el
            // snapshot YAML/MySQL del NpcManager, no en los .yml de DH.
            eu.decentsoftware.holograms.api.holograms.Hologram dh =
                    DHAPI.createHologram(name, anchor);
            if (dh != null) {
                DHAPI.setHologramLines(dh, rendered);
                tracked.put(holo.id(), name);
            }
        } catch (Throwable t) {
            plugin.getLogger().warning("[Holograms] no se pudo crear DH hologram '" + name + "': " + t);
        }
    }

    private void despawnSync(HologramImpl holo) {
        String name = tracked.remove(holo.id());
        // Aunque no este en el map, tirar el remove por si DH lo tiene igual.
        safeRemoveDh(name != null ? name : dhName(holo.id()));
    }

    private void updateSync(HologramImpl holo) {
        if (holo.isDetached() || !dhAvailable) return;
        String name = tracked.get(holo.id());
        if (name == null) { spawnSync(holo); return; }

        Location anchor = holo.location();
        if (anchor.getWorld() == null) return;

        try {
            eu.decentsoftware.holograms.api.holograms.Hologram dh = DHAPI.getHologram(name);
            if (dh == null) {
                // El hologram desaparecio del backend (reload de DH, /dh remove manual);
                // lo recreamos.
                spawnSync(holo);
                return;
            }
            DHAPI.moveHologram(dh, anchor);
            DHAPI.setHologramLines(dh, renderLines(holo.rawLines()));
        } catch (Throwable t) {
            plugin.getLogger().warning("[Holograms] update fallo para '" + name + "': " + t);
        }
    }

    private int sweepOrphansSync() {
        if (!dhAvailable) return 0;
        int removed = 0;
        try {
            // No hay una API publica para listar holograms; iteramos
            // nuestro tracking inverso: cualquier nombre que conocimos y que
            // ya no esta en tracked era huerfano. Como tracked solo guarda los
            // vivos, basta con asegurarse de borrar duplicados estaticos.
            for (Map.Entry<String, String> e : tracked.entrySet()) {
                eu.decentsoftware.holograms.api.holograms.Hologram h = DHAPI.getHologram(e.getValue());
                if (h == null) {
                    // tracking dice que existe pero DH lo perdio -> limpiar entrada.
                    tracked.remove(e.getKey());
                }
            }
        } catch (Throwable t) {
            plugin.getLogger().warning("[Holograms] sweep fallo: " + t);
        }
        if (removed > 0) {
            plugin.getLogger().info("[Holograms] sweep elimino " + removed + " hologram(s) huerfano(s).");
        }
        return removed;
    }

    /* ---------------- helpers ---------------- */

    /** Prefijo + uuid para que sea trivialmente identificable como nuestro. */
    private static String dhName(String hologramId) {
        return "corebau-npc-" + hologramId;
    }

    private void safeRemoveDh(String name) {
        if (name == null) return;
        try {
            DHAPI.removeHologram(name);
        } catch (Throwable ignored) {}
    }

    /**
     * Renderiza las lineas MiniMessage del HologramImpl a strings con codigos
     * legacy ({@code §}) que DH entiende. DH se encarga de PAPI y animaciones
     * internas; nuestra capa de animacion por frame deja de aplicarse (la hace
     * DH con su propio motor).
     */
    private static List<String> renderLines(List<String> mm) {
        List<String> out = new ArrayList<>(mm.size());
        for (String raw : mm) {
            if (raw == null || raw.isEmpty()) { out.add(""); continue; }
            try {
                Component c = LineFormatter.render(raw, null, 0);
                out.add(LegacyComponentSerializer.legacySection().serialize(c));
            } catch (Throwable t) {
                out.add(raw); // fallback: linea cruda, DH al menos parsea &codigos
            }
        }
        return out;
    }

}
