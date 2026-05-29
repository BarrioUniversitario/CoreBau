package dev.blancocl.velocity;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.messaging.PluginMessageListener;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;

/**
 * Plugin-message bridge over channel {@code npc:sync}.
 *
 * <p>Core (the Velocity proxy plugin) relays messages on this channel to every
 * backend except the sender. Backends that receive a message reload their
 * NPC set from the persistence backend, picking up changes made elsewhere.</p>
 */
public final class NpcSyncMessenger implements PluginMessageListener {

    public static final String CHANNEL = "npc:sync";
    public static final String ACTION_RELOAD = "reload";

    private final Plugin plugin;
    private final Runnable onRemoteReload;
    private volatile boolean enabled;

    public NpcSyncMessenger(Plugin plugin, Runnable onRemoteReload) {
        this.plugin = plugin;
        this.onRemoteReload = onRemoteReload;
    }

    public void enable() {
        if (enabled) return;
        Bukkit.getMessenger().registerOutgoingPluginChannel(plugin, CHANNEL);
        Bukkit.getMessenger().registerIncomingPluginChannel(plugin, CHANNEL, this);
        enabled = true;
    }

    public void disable() {
        if (!enabled) return;
        Bukkit.getMessenger().unregisterOutgoingPluginChannel(plugin, CHANNEL);
        Bukkit.getMessenger().unregisterIncomingPluginChannel(plugin, CHANNEL, this);
        enabled = false;
    }

    /** Broadcast a reload to other backends. No-op if no players online (need a carrier). */
    public void broadcastReload() {
        if (!enabled) return;
        Player carrier = pickCarrier();
        if (carrier == null) return;
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        try (DataOutputStream out = new DataOutputStream(baos)) {
            out.writeUTF(ACTION_RELOAD);
        } catch (Exception ignored) {}
        carrier.sendPluginMessage(plugin, CHANNEL, baos.toByteArray());
    }

    @Override
    public void onPluginMessageReceived(String channel, Player player, byte[] message) {
        if (!CHANNEL.equals(channel)) return;
        try (DataInputStream in = new DataInputStream(new ByteArrayInputStream(message))) {
            String action = in.readUTF();
            if (ACTION_RELOAD.equals(action)) {
                onRemoteReload.run();
            }
        } catch (Exception e) {
            plugin.getLogger().warning("Failed to read npc:sync message: " + e);
        }
    }

    private Player pickCarrier() {
        for (Player p : Bukkit.getOnlinePlayers()) if (p.isOnline()) return p;
        return null;
    }
}
