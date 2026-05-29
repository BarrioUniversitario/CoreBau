package me.davidml16.baul.utils;

import com.cryptomorin.xseries.XMaterial;
import me.davidml16.baul.utils.MiniMessageUtils;
import net.kyori.adventure.text.Component;
import org.bukkit.configuration.Configuration;
import org.bukkit.configuration.ConfigurationSection;

import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;

public class Utils {

    private static final Map<net.kyori.adventure.text.format.TextColor, ColorSet<Integer, Integer, Integer>> colorMap = new HashMap<net.kyori.adventure.text.format.TextColor, ColorSet<Integer, Integer, Integer>>();

    static {
        colorMap.put(net.kyori.adventure.text.format.TextColor.color(0, 0, 0), new ColorSet<Integer, Integer, Integer>(0, 0, 0));
        colorMap.put(net.kyori.adventure.text.format.TextColor.color(0, 0, 170), new ColorSet<Integer, Integer, Integer>(0, 0, 170));
        colorMap.put(net.kyori.adventure.text.format.TextColor.color(0, 170, 0), new ColorSet<Integer, Integer, Integer>(0, 170, 0));
        colorMap.put(net.kyori.adventure.text.format.TextColor.color(0, 170, 170), new ColorSet<Integer, Integer, Integer>(0, 170, 170));
        colorMap.put(net.kyori.adventure.text.format.TextColor.color(170, 0, 0), new ColorSet<Integer, Integer, Integer>(170, 0, 0));
        colorMap.put(net.kyori.adventure.text.format.TextColor.color(170, 0, 170), new ColorSet<Integer, Integer, Integer>(170, 0, 170));
        colorMap.put(net.kyori.adventure.text.format.TextColor.color(255, 170, 0), new ColorSet<Integer, Integer, Integer>(255, 170, 0));
        colorMap.put(net.kyori.adventure.text.format.TextColor.color(170, 170, 170), new ColorSet<Integer, Integer, Integer>(170, 170, 170));
        colorMap.put(net.kyori.adventure.text.format.TextColor.color(85, 85, 85), new ColorSet<Integer, Integer, Integer>(85, 85, 85));
        colorMap.put(net.kyori.adventure.text.format.TextColor.color(85, 85, 255), new ColorSet<Integer, Integer, Integer>(85, 85, 255));
        colorMap.put(net.kyori.adventure.text.format.TextColor.color(85, 255, 85), new ColorSet<Integer, Integer, Integer>(85, 255, 85));
        colorMap.put(net.kyori.adventure.text.format.TextColor.color(85, 255, 255), new ColorSet<Integer, Integer, Integer>(85, 255, 255));
        colorMap.put(net.kyori.adventure.text.format.TextColor.color(255, 85, 85), new ColorSet<Integer, Integer, Integer>(255, 85, 85));
        colorMap.put(net.kyori.adventure.text.format.TextColor.color(255, 85, 255), new ColorSet<Integer, Integer, Integer>(255, 85, 255));
        colorMap.put(net.kyori.adventure.text.format.TextColor.color(255, 255, 85), new ColorSet<Integer, Integer, Integer>(255, 255, 85));
        colorMap.put(net.kyori.adventure.text.format.TextColor.color(255, 255, 255), new ColorSet<Integer, Integer, Integer>(255, 255, 255));
    }

    public static String translate(String msg) {
        return MiniMessageUtils.format(msg).toString();
    }

    public static String removeColors(String msg) {
        // For backward compatibility, we'll implement a simple version
        // that removes MiniMessage tags
        if (msg == null) return null;
        return msg.replaceAll("<[^>]*>", "");
    }

    public static net.kyori.adventure.text.format.TextColor fromRGB(int r, int g, int b) {
        TreeMap<Integer, net.kyori.adventure.text.format.TextColor> closest = new TreeMap<Integer, net.kyori.adventure.text.format.TextColor>();
        colorMap.forEach((color, set) -> {
            int red = Math.abs(r - set.getRed());
            int green = Math.abs(g - set.getGreen());
            int blue = Math.abs(b - set.getBlue());
            closest.put(red + green + blue, color);
        });
        return closest.firstEntry().getValue();
    }

    public static net.kyori.adventure.text.format.TextColor getColorByText(String text) {
        for (net.kyori.adventure.text.format.TextColor color : colorMap.keySet())
            if (text.contains("<" + color.toString() + ">")) return color;
        return net.kyori.adventure.text.format.TextColor.color(255, 255, 255);
    }

    public static ColorSet<Integer, Integer, Integer> getRGBbyColor(net.kyori.adventure.text.format.TextColor color) {
        return colorMap.get(color);
    }

    public static ConfigurationSection getConfigurationSection(Configuration config, String path) {
        ConfigurationSection section = config.getConfigurationSection(path);

        if (section == null) section = config.createSection(path);

        return section;
    }

    public static class ColorSet<I extends Number, I1 extends Number, I2 extends Number> {
        Integer red = 0;
        Integer green = 0;
        Integer blue = 0;

        ColorSet(Integer red, Integer green, Integer blue) {
            this.red = red;
            this.green = green;
            this.blue = blue;
        }

        public Integer getRed() {
            return red;
        }

        public Integer getGreen() {
            return green;
        }

        public Integer getBlue() {
            return blue;
        }

    }

}
