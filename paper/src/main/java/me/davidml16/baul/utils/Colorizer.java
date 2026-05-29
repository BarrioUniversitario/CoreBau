package me.davidml16.baul.utils;

import net.md_5.bungee.api.ChatColor;
import org.jetbrains.annotations.NotNull;

public class Colorizer {

    @NotNull
    public static String plain(@NotNull String str) {
        return str.replace(ChatColor.COLOR_CHAR, '&');
    }

    @NotNull
    public static String strip(@NotNull String str) {
        String stripped = ChatColor.stripColor(str);
        return stripped == null ? "" : stripped;
    }

    @NotNull
    public static String restrip(@NotNull String str) {
        return strip(Colorize.format(str));
    }
}
