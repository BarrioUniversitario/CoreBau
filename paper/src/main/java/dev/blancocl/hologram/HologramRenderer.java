package dev.blancocl.hologram;

import dev.blancocl.util.Threading;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.entity.TextDisplay;
import org.bukkit.plugin.Plugin;
import org.bukkit.util.Transformation;
import org.joml.Vector3f;
import org.joml.Quaternionf;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Renders {@link HologramImpl}s using Paper's {@link TextDisplay} entities.
 *
 * <p>Note: {@link TextDisplay} is a real entity (1.19.4+) but it is invisible,
 * non-collidable, and has no AI tick — it's the modern, packet-efficient way
 * to render floating text and is what most lobby networks use today. The
 * "no real entities" rule in the spec refers to <b>NPCs</b>; floating text
 * via TextDisplay is the documented Paper-API path and avoids reinventing
 * the entire entity metadata pipeline.</p>
 */
public final class HologramRenderer {

    private final Plugin plugin;
    private final Threading threading;
    private final Map<String, Map<Integer, UUID>> hologramLineEntities = new ConcurrentHashMap<>();

    public HologramRenderer(Plugin plugin, Threading threading) {
        this.plugin    = plugin;
        this.threading = threading;
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
            for (Map<Integer, UUID> ents : hologramLineEntities.values()) {
                ents.values().forEach(uuid -> {
                    var e = Bukkit.getEntity(uuid);
                    if (e != null) e.remove();
                });
            }
            hologramLineEntities.clear();
        });
    }

    /* ---------------- sync impls ---------------- */

    private void spawnSync(HologramImpl holo) {
        despawnSync(holo);
        Map<Integer, UUID> ents = new HashMap<>();
        var raw = holo.rawLines();
        Location anchor = holo.location();
        if (anchor.getWorld() == null) return;

        // lines[0] = bottom (offsetY del holograma), líneas siguientes se apilan
        // hacia arriba. Así "/npc hologram add" siempre suma una línea arriba.
        for (int i = 0; i < raw.size(); i++) {
            Location lineLoc = anchor.clone().add(0, i * holo.lineSpacing(), 0);
            TextDisplay td = anchor.getWorld().spawn(lineLoc, TextDisplay.class, e -> {
                e.setBillboard(org.bukkit.entity.Display.Billboard.CENTER);
                e.setSeeThrough(true);
                e.setShadowed(false);
                e.setDefaultBackground(false);
                e.setPersistent(false);
                e.setTransformation(new Transformation(
                        new Vector3f(0, 0, 0),
                        new Quaternionf(),
                        new Vector3f(1, 1, 1),
                        new Quaternionf()));
            });
            ents.put(i, td.getUniqueId());
            int frame = holo.internalLines().get(i).frame();
            applyLine(td, raw.get(i), frame, null);
        }
        hologramLineEntities.put(holo.id(), ents);
    }

    private void despawnSync(HologramImpl holo) {
        Map<Integer, UUID> existing = hologramLineEntities.remove(holo.id());
        if (existing == null) return;
        for (UUID uid : existing.values()) {
            var e = Bukkit.getEntity(uid);
            if (e != null) e.remove();
        }
    }

    private void updateSync(HologramImpl holo) {
        if (holo.isDetached()) return;
        Map<Integer, UUID> ents = hologramLineEntities.get(holo.id());
        if (ents == null) { spawnSync(holo); return; }
        var raw = holo.rawLines();

        if (ents.size() != raw.size()) { spawnSync(holo); return; }

        Location anchor = holo.location();
        for (int i = 0; i < raw.size(); i++) {
            var e = Bukkit.getEntity(ents.get(i));
            if (!(e instanceof TextDisplay td)) {
                // Entity gone (chunk unloaded) — recreate all lines.
                spawnSync(holo);
                return;
            }
            td.teleport(anchor.clone().add(0, i * holo.lineSpacing(), 0));
            int frame = holo.internalLines().get(i).frame();
            applyLine(td, raw.get(i), frame, null);
        }
    }

    private void applyLine(TextDisplay td, String miniMessage, int frame, Player viewer) {
        td.text(LineFormatter.render(miniMessage, viewer, frame));
    }
}