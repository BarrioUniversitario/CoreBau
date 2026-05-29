package dev.blancocl.util;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;

/**
 * MiniMessage facade with PlaceholderAPI bridging.
 *
 * <p>Legacy color codes ({@code §c}, {@code &c}) are <b>never</b> parsed —
 * any input string must be MiniMessage or it is rendered literally.</p>
 *
 * <pre>{@code
 * Component c = Mini.parse("<green>Hello <bold><name>", player, Map.of("name", player.getName()));
 * Mini.send(player, "<prefix><gray>Hi", Map.of());
 * }</pre>
 */
public final class Mini {

    private static final MiniMessage MM = MiniMessage.miniMessage();
    private static volatile boolean papiPresent =
            Bukkit.getPluginManager().getPlugin("PlaceholderAPI") != null;

    public static String toLegacy(String miniMessage) {
        if (miniMessage == null || miniMessage.isEmpty()) return "";
        Component c = MM.deserialize(miniMessage);
        return net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer.legacySection().serialize(c);
    }

    private Mini() {}

    /** Re-detect PlaceholderAPI (call after plugin reload events). */
    public static void refreshIntegrations() {
        papiPresent = Bukkit.getPluginManager().getPlugin("PlaceholderAPI") != null;
    }

    public static Component parse(String input) {
        return parse(input, null, Map.of());
    }

    public static Component parse(String input, @Nullable Player papiPlayer) {
        return parse(input, papiPlayer, Map.of());
    }

    public static Component parse(String input, @Nullable Player papiPlayer, Map<String, String> placeholders) {
        if (input == null || input.isEmpty()) return Component.empty();
        // PAPI also resolves server-scoped placeholders (%server_name%, %server_online%, ...)
        // when the player is null. Player-scoped placeholders just stay literal in that case.
        String processed = papiPresent
                ? PapiBridge.apply(papiPlayer, input)
                : input;
        if (placeholders.isEmpty()) return MM.deserialize(processed);

        TagResolver[] resolvers = new TagResolver[placeholders.size()];
        int i = 0;
        for (Map.Entry<String, String> e : placeholders.entrySet()) {
            resolvers[i++] = Placeholder.parsed(e.getKey(), e.getValue() == null ? "" : e.getValue());
        }
        return MM.deserialize(processed, TagResolver.resolver(resolvers));
    }

    public static void send(Player player, String input, Map<String, String> placeholders) {
        player.sendMessage(parse(input, player, placeholders));
    }

    public static void send(Player player, String input) {
        player.sendMessage(parse(input, player, Map.of()));
    }

    public static void actionBar(Player player, String input) {
        player.sendActionBar(parse(input, player, Map.of()));
    }

    public static Map<String, String> ph(String k1, String v1) {
        Map<String, String> m = new HashMap<>(2);
        m.put(k1, v1);
        return m;
    }

    public static Map<String, String> ph(String k1, String v1, String k2, String v2) {
        Map<String, String> m = new HashMap<>(4);
        m.put(k1, v1); m.put(k2, v2);
        return m;
    }

    /** Isolated bridge so a missing PAPI class never crashes the calling site. */
    private static final class PapiBridge {
        static String apply(@Nullable Player p, String s) {
            try {
                // PAPI.setPlaceholders accepts OfflinePlayer = null and still resolves
                // server-scoped placeholders. Player-specific ones just stay literal.
                return me.clip.placeholderapi.PlaceholderAPI.setPlaceholders(p, s);
            } catch (Throwable t) {
                return s;
            }
        }
    }
}
