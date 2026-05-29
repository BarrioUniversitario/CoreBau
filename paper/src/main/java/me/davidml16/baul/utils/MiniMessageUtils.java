package me.davidml16.baul.utils;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.ChatColor;

/**
 * Utility class for handling MiniMessage formatting.
 *
 * Mixed input (legacy {@code &} codes + MiniMessage tags) is supported: legacy
 * codes are converted to MiniMessage tags first, then the whole string is
 * parsed by MiniMessage. This lets callers safely concatenate language-file
 * prefixes (which use {@code &6}-style codes) with hand-written {@code <red>}
 * tags without the tags rendering literally in chat.
 */
public class MiniMessageUtils {

    private static final MiniMessage miniMessage = MiniMessage.miniMessage();
    private static final LegacyComponentSerializer LEGACY = LegacyComponentSerializer.builder()
            .hexColors()
            .useUnusualXRepeatedCharacterHexFormat()
            .build();

    // Pre-process &X → <tag> so a mixed input (legacy prefix + MiniMessage body)
    // parses as a single MiniMessage string. Avoids the "literal <red>" bug in chat.
    private static final String[][] LEGACY_TO_MM = {
            {"&0", "<black>"}, {"&1", "<dark_blue>"}, {"&2", "<dark_green>"},
            {"&3", "<dark_aqua>"}, {"&4", "<dark_red>"}, {"&5", "<dark_purple>"},
            {"&6", "<gold>"}, {"&7", "<gray>"}, {"&8", "<dark_gray>"},
            {"&9", "<blue>"}, {"&a", "<green>"}, {"&b", "<aqua>"},
            {"&c", "<red>"}, {"&d", "<light_purple>"}, {"&e", "<yellow>"},
            {"&f", "<white>"}, {"&l", "<bold>"}, {"&m", "<strikethrough>"},
            {"&n", "<underlined>"}, {"&o", "<italic>"}, {"&k", "<obfuscated>"},
            {"&r", "<reset>"}
    };

    private static String legacyToMiniMessage(String message) {
        for (String[] pair : LEGACY_TO_MM) message = message.replace(pair[0], pair[1]);
        return message;
    }

    /**
     * Formats a (possibly mixed) MiniMessage + legacy string into a legacy
     * section-coded string suitable for {@code Player#sendMessage(String)}.
     */
    public static String format(String message) {
        if (message == null || message.isEmpty()) return "";
        try {
            return LEGACY.serialize(miniMessage.deserialize(legacyToMiniMessage(message)));
        } catch (Exception e) {
            // Last-resort fallback: at least render legacy & codes so chat isn't full of tags.
            return ChatColor.translateAlternateColorCodes('&', message);
        }
    }

    /**
     * Parses a MiniMessage string into an Adventure Component
     * @param message The MiniMessage formatted string
     * @return The parsed Adventure Component
     */
    public static Component parse(String message) {
        if (message == null || message.isEmpty()) return Component.empty();
        try {
            return miniMessage.deserialize(message);
        } catch (Exception e) {
            return Component.text(message);
        }
    }

    /**
     * Serializes an Adventure Component to legacy section format
     * @param component The Adventure Component
     * @return The legacy section format string
     */
    public static String serialize(Component component) {
        if (component == null) return "";
        return LEGACY.serialize(component);
    }
}