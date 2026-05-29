package cl.xgamers.corebau.common.protocol;

/**
 * Constantes del protocolo de plugin-messaging compartido entre el proxy
 * (Velocity / Core) y los servers backend (Paper / Board, Selector).
 *
 * Mantener este contrato sincronizado de ambos lados. Vive en :common para que
 * tanto :velocity como :paper lo referencien sin duplicar literales.
 */
public final class ServerConnectorProtocol {

    private ServerConnectorProtocol() {
    }

    /** Canal usado por Board y Selector para hablar con el Core en el proxy. */
    public static final String CHANNEL = "serverconnector:main";

    /** Pide el conteo de jugadores de TODOS los servers de la red. */
    public static final String ACTION_PLAYER_COUNT_ALL = "PlayerCountAll";

    /** Pide el conteo de jugadores de un server específico. */
    public static final String ACTION_PLAYER_COUNT = "PlayerCount";

    /** Canal de sincronización de Baul (cosméticos / puntos) entre lobbies. */
    public static final String BAUL_SYNC_CHANNEL = "baul:sync";
}
