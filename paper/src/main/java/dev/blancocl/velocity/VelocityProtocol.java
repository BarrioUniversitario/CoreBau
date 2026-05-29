package dev.blancocl.velocity;

/** Channel + subchannel constants for Velocity's BungeeCord-compatible plugin messaging. */
public final class VelocityProtocol {

    /** Default channel — overridable via {@code velocity.channel} in config.yml. */
    public static final String CHANNEL = "bungeecord:main";

    public static final String SUB_CONNECT       = "Connect";
    public static final String SUB_PLAYER_COUNT  = "PlayerCount";
    public static final String SUB_GET_SERVERS   = "GetServers";

    private VelocityProtocol() {}
}
