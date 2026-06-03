package cl.xgamers.board;

import fr.mrmicky.fastboard.FastBoard;
import me.clip.placeholderapi.PlaceholderAPI;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class BoardManager {

    private static final Pattern SERVER_PLACEHOLDER = Pattern.compile("%board_(\\w+?)_(?:online|connected|max)%");

    private final Board plugin;
    private final Map<UUID, FastBoard> boards = new HashMap<>();
    private final Map<UUID, Boolean> toggledOff = new HashMap<>();
    private int titleAnimationIndex = 0;
    private int titleRainbowTicks = 0;
    private int headerAnimationIndex = 0;
    private int footerAnimationIndex = 0;
    private long elapsedTicks = 0;

    // Caches refrescados solo en reload(): evitan reparsear YAML cada tick por cada jugador.
    private boolean animationsEnabled;
    private boolean rainbowTitleEnabled;
    private boolean papiPresent;
    private boolean velocityDebug;
    private int updateIntervalTicks;
    private List<String> cachedLines = Collections.emptyList();
    private List<String> cachedHeaderLines = Collections.emptyList();
    private List<String> cachedFooterLines = Collections.emptyList();
    private List<String> cachedTitleLines = Collections.emptyList();
    private int titleAnimInterval = 20;
    private int headerAnimInterval = 20;
    private int footerAnimInterval = 20;
    private boolean titleAnimateFlag;
    private boolean headerAnimateFlag;
    private boolean footerAnimateFlag;
    // Placeholders %board_<id>_(online|connected|max)% que realmente aparecen en las lineas.
    private Set<String> referencedServerIds = Collections.emptySet();

    public BoardManager(Board plugin) {
        this.plugin = plugin;
        refreshCaches();
    }

    private void refreshCaches() {
        var cfg = plugin.getConfig();
        animationsEnabled    = cfg.getBoolean("animations.enabled", true);
        rainbowTitleEnabled  = cfg.getBoolean("title.rainbow.enabled", false);
        papiPresent          = Bukkit.getPluginManager().isPluginEnabled("PlaceholderAPI");
        velocityDebug        = cfg.getBoolean("velocity.debug", false);
        updateIntervalTicks  = Math.max(1, cfg.getInt("animations.interval", 20));

        cachedLines        = List.copyOf(cfg.getStringList("lines"));
        cachedHeaderLines  = List.copyOf(cfg.getStringList("header.lines"));
        cachedFooterLines  = List.copyOf(cfg.getStringList("footer.lines"));
        cachedTitleLines   = computeTitleLines();

        titleAnimInterval  = Math.max(1, cfg.getInt("title.animation.interval", 20));
        headerAnimInterval = Math.max(1, cfg.getInt("header.animation.interval", 20));
        footerAnimInterval = Math.max(1, cfg.getInt("footer.animation.interval", 20));

        titleAnimateFlag   = cfg.getBoolean("title.animation.enabled", false);
        headerAnimateFlag  = cfg.getBoolean("header.animation.enabled", false);
        footerAnimateFlag  = cfg.getBoolean("footer.animation.enabled", false);

        Set<String> refs = new HashSet<>();
        addReferencedIds(refs, cachedLines);
        addReferencedIds(refs, cachedHeaderLines);
        addReferencedIds(refs, cachedFooterLines);
        referencedServerIds = refs;
    }

    private void addReferencedIds(Set<String> out, List<String> lines) {
        for (String line : lines) {
            Matcher m = SERVER_PLACEHOLDER.matcher(line);
            while (m.find()) out.add(m.group(1));
        }
    }

    private List<String> computeTitleLines() {
        var cfg = plugin.getConfig();
        if (cfg.isString("title")) {
            String s = cfg.getString("title");
            return (s != null && !s.isBlank()) ? List.of(s) : Collections.emptyList();
        }
        List<String> titleLines = cfg.getStringList("title.lines");
        if (!titleLines.isEmpty()) return List.copyOf(titleLines);
        String legacy = cfg.getString("title", "Board");
        return (legacy != null && !legacy.isBlank()) ? List.of(legacy) : Collections.emptyList();
    }

    public void createBoard(Player player) {
        FastBoard board = new FastBoard(player);
        boards.put(player.getUniqueId(), board);
        updateBoard(player);
    }

    public void removeBoard(Player player) {
        FastBoard board = boards.remove(player.getUniqueId());
        if (board != null) {
            board.delete();
        }
    }

    public void updateBoard(Player player) {
        FastBoard board = boards.get(player.getUniqueId());
        if (board == null || isToggledOff(player)) return;

        board.updateTitle(getTitle());

        List<String> headers = cachedHeaderLines;
        List<String> footers = cachedFooterLines;
        List<String> lines = cachedLines;

        String header = getAnimatedLine("header", headerAnimationIndex, "");
        String footer = getAnimatedLine("footer", footerAnimationIndex, "");

        // FastBoard: index 0 = bottom, last index = top (below title)
        int extraLines = (footers.isEmpty() ? 0 : 1) + (headers.isEmpty() ? 0 : 1);
        String[] boardLines = new String[lines.size() + extraLines];
        int index = 0;

        if (!footers.isEmpty()) {
            boardLines[index++] = footer.isEmpty() ? "" : footer;
        }
        for (String line : lines) {
            boardLines[index++] = Hex.colorize(replacePlaceholders(line, player));
        }
        if (!headers.isEmpty()) {
            boardLines[index++] = header.isEmpty() ? "" : header;
        }

        board.updateLines(boardLines);
    }

    private String getTitle() {
        if (isRainbowTitle()) {
            return RainbowAnimator.format(plugin.getConfig(), titleAnimationIndex);
        }
        return Hex.colorize(getAnimatedLine("title", titleAnimationIndex, "Board"));
    }

    private boolean isRainbowTitle() {
        return animationsEnabled && rainbowTitleEnabled;
    }

    private boolean shouldAnimateTitle() {
        if (!animationsEnabled) return false;
        if (isRainbowTitle()) return true;
        return shouldAnimate("title");
    }

    private String getAnimatedLine(String section, int animationIndex, String fallback) {
        List<String> sectionLines = getSectionLines(section);
        if (sectionLines.isEmpty()) return fallback;
        String raw = !shouldAnimate(section)
                ? sectionLines.getFirst()
                : sectionLines.get(animationIndex % sectionLines.size());
        return Hex.colorize(cl.xgamers.corebau.glyph.GlyphRegistry.replace(raw));
    }

    private List<String> getSectionLines(String section) {
        return switch (section) {
            case "title"  -> cachedTitleLines;
            case "header" -> cachedHeaderLines;
            case "footer" -> cachedFooterLines;
            default       -> Collections.emptyList();
        };
    }

    private boolean shouldAnimate(String section) {
        if (!animationsEnabled) return false;
        List<String> sectionLines = getSectionLines(section);
        if (sectionLines.size() < 2) return false;
        return switch (section) {
            case "title"  -> titleAnimateFlag;
            case "header" -> headerAnimateFlag;
            case "footer" -> footerAnimateFlag;
            default       -> false;
        };
    }

    private String replacePlaceholders(String line, Player player) {
        // GlyphRegistry se aplica una sola vez por linea (antes se llamaba aqui y
        // ademas en getAnimatedLine; las lineas del cuerpo solo pasan por aqui).
        line = cl.xgamers.corebau.glyph.GlyphRegistry.replace(line);

        if (line.contains("%board_online%")) {
            line = line.replace("%board_online%", String.valueOf(Bukkit.getOnlinePlayers().size()));
        }

        // Antes iterabamos TODO el registry haciendo 3 replace por entry, aunque
        // la linea no contuviera el placeholder. Ahora solo tocamos ids que
        // realmente aparecen en alguna linea configurada.
        if (!referencedServerIds.isEmpty()) {
            var registry = plugin.getServerRegistry();
            for (String id : referencedServerIds) {
                if (!line.contains("%board_" + id + "_")) continue;
                String count = String.valueOf(plugin.getServerCount(id));
                line = line.replace("%board_" + id + "_online%", count);
                line = line.replace("%board_" + id + "_connected%", count);
                line = line.replace("%board_" + id + "_max%", registry.getMaxPlayers(id));
            }
        }

        if (papiPresent) {
            line = PlaceholderAPI.setPlaceholders(player, line);
        }

        return line;
    }

    public boolean isVelocityDebug() {
        return velocityDebug;
    }

    public void updateAllBoards() {
        for (Player player : Bukkit.getOnlinePlayers()) {
            updateBoard(player);
        }
        elapsedTicks += updateIntervalTicks;
        advanceAnimations();
    }

    private void advanceAnimations() {
        if (!animationsEnabled) return;

        if (shouldAnimateTitle()) {
            if (isRainbowTitle()) {
                titleRainbowTicks++;
                if (titleRainbowTicks >= titleAnimInterval) {
                    titleAnimationIndex++;
                    titleRainbowTicks = 0;
                }
            } else if (elapsedTicks % titleAnimInterval == 0) {
                titleAnimationIndex++;
            }
        }
        if (shouldAnimate("header") && elapsedTicks % headerAnimInterval == 0) {
            headerAnimationIndex++;
        }
        if (shouldAnimate("footer") && elapsedTicks % footerAnimInterval == 0) {
            footerAnimationIndex++;
        }
    }

    public void toggleBoard(Player player) {
        UUID uuid = player.getUniqueId();
        boolean currentlyOff = toggledOff.getOrDefault(uuid, false);
        toggledOff.put(uuid, !currentlyOff);
        if (!currentlyOff) {
            FastBoard board = boards.get(uuid);
            if (board != null) {
                board.updateLines();
            }
        } else {
            updateBoard(player);
        }
    }

    public boolean isToggledOff(Player player) {
        return toggledOff.getOrDefault(player.getUniqueId(), false);
    }

    public void reload() {
        plugin.reloadConfig();
        plugin.reloadServerRegistry();
        refreshCaches();
        titleAnimationIndex = 0;
        titleRainbowTicks = 0;
        headerAnimationIndex = 0;
        footerAnimationIndex = 0;
        elapsedTicks = 0;
        plugin.rescheduleBoardUpdates();
        plugin.rescheduleVelocitySync();
        plugin.requestServerCounts();
        updateAllBoards();
    }
}
