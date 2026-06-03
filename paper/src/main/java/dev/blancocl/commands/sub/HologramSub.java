package dev.blancocl.commands.sub;

import dev.blancocl.ServiceContainer;
import dev.blancocl.api.hologram.Hologram;
import dev.blancocl.api.hologram.HologramLine;
import dev.blancocl.api.npc.Npc;
import dev.blancocl.commands.SubCommand;
import dev.blancocl.commands.feedback.CommandResponder;
import org.bukkit.command.CommandSender;

import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/**
 * NPC hologram management.
 *
 * <p>Operates on the NPC's first hologram (which is what {@code add} creates if none
 * exists). Future multi-hologram support would extend this by accepting a hologram
 * index, but the single-hologram model covers every observed lobby use case.</p>
 *
 * <h3>Actions</h3>
 * <pre>
 *   add &lt;line…&gt;                  append a new line
 *   set &lt;index&gt; &lt;line…&gt;          replace the line at index (edit-in-place)
 *   insert &lt;index&gt; &lt;line…&gt;       insert a line; existing lines shift down
 *   remove &lt;index&gt;               delete a line; deletes the hologram if it was the last
 *   swap &lt;i&gt; &lt;j&gt;                 swap two lines
 *   list                         show all lines with click-to-edit
 *   clear                        remove the whole hologram
 *   up | down [step]             shift the whole hologram vertically (default step 0.25)
 *   offset &lt;value&gt;               set the absolute Y offset above the NPC
 *   spacing &lt;value&gt;              set the vertical spacing between lines
 * </pre>
 */
public final class HologramSub implements SubCommand {

    private final ServiceContainer services;
    private final CommandResponder responder;

    public HologramSub(ServiceContainer services, CommandResponder responder) {
        this.services = services; this.responder = responder;
    }

    @Override public String name()        { return "hologram"; }
    @Override public String permission()  { return "npc.command.hologram"; }
    @Override public String usage()       { return "/npc hologram <id> <add|set|insert|remove|swap|list|clear|up|down|offset|spacing> [args]"; }
    @Override public String description() { return "Manages an NPC's holograms."; }

    private static final double DEFAULT_STEP = 0.25;
    private static final double DEFAULT_OFFSET_Y = 2.3;

    @Override
    public boolean execute(CommandSender sender, String[] args) {
        if (args.length < 2) return false;
        String id = args[0];
        String action = args[1].toLowerCase(Locale.ROOT);

        Optional<Npc> opt = services.npcs().get(id);
        if (opt.isEmpty()) {
            responder.send(sender, "general.unknown-npc", Map.of("id", id));
            return true;
        }
        Npc npc = opt.get();

        return switch (action) {
            case "add"                            -> doAdd(sender, id, npc, args);
            case "set", "edit"                    -> doSet(sender, id, npc, args);
            case "insert", "ins"                  -> doInsert(sender, id, npc, args);
            case "remove", "delete", "del", "rm"  -> doRemove(sender, id, npc, args);
            case "swap"                           -> doSwap(sender, id, npc, args);
            case "list", "ls", "show"             -> doList(sender, id, npc);
            case "clear"                          -> doClear(sender, id, npc);
            case "up", "down"                     -> doShift(sender, id, npc, args, action);
            case "offset"                         -> doOffset(sender, id, npc, args);
            case "spacing"                        -> doSpacing(sender, id, npc, args);
            default -> false;
        };
    }

    /* ============================================================
     *  Actions
     * ============================================================ */

    private boolean doAdd(CommandSender sender, String id, Npc npc, String[] args) {
        if (args.length < 3) return false;
        String line = joinFrom(args, 2);
        if (npc.holograms().isEmpty()) {
            services.holograms().attach(npc, DEFAULT_OFFSET_Y, List.of(line));
        } else {
            npc.holograms().get(0).addLine(line);
        }
        services.npcs().save();
        responder.send(sender, "command.hologram-added", Map.of("id", id));
        return true;
    }

    private boolean doSet(CommandSender sender, String id, Npc npc, String[] args) {
        if (args.length < 4) return false;
        Optional<Hologram> h = firstHologramOrFeedback(sender, id, npc);
        if (h.isEmpty()) return true;

        Optional<Integer> idx = parseIndex(sender, args[2], h.get());
        if (idx.isEmpty()) return true;

        String line = joinFrom(args, 3);
        h.get().setLine(idx.get(), line);
        services.npcs().save();
        responder.send(sender, "command.hologram-line-set",
                Map.of("id", id, "line", String.valueOf(idx.get())));
        return true;
    }

