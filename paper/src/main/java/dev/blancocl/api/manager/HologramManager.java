package dev.blancocl.api.manager;

import dev.blancocl.api.hologram.Hologram;
import dev.blancocl.api.npc.Npc;

import java.util.List;

public interface HologramManager {

    /** Attach a hologram to {@code npc} at the given Y offset. */
    Hologram attach(Npc npc, double offsetY, List<String> lines);

    /** Remove every hologram on the NPC. */
    void detachAll(Npc npc);

    /** Live view of all known holograms. */
    List<Hologram> all();
}
