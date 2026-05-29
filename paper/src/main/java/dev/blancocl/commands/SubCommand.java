package dev.blancocl.commands;

import org.bukkit.command.CommandSender;

import java.util.Collections;
import java.util.List;

/** Single {@code /npc <sub>} subcommand. Implementations are stateless. */
public interface SubCommand {

    /** Name as typed by the user (lowercase). */
    String name();

    /** Permission required, e.g. {@code npc.command.create}. */
    String permission();

    /** Single-line MiniMessage usage description, e.g. {@code /npc create <id> <type>}. */
    String usage();

    /** MiniMessage tooltip / description shown in help. */
    String description();

    /**
     * Execute. {@code args} excludes the subcommand name itself.
     * Return {@code true} if handled (regardless of success).
     */
    boolean execute(CommandSender sender, String[] args);

    /** Tab completion for {@code args[args.length - 1]}. */
    default List<String> tabComplete(CommandSender sender, String[] args) { return Collections.emptyList(); }
}
