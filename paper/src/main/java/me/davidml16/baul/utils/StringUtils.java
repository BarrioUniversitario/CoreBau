package me.davidml16.baul.utils;

import me.davidml16.baul.utils.random.Rnd;
import org.bukkit.Color;
import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.function.Supplier;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class StringUtils {

    @NotNull
    public static String oneSpace(@NotNull String str) {
        return str.trim().replaceAll("\\s+", " ");
    }

    @NotNull
    public static String noSpace(@NotNull String str) {
        return str.trim().replaceAll("\\s+", "");
    }

    @NotNull
    public static List<String> replace(@NotNull List<String> orig, @NotNull String placeholder, boolean keep, String... replacer) {
        return StringUtils.replace(orig, placeholder, keep, Arrays.asList(replacer));
    }

    @NotNull
    public static List<String> replace(@NotNull List<String> orig, @NotNull String placeholder, boolean keep, List<String> replacer) {
        List<String> replaced = new ArrayList<>();
        for (String line : orig) {
            if (line.contains(placeholder)) {
                if (!keep) {
                    replaced.addAll(replacer);
                }
                else {
                    replacer.forEach(lineRep -> replaced.add(line.replace(placeholder, lineRep)));
                }
                continue;
            }
            replaced.add(line);
        }

        return replaced;
    }

    @NotNull
    public static String replaceEach(@NotNull String text, @NotNull List<Pair<String, Supplier<String>>> replacements) {
        if (text.isEmpty() || replacements.isEmpty()) {
            return text;
        }

        final int searchLength = replacements.size();
        // keep track of which still have matches
        final boolean[] noMoreMatchesForReplIndex = new boolean[searchLength];

        // index on index that the match was found
        int textIndex = -1;
        int replaceIndex = -1;
        int tempIndex;

        // index of replace array that will replace the search string found
        // NOTE: logic duplicated below START
        for (int i = 0; i < searchLength; i++) {
            if (noMoreMatchesForReplIndex[i]) {
                continue;
            }
            tempIndex = text.indexOf(replacements.get(i).getFirst());

            // see if we need to keep searching for this
            if (tempIndex == -1) {
                noMoreMatchesForReplIndex[i] = true;
            }
            else if (textIndex == -1 || tempIndex < textIndex) {
                textIndex = tempIndex;
                replaceIndex = i;
            }
        }
        // NOTE: logic mostly below END

        // no search strings found, we are done
        if (textIndex == -1) {
            return text;
        }

        int start = 0;
        final StringBuilder buf = new StringBuilder();
        while (textIndex != -1) {
            for (int i = start; i < textIndex; i++) {
                buf.append(text.charAt(i));
            }
            buf.append(replacements.get(replaceIndex).getSecond().get());

            start = textIndex + replacements.get(replaceIndex).getFirst().length();

            textIndex = -1;
            replaceIndex = -1;
            // find the next earliest match
            // NOTE: logic mostly duplicated above START
            for (int i = 0; i < searchLength; i++) {
                if (noMoreMatchesForReplIndex[i]) {
                    continue;
                }
                tempIndex = text.indexOf(replacements.get(i).getFirst(), start);

                // see if we need to keep searching for this
                if (tempIndex == -1) {
                    noMoreMatchesForReplIndex[i] = true;
                } else if (textIndex == -1 || tempIndex < textIndex) {
                    textIndex = tempIndex;
                    replaceIndex = i;
                }
            }
            // NOTE: logic duplicated above END

        }
        final int textLength = text.length();
        for (int i = start; i < textLength; i++) {
            buf.append(text.charAt(i));
        }
        return buf.toString();
    }

    public static double getDouble(@NotNull String input, double def) {
        return getDouble(input, def, false);
    }

    public static double getDouble(@NotNull String input, double def, boolean allowNegative) {
        try {
            double amount = Double.parseDouble(input);
            return (amount < 0D && !allowNegative ? def : amount);
        }
        catch (NumberFormatException ex) {
            return def;
        }
    }

    public static float getFloat(@NotNull String input, float def) {
        return getFloat(input, def, false);
    }

    public static float getFloat(@NotNull String input, float def, boolean allowNegative) {
        try {
            float amount = Float.parseFloat(input);
            return (amount < 0F && !allowNegative ? def : amount);
        } catch (NumberFormatException ex) {
            return def;
        }
    }

    public static int getInteger(@NotNull String input, int def) {
        return getInteger(input, def, false);
    }

    public static int getInteger(@NotNull String input, int def, boolean allowNegative) {
        return (int) getDouble(input, def, allowNegative);
    }

    public static int[] getIntArray(@NotNull String str) {
        String[] split = noSpace(str).split(",");
        int[] array = new int[split.length];
        for (int index = 0; index < split.length; index++) {
            try {
                array[index] = Integer.parseInt(split[index]);
            }
            catch (NumberFormatException e) {
                array[index] = 0;
            }
        }
        return array;
    }

    @NotNull
    public static <T extends Enum<T>> Optional<T> getEnum(@NotNull String str, @NotNull Class<T> clazz) {
        try {
            return Optional.of(Enum.valueOf(clazz, str.toUpperCase()));
        }
        catch (Exception ex) {
            return Optional.empty();
        }
    }

    private static final Map<String, Color> NAMED_COLORS = Map.ofEntries(
            Map.entry("black", Color.fromRGB(0, 0, 0)),
            Map.entry("white", Color.fromRGB(255, 255, 255)),
            Map.entry("red", Color.fromRGB(255, 0, 0)),
            Map.entry("green", Color.fromRGB(0, 255, 0)),
            Map.entry("blue", Color.fromRGB(0, 0, 255)),
            Map.entry("yellow", Color.fromRGB(255, 255, 0)),
            Map.entry("aqua", Color.fromRGB(0, 255, 255)),
            Map.entry("cyan", Color.fromRGB(0, 255, 255)),
            Map.entry("pink", Color.fromRGB(255, 192, 203)),
            Map.entry("purple", Color.fromRGB(128, 0, 128)),
            Map.entry("magenta", Color.fromRGB(255, 0, 255)),
            Map.entry("orange", Color.fromRGB(255, 165, 0)),
            Map.entry("lime", Color.fromRGB(0, 255, 0)),
            Map.entry("gray", Color.fromRGB(128, 128, 128)),
            Map.entry("grey", Color.fromRGB(128, 128, 128)),
            Map.entry("dark_gray", Color.fromRGB(64, 64, 64)),
            Map.entry("dark_grey", Color.fromRGB(64, 64, 64)),
            Map.entry("light_gray", Color.fromRGB(192, 192, 192)),
            Map.entry("light_grey", Color.fromRGB(192, 192, 192)),
            Map.entry("gold", Color.fromRGB(255, 215, 0)),
            Map.entry("brown", Color.fromRGB(150, 75, 0)),
            Map.entry("navy", Color.fromRGB(0, 0, 128)),
            Map.entry("maroon", Color.fromRGB(128, 0, 0)),
            Map.entry("olive", Color.fromRGB(128, 128, 0)),
            Map.entry("teal", Color.fromRGB(0, 128, 128))
    );

    @NotNull
    public static Color parseColor(@NotNull String colorRaw) {
        String raw = colorRaw.trim();
        if (raw.isEmpty()) throw new IllegalArgumentException("Empty color");

        if (raw.startsWith("#") || raw.startsWith("0x")) {
            String hex = raw.startsWith("#") ? raw.substring(1) : raw.substring(2);
            if (hex.length() == 3) {
                hex = "" + hex.charAt(0) + hex.charAt(0)
                        + hex.charAt(1) + hex.charAt(1)
                        + hex.charAt(2) + hex.charAt(2);
            }
            if (hex.length() == 6) {
                try {
                    int rgb = Integer.parseInt(hex, 16);
                    return Color.fromRGB((rgb >> 16) & 0xFF, (rgb >> 8) & 0xFF, rgb & 0xFF);
                } catch (NumberFormatException ex) {
                    throw new IllegalArgumentException("Invalid hex color: " + colorRaw, ex);
                }
            }
            throw new IllegalArgumentException("Invalid hex color length: " + colorRaw);
        }

        if (raw.contains(",")) {
            String[] rgb = raw.split(",");
            if (rgb.length < 1 || rgb.length > 3) {
                throw new IllegalArgumentException("Invalid RGB color: " + colorRaw);
            }
            int red = StringUtils.getInteger(rgb[0].trim(), 0);
            int green = rgb.length >= 2 ? StringUtils.getInteger(rgb[1].trim(), 0) : 0;
            int blue = rgb.length >= 3 ? StringUtils.getInteger(rgb[2].trim(), 0) : 0;
            return Color.fromRGB(clamp(red), clamp(green), clamp(blue));
        }

        String normalized = raw.toLowerCase(Locale.ROOT).replace(' ', '_');
        Color named = NAMED_COLORS.get(normalized);
        if (named != null) return named;

        throw new IllegalArgumentException("Unknown color: " + colorRaw);
    }

    private static int clamp(int value) {
        return value < 0 ? 0 : Math.min(value, 255);
    }

    @NotNull
    public static String lowerCaseUnderscore(@NotNull String str) {
        return Colorize.restrip(str).toLowerCase().replace(" ", "_");
    }

    @NotNull
    public static String capitalizeUnderscored(@NotNull String str) {
        return capitalizeFully(str.replace("_", " "));
    }

    @NotNull
    public static String capitalizeFully(@NotNull String str) {
        if (str.length() != 0) {
            str = str.toLowerCase();
            return capitalize(str);
        }
        return str;
    }

    @NotNull
    public static String capitalize(@NotNull String str) {
        if (str.length() != 0) {
            int strLen = str.length();
            StringBuilder buffer = new StringBuilder(strLen);
            boolean capitalizeNext = true;

            for (int i = 0; i < strLen; ++i) {
                char ch = str.charAt(i);
                if (Character.isWhitespace(ch)) {
                    buffer.append(ch);
                    capitalizeNext = true;
                }
                else if (capitalizeNext) {
                    buffer.append(Character.toTitleCase(ch));
                    capitalizeNext = false;
                }
                else {
                    buffer.append(ch);
                }
            }
            return buffer.toString();
        }
        return str;
    }

    @NotNull
    public static String capitalizeFirstLetter(@NotNull String original) {
        if (original.isEmpty()) return original;
        return original.substring(0, 1).toUpperCase() + original.substring(1);
    }

    /**
     * @param original List to remove empty lines from.
     * @return A list with no multiple empty lines in a row.
     */
    @NotNull
    public static List<String> stripEmpty(@NotNull List<String> original) {
        List<String> stripped = new ArrayList<>();
        for (int index = 0; index < original.size(); index++) {
            String line = original.get(index);
            if (line.isEmpty()) {
                String last = stripped.isEmpty() ? null : stripped.get(stripped.size() - 1);
                if (last == null || last.isEmpty() || index == (original.size() - 1)) continue;
            }
            stripped.add(line);
        }
        return stripped;
    }

    /**
     * Kinda half-smart completer like in IDEA by partial word matches.
     * @param results A list of all completions.
     * @param input A string to find partial matches for.
     * @param steps Part's size.
     * @return A list of completions that has partial matches to the given string.
     */
    @NotNull
    public static List<String> getByPartialMatches(@NotNull List<String> results, @NotNull String input, int steps) {
        StringBuilder builder = new StringBuilder();
        for (char letter : input.toLowerCase().toCharArray()) {
            builder.append(Pattern.quote(String.valueOf(letter))).append("(?:.*)");
        }

        Pattern pattern = Pattern.compile(builder.toString());
        List<String> result = new ArrayList<>();
        for (String orig : results) {
            if (pattern.matcher(orig.toLowerCase()).find()) {
                result.add(orig);
            }
        }
        List<String> list = new ArrayList<>(result);
        Collections.sort(list);
        return list;
    }

    @NotNull
    public static String extractCommandName(@NotNull String command) {
        String commandName = Colorize.strip(command).split(" ")[0].substring(1);

        String[] pluginPrefix = commandName.split(":");
        if (pluginPrefix.length == 2) {
            commandName = pluginPrefix[1];
        }

        return commandName;
    }

    @Deprecated
    public static boolean isCustomBoolean(@NotNull String str) {
        String[] customs = new String[]{"0","1","on","off","true","false","yes","no"};
        return Stream.of(customs).collect(Collectors.toSet()).contains(str.toLowerCase());
    }

    @Deprecated
    public static boolean parseCustomBoolean(@NotNull String str) {
        if (str.equalsIgnoreCase("0") || str.equalsIgnoreCase("off") || str.equalsIgnoreCase("no")) {
            return false;
        }
        if (str.equalsIgnoreCase("1") || str.equalsIgnoreCase("on") || str.equalsIgnoreCase("yes")) {
            return true;
        }
        return Boolean.parseBoolean(str);
    }
}