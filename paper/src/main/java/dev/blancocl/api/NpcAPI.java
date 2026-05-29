package dev.blancocl.api;

import dev.blancocl.api.manager.HologramManager;
import dev.blancocl.api.manager.NpcManager;
import dev.blancocl.api.manager.SkinManager;
import dev.blancocl.api.manager.VelocityManager;

/**
 * Top-level entry point for third-party plugins.
 *
 * <p>Acquire via Bukkit's {@code ServicesManager}:</p>
 * <pre>{@code
 * NpcAPI api = Bukkit.getServicesManager().load(NpcAPI.class);
 * if (api != null) {
 *     api.npcs().list().forEach(n -> getLogger().info(n.id()));
 * }
 * }</pre>
 *
 * <p>All async operations on the returned managers run off-thread; results are
 * delivered through {@link java.util.concurrent.CompletableFuture}. Callers
 * must not call {@code .join()} on the main thread.</p>
 */
public interface NpcAPI {

    NpcManager       npcs();
    HologramManager  holograms();
    SkinManager      skins();
    VelocityManager  velocity();
}
