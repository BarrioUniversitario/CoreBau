package me.davidml16.baul.cosmetics.types;

import me.davidml16.baul.cosmetics.Cosmetic;
import me.davidml16.baul.cosmetics.CosmeticCategory;
import org.bukkit.Color;

import java.util.List;

/**
 * Cosmético tipo Wing — dibuja una forma paramétrica (mariposa, corazón, halo,
 * etc.) detrás del jugador cada {@code intervalTicks} ticks. El renderizado lo
 * hace {@link me.davidml16.baul.cosmetics.render.WingTask} usando
 * {@code World.spawnParticle(..., force=true)} para que sea visible a todos.
 */
public class Wing extends Cosmetic {

    private final WingShape shape;
    private final Color primaryColor;
    private final Color secondaryColor;
    private final float dustSize;
    private final double scale;
    private final double density;       // grados entre cada punto emitido
    private final int intervalTicks;    // cada cuántos ticks redibujar
    private final boolean gradient;     // alterna primary↔secondary por phase

    public Wing(String key, String displayName, String rarity, String iconMaterial, String permission,
                WingShape shape, Color primaryColor, Color secondaryColor, float dustSize,
                double scale, double density, int intervalTicks, boolean gradient, long price) {
        super(key, CosmeticCategory.WING, displayName, rarity, iconMaterial, permission, price);
        this.shape = shape != null ? shape : WingShape.BUTTERFLY;
        this.primaryColor = primaryColor != null ? primaryColor : Color.fromRGB(212, 146, 53);
        this.secondaryColor = secondaryColor != null ? secondaryColor : this.primaryColor;
        this.dustSize = dustSize <= 0 ? 0.6f : dustSize;
        this.scale = scale <= 0 ? 0.35 : scale;
        this.density = density <= 0 ? 2.0 : density;
        this.intervalTicks = intervalTicks <= 0 ? 2 : intervalTicks;
        this.gradient = gradient;
    }

    public WingShape getShape() { return shape; }
    public Color getPrimaryColor() { return primaryColor; }
    public Color getSecondaryColor() { return secondaryColor; }
    public float getDustSize() { return dustSize; }
    public double getScale() { return scale; }
    public double getDensity() { return density; }
    public int getIntervalTicks() { return intervalTicks; }
    public boolean isGradient() { return gradient; }

    /**
     * Color resultante para un punto dado: si {@link #isGradient()} es false
     * siempre devuelve {@link #getPrimaryColor()}; si es true, alterna entre
     * primary y secondary según la fase del punto y el tick global.
     */
    public Color colorAt(double phaseDegrees, long tick) {
        if (!gradient) return primaryColor;
        // Mezcla suave por fase + corrimiento por tick para dar movimiento.
        double t = (phaseDegrees + tick * 6.0) % 360.0;
        double k = (Math.sin(Math.toRadians(t)) + 1.0) / 2.0; // 0..1
        int r = (int) Math.round(primaryColor.getRed() * (1 - k) + secondaryColor.getRed() * k);
        int g = (int) Math.round(primaryColor.getGreen() * (1 - k) + secondaryColor.getGreen() * k);
        int b = (int) Math.round(primaryColor.getBlue() * (1 - k) + secondaryColor.getBlue() * k);
        return Color.fromRGB(clamp(r), clamp(g), clamp(b));
    }

    private static int clamp(int v) {
        return v < 0 ? 0 : (v > 255 ? 255 : v);
    }

    /** Lista vacía no usada — placeholder para futuras paletas. */
    @SuppressWarnings("unused")
    public List<Color> palette() {
        return List.of(primaryColor, secondaryColor);
    }
}
