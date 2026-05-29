package dev.blancocl.api.hologram;

/** Single line of a {@link Hologram}, holding its MiniMessage source. */
public interface HologramLine {

    String  miniMessage();
    boolean animated();
    int     frame();
}
