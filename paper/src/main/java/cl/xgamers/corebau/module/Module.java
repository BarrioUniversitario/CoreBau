package cl.xgamers.corebau.module;

import cl.xgamers.corebau.CoreBauPlugin;

/**
 * Un módulo de CoreBau (antes un plugin independiente: Baul, Board, Selector, Npc).
 *
 * El {@link CoreBauPlugin} habilita los módulos en orden durante onEnable y los
 * deshabilita en orden inverso durante onDisable. Cada módulo recibe la instancia
 * compartida del plugin y debe usarla en lugar de su antiguo {@code this} de
 * JavaPlugin (para scheduler, eventos, getDataFolder, logger, etc.).
 */
public interface Module {

    /** Identificador corto del módulo (ej. "baul", "board"). Define su subcarpeta de datos. */
    String id();

    /** Se llama una vez al habilitar. No debe lanzar; capturar y loguear sus propios errores. */
    void enable(CoreBauPlugin plugin);

    /** Se llama una vez al deshabilitar (orden inverso al enable). */
    void disable();
}
