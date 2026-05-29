package cl.xgamers.selector.lobby;

public final class LobbyConnectResult {

    private final boolean success;
    private final String targetServer;
    private final LobbyState deniedState;
    private final String sourceLobbyKey;
    private final boolean routedViaSmart;

    private LobbyConnectResult(boolean success, String targetServer, LobbyState deniedState,
                               String sourceLobbyKey, boolean routedViaSmart) {
        this.success = success;
        this.targetServer = targetServer;
        this.deniedState = deniedState;
        this.sourceLobbyKey = sourceLobbyKey;
        this.routedViaSmart = routedViaSmart;
    }

    public static LobbyConnectResult ok(String targetServer, String sourceLobbyKey, boolean smartRoute) {
        return new LobbyConnectResult(true, targetServer, null, sourceLobbyKey, smartRoute);
    }

    public static LobbyConnectResult denied(LobbyState state, String sourceLobbyKey) {
        return new LobbyConnectResult(false, null, state, sourceLobbyKey, false);
    }

    public boolean isSuccess() {
        return success;
    }

    public String getTargetServer() {
        return targetServer;
    }

    public LobbyState getDeniedState() {
        return deniedState;
    }

    public String getSourceLobbyKey() {
        return sourceLobbyKey;
    }

    public boolean isRoutedViaSmart() {
        return routedViaSmart;
    }
}
