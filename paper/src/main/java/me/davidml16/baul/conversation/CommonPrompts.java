package me.davidml16.baul.conversation;

import me.davidml16.baul.Main;
import me.davidml16.baul.utils.Sounds;
import me.davidml16.baul.utils.Utils;
import org.apache.commons.lang3.math.NumberUtils;
import org.bukkit.ChatColor;
import org.bukkit.conversations.ConversationContext;
import org.bukkit.conversations.Prompt;
import org.bukkit.conversations.StringPrompt;
import org.bukkit.entity.Player;

public interface CommonPrompts  {

    abstract class BaseStringPrompt extends StringPrompt {
        protected Prompt parentPrompt;
        protected String text;
        protected String storeValue;
        protected boolean allowSpaces;
        protected Main main;

        public BaseStringPrompt(Main main, Prompt parentPrompt, boolean allowSpaces, String text, String storeValue) {
            this.main = main;
            this.parentPrompt = parentPrompt;
            this.allowSpaces = allowSpaces;
            this.text = text;
            this.storeValue = storeValue;
        }

        public BaseStringPrompt(Prompt parentPrompt, String text, String storeValue) {
            this(null, parentPrompt, true, text, storeValue);
        }

        public String getPromptText(ConversationContext context) {
            return this.text;
        }

        protected boolean isCancelled(String input, ConversationContext context) {
            if (input.trim().equalsIgnoreCase("cancel")) {
                return true;
            }
            return false;
        }

        protected boolean hasSpaces(String input, ConversationContext context) {
            if (!this.allowSpaces && input.contains(" ")) {
                context.getForWhom().sendRawMessage(ChatColor.RED + "  Spaces are not allowed!\n ");
                Sounds.playSound((Player) context.getSessionData("player"),
                        ((Player) context.getSessionData("player")).getLocation(), Sounds.MySound.NOTE_PLING, 10, 0);
                return true;
            }
            return false;
        }

        protected void storeAndSound(ConversationContext context, Object value) {
            context.setSessionData(this.storeValue, value);
            Sounds.playSound((Player) context.getSessionData("player"),
                    ((Player) context.getSessionData("player")).getLocation(), Sounds.MySound.CLICK, 10, 2);
        }
    }

    public static class CommonStringPrompt extends BaseStringPrompt {

        public CommonStringPrompt(Main main, Prompt parentPrompt, boolean allowSpaces, String text, String storeValue) {
            super(main, parentPrompt, allowSpaces, text, storeValue);
        }

        public CommonStringPrompt(Prompt parentPrompt, String text, String storeValue) {
            super(parentPrompt, text, storeValue);
        }

        public Prompt acceptInput(ConversationContext context, String input) {
            if (isCancelled(input, context)) return this.parentPrompt;
            if (hasSpaces(input, context)) return this;
            storeAndSound(context, input);
            return this.parentPrompt;
        }
    }

    public static class DuplicateRangeStringPrompt extends BaseStringPrompt {

        public DuplicateRangeStringPrompt(Main main, Prompt parentPrompt, boolean allowSpaces, String text, String storeValue) {
            super(main, parentPrompt, allowSpaces, text, storeValue);
        }

        public DuplicateRangeStringPrompt(Prompt parentPrompt, String text, String storeValue) {
            super(parentPrompt, text, storeValue);
        }

        public Prompt acceptInput(ConversationContext context, String input) {
            if (isCancelled(input, context)) return this.parentPrompt;
            if (hasSpaces(input, context)) return this;
            if (!input.contains("-") || input.split("-").length != 2) {
                context.getForWhom().sendRawMessage(ChatColor.RED + "  Format: min-max\n ");
                Sounds.playSound((Player) context.getSessionData("player"),
                        ((Player) context.getSessionData("player")).getLocation(), Sounds.MySound.NOTE_PLING, 10, 0);
                return this;
            }
            storeAndSound(context, input);
            return this.parentPrompt;
        }
    }

    public static class UncoloredStringPrompt extends BaseStringPrompt {

        public UncoloredStringPrompt(Main main, Prompt parentPrompt, boolean allowSpaces, String text, String storeValue) {
            super(main, parentPrompt, allowSpaces, text, storeValue);
        }

        public UncoloredStringPrompt(Prompt parentPrompt, String text, String storeValue) {
            super(parentPrompt, text, storeValue);
        }

        public Prompt acceptInput(ConversationContext context, String input) {
            if (isCancelled(input, context)) return this.parentPrompt;
            if (hasSpaces(input, context)) return this;
            if (input.contains("&") || input.contains("§")) {
                context.getForWhom().sendRawMessage(ChatColor.RED + "  Color codes are not allowed!\n ");
                Sounds.playSound((Player) context.getSessionData("player"),
                        ((Player) context.getSessionData("player")).getLocation(), Sounds.MySound.NOTE_PLING, 10, 0);
                return this;
            }
            storeAndSound(context, input);
            return this.parentPrompt;
        }
    }

    public static class SkullStringPrompt extends BaseStringPrompt {

