package dev.blancocl.npc;

import java.util.Collection;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

/** Thread-safe id -> NpcImpl registry. */
public final class NpcRegistry {

    private final Map<String, NpcImpl> byId = new ConcurrentHashMap<>();

    public void put(NpcImpl npc) { byId.put(npc.id(), npc); }

    public Optional<NpcImpl> get(String id) { return Optional.ofNullable(byId.get(id)); }

    public NpcImpl remove(String id) { return byId.remove(id); }

    public Collection<NpcImpl> all() { return byId.values(); }

    public int size() { return byId.size(); }

    public void clear() { byId.clear(); }
}