    private boolean doInsert(CommandSender sender, String id, Npc npc, String[] args) {
        if (args.length < 4) return false;
        Optional<Hologram> h = firstHologramOrFeedback(sender, id, npc);
        if (h.isEmpty()) return true;

        int max = h.get().lineCount(); // insert allows index == size (append)
        int idx;
        try { idx = Integer.parseInt(args[2]); }
        catch (NumberFormatException ex) {
            responder.send(sender, "command.hologram-invalid-line", Map.of("line", args[2]));
            return true;
        }
        if (idx < 0 || idx > max) {
            responder.send(sender, "command.hologram-line-out-of-range",
                    Map.of("line", String.valueOf(idx), "count", String.valueOf(max)));
            return true;
        }

        String line = joinFrom(args, 3);
        h.get().insertLine(idx, line);
        services.npcs().save();
        responder.send(sender, "command.hologram-line-inserted",
                Map.of("id", id, "line", String.valueOf(idx)));
        return true;
    }

    private boolean doRemove(CommandSender sender, String id, Npc npc, String[] args) {
        if (args.length < 3) return false;
        Optional<Hologram> h = firstHologramOrFeedback(sender, id, npc);
        if (h.isEmpty()) return true;

        Optional<Integer> idx = parseIndex(sender, args[2], h.get());
        if (idx.isEmpty()) return true;

        // Removing the last line removes the hologram entirely so the NPC doesn't
        // keep a zero-line floating ghost.
        if (h.get().lineCount() == 1) {
            services.holograms().detach(h.get());
        } else {
            h.get().removeLine(idx.get());
        }
        services.npcs().save();
        responder.send(sender, "command.hologram-line-removed",
                Map.of("id", id, "line", String.valueOf(idx.get())));
        return true;
    }

    private boolean doSwap(CommandSender sender, String id, Npc npc, String[] args) {
        if (args.length < 4) return false;
        Optional<Hologram> h = firstHologramOrFeedback(sender, id, npc);
        if (h.isEmpty()) return true;

        Optional<Integer> i = parseIndex(sender, args[2], h.get());
        if (i.isEmpty()) return true;
        Optional<Integer> j = parseIndex(sender, args[3], h.get());
        if (j.isEmpty()) return true;

        h.get().swapLines(i.get(), j.get());
        services.npcs().save();
        responder.send(sender, "command.hologram-line-swapped",
                Map.of("id", id, "i", String.valueOf(i.get()), "j", String.valueOf(j.get())));
        return true;
    }

    private boolean doList(CommandSender sender, String id, Npc npc) {
        if (npc.holograms().isEmpty()) {
            responder.send(sender, "command.hologram-no-lines", Map.of("id", id));
            return true;
        }
        Hologram h = npc.holograms().get(0);
        List<HologramLine> lines = h.lines();
        responder.send(sender, "command.hologram-list-header", Map.of(
                "id", id,
                "count", String.valueOf(lines.size()),
                "offset", fmt(h.offsetY()),
                "spacing", fmt(h.lineSpacing())));
        for (int i = 0; i < lines.size(); i++) {
            String mm = lines.get(i).miniMessage();
            responder.send(sender, "command.hologram-list-line", Map.of(
                    "id", id,
                    "index", String.valueOf(i),
                    // Parsed as MiniMessage → rendered preview of the line.
                    "preview", mm));
        }
        return true;
    }

    private boolean doClear(CommandSender sender, String id, Npc npc) {
        services.holograms().detachAll(npc);
        services.npcs().save();
        responder.send(sender, "command.hologram-cleared", Map.of("id", id));
        return true;
    }

    private boolean doShift(CommandSender sender, String id, Npc npc, String[] args, String action) {
        Optional<Hologram> h = firstHologramOrFeedback(sender, id, npc);
        if (h.isEmpty()) return true;

        double step = DEFAULT_STEP;
        if (args.length >= 3) {
            try { step = Double.parseDouble(args[2]); }
            catch (NumberFormatException e) {
                responder.send(sender, "command.hologram-invalid-line", Map.of("line", args[2]));
                return true;
            }
        }
        double delta = "up".equals(action) ? step : -step;
        double newOffset = h.get().offsetY() + delta;
        h.get().setOffsetY(newOffset);
        services.npcs().save();
        responder.send(sender, "command.hologram-moved", Map.of(
                "id", id,
                "direction", action,
                "offset", fmt(newOffset)));
        return true;
    }