        public SkullStringPrompt(Main main, Prompt parentPrompt, boolean allowSpaces, String text, String storeValue) {
            super(main, parentPrompt, allowSpaces, text, storeValue);
        }

        public SkullStringPrompt(Prompt parentPrompt, String text, String storeValue) {
            super(parentPrompt, text, storeValue);
        }

        public Prompt acceptInput(ConversationContext context, String input) {
            if (isCancelled(input, context)) return this.parentPrompt;
            if (hasSpaces(input, context)) return this;
            context.setSessionData(this.storeValue, input);
            context.getForWhom().sendRawMessage(
                    ChatColor.GREEN + "  Succesfully setup skull texture with method " +
                        ChatColor.YELLOW + context.getSessionData("method"));
            Sounds.playSound((Player) context.getSessionData("player"),
                    ((Player) context.getSessionData("player")).getLocation(), Sounds.MySound.CLICK, 10, 2);
            return this.parentPrompt;
        }
    }

    public static class MineSkinStringPrompt extends BaseStringPrompt {

        public MineSkinStringPrompt(Main main, Prompt parentPrompt, boolean allowSpaces, String text, String storeValue) {
            super(main, parentPrompt, allowSpaces, text, storeValue);
        }

        public MineSkinStringPrompt(Prompt parentPrompt, String text, String storeValue) {
            super(parentPrompt, text, storeValue);
        }

        public Prompt acceptInput(ConversationContext context, String input) {
            if (isCancelled(input, context)) return this.parentPrompt;
            if (hasSpaces(input, context)) return this;
            if (!input.contains("minesk.in")) {
                context.getForWhom().sendRawMessage(ChatColor.RED + "  Invalid Mineskin direct link!\n ");
                Sounds.playSound((Player) context.getSessionData("player"),
                        ((Player) context.getSessionData("player")).getLocation(), Sounds.MySound.NOTE_PLING, 10, 0);
                return this;
            }
            context.setSessionData(this.storeValue, input);
            context.getForWhom().sendRawMessage(
                    ChatColor.GREEN + "  Succesfully setup skull texture with method " +
                            ChatColor.YELLOW + context.getSessionData("method"));
            Sounds.playSound((Player) context.getSessionData("player"),
                    ((Player) context.getSessionData("player")).getLocation(), Sounds.MySound.CLICK, 10, 2);
            return this.parentPrompt;
        }
    }

    abstract class BaseNumericPrompt extends StringPrompt {
        protected Prompt parentPrompt;
        protected String text;
        protected String storeValue;
        protected boolean hasRange;
        protected Main main;

        public BaseNumericPrompt(Main main, Prompt parentPrompt, String text, String storeValue) {
            this.main = main;
            this.parentPrompt = parentPrompt;
            this.text = text;
            this.storeValue = storeValue;
            this.hasRange = false;
        }

        public String getPromptText(ConversationContext context) { return this.text; }
    }

    public static class NumericRangePrompt extends BaseNumericPrompt {
        private double minValue;
        private double maxValue;

        public NumericRangePrompt(Main main, Prompt parentPrompt, String text, String storeValue, double minValue, double maxValue) {
            super(main, parentPrompt, text, storeValue);
            this.minValue = minValue;
            this.maxValue = maxValue;
            this.hasRange = true;
        }

        public NumericRangePrompt(Prompt parentPrompt, String text, String storeValue) {
            super(null, parentPrompt, text, storeValue);
        }

        public Prompt acceptInput(ConversationContext context, String input) {
            if (input.equalsIgnoreCase("cancel")) return this.parentPrompt;
            if (!NumberUtils.isNumber(input)) {
                context.getForWhom().sendRawMessage(ChatColor.RED + "  That's not a valid number!\n ");
                return this;
            }
            double value = Double.parseDouble(input);
            if (this.hasRange && (value < this.minValue || value > this.maxValue)) {
                context.getForWhom().sendRawMessage(ChatColor.RED + "  The value must be between " + this.minValue + " and " + this.maxValue + "!\n ");
                return this;
            }
            context.setSessionData(this.storeValue, value);
            Sounds.playSound((Player) context.getSessionData("player"),
                    ((Player) context.getSessionData("player")).getLocation(), Sounds.MySound.CLICK, 10, 2);
            return this.parentPrompt;
        }
    }

    public static class NumericIntegerRangePrompt extends BaseNumericPrompt {
        private int minValue;
        private int maxValue;

        public NumericIntegerRangePrompt(Main main, Prompt parentPrompt, String text, String storeValue, int minValue, int maxValue) {
            super(main, parentPrompt, text, storeValue);
            this.minValue = minValue;
            this.maxValue = maxValue;
            this.hasRange = true;
        }

        public NumericIntegerRangePrompt(Prompt parentPrompt, String text, String storeValue) {
            super(null, parentPrompt, text, storeValue);
        }

