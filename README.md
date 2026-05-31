# CoreBau

Monorepo de la red. Un solo repositorio, un solo build de Gradle, que produce
dos artefactos principales:

- `CoreBau-1.0.0.jar` — Paper (servidores backend). Fusiona los módulos
  Board, Selector, Lobby, Baul y Npc bajo un único plugin.
- `Core-1.0.0.jar` — Velocity (proxy). Plugin del proxy, separado por
  plataforma.

> Restricción de plataforma: un plugin de Velocity y uno de Paper no pueden ser
> el mismo `.jar` (APIs y entry points distintos). Por eso el monorepo genera
> dos artefactos, no uno.

**Nota:** las mascotas (pets), incluyendo el sistema completo de habilidades por
especie, niveles y rarezas, viven dentro de `CoreBau-1.0.0.jar` como subsistema
embebido bajo `me.davidml16.baul.pets`. No se requiere ningún plugin externo de
mascotas (ni BetterPets como JAR separado, ni GoldmanPets, ni nada).

---

## Tabla de contenidos

1. [Arquitectura](#arquitectura)
2. [Estructura del repositorio](#estructura-del-repositorio)
3. [Cómo compilar](#cómo-compilar)
4. [Instalación en el servidor](#instalación-en-el-servidor)
5. [Cómo funciona el sistema de módulos](#cómo-funciona-el-sistema-de-módulos)
6. [Guía paso a paso: agregar un módulo nuevo](#guía-paso-a-paso-agregar-un-módulo-nuevo)
7. [Cómo agregar cosméticos (Baul)](#cómo-agregar-cosméticos-baul)
8. [Ítem de cosméticos en el Selector](#ítem-de-cosméticos-en-el-selector)
9. [Sistema de Lobby](#sistema-de-lobby)
10. [Buenas prácticas](#buenas-prácticas)
11. [English version](#english-version)

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
                         |  | Módulo: Board      |  |
                         |  | Módulo: Selector   |  |
                         |  | Módulo: Lobby      |  |
                         |  | Módulo: Baul       |  |
                         |  |  └─ pets (subsis.) |  |
                         |  | Módulo: Npc        |  |
                         |  +--------------------+  |
                         +--------------------------+
```

Cada módulo era antes un plugin independiente. Ahora todos cargan bajo un único
`CoreBauPlugin` (el verdadero `JavaPlugin`), y cada uno guarda sus datos en una
subcarpeta propia dentro de `plugins/CoreBau/`. El subsistema de pets vive
dentro del módulo Baul, no como módulo separado.

---

## Estructura del repositorio

```
CoreBau/
  settings.gradle.kts        Incluye :common, :velocity, :paper
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
    build.gradle.kts         shadowJar + relocations
    src/main/java/
      cl/xgamers/corebau/CoreBauPlugin.java   Main único (JavaPlugin)
      cl/xgamers/corebau/module/Module.java   Interfaz de módulo
      cl/xgamers/board/                        Módulo Board
      cl/xgamers/selector/                     Módulo Selector
      cl/xgamers/lobby/                        Módulo Lobby (spawn, antivoid, protecciones)
      me/davidml16/baul/                       Módulo Baul
        └─ pets/                               Subsistema de pets embebido
      dev/blancocl/                            Módulo Npc
    src/main/resources/
      plugin.yml             Fusionado (todos los comandos y depends)
      board/  selector/  lobby/  baul/  npc/   Recursos por módulo
        baul/cosmetics/      trails.yml, hats.yml, wings.yml, pets.yml, ...
        baul/items/          Items custom del subsistema de pets
        baul/skins/          Skins de pets
```

Punto clave: cada módulo conserva su paquete original. No se renombró nada para
evitar romper datos guardados, nombres de tablas y referencias internas. El
código rescatado de BetterPets se reempaquetó bajo `me.davidml16.baul.pets`.

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
3. Dependencias opcionales (softdepend): PlaceholderAPI, Vault, LuckPerms,
   HolographicDisplays o DecentHolograms, Multiverse o Hyperverse, WorldGuard.
4. Arrancar el servidor. En consola debe aparecer un mensaje
   `Módulo habilitado: ...` por cada módulo. Cuando el subsistema de pets se
   inicializa correctamente se ve también:

   ```
   [Baul] Mascotas cosméticas: subsistema de pets embebido activo.
   ```

Cada módulo crea su carpeta de datos:

```
plugins/CoreBau/
  board/      config.yml
  selector/   config.yml
  lobby/      config.yml (spawn por servidor, antivoid, protecciones)
  baul/       config.yml, crafting.yml, cosmetics/, gui_layouts/, language/,
              types/, items/, skins/, pets.db (subsistema de pets)
  npc/        config.yml, messages.yml, npcs.yml
```

### Proxy (Velocity)

1. Copiar `Core-1.0.0.jar` a la carpeta `plugins/` del proxy.
2. Arrancar el proxy. Reenvía los canales `serverconnector:main` y `baul:sync`
   entre los servidores backend.

### Migración desde los plugins antiguos

Como los paquetes y nombres de tablas se conservan, los datos previos siguen
siendo válidos. Solo cambia la ruta de los archivos de config a las subcarpetas
de cada módulo. Copiar las configs antiguas:

```
plugins/Baul/config.yml      ->  plugins/CoreBau/baul/config.yml
plugins/Board/config.yml     ->  plugins/CoreBau/board/config.yml
plugins/Selector/config.yml  ->  plugins/CoreBau/selector/config.yml
plugins/Npc/config.yml       ->  plugins/CoreBau/npc/config.yml
```

Si no se copia nada, cada módulo regenera su config por defecto.

> Si la instalación previa usaba `BetterPets-1.0.0-shaded.jar` como plugin
> separado, ya no es necesario: el subsistema de pets vive embebido. Conviene
> eliminar el JAR antiguo de `plugins/` para evitar colisiones de comandos o
> listeners duplicados.

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
    modules.add(new cl.xgamers.lobby.LobbyModule());
    modules.add(new me.davidml16.baul.Main());
    modules.add(new dev.blancocl.NpcPlugin());
}
```

Hay dos formas de adaptar un plugin existente a módulo, según cómo esté escrito:

### Forma A: clase liviana (recomendada para plugins chicos)

Usada por Board y Selector. La clase principal pasa de `extends JavaPlugin` a
`implements Module` y agrega métodos delegados para que el resto del código no
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

Usada por Baul y Npc, donde mucho código pasa el plugin a APIs de Bukkit
(scheduler, eventos, mensajería) y no conviene editar 100+ lugares. La clase
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
cambios. Donde una API exige específicamente un `JavaPlugin` (por ejemplo
bStats o el registro de comandos Brigadier), se pasa el `host` en vez de `this`.

---

## Guía paso a paso: agregar un módulo nuevo

Para agregar un módulo "Welcome" (paquete `cl.xgamers.welcome`):

### Paso 1: copiar las fuentes al módulo paper

```
paper/src/main/java/cl/xgamers/welcome/...
paper/src/main/resources/welcome/config.yml   (recursos en su subcarpeta)
```

### Paso 2: convertir la clase principal a Module

Ejemplo de Forma A (plugin chico):

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
    }

    @Override public void disable() { /* cancelar tareas, cerrar recursos */ }

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
deshabilitan en orden inverso. Conviene poner un módulo después de aquel del
que depende.

### Paso 4: declarar comandos en el plugin.yml fusionado

`paper/src/main/resources/plugin.yml`:

```yaml
commands:
  welcome:
    description: Comando del módulo Welcome
    permission: welcome.use
```

Si el módulo registra comandos por reflection / commandMap, no hace falta
declararlos, igual que Baul.

### Paso 5: dependencias y shading

Si el módulo trae una librería que hay que empaquetar (shade), agregarla en
`paper/build.gradle.kts` como `implementation(...)` y relocarla para evitar
choques con otros módulos:

```kotlin
dependencies {
    implementation("com.ejemplo:milib:1.0.0")
}

tasks.shadowJar {
    relocate("com.ejemplo.milib", "cl.xgamers.welcome.libs.milib")
}
```

Si la librería la provee el servidor o es un plugin (PlaceholderAPI,
packetevents, ProtocolLib, etc.), usar `compileOnly` y no shadearla. Adventure
y MiniMessage ya los provee Paper: nunca conviene shadearlos.

### Paso 6: compilar y probar

```bash
./gradlew :paper:build
```

Arrancar un servidor de prueba y verificar en consola
`Módulo habilitado: welcome`.

---

## Cómo agregar cosméticos (Baul)

El sistema de cosméticos de Baul usa un registro central (`CosmeticRegistry`)
que carga archivos YAML de la carpeta `plugins/CoreBau/baul/cosmetics/`. Cada
tipo de cosmético tiene su propio archivo. Para agregar entradas nuevas solo
se edita el YAML correspondiente — no hace falta tocar código Java.

### Estructura de la carpeta

```
plugins/CoreBau/baul/cosmetics/
  trails.yml        Senderos de partículas
  hats.yml          Sombreros (items encima de la cabeza)
  wings.yml         Alas con formas paramétricas (mariposa, halo, ángel, etc.)
  pets.yml          Mascotas con habilidades (subsistema embebido)
  join_effects.yml  Efectos al entrar al servidor
  emotes.yml        Animaciones con comandos
```

### Agregar un trail (sendero de partículas)

```yaml
# trails.yml
mi_trail_fuego:
  display: "<gradient:#ff4500:#ffff00><bold>Rastro de Fuego"
  rarity: EPIC
  icon: BLAZE_POWDER
  permission: baul.cosmetic.trail.fuego
  price: 5000
  particle: FLAME
  amount: 8
  interval: 2
  speed: 0.05
```

Valores válidos de `particle`: cualquier `org.bukkit.Particle` (FLAME, HEART,
ENCHANTMENT_TABLE, etc.). Los trails se emiten con `force=true` para que sean
visibles a todos los espectadores en rango extendido.

### Agregar un sombrero (hat)

```yaml
# hats.yml
corona_dorada:
  display: "<gold><bold>Corona Dorada"
  rarity: LEGENDARY
  icon: GOLD_INGOT
  permission: baul.cosmetic.hat.corona
  price: 8000
  material: GOLDEN_HELMET
```

### Agregar un par de alas (wing)

Las alas dibujan formas paramétricas detrás del jugador con partículas DUST
coloreables. Cada entrada elige una de las nueve formas disponibles:
`BUTTERFLY`, `HEART`, `INFINITY`, `STAR`, `RING`, `HALO`, `ANGEL`, `BAT`,
`DRAGONFLY`.

```yaml
# wings.yml
mariposa_arcoiris:
  display: "<rainbow><bold>Mariposa Arcoíris"
  rarity: LEGENDARY
  icon: NETHER_STAR
  permission: baul.cosmetic.wings.rainbow
  price: 5500
  shape: BUTTERFLY
  color: "#ff0080"
  secondaryColor: "#00ffff"
  gradient: true
  dustSize: 0.7
  scale: 0.4
  density: 2.0
  interval: 2
```

Campos:
- `shape` — forma paramétrica.
- `color` — color primario (hex `#RRGGBB` o nombre).
- `secondaryColor` y `gradient: true` — mezcla animada entre dos colores.
- `dustSize` — tamaño del punto DUST (0.4 a 1.2).
- `scale` — tamaño general de la forma.
- `density` — grados entre cada punto emitido (2 denso, 6 esparcido).
- `interval` — cada cuántos ticks redibujar (2 = 10 FPS, 4 = 5 FPS).

### Agregar una mascota (pet)

Las mascotas usan el subsistema embebido `me.davidml16.baul.pets`, que incluye
35 especies con habilidades por especie, niveles, rarezas y persistencia
(SQLite o MySQL).

Cada entrada de `pets.yml` debe declarar el campo `betterPets` con la id de
plantilla del registro, en la forma `<baseId>_<rareza>`:

```yaml
# pets.yml
wolf:
  display: "<gradient:#aa7744:#ddccaa>Lobo"
  rarity: COMMON
  icon: BONE
  permission: ""
  price: 200
  betterPets: wolf_common

dragon:
  display: "<gradient:#ffaa00:#ff4400><bold>Dragón de Ender"
  rarity: LEGENDARY
  icon: DRAGON_HEAD
  permission: baul.cosmetic.dragon
  price: 8000
  betterPets: ender_dragon_legendary
```

**Especies disponibles** (registradas en
`me.davidml16.baul.pets.registry.PetManager.initializePets`):

| Categoría    | Especies                                                                                                                                       |
|--------------|------------------------------------------------------------------------------------------------------------------------------------------------|
| Combat       | ender_dragon, enderman, endermite, ghoul, golem, grandma_wolf, horse, hound, lion, phoenix, pigman, rock, skeleton_horse, skeleton, spider, tarantula, tiger, wolf, zombie |
| Farming      | bee, chicken, elephant, pig, rabbit                                                                                                            |
| Fishing      | blue_whale, dolphin, flying_fish, squid                                                                                                        |
| Mining       | silverfish, wither_skeleton                                                                                                                    |
| Foraging     | giraffe, monkey, ocelot                                                                                                                        |
| Alchemy      | parrot, sheep                                                                                                                                  |
| Enchanting   | guardian                                                                                                                                       |

Rarezas válidas: `COMMON`, `UNCOMMON`, `RARE`, `EPIC`, `LEGENDARY`, `MYTHIC`.
Cada especie soporta solo un subconjunto; consultar `pets.yml` por defecto para
ver el rango disponible por especie.

### Agregar un efecto de entrada (join effect)

```yaml
# join_effects.yml
explosion_arcoiris:
  display: "<rainbow>Explosión Arcoíris"
  rarity: RARE
  icon: FIREWORK_ROCKET
  permission: baul.cosmetic.joineffect.arcoiris
  price: 2000
  particle: HAPPY_VILLAGER
  amount: 60
  spread: 2.0
  sound: ENTITY_PLAYER_LEVELUP
  pitch: 1.0
  broadcast: true
```

### Agregar un emote

```yaml
# emotes.yml
baile_zombie:
  display: "<green>Baile Zombi"
  rarity: COMMON
  icon: ZOMBIE_HEAD
  permission: baul.cosmetic.emote.zombie
  price: 500
  particle: HAPPY_VILLAGER
  amount: 20
  spread: 0.5
  sound: ENTITY_ZOMBIE_AMBIENT
  pitch: 1.0
  cooldown: 30000
```

### Recetas de crafteo

Cada cosmético puede tener una receta opcional en `crafting.yml` que permite
desbloquearlo gastando puntos, baúles o dinero (Vault):

```yaml
# crafting.yml
cosmetic-crafting:
  crafts:
    mariposa_arcoiris:
      ingredients:
        1:
          type: points
          amount: 6500
        2:
          type: cubelet
          name: example
          amount: 5
```

Tipos de ingredientes: `points` (puntos de botín), `cubelet` (baúles de un
tipo específico — requiere campo `name`), `money` (economía Vault).

### Permisos

Todos los cosméticos respetan el campo `permission` del YAML. Los jugadores
con `baul.cosmetic.*` tienen acceso a todos. Si el campo está vacío, cualquier
jugador puede usarlo.

### Visibilidad de cosméticos

Trails, emotes, join effects, particles y alas se emiten con
`World.spawnParticle(..., force=true)` para que sean visibles a todos los
espectadores en rango extendido (~256 bloques), no solo al dueño. Para las
mascotas, la visibilidad por espectador la gestiona el subsistema embebido a
través del `VisibilityType` del jugador (`ALL`, `OWN`, `NONE`).

### Desbloquear cosméticos desde código

```java
// Desbloquear un cosmético para un jugador desde otro módulo o comando
me.davidml16.baul.Main baulMain = me.davidml16.baul.Main.get();
if (baulMain != null) {
    Profile profile = baulMain.getPlayerDataHandler().getData(player);
    if (profile != null) {
        profile.unlockCosmetic("lobo_cosmetico");
        baulMain.getDatabaseHandler().saveProfileAsync(profile, player.getName());
    }
}
```

---

## Ítem de cosméticos en el Selector

El módulo Selector coloca automáticamente tres ítems en el hotbar de cada
jugador al entrar:

| Slot (default) | Material       | Función                              |
|----------------|----------------|--------------------------------------|
| 0              | ENDER_CHEST    | Abre el menú de cosméticos de Baul   |
| 4              | COMPASS        | Abre el selector de servidores       |
| 8              | EMERALD        | Abre la cola de lobbies              |

Todos son configurables en `plugins/CoreBau/selector/config.yml`. El ítem de
cosméticos ejecuta el comando definido en `cosmetics-item.click-command`
(por defecto `baul cosmetics`) como si el jugador lo escribiera.

```yaml
# selector/config.yml
cosmetics-item:
  enabled: true
  slot: 0
  material: ENDER_CHEST
  title: "<light_purple><bold>Cosméticos"
  lore:
    - ""
    - "<gray>Administra tus cosméticos"
    - "<light_purple>Click para abrir"
  glowing: false
  click-command: "baul cosmetics"
```

---

## Sistema de Lobby

El módulo `Lobby` (paquete `cl.xgamers.lobby`) convierte cada servidor de
backend en un lobby completo: registra el spawn de la instancia, teletransporta
a los jugadores y aplica todas las protecciones esenciales (anti-daño,
anti-hambre, anti-drop, anti-construcción, antivoid, etc.). Cada paper instance
es un lobby independiente (`lobby1`, `lobby2`, …) y guarda su propio spawn.

### Identificador del lobby

Cada paper de lobby debe declarar su identificador en
`plugins/CoreBau/lobby/config.yml`:

```yaml
server-id: "lobby1"   # en el segundo lobby: "lobby2", etc.
```

El comando `/setspawn` guarda la posición bajo `spawns.<server-id>`, así dos
servidores distintos nunca pisan el mismo spawn aunque compartan el archivo.

### Comandos

| Comando        | Permiso          | Descripción                                                                 |
|----------------|------------------|-----------------------------------------------------------------------------|
| `/setspawn`    | `lobby.setspawn` | Guarda la ubicación actual del jugador (incluye yaw y pitch) como spawn del lobby actual. |
| `/spawn`       | (público)        | Teletransporta al spawn del lobby actual. También se usa al entrar y al respawnear. |
| `/lobbyreload` | `lobby.admin`    | Recarga `lobby/config.yml` sin reiniciar el servidor.                       |

### Protecciones disponibles

Todas son configurables y se pueden desactivar individualmente:

| Sección de config           | Efecto                                                                                       |
|-----------------------------|----------------------------------------------------------------------------------------------|
| `antivoid`                  | Devuelve al spawn si el jugador cae por debajo de `min-y` (chequeo periódico + listener de movimiento). |
| `anti-damage`               | Cancela todo daño a jugadores. Incluye daño por VOID si `cancel-void: true`.                 |
| `anti-hunger`               | Mantiene comida y saturación al máximo.                                                      |
| `anti-death.keep-inventory` | Activa keep inventory y limpia drops y XP.                                                   |
| `anti-drop`                 | Bloquea soltar ítems. Con `protected-items-only: true` solo se bloquean los ítems del lobby. |
| `anti-inventory-move`       | Bloquea mover ítems en el inventario fuera de modo creativo.                                 |
| `anti-build`                | Bloquea romper/colocar bloques, cubetas, portales y manipular armor stands.                  |
| `anti-weather`              | Cancela el cambio a lluvia/tormenta.                                                         |
| `anti-mob-target`           | Impide que los mobs ataquen al jugador.                                                      |
| `anti-item-damage`          | Las herramientas/armadura no pierden durabilidad.                                            |
| `anti-projectiles`          | Bloquea el lanzamiento de proyectiles por parte de jugadores.                                |
| `on-join`                   | Al entrar: tp al spawn, heal completo, modo `ADVENTURE`, flight on/off.                      |

### Ítems protegidos del lobby

El módulo reconoce los tres ítems del Selector vía sus `NamespacedKey`:

- `selector` — brújula del selector de servidores.
- `lobby-opener` — esmeralda que abre la cola de lobbies.
- `cosmetics-opener` — cofre del ender que abre el menú de cosméticos.

Estos ítems no se pueden soltar ni mover dentro del inventario nunca, sin
importar el valor de `anti-drop.protected-items-only`.

### Permisos de bypass

| Permiso                      | Bypass                                          |
|------------------------------|-------------------------------------------------|
| `lobby.bypass.drop`          | Permite soltar ítems aun con `anti-drop` activo.|
| `lobby.bypass.inventory`     | Permite mover ítems en el inventario.           |
| `lobby.bypass.build`         | Permite construir, romper bloques, cubetas, armor stands. |
| `lobby.bypass.projectiles`   | Permite lanzar proyectiles.                     |

### Configuración por defecto resumida

```yaml
server-id: "lobby1"
spawns: {}                # lo rellena /setspawn

on-join:
  teleport-to-spawn: true
  heal: true
  gamemode-adventure: true
  allow-flight: false

antivoid:
  enabled: true
  min-y: 0.0
  check-interval-ticks: 10

anti-damage:    { enabled: true, cancel-void: true }
anti-hunger:    { enabled: true }
anti-death:     { keep-inventory: true }
anti-drop:      { enabled: true, protected-items-only: false }
anti-inventory-move: { enabled: true }
anti-build:     { enabled: true }
anti-weather:   { enabled: true }
anti-mob-target: { enabled: true }
anti-item-damage: { enabled: true }
anti-projectiles: { enabled: true }
```

### Despliegue multi-lobby

Para un segundo lobby:

1. Copiar el mismo `CoreBau-1.0.0.jar` en la segunda paper instance.
2. Editar `plugins/CoreBau/lobby/config.yml` y poner `server-id: "lobby2"`.
3. Entrar como admin y ejecutar `/setspawn` en la ubicación deseada.

Cada lobby queda con su propio spawn aislado bajo `spawns.lobby2` sin afectar
al spawn de `lobby1`.

---

## Buenas prácticas

- **Un módulo, una subcarpeta.** Siempre escribir los archivos en
  `plugin.getModuleFolder("<id>")`, nunca en la raíz de `plugins/CoreBau/`. Evita
  colisiones de `config.yml` entre módulos.
- **Recursos con namespace.** Los recursos del JAR van bajo `resources/<id>/...`
  y se leen con la ruta `"<id>/archivo.yml"`.
- **No shadear lo que provee la plataforma.** Adventure, MiniMessage y la API
  de Paper son `compileOnly`. Shadear copias duplicadas rompe el classloader.
- **Relocar todo lo que sí se shadea.** Dos módulos pueden traer la misma
  librería (por ejemplo HikariCP); conviene unificar la versión en
  `gradle.properties` y relocarla a un paquete único.
- **Capturar errores por módulo.** `CoreBauPlugin` ya envuelve cada
  `enable()` / `disable()` en `try/catch`: un módulo que falla no tira abajo a
  los demás. No conviene dejar que `enable()` propague excepciones sin loguear.
- **Una sola versión de paper-api.** Definida en `gradle.properties`
  (`paperApiVersion`). No sobreescribirla por módulo.
- **Conservar paquetes y nombres de tablas al portar.** Renombrar invalida
  datos guardados y configs de los usuarios.
- **Deshabilitar en orden inverso.** Si el módulo A depende de B, registrar A
  después de B; el apagado en orden inverso garantiza que A se cierre antes
  que B.
- **Comandos en un solo lugar.** Todos los comandos basados en `plugin.yml` se
  declaran en el `plugin.yml` fusionado del módulo paper.

---

# English version

## CoreBau (English)

Network monorepo. One repository, one Gradle build, producing two plugins:

- `CoreBau-1.0.0.jar` — Paper (backend servers). Merges the Board, Selector,
  Lobby, Baul and Npc modules under a single plugin.
- `Core-1.0.0.jar` — Velocity (proxy). Separate by platform.

Pet cosmetics, including the full BetterPets ability/level system, live inside
`CoreBau-1.0.0.jar` as an embedded subsystem under `me.davidml16.baul.pets`.
No external pet plugin is required.

Platform constraint: a Velocity plugin and a Paper plugin cannot be the same
`.jar` (different APIs and entry points). That is why the monorepo emits two
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

- **Style A** (small plugins, e.g. Board/Selector): the main class switches
  from `extends JavaPlugin` to `implements Module` and adds delegating helpers
  (`getServer`, `getLogger`, `getConfig` scoped to the module folder) so the
  rest of the code is untouched.
- **Style B** (large plugins, e.g. Baul/Npc): the main class implements
  `org.bukkit.plugin.Plugin` by delegating to the host `CoreBauPlugin`. This
  keeps existing code that passes the plugin to Bukkit APIs (scheduler, events,
  messaging) working. Where an API requires a real `JavaPlugin` (bStats,
  Brigadier command registration), pass the `host` instead of `this`.

### Adding a new module (summary)

1. Copy sources into `paper/src/main/java/<package>/`, resources into
   `paper/src/main/resources/<id>/`.
2. Make the main class `implements Module`; scope config/data to
   `plugin.getModuleFolder("<id>")` and resources to `"<id>/..."`.
3. Register it in `CoreBauPlugin.registerModules()` (order matters: enable a
   module after its dependencies; shutdown runs in reverse).
4. Declare plugin.yml commands in the merged
   `paper/src/main/resources/plugin.yml`.
5. Add shaded libs as `implementation(...)` and relocate them in `shadowJar`;
   use `compileOnly` for server-provided libs and never shade Adventure.
6. Run `./gradlew :paper:build` and verify `Módulo habilitado: <id>` in
   console.

### Adding cosmetics (Baul module)

Cosmetics live in `plugins/CoreBau/baul/cosmetics/`. Each type has its own
YAML file — no Java changes are needed to add new entries.

| File              | Type                                                |
|-------------------|-----------------------------------------------------|
| `trails.yml`      | Particle trails                                     |
| `hats.yml`        | Head items                                          |
| `wings.yml`       | Parametric wings (butterfly, heart, halo, …)        |
| `pets.yml`        | Companion pets (embedded subsystem, 35 species)     |
| `join_effects.yml`| Join particles                                      |
| `emotes.yml`      | Animations                                          |

Pet entries require a `betterPets` template id of the form
`<baseId>_<rarity>` (for example `chicken_common`,
`ender_dragon_legendary`). Available species are listed in
`me.davidml16.baul.pets.registry.PetManager.initializePets`.

The Selector module places an **Ender Chest** item (slot 0 by default) that
runs `baul cosmetics` when clicked. Configure it under `cosmetics-item` in
`selector/config.yml`.

### Lobby system

The `Lobby` module (`cl.xgamers.lobby`) turns each backend paper instance into
a full lobby: per-server spawn, automatic teleport on join, and every essential
protection (anti-damage, anti-hunger, anti-drop, anti-build, antivoid, etc.).
Each paper instance is its own lobby (`lobby1`, `lobby2`, …) with its own
spawn.

Set the lobby identifier in `plugins/CoreBau/lobby/config.yml`:

```yaml
server-id: "lobby1"   # use "lobby2", "lobby3", … on the other paper instances
```

`/setspawn` stores the location under `spawns.<server-id>`, so different
lobbies never overwrite each other's spawn.

**Commands**

| Command        | Permission       | Description                                                                 |
|----------------|------------------|-----------------------------------------------------------------------------|
| `/setspawn`    | `lobby.setspawn` | Saves the player's current location (yaw and pitch included) as the spawn of the current lobby. |
| `/spawn`       | (public)         | Teleports the player to the current lobby spawn. Also used on join and on respawn. |
| `/lobbyreload` | `lobby.admin`    | Reloads `lobby/config.yml` without restarting.                              |

**Protections** (all toggleable in the config):

| Config section              | Effect                                                                                      |
|-----------------------------|---------------------------------------------------------------------------------------------|
| `antivoid`                  | Sends the player back to spawn when they fall below `min-y` (periodic check + move listener).|
| `anti-damage`               | Cancels all damage to players (including VOID when `cancel-void: true`).                    |
| `anti-hunger`               | Keeps food and saturation at max.                                                           |
| `anti-death.keep-inventory` | Enables keep inventory, clears drops and XP.                                                |
| `anti-drop`                 | Blocks item drops. With `protected-items-only: true` only lobby items are blocked.          |
| `anti-inventory-move`       | Blocks inventory clicks/drags outside creative mode.                                        |
| `anti-build`                | Blocks block break/place, buckets, portal creation, and armor stand interaction.            |
| `anti-weather`              | Cancels rain/storm transitions.                                                             |
| `anti-mob-target`           | Prevents mobs from targeting players.                                                       |
| `anti-item-damage`          | Tools and armor lose no durability.                                                         |
| `anti-projectiles`          | Blocks projectile launches by players.                                                      |
| `on-join`                   | On join: tp to spawn, full heal, `ADVENTURE` mode, flight on/off.                           |

**Protected lobby items.** The module recognizes the three Selector items via
their `NamespacedKey`s — `selector` (server compass), `lobby-opener` (lobby
queue emerald), and `cosmetics-opener` (cosmetics ender chest) — and prevents
them from being dropped or moved in the inventory regardless of
`anti-drop.protected-items-only`.

**Bypass permissions:** `lobby.bypass.drop`, `lobby.bypass.inventory`,
`lobby.bypass.build`, `lobby.bypass.projectiles`.

### Best practices

- One module, one subfolder; never write to the `plugins/CoreBau/` root.
- Namespace JAR resources under `<id>/` and read them as `"<id>/file.yml"`.
- Never shade platform-provided libraries (Adventure, MiniMessage, Paper API).
- Relocate everything you do shade; unify shared library versions in
  `gradle.properties`.
- Per-module error isolation: `enable()` / `disable()` are wrapped in
  `try/catch` by `CoreBauPlugin`, so a failing module does not take down the
  others.
- Keep packages and database table names when porting to preserve saved data.