    private boolean doOffset(CommandSender sender, String id, Npc npc, String[] args) {
        if (args.length < 3) return false;
        Optional<Hologram> h = firstHologramOrFeedback(sender, id, npc);
        if (h.isEmpty()) return true;

        double value;
        try { value = Double.parseDouble(args[2]); }
        catch (NumberFormatException e) {
            responder.send(sender, "command.hologram-invalid-line", Map.of("line", args[2]));
            return true;
        }
        h.get().setOffsetY(value);
        services.npcs().save();
        responder.send(sender, "command.hologram-offset-set",
                Map.of("id", id, "offset", fmt(value)));
        return true;
    }

    private boolean doSpacing(CommandSender sender, String id, Npc npc, String[] args) {
        if (args.length < 3) return false;
        Optional<Hologram> h = firstHologramOrFeedback(sender, id, npc);
        if (h.isEmpty()) return true;

        double value;
        try { value = Double.parseDouble(args[2]); }
        catch (NumberFormatException e) {
            responder.send(sender, "command.hologram-invalid-line", Map.of("line", args[2]));
            return true;
        }
        if (value <= 0) {
            responder.send(sender, "command.hologram-invalid-line", Map.of("line", args[2]));
            return true;
        }
        h.get().setLineSpacing(value);
        services.npcs().save();
        responder.send(sender, "command.hologram-spacing-set",
                Map.of("id", id, "spacing", fmt(value)));
        return true;
    }

    /* ============================================================
     *  Helpers
     * ============================================================ */

    private Optional<Hologram> firstHologramOrFeedback(CommandSender sender, String id, Npc npc) {
        if (npc.holograms().isEmpty()) {
            responder.send(sender, "command.hologram-no-lines", Map.of("id", id));
            return Optional.empty();
        }
        return Optional.of(npc.holograms().get(0));
    }

    private Optional<Integer> parseIndex(CommandSender sender, String raw, Hologram h) {
        int idx;
        try { idx = Integer.parseInt(raw); }
        catch (NumberFormatException ex) {
            responder.send(sender, "command.hologram-invalid-line", Map.of("line", raw));
            return Optional.empty();
        }
        if (idx < 0 || idx >= h.lineCount()) {
            responder.send(sender, "command.hologram-line-out-of-range",
                    Map.of("line", String.valueOf(idx), "count", String.valueOf(h.lineCount())));
            return Optional.empty();
        }
        return Optional.of(idx);
    }

    private static String joinFrom(String[] args, int from) {
        return String.join(" ", Arrays.copyOfRange(args, from, args.length));
    }

    private static String fmt(double v) {
        return String.format(Locale.ROOT, "%.2f", v);
    }

    /* ============================================================
     *  Tab completion
     * ============================================================ */

    private static final List<String> ACTIONS = List.of(
            "add", "set", "insert", "remove", "swap",
            "list", "clear", "up", "down", "offset", "spacing");

    @Override
    public List<String> tabComplete(CommandSender sender, String[] args) {
        if (args.length == 1) {
            String partial = args[0].toLowerCase(Locale.ROOT);
            return services.npcs().registry().all().stream()
                    .map(n -> n.id())
                    .filter(n -> n.toLowerCase(Locale.ROOT).startsWith(partial))
                    .collect(Collectors.toList());
        }
        if (args.length == 2) {
            String partial = args[1].toLowerCase(Locale.ROOT);
            return ACTIONS.stream()
                    .filter(a -> a.startsWith(partial))
                    .collect(Collectors.toList());
        }

        String action = args[1].toLowerCase(Locale.ROOT);
        Optional<Npc> opt = services.npcs().get(args[0]);
        if (opt.isEmpty()) return List.of();

        // 3rd-arg index suggestions for actions that need a line index.
        if (args.length == 3) {
            return switch (action) {
                case "set", "edit", "remove", "delete", "del", "rm", "swap"
                        -> existingIndexes(opt.get());
                case "insert", "ins" -> insertIndexes(opt.get());
                default -> List.of();
            };
        }
        if (args.length == 4 && "swap".equals(action)) {
            return existingIndexes(opt.get());
        }
        return List.of();
    }

    private static List<String> existingIndexes(Npc npc) {
        if (npc.holograms().isEmpty()) return List.of();
        int size = npc.holograms().get(0).lineCount();
        return IntStream.range(0, size).mapToObj(String::valueOf).toList();
    }

    private static List<String> insertIndexes(Npc npc) {
        if (npc.holograms().isEmpty()) return List.of("0");
        int size = npc.holograms().get(0).lineCount();
        return IntStream.rangeClosed(0, size).mapToObj(String::valueOf).toList();
    }
}
