package me.davidml16.baul.cosmetics.types;

import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

import java.util.function.BiConsumer;

/**
 * Formas paramétricas para las alas cosméticas. Cada shape produce una nube de
 * puntos relativos al jugador; el renderer ({@code WingTask}) los emite con
 * {@code World.spawnParticle(..., force=true)} para que sean visibles a todos
 * los espectadores en rango.
 *
 * <p>La base de Butterfly viene del task original de CompletosCore (Pablo B07);
 * aquí se generaliza y se añaden Heart, Infinity, Star, Ring, Halo, Angel, Bat
 * y Dragonfly con la misma estrategia: parametrizar (x,z), rotar al yaw del
 * jugador, y emitir en una capa horizontal pegada al pecho.
 */
public enum WingShape {

    /**
     * Curva clásica de mariposa: r(t) = e^cos(t) - cos(4t).
     * Coloca dos lóbulos detrás del jugador.
     */
    BUTTERFLY {
        @Override
        public void emit(Player player, Location anchor, double scale, double densityDegrees, Emitter emitter) {
            for (double deg = 0; deg < 360; deg += densityDegrees) {
                double t = Math.toRadians(deg);
                double r = scale * Math.exp(Math.cos(t)) - Math.cos(4 * t);
                double x = Math.sin(t) * r;
                double z = Math.cos(t) * r;
                Vector v = new Vector(x, 0, z);
                rotateX(v, -90);
                rotateY(v, anchor.getYaw());
                emitter.emit(anchor.clone().add(v), deg);
            }
        }
    },

    /**
     * Corazón clásico: x = 16 sin³(t), y = 13cos(t) - 5cos(2t) - 2cos(3t) - cos(4t).
     */
    HEART {
        @Override
        public void emit(Player player, Location anchor, double scale, double densityDegrees, Emitter emitter) {
            double s = scale * 0.06;
            for (double deg = 0; deg < 360; deg += densityDegrees) {
                double t = Math.toRadians(deg);
                double x = 16 * Math.pow(Math.sin(t), 3);
                double y = 13 * Math.cos(t) - 5 * Math.cos(2 * t) - 2 * Math.cos(3 * t) - Math.cos(4 * t);
                Vector v = new Vector(x * s, y * s, 0);
                rotateY(v, anchor.getYaw());
                emitter.emit(anchor.clone().add(v), deg);
            }
        }
    },

    /**
     * Lemniscata de Bernoulli (símbolo de infinito).
     */
    INFINITY {
        @Override
        public void emit(Player player, Location anchor, double scale, double densityDegrees, Emitter emitter) {
            for (double deg = 0; deg < 360; deg += densityDegrees) {
                double t = Math.toRadians(deg);
                double denom = 1 + Math.sin(t) * Math.sin(t);
                double x = scale * Math.cos(t) / denom;
                double z = scale * Math.sin(t) * Math.cos(t) / denom;
                Vector v = new Vector(x, 0, z);
                rotateX(v, -90);
                rotateY(v, anchor.getYaw());
                emitter.emit(anchor.clone().add(v), deg);
            }
        }
    },

    /**
     * Estrella de 5 puntas: rosa polar r = cos(2.5 t).
     */
    STAR {
        @Override
        public void emit(Player player, Location anchor, double scale, double densityDegrees, Emitter emitter) {
            for (double deg = 0; deg < 720; deg += densityDegrees) {
                double t = Math.toRadians(deg);
                double r = scale * Math.cos(2.5 * t);
                double x = Math.cos(t) * r;
                double y = Math.sin(t) * r;
                Vector v = new Vector(x, y, 0);
                rotateY(v, anchor.getYaw());
                emitter.emit(anchor.clone().add(v), deg);
            }
        }
    },

    /**
     * Anillo plano detrás del jugador.
     */
    RING {
        @Override
        public void emit(Player player, Location anchor, double scale, double densityDegrees, Emitter emitter) {
            for (double deg = 0; deg < 360; deg += densityDegrees) {
                double t = Math.toRadians(deg);
                double x = Math.cos(t) * scale;
                double z = Math.sin(t) * scale;
                Vector v = new Vector(x, 0, z);
                rotateY(v, anchor.getYaw());
                emitter.emit(anchor.clone().add(v), deg);
            }
        }
    },

    /**
     * Halo horizontal flotando sobre la cabeza.
     */
    HALO {
        @Override
        public void emit(Player player, Location anchor, double scale, double densityDegrees, Emitter emitter) {
            Location above = anchor.clone().add(0, 0.7, 0);
            for (double deg = 0; deg < 360; deg += densityDegrees) {
                double t = Math.toRadians(deg);
                double x = Math.cos(t) * scale * 0.6;
                double z = Math.sin(t) * scale * 0.6;
                emitter.emit(above.clone().add(x, 0, z), deg);
            }
        }
    },

