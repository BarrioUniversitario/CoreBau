package dev.blancocl.config;

import dev.blancocl.util.Mini;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;

import java.util.HashMap;
import java.util.Map;

/**
 * Typed view of {@code messages.yml}. Every value is resolved through MiniMessage
 * with the configured prefix exposed as the {@code <prefix>} tag.
 */
public final class Messages {

    private final String prefix;
    private final Map<String, String> raw = new HashMap<>();

    Messages(FileConfiguration c) {
        this.prefix = c.getString("prefix", "");
        flatten("", c.getValues(true));
    }

    public String prefix() { return prefix; }

    /** Raw template (still MiniMessage) for use with custom resolvers. */
    public String raw(String key, String fallback) {
        return raw.getOrDefault(key, fallback);
    }

    public void send(CommandSender to, String key, Map<String, String> placeholders) {
        String template = raw.get(key);
        if (template == null) {
            to.sendMessage("§c[Npc] missing message: " + key);
            return;
        }
        Map<String, String> all = new HashMap<>(placeholders.size() + 1);
        all.putAll(placeholders);
        all.putIfAbsent("prefix", prefix);
        to.sendMessage(Mini.parse(template,
                to instanceof org.bukkit.entity.Player p ? p : null,
                all));
    }

    public void send(CommandSender to, String key) { send(to, key, Map.of()); }

    /** Build a one-shot Placeholder resolver including the prefix tag. */
    public TagResolver prefixResolver() {
        return TagResolver.resolver(Placeholder.parsed("prefix", prefix));
    }

    @SuppressWarnings("unchecked")
    private void flatten(String basePath, Map<String, Object> values) {
        for (var e : values.entrySet()) {
            String path = basePath.isEmpty() ? e.getKey() : basePath + "." + e.getKey();
            Object v = e.getValue();
            if (v instanceof Map<?, ?> child) {
                flatten(path, (Map<String, Object>) child);
            } else if (v != null && !(v instanceof org.bukkit.configuration.ConfigurationSection)) {
                raw.put(path, String.valueOf(v));
            }
        }
    }
}
