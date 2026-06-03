# scripts/

## lobby2-cleanup.sh

Limpia un lobby Paper para que solo haga el trabajo de un lobby. Pensado
para el problema concreto que vimos en el spark profile de lobby2:
mundo nether/end cargados sin uso, WorldGuard + FAWE instalados, gamerules
vanilla activos, view-distance grande.

### Que hace

1. Mueve `world_nether/` y `world_the_end/` a `*.bak-<stamp>/`.
2. En `server.properties`: `allow-nether=false`, `spawn-protection=0`,
   `view-distance=6`, `simulation-distance=4`, `difficulty=peaceful`,
   `spawn-monsters/animals/npcs=false`, `pvp=false`.
3. En `bukkit.yml`: pone `spawn-limits` en 0.
4. Mueve jars de WorldGuard / WorldEdit / FAWE a `plugins/disabled/`.
5. Crea un datapack `<world>/datapacks/lobby-gamerules/` que aplica via
   `#minecraft:load` (tag de funciones que corren al cargar el mundo) un
   set de gamerules optimizados para lobby:
   - `doMobSpawning false`, `doDaylightCycle false`, `doWeatherCycle false`
   - `doFireTick false`, `mobGriefing false`, `doInsomnia false`
   - `randomTickSpeed 0`, `keepInventory true`, `fallDamage false`, etc.
6. En `config/paper-global.yml`: sube el `early-warning-delay` del watchdog
   a 30s para que un freeze esporadico no crashee de inmediato (sin ocultar
   el problema; igual se loguea).
7. Hace `.bak-<stamp>` de cada archivo que toca.

### Uso

```bash
# Apagar el server primero (systemctl/screen/tmux/lo que uses).
chmod +x scripts/lobby2-cleanup.sh

# Dry-run: imprime lo que haria sin tocar nada.
./scripts/lobby2-cleanup.sh /ruta/al/lobby2 -n

# Aplicar pidiendo confirmacion por cada paso destructivo.
./scripts/lobby2-cleanup.sh /ruta/al/lobby2

# Aplicar sin preguntar (usar con cuidado).
./scripts/lobby2-cleanup.sh /ruta/al/lobby2 -y
```

### Rollback

Cada archivo modificado tiene `.bak-<timestamp>`. Para revertir:

```bash
cd /ruta/al/lobby2
mv server.properties.bak-* server.properties
mv bukkit.yml.bak-*        bukkit.yml
mv config/paper-global.yml.bak-* config/paper-global.yml
mv world_nether.bak-*  world_nether
mv world_the_end.bak-* world_the_end
mv plugins/disabled/*.jar plugins/
```

### Notas

- El datapack usa `pack_format: 48` (1.21.x). Si actualizas a otra version
  ajustá el numero.
- El script se rehusa a correr si detecta un proceso `paper*.jar` activo.
- Probalo primero en lobby2 con `-n`, leelo, y despues aplicalo.
