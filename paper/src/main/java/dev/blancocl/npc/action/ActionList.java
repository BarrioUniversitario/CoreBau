package dev.blancocl.npc.action;

import dev.blancocl.api.npc.ClickContext;
import dev.blancocl.api.npc.NpcAction;

import java.util.Iterator;
import java.util.concurrent.CopyOnWriteArrayList;

/** Append-only list of click handlers. Iteration is safe with concurrent mutation. */
public final class ActionList implements Iterable<NpcAction> {

    private final CopyOnWriteArrayList<NpcAction> actions = new CopyOnWriteArrayList<>();

    public void add(NpcAction action) { actions.add(action); }

    public void clear() { actions.clear(); }

    public int size() { return actions.size(); }

    public int dispatch(ClickContext ctx) {
        int n = 0;
        for (NpcAction a : actions) {
            try { a.run(ctx); n++; }
            catch (Throwable t) {
                // Loggeamos pero no rompemos el resto de acciones. Antes era silencioso
                // y dificultaba diagnosticar acciones rotas.
                org.bukkit.Bukkit.getLogger().warning(
                        "[Npc] action " + a.getClass().getSimpleName() + " failed: " + t);
            }
        }
        return n;
    }

    @Override public Iterator<NpcAction> iterator() { return actions.iterator(); }
}
