package dev.blancocl.commands.feedback;

import dev.blancocl.config.Messages;
import org.bukkit.command.CommandSender;

import java.util.Map;

/** Thin helper that always routes through {@link Messages} so output stays MiniMessage. */
public final class CommandResponder {

    private final Messages messages;

    public CommandResponder(Messages messages) { this.messages = messages; }

    public void send(CommandSender to, String key) {
        messages.send(to, key, Map.of());
    }

    public void send(CommandSender to, String key, Map<String, String> placeholders) {
        messages.send(to, key, placeholders);
    }
}
