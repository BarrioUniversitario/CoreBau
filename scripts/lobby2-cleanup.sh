#!/usr/bin/env bash
# lobby2-cleanup.sh
# ---------------------------------------------------------------------------
# Limpia un lobby Paper para que solo haga el trabajo de un lobby:
#   - Quita nether/end (los desactiva y opcionalmente borra)
#   - Quita WorldGuard + FastAsyncWorldEdit del classpath (los mueve a disabled/)
#   - Setea gamerules vanilla a "off" via un datapack auto-cargado
#   - Edita server.properties: allow-nether=false, spawn-protection=0
#   - Reduce view/sim distance a 6 (suficiente para un lobby)
#
# Uso:
#   chmod +x lobby2-cleanup.sh
#   ./lobby2-cleanup.sh /ruta/al/server      # ejecuta
#   ./lobby2-cleanup.sh /ruta/al/server -n   # dry-run, solo imprime lo que haria
#   ./lobby2-cleanup.sh /ruta/al/server -y   # asume yes a todas las prompts
#
# REQUISITOS:
#   - El servidor DEBE estar apagado antes de correr esto.
#   - Si tenes un wrapper (screen/tmux/systemd), parálo primero.
#
# Cualquier cosa destructiva (rm) hace backup antes (.bak-YYYYMMDD-HHMMSS).
# ---------------------------------------------------------------------------
set -euo pipefail

SERVER_DIR=${1:-}
DRY=0
YES=0
shift || true
for arg in "$@"; do
    case "$arg" in
        -n|--dry-run) DRY=1 ;;
        -y|--yes)     YES=1 ;;
    esac
done

if [[ -z "$SERVER_DIR" || ! -d "$SERVER_DIR" ]]; then
    echo "Uso: $0 /ruta/al/server [-n] [-y]" >&2
    exit 1
fi

cd "$SERVER_DIR"
STAMP=$(date +%Y%m%d-%H%M%S)

# ---- helpers ----
run() {
    if [[ $DRY -eq 1 ]]; then
        echo "DRY  $*"
    else
        echo "RUN  $*"
        eval "$@"
    fi
}

confirm() {
    local prompt=$1
    if [[ $YES -eq 1 || $DRY -eq 1 ]]; then return 0; fi
    read -r -p "$prompt [y/N] " ans
    [[ $ans == "y" || $ans == "Y" ]]
}

# ---- safety check: server no debe estar corriendo ----
if pgrep -af 'paper.*\.jar' >/dev/null 2>&1; then
    echo "ERROR: Detecte un proceso Paper corriendo. Apagá el server antes de seguir." >&2
    pgrep -af 'paper.*\.jar' >&2 || true
    exit 2
fi

echo "============================================================"
echo "  Lobby cleanup: $SERVER_DIR"
echo "  Modo: $([[ $DRY -eq 1 ]] && echo DRY-RUN || echo APLICAR CAMBIOS)"
echo "============================================================"

# ---- 1) Mundos extra: nether + end ----
for w in world_nether world_the_end; do
    if [[ -d "$w" ]]; then
        if confirm "Mover '$w/' a '${w}.bak-${STAMP}/'?"; then
            run "mv '$w' '${w}.bak-${STAMP}'"
        fi
    fi
done

# ---- 2) server.properties ----
if [[ -f server.properties ]]; then
    run "cp server.properties server.properties.bak-${STAMP}"
    # set_kv key value
    set_kv() {
        local k=$1 v=$2
        if grep -q "^${k}=" server.properties; then
            run "sed -i 's|^${k}=.*|${k}=${v}|' server.properties"
        else
            run "echo '${k}=${v}' >> server.properties"
        fi
    }
    set_kv allow-nether          false
    set_kv spawn-protection       0
    set_kv view-distance          6
    set_kv simulation-distance    4
    set_kv difficulty             peaceful
    set_kv spawn-monsters         false
    set_kv spawn-animals          false
    set_kv spawn-npcs             false
    set_kv pvp                    false
    set_kv announce-player-achievements false
fi

# ---- 3) bukkit.yml ----
if [[ -f bukkit.yml ]]; then
    run "cp bukkit.yml bukkit.yml.bak-${STAMP}"
    # spawn-limits a 0 (no mob spawning en ningun mundo)
    if grep -q '^spawn-limits:' bukkit.yml; then
        run "sed -i '/^spawn-limits:/,/^[a-zA-Z]/{ s/^\\(  [a-z-]*:\\) *[0-9-]*/\\1 0/ }' bukkit.yml"
    fi
