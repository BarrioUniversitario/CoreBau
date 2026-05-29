package dev.blancocl.npc.animation;

import com.github.juliarn.npclib.api.protocol.enums.EntityAnimation;

/**
 * Built-in idle / emote animations. {@link #NONE} disables idle ticking.
 *
 * <p>npc-lib expone solo el set vanilla de entity-animations
 * ({@link EntityAnimation}): swing main arm, swing offhand, take damage, crit.
 * No hay un "wave" nativo en Minecraft — lo aproximamos con offhand swing
 * (visualmente la mano izquierda saluda).</p>
 */
public enum NpcAnimation {
    NONE,
    SWING,          // main-arm swing — el más usado
    WAVE,           // alias estético: swing de la mano izquierda
    EMOTE_NOD;      // alias estético: pequeño "tirón" usando TAKE_DAMAGE

    EntityAnimation toEntityAnimation() {
        return switch (this) {
            case NONE      -> null;
            case SWING     -> EntityAnimation.SWING_MAIN_ARM;
            case WAVE      -> EntityAnimation.SWING_OFF_HAND;
            case EMOTE_NOD -> EntityAnimation.TAKE_DAMAGE;
        };
    }
}
