package me.davidml16.baul.utils;

import me.davidml16.baul.Main;
import me.davidml16.baul.objects.CubeletMachine;
import me.davidml16.baul.objects.CubeletType;
import me.davidml16.baul.objects.rewards.Reward;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class Colorize {

    private static final Pattern HEX_PATTERN = Pattern.compile("&#([A-Fa-f0-9]{6})");
    private static final Pattern HEX_BARE_PATTERN = Pattern.compile("(?<!<|:)#([A-Fa-f0-9]{6})");

    private static final MiniMessage MINI_MESSAGE = MiniMessage.builder().build();
    private static final LegacyComponentSerializer LEGACY = LegacyComponentSerializer.builder()
            .hexColors()
            .useUnusualXRepeatedCharacterHexFormat()
            .build();

    private Colorize() {
    }

    public static String escapeTags(String input) {
        if (input == null || input.isEmpty()) return input;
        return input.replace("\\", "\\\\").replace("<", "\\<").replace(">", "\\>");
    }

    public static Component parseMessage(String message) {
        if (message == null || message.isEmpty()) {
            return Component.empty();
        }
        String converted = legacyToMiniMessage(message);
        try {
            return MINI_MESSAGE.deserialize(converted);
        } catch (Exception e) {
            Bukkit.getLogger().warning("[Baul] Failed to parse MiniMessage: " + converted);
            return Component.text(message);
        }
    }

    public static String format(String message) {
        if (message == null || message.isEmpty()) {
            return "";
        }
        return LEGACY.serialize(parseMessage(message));
    }

    public static String format(Component component) {
        if (component == null) return "";
        return LEGACY.serialize(component);
    }

    public static List<String> format(List<String> lines) {
        List<String> result = new ArrayList<>();
        if (lines == null) return result;
        for (String line : lines) {
            result.add(format(line));
        }
        return result;
    }

    public static String formatTemplate(String template, Map<String, String> placeholders) {
        if (template == null || template.isEmpty()) return "";
        String merged = template;
        if (placeholders != null) {
            for (Map.Entry<String, String> entry : placeholders.entrySet()) {
                String value = entry.getValue() == null ? "" : entry.getValue();
                merged = merged.replace("%" + entry.getKey() + "%", escapeTags(value));
            }
        }
        return format(merged);
    }

    public static String toPlainText(Component component) {
        if (component == null) return "";
        return PlainTextComponentSerializer.plainText().serialize(component);
    }

    public static String legacyToMiniMessage(String input) {
        if (input == null || input.isEmpty()) return input;

        input = input.replace("§", "&");

        Matcher hexMatcher = HEX_PATTERN.matcher(input);
        StringBuffer sb = new StringBuffer();
        while (hexMatcher.find()) {
            hexMatcher.appendReplacement(sb, "<color:#$1>");
        }
        hexMatcher.appendTail(sb);
        input = sb.toString();

        input = input.replace("&0", "<black>");
        input = input.replace("&1", "<dark_blue>");
        input = input.replace("&2", "<dark_green>");
        input = input.replace("&3", "<dark_aqua>");
        input = input.replace("&4", "<dark_red>");
        input = input.replace("&5", "<dark_purple>");
        input = input.replace("&6", "<gold>");
        input = input.replace("&7", "<gray>");
        input = input.replace("&8", "<dark_gray>");
        input = input.replace("&9", "<blue>");
        input = input.replace("&a", "<green>");
        input = input.replace("&b", "<aqua>");
        input = input.replace("&c", "<red>");
        input = input.replace("&d", "<light_purple>");
        input = input.replace("&e", "<yellow>");
        input = input.replace("&f", "<white>");
        input = input.replace("&k", "<obfuscated>");
        input = input.replace("&l", "<bold>");
        input = input.replace("&m", "<strikethrough>");
        input = input.replace("&n", "<underline>");
        input = input.replace("&o", "<italic>");
        input = input.replace("&r", "<reset>");

        input = input.replaceAll("<gradient:([^>]+)>([^<]*)</gradient:[^>]+>", "<gradient:$1>$2</gradient>");
        input = input.replaceAll("<gradient:([^>]+)>", "<gradient:$1>");

        Matcher bareHex = HEX_BARE_PATTERN.matcher(input);
        sb = new StringBuffer();
        while (bareHex.find()) {
            bareHex.appendReplacement(sb, "<color:#$1>");
        }
        bareHex.appendTail(sb);
        input = sb.toString();

        return input;
    }

    public static void sendMessage(CommandSender sender, String message) {
        sender.sendMessage(format(message));
    }

    public static void sendMessage(CommandSender sender, Component component) {
        sender.sendMessage(format(component));
    }

    public static void actionBar(Player player, String message) {
        if (player == null) return;
        player.spigot().sendMessage(ChatMessageType.ACTION_BAR, TextComponent.fromLegacyText(format(message)));
    }

    public static void tell(Player player, String... messages) {
        if (player == null || messages == null) return;
        for (String message : messages) {
            player.sendMessage(format(message));
        }
    }

    public static void send(Player player, String message) {
        if (player == null) return;
        player.sendMessage(format(message));
    }

    public enum DefaultFontInfo {

        A('A', 5),
        a('a', 5),
        B('B', 5),
        b('b', 5),
        C('C', 5),
        c('c', 5),
        D('D', 5),
        d('d', 5),
        E('E', 5),
        e('e', 5),
        F('F', 5),
        f('f', 4),
        G('G', 5),
        g('g', 5),
        H('H', 5),
        h('h', 5),
        I('I', 3),
        i('i', 1),
        J('J', 5),
        j('j', 5),
        K('K', 5),
        k('k', 4),
        L('L', 5),
        l('l', 1),
        M('M', 5),
        m('m', 5),
        N('N', 5),
        n('n', 5),
        O('O', 5),
        o('o', 5),
        P('P', 5),
        p('p', 5),
        Q('Q', 5),
        q('q', 5),
        R('R', 5),
        r('r', 5),
        S('S', 5),
        s('s', 5),
        T('T', 5),
        t('t', 4),
        U('U', 5),
        u('u', 5),
        V('V', 5),
        v('v', 5),
        W('W', 5),
        w('w', 5),
        X('X', 5),
        x('x', 5),
        Y('Y', 5),
        y('y', 5),
        Z('Z', 5),
        z('z', 5),
        NUM_1('1', 5),
        NUM_2('2', 5),
        NUM_3('3', 5),
        NUM_4('4', 5),
        NUM_5('5', 5),
        NUM_6('6', 5),
        NUM_7('7', 5),
        NUM_8('8', 5),
        NUM_9('9', 5),
        NUM_0('0', 5),
        EXCLAMATION_POINT('!', 1),
        AT_SYMBOL('@', 6),
        NUM_SIGN('#', 5),
        DOLLAR_SIGN('$', 5),
        PERCENT('%', 5),
        UP_ARROW('^', 5),
        AMPERSAND('&', 5),
        ASTERISK('*', 5),
        LEFT_PARENTHESIS('(', 4),
        RIGHT_PERENTHESIS(')', 4),
        MINUS('-', 5),
        UNDERSCORE('_', 5),
        PLUS_SIGN('+', 5),
        EQUALS_SIGN('=', 5),
        LEFT_CURL_BRACE('{', 4),
        RIGHT_CURL_BRACE('}', 4),
        LEFT_BRACKET('[', 3),
        RIGHT_BRACKET(']', 3),
        COLON(':', 1),
        SEMI_COLON(';', 1),
        DOUBLE_QUOTE('"', 3),
        SINGLE_QUOTE('\'', 1),
        LEFT_ARROW('<', 4),
        RIGHT_ARROW('>', 4),
        QUESTION_MARK('?', 5),
        SLASH('/', 5),
        BACK_SLASH('\\', 5),
        LINE('|', 1),
        TILDE('~', 5),
        TICK('`', 2),
        PERIOD('.', 1),
        COMMA(',', 1),
        SPACE(' ', 3),
        DEFAULT('a', 4);

        private char character;
        private int length;

        DefaultFontInfo(char character, int length) {
            this.character = character;
            this.length = length;
        }

        public char getCharacter() {
            return this.character;
        }

        public int getLength() {
            return this.length;
        }

        public int getBoldLength() {
            if (this == DefaultFontInfo.SPACE) return this.getLength();
            return this.length + 1;
        }

        public static DefaultFontInfo getDefaultFontInfo(char c) {
            for (DefaultFontInfo dFI : DefaultFontInfo.values()) {
                if (dFI.getCharacter() == c) return dFI;
            }
            return DefaultFontInfo.DEFAULT;
        }
    }

    private final static int CENTER_PX = 154;

    public static String centeredMessage(String message) {
        String[] lines = ChatColor.translateAlternateColorCodes('&', message).split("\n", 40);
        StringBuilder returnMessage = new StringBuilder();

        for (String line : lines) {
            int messagePxSize = 0;
            boolean previousCode = false;
            boolean isBold = false;

            for (char c : line.toCharArray()) {
                if (c == '§') {
                    previousCode = true;
                } else if (previousCode) {
                    previousCode = false;
                    isBold = c == 'l';
                } else {
                    DefaultFontInfo dFI = DefaultFontInfo.getDefaultFontInfo(c);
                    messagePxSize = isBold ? messagePxSize + dFI.getBoldLength() : messagePxSize + dFI.getLength();
                    messagePxSize++;
                }
            }
            int toCompensate = CENTER_PX - messagePxSize / 2;
            int spaceLength = DefaultFontInfo.SPACE.getLength() + 1;
            int compensated = 0;
            StringBuilder sb = new StringBuilder();
            while (compensated < toCompensate) {
                sb.append(" ");
                compensated += spaceLength;
            }
            returnMessage.append(sb.toString()).append(line).append("\n");
        }

        return returnMessage.toString();
    }

    public static void sendLootMessage(CubeletMachine cubeletMachine, CubeletType cubeletType, Reward reward) {
        Player target = Bukkit.getPlayer(cubeletMachine.getPlayerOpening().getUuid());
        if (target != null) {
            if (!Main.get().isSetting("Rewards.Duplication.Enabled")) {
                newLootMessage(target, cubeletType, reward);
            } else if (!Main.get().getCubeletRewardHandler().isDuplicated(cubeletMachine, reward)) {
                newLootMessage(target, cubeletType, reward);
            } else if (Main.get().isSetting("Rewards.Duplication.Enabled") && Main.get().getCubeletRewardHandler().isDuplicated(cubeletMachine, reward)) {
                duplicateLootMessage(target, cubeletType, reward, cubeletMachine.getLastDuplicationPoints());
            }
        }
    }

    private static String applyPlaceholders(String line, CubeletType cubeletType, Reward reward) {
        return line
                .replaceAll("%baul_type%", Matcher.quoteReplacement(cubeletType.getName()))
                .replaceAll("%reward_name%", Matcher.quoteReplacement(reward.getName()))
                .replaceAll("%reward_rarity%", Matcher.quoteReplacement(reward.getRarity().getName()));
    }

    private static void newLootMessage(Player target, CubeletType cubeletType, Reward reward) {
        List<String> lines = Main.get().getLanguageHandler().getRawMessageList("Cubelet.Reward.New");

        if (lines.isEmpty()) return;

        for (String line : lines) {
            line = applyPlaceholders(line, cubeletType, reward);
            if (line.contains("%center%")) {
                line = line.replaceAll("%center%", "");
                target.sendMessage(Colorize.centeredMessage(Colorize.format(line)));
            } else {
                target.sendMessage(Colorize.format(line));
            }
        }
    }

    private static void duplicateLootMessage(Player target, CubeletType cubeletType, Reward reward, int duplicatePoints) {
        if (duplicatePoints <= 0) {
            newLootMessage(target, cubeletType, reward);
            return;
        }
        
        List<String> lines = Main.get().getLanguageHandler().getRawMessageList("Cubelet.Reward.Duplicate");
        
        if (lines.isEmpty()) return;
        
        for (String line : lines) {
            line = applyPlaceholders(line, cubeletType, reward)
                    .replaceAll("%points%", "" + duplicatePoints);
            if (line.contains("%center%")) {
                line = line.replaceAll("%center%", "");
                target.sendMessage(Colorize.centeredMessage(Colorize.format(line)));
            } else {
                target.sendMessage(Colorize.format(line));
            }
        }
    }

    public static void sendShopMessage(Player player) {
        if (player != null) {
            if (Main.get().isSetting("NoCubelets.ExecuteCommand")) {
                switch (Main.get().getSetting("NoCubelets.Executor")) {
                    case "player":
                        player.chat("/" + Main.get().getSetting("NoCubelets.Command"));
                        break;
                    case "console":
                        Bukkit.getServer().dispatchCommand(Bukkit.getServer().getConsoleSender(),
                                Main.get().getSetting("NoCubelets.Command").replaceAll("%player%", player.getName()));
                        break;
                }
            } else {
                List<String> lines = Main.get().getLanguageHandler().getMessageList("Cubelet.NoCubelets");
                if (lines.isEmpty()) return;
                for (String line : lines) {
                    if (line.contains("%center%")) {
                        line = line.replaceAll("%center%", "");
                        player.sendMessage(Colorize.centeredMessage(Colorize.format(line)));
                    } else {
                        player.sendMessage(Colorize.format(line));
                    }
                }
            }
        }
    }

    public static String plain(String str) {
        return str.replace(ChatColor.COLOR_CHAR, '&');
    }

    public static String strip(String str) {
        String stripped = ChatColor.stripColor(str);
        return stripped == null ? "" : stripped;
    }

    public static String restrip(String str) {
        return strip(Colorize.format(str));
    }

    public static void sendBroadcastMessage(Main main, CubeletMachine cubeletMachine, CubeletType cubeletType, Reward reward) {
    
        List<String> lines = new ArrayList<>();
    
        String key = "Cubelet.Reward.Broadcast";
        FileConfiguration config = main.getLanguageHandler().getConfig();
    
        if (config.get(key) instanceof ArrayList)
            lines.addAll(main.getLanguageHandler().getRawMessageList(key));
        else
            lines.add(main.getLanguageHandler().getRawMessage(key));

        if (lines.isEmpty()) return;

        for (String msg : lines) {
            msg = msg
                    .replaceAll("%player%", Matcher.quoteReplacement(cubeletMachine.getPlayerOpening().getName()))
                    .replaceAll("%reward%", Matcher.quoteReplacement(reward.getName()))
                    .replaceAll("%baul%", Matcher.quoteReplacement(reward.getParentCubelet().getName()));

            if (msg.contains("%center%")) {
                msg = msg.replaceAll("%center%", "");
                main.getServer().broadcastMessage(Colorize.centeredMessage(Colorize.format(msg)));
            } else {
                main.getServer().broadcastMessage(Colorize.format(msg));
            }
        }
    }

}
