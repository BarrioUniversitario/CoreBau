package dev.blancocl.npc.engine;

import com.github.juliarn.npclib.api.protocol.enums.EntityAnimation;
import dev.blancocl.api.npc.ClickType;
import dev.blancocl.api.npc.NpcType;
import dev.blancocl.api.skin.Skin;
import org.bukkit.Location;
import org.bukkit.entity.Player;

import java.util.function.BiConsumer;

/**
 * Engine boundary — every operation that translates into outbound packets
 * goes through this interface. Lets us swap the underlying packet library
 * (npc-lib today; PacketEvents-direct / FancyNpcs tomorrow) without
 * touching the rest of the plugin.
 *
 * <p>Implementations must be thread-safe; the {@link dev.blancocl.util.Threading}
 * service decides which calls run on main vs async.</p>
 */
public interface NpcEngine {

    void start();
    void stop();

    /** Create an engine-side NPC handle for the given type at the given location. */
    Handle create(String name, NpcType type, Location location);

    /** Engine-managed NPC reference. */
    interface Handle {
        Object engineNpc();                 // raw library object (escape hatch)
        void spawn(Player viewer);
        void despawn(Player viewer);
        void teleport(Location to);
        void applySkin(Skin skin);
        void setLookAtPlayer(boolean enabled);
        void setName(String miniMessage);
        void playAnimation(EntityAnimation animation);
        void destroy();
        /** Register a click handler invoked on the Bukkit main thread. */
        void onClick(BiConsumer<Player, ClickType> handler);
    }
}