    /**
     * Alas de ángel: dos arcos curvados hacia arriba detrás del jugador.
     */
    ANGEL {
        @Override
        public void emit(Player player, Location anchor, double scale, double densityDegrees, Emitter emitter) {
            for (double deg = 0; deg < 180; deg += densityDegrees) {
                double t = Math.toRadians(deg);
                // Lóbulo derecho
                double r = scale * (1 + 0.5 * Math.sin(2 * t));
                double xR = Math.sin(t) * r;
                double yR = Math.cos(t) * r * 0.8;
                Vector right = new Vector(xR, yR, 0);
                rotateY(right, anchor.getYaw() + 25);
                emitter.emit(anchor.clone().add(right), deg);

                // Lóbulo izquierdo (espejo)
                Vector left = new Vector(-xR, yR, 0);
                rotateY(left, anchor.getYaw() - 25);
                emitter.emit(anchor.clone().add(left), deg + 180);
            }
        }
    },

    /**
     * Alas de murciélago: dos arcos angulosos con borde dentado.
     */
    BAT {
        @Override
        public void emit(Player player, Location anchor, double scale, double densityDegrees, Emitter emitter) {
            for (double deg = 0; deg < 180; deg += densityDegrees) {
                double t = Math.toRadians(deg);
                double r = scale * (1 - 0.4 * Math.cos(3 * t));
                double xR = Math.sin(t) * r;
                double yR = -Math.abs(Math.cos(t)) * r * 0.6 + scale * 0.5;
                Vector right = new Vector(xR, yR, 0);
                rotateY(right, anchor.getYaw() + 20);
                emitter.emit(anchor.clone().add(right), deg);

                Vector left = new Vector(-xR, yR, 0);
                rotateY(left, anchor.getYaw() - 20);
                emitter.emit(anchor.clone().add(left), deg + 180);
            }
        }
    },

    /**
     * Alas de libélula: cuatro elipses alargadas horizontalmente.
     */
    DRAGONFLY {
        @Override
        public void emit(Player player, Location anchor, double scale, double densityDegrees, Emitter emitter) {
            for (double deg = 0; deg < 360; deg += densityDegrees) {
                double t = Math.toRadians(deg);
                double x = Math.cos(t) * scale * 0.9;
                double z = Math.sin(t) * scale * 0.35;
                // Par delantero
                Vector front = new Vector(x, 0, z + scale * 0.4);
                rotateY(front, anchor.getYaw() + 90);
                emitter.emit(anchor.clone().add(front), deg);
                // Par trasero (un poco más pequeño y desplazado)
                Vector back = new Vector(x * 0.8, 0, z * 0.8 - scale * 0.4);
                rotateY(back, anchor.getYaw() + 90);
                emitter.emit(anchor.clone().add(back), deg + 360);
            }
        }
    };

    /**
     * Emite los puntos de la forma. El parámetro {@code phase} (0..720 según
     * la forma) permite al renderer alternar colores en gradiente.
     */
    public abstract void emit(Player player, Location anchor, double scale, double densityDegrees, Emitter emitter);

    @FunctionalInterface
    public interface Emitter {
        void emit(Location at, double phaseDegrees);
    }

    public static WingShape fromString(String name, WingShape fallback) {
        if (name == null) return fallback;
        try {
            return WingShape.valueOf(name.trim().toUpperCase());
        } catch (IllegalArgumentException ex) {
            return fallback;
        }
    }

    static void rotateX(Vector v, double angleDeg) {
        double a = Math.toRadians(angleDeg);
        double cos = Math.cos(a);
        double sin = Math.sin(a);
        double y = v.getY() * cos - v.getZ() * sin;
        double z = v.getY() * sin + v.getZ() * cos;
        v.setY(y).setZ(z);
    }

    static void rotateY(Vector v, double angleDeg) {
        double a = Math.toRadians(-angleDeg);
        double cos = Math.cos(a);
        double sin = Math.sin(a);
        double x = v.getX() * cos + v.getZ() * sin;
        double z = v.getX() * -sin + v.getZ() * cos;
        v.setX(x).setZ(z);
    }

    /** Convenience no-op para señalar dónde se emite. */
    @SuppressWarnings("unused")
    private static final BiConsumer<Particle, Location> NOOP = (p, l) -> {};
}
