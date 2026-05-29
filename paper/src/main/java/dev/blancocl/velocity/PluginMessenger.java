package dev.blancocl.velocity;

import dev.blancocl.config.PluginConfig;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.messaging.PluginMessageListener;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.TimeUnit;

/**
 * Two-way bridge over the {@code BungeeCord} plugin-messaging channel.
 * Velocity speaks this protocol natively when modern forwarding is on, so
 * there is no extra Velocity-side plugin to deploy for the basic flows.
 */
public final class PluginMessenger implements PluginMessageListener {

    private final Plugin plugin;
    private final PluginConfig cfg;

    // Pending request queues. We pop on response (FIFO) — Velocity replies
    // in the order requests arrive on a single backend, which is sufficient
    // for these subchannels.
    private final ConcurrentLinkedQueue<CompletableFuture<Integer>>       pendingPlayerCount = new ConcurrentLinkedQueue<>();
    private final ConcurrentLinkedQueue<CompletableFuture<List<String>>>  pendingGetServers  = new ConcurrentLinkedQueue<>();

    private volatile long lastResponseAtMillis;

    public PluginMessenger(Plugin plugin, PluginConfig cfg) {
        this.plugin = plugin;
        this.cfg = cfg;
    }

    public void enable() {
        if (!cfg.velocityEnabled()) return;
        Bukkit.getMessenger().registerOutgoingPluginChannel(plugin, cfg.velocityChannel());
        Bukkit.getMessenger().registerIncomingPluginChannel(plugin, cfg.velocityChannel(), this);
    }

    public void disable() {
        Bukkit.getMessenger().unregisterOutgoingPluginChannel(plugin, cfg.velocityChannel());
        Bukkit.getMessenger().unregisterIncomingPluginChannel(plugin, cfg.velocityChannel(), this);

        // Resolve all pending futures so callers don't leak.
        drain(pendingPlayerCount, -1);
        drain(pendingGetServers, List.of());
    }

    public boolean proxyDetected() {
        return lastResponseAtMillis > 0
            && (System.currentTimeMillis() - lastResponseAtMillis) < 60_000L;
    }

    /* ----------------- send ----------------- */

    public void connect(Player carrier, String server) {
        if (!cfg.velocityEnabled()) {
            plugin.getLogger().warning("Connect requested but velocity.enabled=false (server=" + server + ")");
            return;
        }
        if (carrier == null || !carrier.isOnline() || server == null || server.isEmpty()) return;
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        try (DataOutputStream out = new DataOutputStream(baos)) {
            out.writeUTF(VelocityProtocol.SUB_CONNECT);
            out.writeUTF(server);
        } catch (IOException e) {
            plugin.getLogger().warning("Failed to encode Connect packet: " + e);
            return;
        }
        carrier.sendPluginMessage(plugin, cfg.velocityChannel(), baos.toByteArray());
        if (cfg.debugEnabled()) {
            plugin.getLogger().info("[proxy] Connect → " + server + " for " + carrier.getName()
                    + " (channel=" + cfg.velocityChannel() + ")");
        }
    }

    public CompletableFuture<Integer> playerCount(String server) {
        Player carrier = pickCarrier();
        CompletableFuture<Integer> cf = new CompletableFuture<>();
        if (carrier == null) { cf.complete(-1); return cf; }
        pendingPlayerCount.add(cf);
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        try (DataOutputStream out = new DataOutputStream(baos)) {
            out.writeUTF(VelocityProtocol.SUB_PLAYER_COUNT);
            out.writeUTF(server);
        } catch (IOException ignored) {}
        carrier.sendPluginMessage(plugin, cfg.velocityChannel(), baos.toByteArray());
        return cf.orTimeout(5, TimeUnit.SECONDS).exceptionally(ex -> -1);
    }

    public CompletableFuture<List<String>> servers() {
        Player carrier = pickCarrier();
        CompletableFuture<List<String>> cf = new CompletableFuture<>();
        if (carrier == null) { cf.complete(List.of()); return cf; }
        pendingGetServers.add(cf);
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        try (DataOutputStream out = new DataOutputStream(baos)) {
            out.writeUTF(VelocityProtocol.SUB_GET_SERVERS);
        } catch (IOException ignored) {}
        carrier.sendPluginMessage(plugin, cfg.velocityChannel(), baos.toByteArray());
        return cf.orTimeout(5, TimeUnit.SECONDS).exceptionally(ex -> List.of());
    }

    /* ----------------- receive ----------------- */

    @Override
    public void onPluginMessageReceived(String channel, Player player, byte[] message) {
        if (!cfg.velocityChannel().equals(channel)) return;
        lastResponseAtMillis = System.currentTimeMillis();
        try (DataInputStream in = new DataInputStream(new ByteArrayInputStream(message))) {
            String sub = in.readUTF();
            switch (sub) {
                case VelocityProtocol.SUB_PLAYER_COUNT -> {
                    in.readUTF(); // server name (echoed)
                    int count = in.readInt();
                    var cf = pendingPlayerCount.poll();
                    if (cf != null) cf.complete(count);
                }
                case VelocityProtocol.SUB_GET_SERVERS -> {
                    String csv = in.readUTF();
                    var list = new ArrayList<String>();
                    for (String s : csv.split(", ")) if (!s.isEmpty()) list.add(s);
                    var cf = pendingGetServers.poll();
                    if (cf != null) cf.complete(list);
                }
                default -> { /* ignore other subchannels */ }
            }
        } catch (IOException ignored) {}
    }

    private Player pickCarrier() {
        for (Player p : Bukkit.getOnlinePlayers()) if (p.isOnline()) return p;
        return null;
    }

    private static <T> void drain(ConcurrentLinkedQueue<CompletableFuture<T>> q, T value) {
        CompletableFuture<T> cf;
        while ((cf = q.poll()) != null) cf.complete(value);
    }
}
