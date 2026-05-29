package cl.xgamers.selector.velocity;

import cl.xgamers.selector.Selector;
import com.google.common.io.ByteArrayDataOutput;
import com.google.common.io.ByteStreams;
import org.bukkit.entity.Player;
import org.bukkit.plugin.messaging.PluginMessageListener;

import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;

/**
 * Puente Paper ↔ Velocity por plugin messaging.
 *
 * <p>Canal: {@value #CHANNEL}</p>
 * <table>
 *   <tr><th>Subcanal</th><th>Dirección</th><th>Payload</th></tr>
 *   <tr><td>PlayerCountAll</td><td>Paper → Proxy</td><td>(vacío)</td></tr>
 *   <tr><td>PlayerCount</td><td>Proxy → Paper</td><td>UTF server, int count</td></tr>
 *   <tr><td>PlayerCountAll</td><td>Proxy → Paper</td><td>repetido: UTF server, int count</td></tr>
 *   <tr><td>Connect</td><td>Paper → Proxy</td><td>UTF serverName</td></tr>
 * </table>
 */
public class VelocityBridge implements PluginMessageListener {

    public static final String CHANNEL = "serverconnector:main";

    private final Selector plugin;
    private final Map<String, Integer> playerCounts = new ConcurrentHashMap<>();
    private Consumer<CountUpdateEvent> onCountsUpdated = event -> {};

    public VelocityBridge(Selector plugin) {
        this.plugin = plugin;
    }

    public void register() {
        plugin.getServer().getMessenger().registerOutgoingPluginChannel(plugin.getPlugin(), CHANNEL);
        plugin.getServer().getMessenger().registerIncomingPluginChannel(plugin.getPlugin(), CHANNEL, this);
    }

    public void unregister() {
        plugin.getServer().getMessenger().unregisterOutgoingPluginChannel(plugin.getPlugin());
        plugin.getServer().getMessenger().unregisterIncomingPluginChannel(plugin.getPlugin(), CHANNEL, this);
    }

    public void setOnCountsUpdated(Consumer<CountUpdateEvent> listener) {
        this.onCountsUpdated = listener != null ? listener : event -> {};
    }

    public Map<String, Integer> getPlayerCounts() {
        return playerCounts;
    }

    public int getCount(String serverName) {
        return playerCounts.getOrDefault(serverName.toLowerCase(), 0);
    }

    public boolean isOnline(String serverName) {
        return playerCounts.containsKey(serverName.toLowerCase());
    }

    public void clearCounts() {
        playerCounts.clear();
    }

    public void requestAllCounts(Player player) {
        ByteArrayDataOutput out = ByteStreams.newDataOutput();
        out.writeUTF("PlayerCountAll");
        player.sendPluginMessage(plugin.getPlugin(), CHANNEL, out.toByteArray());
    }

    public void connect(Player player, String serverName) {
        ByteArrayDataOutput out = ByteStreams.newDataOutput();
        out.writeUTF("Connect");
        out.writeUTF(serverName);
        player.sendPluginMessage(plugin.getPlugin(), CHANNEL, out.toByteArray());
    }

    @Override
    public void onPluginMessageReceived(String channel, Player player, byte[] message) {
        if (!CHANNEL.equals(channel)) {
            return;
        }

        try {
            DataInputStream in = new DataInputStream(new ByteArrayInputStream(message));
            String subChannel = in.readUTF();

            if ("PlayerCount".equals(subChannel)) {
                String server = in.readUTF();
                int count = in.readInt();
                playerCounts.put(server.toLowerCase(), count);
                onCountsUpdated.accept(new CountUpdateEvent(player, false));
            } else if ("PlayerCountAll".equals(subChannel)) {
                while (in.available() > 0) {
                    playerCounts.put(in.readUTF().toLowerCase(), in.readInt());
                }
                onCountsUpdated.accept(new CountUpdateEvent(player, true));
            }
        } catch (Exception e) {
            plugin.getLogger().severe("Error procesando mensaje Velocity: " + e.getMessage());
        }
    }

    public record CountUpdateEvent(Player triggerPlayer, boolean fullSnapshot) {
    }
}
