package cl.xgamers.selector.lobby;

/**
 * Estado visual y de conexión de un lobby según datos de Velocity ({@code PlayerCount}).
 */
public enum LobbyState {

    /** Online, con cupo → DIAMOND_BLOCK, click manual permitido. */
    AVAILABLE,

    /** {@code jugadores >= max-players} → RED_CONCRETE, click manual bloqueado. */
    FULL,

    /** Servidor no reportado por Velocity (offline / no registrado). */
    OFFLINE,

    /** {@code enabled: false} en config. */
    DISABLED;

    public boolean isJoinableManual() {
        return this == AVAILABLE;
    }

    public boolean usesAvailableMaterial() {
        return this == AVAILABLE;
    }
}
