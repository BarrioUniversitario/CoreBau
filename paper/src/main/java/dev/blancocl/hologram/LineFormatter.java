package dev.blancocl.hologram;

import dev.blancocl.util.Mini;
import net.kyori.adventure.text.Component;
import org.bukkit.entity.Player;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/** Centralised MiniMessage + PAPI evaluator for hologram lines with animation support. */
public final class LineFormatter {

    private static final Pattern ANIM_PATTERN = Pattern.compile("<#ANIM:([^>]+)>([\\s\\S]*?)</#ANIM>");
    private static final Pattern RAINBOW_PATTERN = Pattern.compile("<rainbow(?::(\\d+))?>");
    private static final Pattern GRADIENT_PATTERN = Pattern.compile("<(rainbow|gradient):([^>]*)>");
    private static final Pattern GRADIENT_CLOSE = Pattern.compile("</(rainbow|gradient)>");

    private LineFormatter() {}

    public static Component render(String miniMessage, Player viewer, int frame) {
        if (miniMessage == null || miniMessage.isEmpty()) return Component.empty();

        String processed = miniMessage;

        // Process DH-style animations: <#ANIM:name>text</#ANIM>
        processed = processDHAnimations(processed, frame);

        // Animate rainbow gradients by shifting hue based on frame
        processed = animateRainbow(processed, frame);

        return Mini.parse(processed, viewer);
    }

    private static String processDHAnimations(String input, int frame) {
        Matcher matcher = ANIM_PATTERN.matcher(input);
        StringBuffer sb = new StringBuffer();
        while (matcher.find()) {
            String animType = matcher.group(1);
            String content = matcher.group(2);
            matcher.appendReplacement(sb, applyDHAnimation(content, animType, frame));
        }
        matcher.appendTail(sb);
        return sb.toString();
    }

    private static String applyDHAnimation(String content, String animType, int frame) {
        String[] parts = animType.split(":");
        String name = parts[0].toLowerCase();

        return switch (name) {
            case "colors" -> applyColorsAnimation(content, frame);
            case "wave" -> applyWaveAnimation(content, parts.length > 1 ? parts[1] : "&f", parts.length > 2 ? parts[2] : "&b");
            case "burn" -> applyBurnAnimation(content, parts.length > 1 ? parts[1] : "&f", parts.length > 2 ? parts[2] : "&e");
            case "typewriter" -> applyTypewriterAnimation(content, frame);
            case "scroll" -> applyScrollAnimation(content, frame);
            default -> content;
        };
    }

    private static String applyColorsAnimation(String text, int frame) {
        char[] colors = {'0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'a', 'b', 'c', 'd', 'e', 'f'};
        StringBuilder result = new StringBuilder();
        for (int i = 0; i < text.length(); i++) {
            char c = text.charAt(i);
            if (c != ' ') {
                int colorIdx = (frame + i) % colors.length;
                result.append("&").append(colors[colorIdx]).append(c);
            } else {
                result.append(' ');
            }
        }
        return result.toString();
    }

    private static String applyWaveAnimation(String text, String color1, String color2) {
        StringBuilder result = new StringBuilder();
        for (int i = 0; i < text.length(); i++) {
            char c = text.charAt(i);
            if (c != ' ') {
                if (i % 2 == 0) {
                    result.append(color1).append(c);
                } else {
                    result.append(color2).append(c);
                }
            } else {
                result.append(' ');
            }
        }
        return result.toString();
    }

    private static String applyBurnAnimation(String text, String color1, String color2) {
        return color1 + text;
    }

    private static String applyTypewriterAnimation(String text, int frame) {
        int visibleChars = Math.min(text.length(), Math.max(0, frame % (text.length() + 20)));
        if (visibleChars <= 0) return "";
        return text.substring(0, Math.min(Math.max(0, visibleChars), text.length()));
    }

    private static String applyScrollAnimation(String text, int frame) {
        if (text.length() <= 3) return text;
        int displayLen = Math.max(1, (text.length() / 3) * 2);
        int maxOffset = Math.max(0, text.length() - displayLen);
        int offset = Math.max(0, (frame / 2) % Math.max(1, maxOffset + 1));
        return text.substring(offset, Math.min(offset + displayLen, text.length()));
    }

    private static String animateRainbow(String input, int frame) {
        Matcher matcher = RAINBOW_PATTERN.matcher(input);
        StringBuffer sb = new StringBuffer();
        while (matcher.find()) {
            String existing = matcher.group(1);
            int hueShift = (existing != null ? Integer.parseInt(existing) : 0) + frame;
            matcher.appendReplacement(sb, "<rainbow:" + (hueShift % 360) + ">");
        }
        matcher.appendTail(sb);
        return sb.toString();
    }
}