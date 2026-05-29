package dev.blancocl.npc.persistence;

import dev.blancocl.npc.NpcImpl;

import java.util.List;
import java.util.concurrent.CompletableFuture;

/** Persistence contract for {@link NpcImpl} snapshots. */
public interface NpcRepository {

    /** Load every persisted NPC definition. */
    CompletableFuture<List<NpcSnapshot>> loadAll();

    /** Atomically replace the on-disk state with the supplied snapshots. */
    CompletableFuture<Void> saveAll(List<NpcSnapshot> snapshots);
}
