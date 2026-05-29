package dev.blancocl.npc.action;

import dev.blancocl.api.npc.ClickContext;
import dev.blancocl.api.npc.NpcAction;
import org.bukkit.Bukkit;

import java.util.Map;

/**
 * Action that runs a command on behalf of the clicking player.
 * <p>If {@code asConsole: true}, runs as console; otherwise as the player.</p>
 */
public final class CommandAction implements NpcAction {

    private final String command;
    private final boolean asConsole;

    public CommandAction(String command, boolean asConsole) {
        this.command   = command.startsWith("/") ? command.substring(1) : command;
        this.asConsole = asConsole;
    }

    public static CommandAction fromMap(Map<String, Object> args) {
        return new CommandAction(
                String.valueOf(args.getOrDefault("value", "")),
                Boolean.parseBoolean(String.valueOf(args.getOrDefault("asConsole", "false"))));
    }

    @Override
    public void run(ClickContext ctx) {
        String resolved = command.replace("%player%", ctx.player().getName());
        if (asConsole) Bukkit.dispatchCommand(Bukkit.getConsoleSender(), resolved);
        else ctx.player().performCommand(resolved);
    }
}
