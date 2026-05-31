package cl.xgamers.corebau;

import cl.xgamers.corebau.module.Module;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Plugin único de CoreBau para Paper. Fusiona los antiguos plugins
 * Baul + Board + Selector + Npc como módulos cargados bajo un solo Main.
 *
 * Cada módulo guarda sus datos en una subcarpeta de {@link #getDataFolder()}
 * (ej. {@code plugins/CoreBau/baul/}) para evitar colisiones de archivos de
 * config entre módulos.
 */
public final class CoreBauPlugin extends JavaPlugin {

    private static CoreBauPlugin instance;

    private final List<Module> modules = new ArrayList<>();

    public static CoreBauPlugin get() {
        return instance;
    }

    @Override
    public void onEnable() {
        instance = this;

        if (!getDataFolder().exists()) getDataFolder().mkdirs();

        registerModules();

        for (Module module : modules) {
            try {
                File moduleFolder = new File(getDataFolder(), module.id());
                if (!moduleFolder.exists()) moduleFolder.mkdirs();
                module.enable(this);
                getLogger().info("Módulo habilitado: " + module.id());
            } catch (Throwable t) {
                getLogger().severe("Fallo al habilitar el módulo '" + module.id() + "': " + t.getMessage());
                t.printStackTrace();
            }
        }

        getLogger().info("CoreBau habilitado con " + modules.size() + " módulo(s).");
    }

    @Override
    public void onDisable() {
        // Orden inverso al enable.
        List<Module> reversed = new ArrayList<>(modules);
        Collections.reverse(reversed);
        for (Module module : reversed) {
            try {
                module.disable();
                getLogger().info("Módulo deshabilitado: " + module.id());
            } catch (Throwable t) {
                getLogger().severe("Fallo al deshabilitar el módulo '" + module.id() + "': " + t.getMessage());
                t.printStackTrace();
            }
        }
        modules.clear();
        instance = null;
    }

    /**
     * Registra los módulos en orden de carga. Se irán agregando en la Etapa 4
     * a medida que se porten los plugins:
     *   modules.add(new BoardModule());
     *   modules.add(new SelectorModule());
     *   modules.add(new BaulModule());
     *   modules.add(new NpcModule());
     */
    private void registerModules() {
        modules.add(new cl.xgamers.board.Board());
        modules.add(new cl.xgamers.selector.Selector());
        modules.add(new cl.xgamers.lobby.LobbyModule());
        modules.add(new me.davidml16.baul.Main());
        modules.add(new dev.blancocl.NpcPlugin());
    }

    /** Subcarpeta de datos de un módulo (ej. plugins/CoreBau/baul). */
    public File getModuleFolder(String moduleId) {
        return new File(getDataFolder(), moduleId);
    }
}
