package dev.blancocl.hologram;

import dev.blancocl.api.hologram.Hologram;
import dev.blancocl.api.hologram.HologramLine;
import dev.blancocl.npc.NpcImpl;
import org.bukkit.Location;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CopyOnWriteArrayList;

/** Mutable hologram backed by a list of {@link LineImpl}s. */
public final class HologramImpl implements Hologram {

    private final String id = UUID.randomUUID().toString();
    private final NpcImpl owner;
    private final HologramRenderer renderer;
    private final CopyOnWriteArrayList<LineImpl> lines = new CopyOnWriteArrayList<>();

    private volatile double offsetY;
    private volatile double lineSpacing;
    private volatile boolean detached = false;

    public HologramImpl(NpcImpl owner, HologramRenderer renderer, double offsetY, double lineSpacing, List<String> initialLines) {
        this.owner    = owner;
        this.renderer = renderer;
        this.offsetY  = offsetY;
        this.lineSpacing = lineSpacing;
        for (String l : initialLines) lines.add(new LineImpl(l));
    }

    public NpcImpl owner() { return owner; }

    @Override public String id() { return id; }

    @Override public Location location() {
        Location l = owner.location();
        l.add(0, offsetY, 0);
        return l;
    }

    @Override public double offsetY() { return offsetY; }
    @Override public void setOffsetY(double y) { this.offsetY = y; refresh(); }

    @Override public double lineSpacing() { return lineSpacing; }
    @Override public void setLineSpacing(double spacing) { this.lineSpacing = spacing; refresh(); }

    @Override public List<HologramLine> lines() { return new ArrayList<>(lines); }

    @Override public void setLines(List<String> miniMessage) {
        lines.clear();
        for (String s : miniMessage) lines.add(new LineImpl(s));
        refresh();
    }

    public boolean isDetached() { return detached; }
    public void detach() { detached = true; }

    @Override public void refresh() { renderer.broadcastUpdate(this); }

    public List<String> rawLines() {
        List<String> out = new ArrayList<>(lines.size());
        for (LineImpl l : lines) out.add(l.miniMessage());
        return out;
    }

    static final class LineImpl implements HologramLine {
        private final String mm;
        private volatile int frame;
        LineImpl(String mm) { this.mm = mm; }
        @Override public String  miniMessage() { return mm; }
        @Override public boolean animated()    { return mm.contains("<rainbow") || mm.contains("<gradient") || mm.contains("<#ANIM:"); }
        @Override public int     frame()       { return frame; }
        void advanceFrame() { frame++; }
    }

    public List<LineImpl> internalLines() { return lines; }
}
