package dev.blancocl.api.hologram;

import org.bukkit.Location;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;

/** A multi-line floating text rendered with MiniMessage. */
public interface Hologram {

    /** Stable id within the parent NPC (or globally for standalone holograms). */
    String id();

    Location location();

    double offsetY();
    void   setOffsetY(double y);

    double lineSpacing();
    void   setLineSpacing(double spacing);

    List<HologramLine> lines();
    void               setLines(List<String> miniMessage);

    /** Push a fresh frame to all viewers in range, re-evaluating PAPI. */
    void refresh();

    /** Visibility override — return false to hide for {@code viewer} regardless of distance. */
    default boolean visibleTo(Player viewer) { return true; }

    /* ------------ Convenience editors ------------ */
    /** Number of lines currently in the hologram. */
    default int lineCount() { return lines().size(); }

    /** Append a MiniMessage line at the end. */
    default void addLine(String miniMessage) {
        List<String> next = mutableRaw();
        next.add(miniMessage);
        setLines(next);
    }

    /** Insert a MiniMessage line at {@code index}; shifts the rest down. */
    default void insertLine(int index, String miniMessage) {
        List<String> next = mutableRaw();
        if (index < 0 || index > next.size())
            throw new IndexOutOfBoundsException("index " + index + " out of [0," + next.size() + "]");
        next.add(index, miniMessage);
        setLines(next);
    }

    /** Replace the line at {@code index} with {@code miniMessage}. */
    default void setLine(int index, String miniMessage) {
        List<String> next = mutableRaw();
        if (index < 0 || index >= next.size())
            throw new IndexOutOfBoundsException("index " + index + " out of [0," + (next.size() - 1) + "]");
        next.set(index, miniMessage);
        setLines(next);
    }

    /** Remove the line at {@code index}; shifts the rest up. */
    default void removeLine(int index) {
        List<String> next = mutableRaw();
        if (index < 0 || index >= next.size())
            throw new IndexOutOfBoundsException("index " + index + " out of [0," + (next.size() - 1) + "]");
        next.remove(index);
        setLines(next);
    }

    /** Swap the lines at indices {@code i} and {@code j}. */
    default void swapLines(int i, int j) {
        List<String> next = mutableRaw();
        if (i < 0 || j < 0 || i >= next.size() || j >= next.size())
            throw new IndexOutOfBoundsException("indices " + i + "," + j
                    + " out of [0," + (next.size() - 1) + "]");
        if (i == j) return;
        String tmp = next.get(i);
        next.set(i, next.get(j));
        next.set(j, tmp);
        setLines(next);
    }

    private List<String> mutableRaw() {
        List<HologramLine> current = lines();
        List<String> out = new ArrayList<>(current.size());
        for (HologramLine l : current) out.add(l.miniMessage());
        return out;
    }
}