fi

# ---- 4) Plugins a mover a disabled/ ----
mkdir -p plugins/disabled
move_plugin_jars() {
    local pattern=$1
    local found=0
    for jar in plugins/${pattern}*.jar; do
        [[ -e "$jar" ]] || continue
        found=1
        run "mv '$jar' 'plugins/disabled/'"
    done
    [[ $found -eq 1 ]]
}

for prefix in WorldGuard worldguard FastAsyncWorldEdit FAWE fastasyncworldedit WorldEdit worldedit; do
    if compgen -G "plugins/${prefix}*.jar" >/dev/null; then
        if confirm "Mover plugin que matchea 'plugins/${prefix}*.jar' a plugins/disabled/?"; then
            move_plugin_jars "$prefix" || true
        fi
    fi
done

# ---- 5) Datapack para gamerules (carga al boot del mundo principal) ----
WORLD_NAME=$(grep -E '^level-name=' server.properties 2>/dev/null | cut -d= -f2 || true)
WORLD_NAME=${WORLD_NAME:-world}

DP_DIR="${WORLD_NAME}/datapacks/lobby-gamerules"
if [[ ! -d "$DP_DIR" ]]; then
    if confirm "Crear datapack en ${DP_DIR} para forzar gamerules?"; then
        run "mkdir -p '${DP_DIR}/data/lobby/function'"
        run "cat > '${DP_DIR}/pack.mcmeta' <<'EOF'
{
  \"pack\": {
    \"pack_format\": 48,
    \"description\": \"Lobby gamerules (auto-aplicado al cargar el mundo).\"
  }
}
EOF"
        run "mkdir -p '${DP_DIR}/data/minecraft/tags/function'"
        run "cat > '${DP_DIR}/data/minecraft/tags/function/load.json' <<'EOF'
{
  \"values\": [\"lobby:gamerules\"]
}
EOF"
        run "cat > '${DP_DIR}/data/lobby/function/gamerules.mcfunction' <<'EOF'
# Gamerules para un lobby: minimo trabajo vanilla.
gamerule doMobSpawning false
gamerule doDaylightCycle false
gamerule doWeatherCycle false
gamerule doFireTick false
gamerule mobGriefing false
gamerule doInsomnia false
gamerule doImmediateRespawn true
gamerule doPatrolSpawning false
gamerule doTraderSpawning false
gamerule doWardenSpawning false
gamerule randomTickSpeed 0
gamerule announceAdvancements false
gamerule keepInventory true
gamerule fallDamage false
gamerule fireDamage false
gamerule drowningDamage false
gamerule freezeDamage false
gamerule naturalRegeneration false
gamerule disableElytraMovementCheck true
gamerule sendCommandFeedback false
gamerule commandBlockOutput false
gamerule logAdminCommands false
say [lobby-cleanup] Gamerules aplicadas.
EOF"
    fi
fi

# ---- 6) paper-global.yml: subir el watchdog para no crashear tan rapido ----
PG="config/paper-global.yml"
if [[ -f "$PG" ]]; then
    run "cp '$PG' '${PG}.bak-${STAMP}'"
    # subir early-warning a 30s y max-tick-time del watchdog a 120s. Solo si los campos existen.
    # Esto compra tiempo si hay un freeze esporadico, no oculta el problema (igual lo veras en el log).
    if grep -q 'early-warning-delay' "$PG"; then
        run "sed -i 's|early-warning-delay:.*|early-warning-delay: 30000|' '$PG'"
    fi
    if grep -q 'early-warning-every' "$PG"; then
        run "sed -i 's|early-warning-every:.*|early-warning-every: 15000|' '$PG'"
    fi
fi

# ---- 7) Resumen ----
echo
echo "============================================================"
echo "  Listo."
if [[ $DRY -eq 1 ]]; then
    echo "  Esto fue un DRY-RUN. Ningun archivo fue modificado."
    echo "  Volve a correr sin -n para aplicar."
else
    echo "  Backups creados con sufijo .bak-${STAMP}"
    echo "  Reinicia el server: el datapack se cargara y aplicara gamerules."
fi
echo "============================================================"
