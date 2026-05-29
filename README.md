# CoreBau

Monorepo de la red. Un solo repositorio, un solo build de Gradle, que produce
dos plugins:

- `CoreBau-1.0.0.jar` -> Paper (servers backend). Fusiona los módulos
  Baul, Board, Selector y Npc bajo un único plugin.
- `Core-1.0.0.jar` -> Velocity (proxy). Plugin del proxy, separado por
  plataforma.

> Restricción de plataforma: un plugin de Velocity y uno de Paper NO pueden ser
> el mismo .jar (APIs y entry points distintos). Por eso el monorepo genera dos
> artefactos, no uno.

---

## Tabla de contenidos

1. [Arquitectura](#arquitectura)
2. [Estructura del repositorio](#estructura-del-repositorio)
3. [Cómo compilar](#cómo-compilar)
4. [Instalación en el servidor](#instalación-en-el-servidor)
5. [Cómo funciona el sistema de módulos](#cómo-funciona-el-sistema-de-módulos)
6. [Guía paso a paso: agregar un módulo nuevo](#guía-paso-a-paso-agregar-un-módulo-nuevo)
7. [Buenas prácticas](#buenas-prácticas)
8. [English version](#english-version)

---

## Arquitectura

```
                         +--------------------------+
                         |   Velocity (proxy)       |
                         |   Core-1.0.0.jar         |
                         +------------+-------------+
                                      |  canal "serverconnector:main"
                                      |  canal "baul:sync"
                         +------------v-------------+
                         |   Paper (backend)        |
                         |   CoreBau-1.0.0.jar      |
                         |  +--------------------+  |
                         |  | Module: Board      |  |
                         |  | Module: Selector   |  |
                         |  | Module: Baul       |  |
                         |  | Module: Npc        |  |
                         |  +--------------------+  |
                         +--------------------------+
```

Cada módulo era antes un plugin independiente. Ahora todos cargan bajo un único
`CoreBauPlugin` (el verdadero `JavaPlugin`), y cada uno guarda sus datos en una
subcarpeta propia dentro de `plugins/CoreBau/`.

---

## Estructura del repositorio

```
CoreBau/
  settings.gradle.kts        Incluye los subproyectos :common, :velocity, :paper
  build.gradle.kts           Configuración común (Java 21, repos, versión)
  gradle.properties          Versiones centralizadas (paper-api, hikari, etc.)
  gradlew / gradlew.bat      Wrapper de Gradle

  common/                    Código neutral compartido (sin API de plataforma)
    src/main/java/cl/xgamers/corebau/common/protocol/

  velocity/                  Core-1.0.0.jar (proxy)
    build.gradle.kts
    src/main/java/cl/xgamers/core/
    src/main/resources/      config.properties (velocity-plugin.json lo genera @Plugin)

  paper/                     CoreBau-1.0.0.jar (backend)
    build.gradle.kts         shadowJar + relocations de todos los módulos
    src/main/java/
      cl/xgamers/corebau/CoreBauPlugin.java   Main único (JavaPlugin)
      cl/xgamers/corebau/module/Module.java   Interfaz de módulo
      cl/xgamers/board/                        Módulo Board
      cl/xgamers/selector/                     Módulo Selector
      me/davidml16/baul/                       Módulo Baul
      dev/blancocl/                            Módulo Npc
    src/main/resources/
      plugin.yml             Fusionado (todos los comandos y depends)
      board/  selector/  baul/  npc/           Recursos por módulo
```

Punto clave: cada módulo conserva su paquete original. No se renombró nada, lo
que evita romper datos guardados, nombres de tablas y referencias internas.

---

## Cómo compilar

Requisitos: JDK 21.

```bash
# Linux / macOS
./gradlew build

# Windows
.\gradlew.bat build
```

Artefactos resultantes:

- `paper/build/libs/CoreBau-1.0.0.jar`
- `velocity/build/libs/Core-1.0.0.jar`

Para compilar un solo módulo del monorepo:

```bash
./gradlew :paper:build
./gradlew :velocity:build
```

---

## Instalación en el servidor

### Backend (Paper)

1. Copiar `CoreBau-1.0.0.jar` a `plugins/`.
2. Instalar las dependencias duras: `ProtocolLib` y `packetevents`.
3. Opcionales (softdepend): PlaceholderAPI, Vault, LuckPerms, GoldmanPets,
   HolographicDisplays o DecentHolograms, Multiverse o Hyperverse.
4. Arrancar el server. En consola debe aparecer "Módulo habilitado: ..." por
   cada módulo.

Cada módulo crea su carpeta de datos:

```
plugins/CoreBau/
  board/      config.yml
  selector/   config.yml
  baul/       config.yml, crafting.yml, cosmetics/, gui_layouts/, language/, types/
  npc/        config.yml, messages.yml, npcs.yml
```

### Proxy (Velocity)

1. Copiar `Core-1.0.0.jar` a la carpeta `plugins/` del proxy.
2. Arrancar el proxy. Reenvía los canales `serverconnector:main` y `baul:sync`
   entre los servers backend.

### Migración desde los plugins antiguos

Como los paquetes y nombres de tablas se conservan, los datos siguen siendo
válidos. Solo cambia la RUTA de los archivos de config a las subcarpetas de cada
módulo. Copiá tus configs viejas:

```
plugins/Baul/config.yml      -> plugins/CoreBau/baul/config.yml
plugins/Board/config.yml     -> plugins/CoreBau/board/config.yml
plugins/Selector/config.yml  -> plugins/CoreBau/selector/config.yml
plugins/Npc/config.yml       -> plugins/CoreBau/npc/config.yml
```

(Si no copiás nada, cada módulo regenera su config por defecto.)

---

## Cómo funciona el sistema de módulos

El contrato es la interfaz `Module`:

```java
// paper/src/main/java/cl/xgamers/corebau/module/Module.java
public interface Module {
    String id();                          // ej. "board" -> subcarpeta de datos
    void enable(CoreBauPlugin plugin);    // se llama en onEnable, en orden
    void disable();                       // se llama en onDisable, en orden inverso
}
```

`CoreBauPlugin` los registra y los habilita en orden:

```java
// paper/src/main/java/cl/xgamers/corebau/CoreBauPlugin.java
private void registerModules() {
    modules.add(new cl.xgamers.board.Board());
    modules.add(new cl.xgamers.selector.Selector());
    modules.add(new me.davidml16.baul.Main());
    modules.add(new dev.blancocl.NpcPlugin());
}
```

Hay dos formas de adaptar un plugin existente a módulo, según cómo esté escrito:

### Forma A: clase liviana (recomendada para plugins chicos)

Usada por Board y Selector. La clase principal pasa de `extends JavaPlugin` a
`implements Module` y agrega métodos "delegados" para que el resto del código no
se toque:

```java
public final class Board implements Module, Listener, PluginMessageListener {
    private CoreBauPlugin plugin;

    @Override public String id() { return "board"; }

    @Override public void enable(CoreBauPlugin plugin) {
        this.plugin = plugin;
        saveDefaultConfig();
        // ... lo que antes estaba en onEnable, cambiando 'this' por 'plugin'
        // solo donde se requiere un Plugin real (eventos, scheduler, mensajería).
    }

    @Override public void disable() { /* lo que estaba en onDisable */ }

    // Métodos delegados: el resto del código sigue llamando getConfig(), etc.
    public org.bukkit.Server getServer()    { return plugin.getServer(); }
    public java.util.logging.Logger getLogger() { return plugin.getLogger(); }
    public java.io.File getDataFolder()     { return plugin.getModuleFolder("board"); }
    // getConfig() / reloadConfig() leen plugins/CoreBau/board/config.yml
}
```

### Forma B: implementar Plugin por delegación (para plugins grandes)

Usada por Baul y Npc, donde MUCHO código pasa el plugin a APIs de Bukkit
(scheduler, eventos, mensajería) y no queremos editar 100+ lugares. La clase
principal implementa `org.bukkit.plugin.Plugin` delegando todo al host:

```java
public class Main implements Module, org.bukkit.plugin.Plugin {
    private CoreBauPlugin host;

    @Override public String id() { return "baul"; }
    @Override public void enable(CoreBauPlugin host) { this.host = host; /* ... */ }
    @Override public void disable() { /* ... */ }

    // Archivos/config scopeados a la subcarpeta del módulo:
    @Override public java.io.File getDataFolder()           { return host.getModuleFolder("baul"); }
    @Override public java.io.InputStream getResource(String f) { return host.getResource("baul/" + f); }
    // saveResource, getConfig, reloadConfig: scopeados a "baul/"

    // El resto de los métodos de Plugin delegan al host:
    @Override public org.bukkit.Server getServer() { return host.getServer(); }
    @Override public java.util.logging.Logger getLogger() { return host.getLogger(); }
    // getDescription, getPluginMeta, getPluginLoader, getLifecycleManager,
    // isNaggable, setNaggable, onLoad, onEnable, onDisable, onCommand, onTabComplete...
}
```

Así, `this` sigue siendo un `Plugin` válido y el código existente compila sin
cambios. Donde una API exige específicamente un `JavaPlugin` (por ejemplo bStats
o el registro de comandos Brigadier), se pasa el `host` en vez de `this`.

---

## Guía paso a paso: agregar un módulo nuevo

Supongamos que quieres agregar un módulo "Welcome" (paquete `cl.xgamers.welcome`).

### Paso 1: copiar las fuentes al módulo paper

```
paper/src/main/java/cl/xgamers/welcome/...
paper/src/main/resources/welcome/config.yml   (recursos en su subcarpeta)
```

### Paso 2: convertir la clase principal a Module

Para un plugin chico (Forma A):

```java
package cl.xgamers.welcome;

import cl.xgamers.corebau.CoreBauPlugin;
import cl.xgamers.corebau.module.Module;
import org.bukkit.event.Listener;

public final class Welcome implements Module, Listener {
    private CoreBauPlugin plugin;

    @Override public String id() { return "welcome"; }

    @Override
    public void enable(CoreBauPlugin plugin) {
        this.plugin = plugin;
        saveDefaultConfig();
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
        // si declarás comandos en plugin.yml:
        // plugin.getCommand("welcome").setExecutor(new WelcomeCommand(this));
    }

    @Override
    public void disable() {
        // cancelar tareas, cerrar recursos
    }

    // --- config propio en plugins/CoreBau/welcome/ ---
    private org.bukkit.configuration.file.FileConfiguration config;

    public void saveDefaultConfig() {
        java.io.File f = new java.io.File(plugin.getModuleFolder("welcome"), "config.yml");
        if (!f.exists()) plugin.saveResource("welcome/config.yml", false);
        config = org.bukkit.configuration.file.YamlConfiguration.loadConfiguration(f);
    }
    public org.bukkit.configuration.file.FileConfiguration getConfig() {
        if (config == null) saveDefaultConfig();
        return config;
    }
    public org.bukkit.Server getServer() { return plugin.getServer(); }
    public java.util.logging.Logger getLogger() { return plugin.getLogger(); }
}
```

### Paso 3: registrar el módulo

En `CoreBauPlugin.registerModules()`:

```java
modules.add(new cl.xgamers.welcome.Welcome());
```

El orden importa: los módulos se habilitan de arriba hacia abajo y se
deshabilitan en orden inverso. Pon un módulo después de aquel del que dependa.

### Paso 4: declarar comandos en el plugin.yml fusionado

`paper/src/main/resources/plugin.yml`:

```yaml
commands:
  welcome:
    description: Comando del módulo Welcome
    permission: welcome.use
```

(Si tu módulo registra comandos por reflection/commandMap, no hace falta
declararlos, igual que Baul.)

### Paso 5: dependencias y shading

Si tu módulo trae una librería que hay que empaquetar (shade), agrégala en
`paper/build.gradle.kts` como `implementation(...)` y relocala para evitar
choques con otros módulos:

```kotlin
dependencies {
    implementation("com.ejemplo:milib:1.0.0")
}

tasks.shadowJar {
    relocate("com.ejemplo.milib", "cl.xgamers.welcome.libs.milib")
}
```

Si la librería la provee el server o es un plugin (PlaceholderAPI, packetevents,
ProtocolLib, etc.), usá `compileOnly` y NO la shadees. Adventure y MiniMessage
ya los provee Paper: nunca los shadees.

### Paso 6: compilar y probar

```bash
./gradlew :paper:build
```

Arranca un servidor de prueba y verifica en consola "Módulo habilitado: welcome".

---

## Buenas prácticas

- Un módulo, una subcarpeta. Siempre escribe los archivos en
  `plugin.getModuleFolder("<id>")`, nunca en la raíz de `plugins/CoreBau/`.
  Evita colisiones de `config.yml` entre módulos.
- Recursos namespaced. Los recursos del jar van bajo `resources/<id>/...` y se
  leen con la ruta `"<id>/archivo.yml"`.
- No shadear lo que provee la plataforma. Adventure, MiniMessage y la API de
  Paper son `compileOnly`. Shadear copias duplicadas rompe el classloader.
- Relocá todo lo que sí shadeás. Dos módulos pueden traer la misma librería
  (por ejemplo HikariCP); unificá la versión en `gradle.properties` y relocá a
  un paquete único.
- Capturá errores por módulo. `CoreBauPlugin` ya envuelve cada
  `enable()`/`disable()` en try/catch: un módulo que falla no tira abajo a los
  demás. No dejes que tu `enable()` propague excepciones sin loguear.
- Una sola versión de paper-api. Definida en `gradle.properties`
  (`paperApiVersion`). No la sobreescribas por módulo.
- Conservá paquetes y nombres de tablas al portar. Renombrar invalida datos
  guardados y configs de los usuarios.
- Deshabilitá en orden inverso. Si el módulo A depende de B, registrá A después
  de B; el apagado en orden inverso garantiza que A se cierre antes que B.
- Comandos en un solo lugar. Todos los comandos basados en plugin.yml se
  declaran en el `plugin.yml` fusionado del módulo paper.

---

# English version

## CoreBau (English)

Network monorepo. One repository, one Gradle build, producing two plugins:

- `CoreBau-1.0.0.jar` -> Paper (backend servers). Merges the Baul, Board,
  Selector and Npc modules under a single plugin.
- `Core-1.0.0.jar` -> Velocity (proxy). Separate by platform.

Platform constraint: a Velocity plugin and a Paper plugin cannot be the same
.jar (different APIs and entry points). That is why the monorepo emits two
artifacts, not one.

### Build

Requires JDK 21.

```bash
./gradlew build        # Linux / macOS
.\gradlew.bat build    # Windows
```

Outputs: `paper/build/libs/CoreBau-1.0.0.jar` and
`velocity/build/libs/Core-1.0.0.jar`.

### Module system

Every module was once a standalone plugin. They now load under a single
`CoreBauPlugin` (the real `JavaPlugin`). The contract is the `Module` interface:

```java
public interface Module {
    String id();                       // e.g. "board" -> its data subfolder
    void enable(CoreBauPlugin plugin); // called on enable, in order
    void disable();                    // called on disable, reverse order
}
```

Each module stores data in its own subfolder under `plugins/CoreBau/`, keeps its
original package, and is registered in `CoreBauPlugin.registerModules()`.

### Two porting styles

- Style A (small plugins, e.g. Board/Selector): the main class switches from
  `extends JavaPlugin` to `implements Module` and adds delegating helpers
  (`getServer`, `getLogger`, `getConfig` scoped to the module folder) so the rest
  of the code is untouched.
- Style B (large plugins, e.g. Baul/Npc): the main class implements
  `org.bukkit.plugin.Plugin` by delegating to the host `CoreBauPlugin`. This way
  the existing code that passes the plugin to Bukkit APIs (scheduler, events,
  messaging) keeps compiling. Where an API requires a real `JavaPlugin` (bStats,
  Brigadier command registration), pass the `host` instead of `this`.

### Adding a new module (summary)

1. Copy sources into `paper/src/main/java/<package>/`, resources into
   `paper/src/main/resources/<id>/`.
2. Make the main class `implements Module`; scope config/data to
   `plugin.getModuleFolder("<id>")` and resources to `"<id>/..."`.
3. Register it in `CoreBauPlugin.registerModules()` (order matters: enable a
   module after its dependencies; shutdown runs in reverse).
4. Declare plugin.yml commands in the merged `paper/src/main/resources/plugin.yml`.
5. Add shaded libs as `implementation(...)` and relocate them in `shadowJar`;
   use `compileOnly` for server-provided libs and never shade Adventure.
6. `./gradlew :paper:build` and verify "Módulo habilitado: <id>" in console.

### Best practices

- One module, one subfolder; never write to the `plugins/CoreBau/` root.
- Namespace jar resources under `<id>/` and read them as `"<id>/file.yml"`.
- Never shade platform-provided libraries (Adventure, MiniMessage, Paper API).
- Relocate everything you do shade; unify shared library versions in
  `gradle.properties`.
- Per-module error isolation: `enable()`/`disable()` are wrapped in try/catch by
  `CoreBauPlugin`, so a failing module does not take down the others.
- Keep packages and database table names when porting to preserve saved data.
