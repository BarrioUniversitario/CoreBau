package cl.xgamers.selector.utils;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.entity.Player;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public final class Colorize {

    private static final MiniMessage MINI_MESSAGE = MiniMessage.miniMessage();
    private static final LegacyComponentSerializer LEGACY = LegacyComponentSerializer.legacySection();

    private Colorize() {
    }

    public static Component parseMessage(String message) {
        if (message == null || message.isEmpty()) {
            return Component.empty();
        }
        return MINI_MESSAGE.deserialize(message);
    }

    /** Convierte MiniMessage a string legacy para ítems/inventarios (FastInv). */
    public static String format(String message) {
        if (message == null || message.isEmpty()) {
            return "";
        }
        return LEGACY.serialize(parseMessage(message));
    }

    /**
     * Resuelve placeholders y parsea la línea completa como MiniMessage en un solo paso,
     * para que los tags de color envuelvan correctamente los valores insertados.
     */
    public static String formatTemplate(String template, Map<String, String> placeholders) {
        if (template == null || template.isEmpty()) {
            return "";
        }
        String merged = template;
        if (placeholders != null) {
            for (Map.Entry<String, String> entry : placeholders.entrySet()) {
                String value = entry.getValue() == null ? "" : entry.getValue();
                merged = merged.replace("{" + entry.getKey() + "}", value);
            }
        }
        return format(merged);
    }

    public static List<String> formatList(List<String> lines) {
        return lines.stream().map(Colorize::format).collect(Collectors.toList());
    }

    public static void actionBar(Player player, String message) {
        player.sendActionBar(parseMessage(message));
    }

    public static void tell(Player player, String... messages) {
        for (String message : messages) {
            player.sendMessage(parseMessage(message));
        }
    }

    public static void send(Player player, String message) {
        player.sendMessage(parseMessage(message));
    }
}