        public Prompt acceptInput(ConversationContext context, String input) {
            if (input.equalsIgnoreCase("cancel")) return this.parentPrompt;
            if (!NumberUtils.isNumber(input)) {
                context.getForWhom().sendRawMessage(ChatColor.RED + "  That's not a valid number!\n ");
                return this;
            }
            int value = Integer.parseInt(input);
            if (this.hasRange && (value < this.minValue || value > this.maxValue)) {
                context.getForWhom().sendRawMessage(ChatColor.RED + "  The value must be between " + this.minValue + " and " + this.maxValue + "!\n ");
                return this;
            }
            context.setSessionData(this.storeValue, value);
            Sounds.playSound((Player) context.getSessionData("player"),
                    ((Player) context.getSessionData("player")).getLocation(), Sounds.MySound.CLICK, 10, 2);
            return this.parentPrompt;
        }
    }

    public static class BooleanPrompt extends BaseStringPrompt {

        public BooleanPrompt(Main main, Prompt parentPrompt, String text, String storeValue) {
            super(main, parentPrompt, false, text, storeValue);
        }

        public Prompt acceptInput(ConversationContext context, String input) {
            if (isCancelled(input, context)) return this.parentPrompt;
            if (hasSpaces(input, context)) return this;
            if (input.equalsIgnoreCase("true") || input.equalsIgnoreCase("false")) {
                context.setSessionData(this.storeValue, Boolean.parseBoolean(input.toLowerCase()));
                Sounds.playSound((Player) context.getSessionData("player"),
                        ((Player) context.getSessionData("player")).getLocation(), Sounds.MySound.CLICK, 10, 2);
                return parentPrompt;
            }
            context.getForWhom().sendRawMessage(ChatColor.RED + "  That's not a valid option!\n ");
            Sounds.playSound((Player) context.getSessionData("player"),
                    ((Player) context.getSessionData("player")).getLocation(), Sounds.MySound.NOTE_PLING, 10, 0);
            return this;
        }
    }

    public static class IntegerPrompt extends BaseStringPrompt {

        public IntegerPrompt(Main main, Prompt parentPrompt, String text, String storeValue) {
            super(main, parentPrompt, false, text, storeValue);
        }

        public Prompt acceptInput(ConversationContext context, String input) {
            if (isCancelled(input, context)) return this.parentPrompt;
            if (hasSpaces(input, context)) return this;
            try {
                int value = Integer.parseInt(input);
                context.setSessionData(this.storeValue, value);
                Sounds.playSound((Player) context.getSessionData("player"),
                        ((Player) context.getSessionData("player")).getLocation(), Sounds.MySound.CLICK, 10, 2);
                return parentPrompt;
            } catch (NumberFormatException e) {
                context.getForWhom().sendRawMessage(ChatColor.RED + "  Invalid number!\n ");
                Sounds.playSound((Player) context.getSessionData("player"),
                        ((Player) context.getSessionData("player")).getLocation(), Sounds.MySound.NOTE_PLING, 10, 0);
                return this;
            }
        }
    }

    public static class ConfirmExitPrompt extends StringPrompt {
        private Prompt parent;
        private Main main;

        public ConfirmExitPrompt(Main main, Prompt parentPrompt) {
            this.main = main;
            this.parent = parentPrompt;
        }

        public String getPromptText(ConversationContext context) {
            String str = ChatColor.GREEN + "  1 " + ChatColor.GRAY + "- Yes\n" + ChatColor.RED + "  2 " + ChatColor.GRAY + "- No\n ";
            return ChatColor.YELLOW + "\n Are you sure you want to exit without saving?\n \n" + str;
        }

        public Prompt acceptInput(ConversationContext context, String input) {
            if (input.equals("1") || input.equalsIgnoreCase("Yes")) {
                context.getForWhom().sendRawMessage("\n" + Utils.translate(main.getLanguageHandler().getPrefix()
                        + " &cYou leave type setup menu!"));
                Main.get().getConversationHandler().removeConversation((Player) context.getSessionData("player"));
                return Prompt.END_OF_CONVERSATION;
            }
            if (input.equals("2") || input.equalsIgnoreCase("No")) {
                return this.parent;
            }
            context.getForWhom().sendRawMessage(ChatColor.RED + "  That's not a valid option!\n");
            Sounds.playSound((Player) context.getSessionData("player"),
                    ((Player) context.getSessionData("player")).getLocation(), Sounds.MySound.NOTE_PLING, 10, 0);
            return this;
        }
    }

    public static class ErrorPrompt extends StringPrompt {
        private Prompt parent;
        private String text;
        private Main main;

        public ErrorPrompt(Main main, Prompt parentPrompt, String text) {
            this.main = main;
            this.parent = parentPrompt;
            this.text = text;
        }

        public String getPromptText(ConversationContext context) {
            Sounds.playSound((Player) context.getSessionData("player"),
                    ((Player) context.getSessionData("player")).getLocation(), Sounds.MySound.NOTE_PLING, 10, 0);
            return text;
        }

        public Prompt acceptInput(ConversationContext context, String input) {
            context.getForWhom().sendRawMessage(text);
            return parent;
        }
    }

}
