package me.davidml16.baul.pets.utils;

import me.davidml16.baul.Main;
import me.davidml16.baul.pets.abilities.PetAbility;
import me.davidml16.baul.pets.pet.Pet;
import me.davidml16.baul.pets.pet.PetAttribute;
import me.davidml16.baul.pets.pet.PetSkin;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextDecoration;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;
import org.bukkit.configuration.file.FileConfiguration;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Messages {

    private static final Map<String, String> messages = new HashMap<>();
    private static final MiniMessage miniMessage = MiniMessage.miniMessage();
    private static boolean debug = false;
    private static Main plugin;

    public static void load(Main main) {
        plugin = main;
        FileConfiguration config = main.getConfig();
        messages.clear();

        debug = config.getBoolean("debug", false);
        loadMessagesRecursively(config.getConfigurationSection("messages"), "");
        if (debug) {
            debug("Loaded " + messages.size() + " messages");
        }
    }


    private static void loadMessagesRecursively(org.bukkit.configuration.ConfigurationSection section, String path) {
        if (section == null) return;

        for (String key : section.getKeys(false)) {
            String fullPath = path.isEmpty() ? key : path + "." + key;
            if (section.isConfigurationSection(key)) {
                loadMessagesRecursively(section.getConfigurationSection(key), fullPath);
            } else {
                messages.put(fullPath, section.getString(key));
                if (debug) {
                    debug("Loaded message: " + fullPath);
                }
            }
        }
    }

    public static Component get(String key, TagResolver... placeholders) {
        String message = messages.getOrDefault(key, "Message not found: " + key);
        if (debug) {
            debug("Getting message for key: " + key + " -> " + message);
        }
        return miniMessage.deserialize(message, Placeholder.parsed("prefix", messages.get("prefix")), TagResolver.resolver(placeholders))
                .decoration(TextDecoration.ITALIC, false);
    }

    public static Component getPetNameColored(Pet pet) {
        return miniMessage.deserialize("<dark_gray>[<gray>Lv<level></gray>]</dark_gray> <pet_name>",
                        Placeholder.parsed("pet_name", "<" + pet.getRarity().getColor() + ">" + pet.getName()),
                        Placeholder.parsed("level", String.valueOf(pet.getLevel()))).
                decoration(TextDecoration.ITALIC, false);
    }

    // Utility methods for common message patterns
    public static Component getPetEquipped(Pet pet) {
        return get("pet-equipped", Placeholder.parsed("pet_name", pet.getName()));
    }

    public static Component getPetLevelUp(Pet pet, int newLevel) {
        return get("pet-leveled-up",
                Placeholder.parsed("pet_name", pet.getName()),
                Placeholder.parsed("level", String.valueOf(newLevel)));
    }

    public static Component getPetName(Pet pet) {
        return get("pet-name",
                Placeholder.parsed("pet_name", pet.getColoredName()),
                Placeholder.parsed("level", String.valueOf(pet.getLevel())));
    }

    public static Component getPetUnequipped(Pet pet) {
        return get("pet-unequipped", Placeholder.parsed("pet_name", pet.getName()));
    }

    public static Component getPetGainedXP(Pet pet, int amount, int current, int required) {
        return get("experience.gained",
                Placeholder.parsed("pet_name", pet.getName()),
                Placeholder.parsed("amount", String.valueOf(amount)),
                Placeholder.parsed("current", String.valueOf(current)),
                Placeholder.parsed("required", String.valueOf(required)));
    }

    public static Component getAbilityActivated(PetAbility ability) {
        return get("ability.activated", Placeholder.parsed("ability_name", ability.name()));
    }

    public static Component getCommandUsage(String usage) {
        return get("command.usage", Placeholder.parsed("command", usage));
    }

    public static Component getGuiTitle(int selectedPage) {
        return get("gui.title",
                Placeholder.parsed("current_page", String.valueOf(selectedPage)));
    }

    public static Component getNoPermission() {
        return get("no-permission");
    }

    public static Component getPlayerOnly() {
        return get("command.player-only");
    }

    public static Component getPetNotFound() {
        return get("pet-not-found");
    }

    public static Component getNoActivePet() {
        return get("no-active-pet");
    }

    public static Component getSkinNotFound() {
        return get("skin.not-found");
    }

    public static Component getSkinEquipped(PetSkin skin, Pet pet) {
        return get("skin.equipped",
                Placeholder.parsed("skin_name", skin.name()),
                Placeholder.parsed("pet_name", pet.getName()));
    }

    public static Component getSkinIncompatible() {
        return get("skin.unequipped");
    }

    // GUI Messages
    public static Component getGuiExpShareTitle() {
        return get("gui.exp-share.title");
    }

    public static Component getGuiPetSelectionTitle() {
        return get("gui.pet-selection.title");
    }

    public static Component getGuiSharePetClick() {
        return get("gui.exp-share.click-to-add");
    }

    public static Component getGuiSharePetLore() {
        return get("gui.exp-share.empty-slot-lore");
    }

    public static Component getGuiShareStatusTitle() {
        return get("gui.exp-share.status-title");
    }

    public static Component getGuiShareStatusActive() {
        return get("gui.exp-share.status-sharing");
    }


    public static Component getGuiShareStatusInactive() {
        return get("gui.exp-share.status-not-sharing");
    }

    public static Component getGuiShareStatusCount(int current) {
        return get("gui.exp-share.status-count",
                Placeholder.parsed("count", String.valueOf(current)));
    }

    public static Component getGuiDisabledSlot() {
        return get("gui.exp-share.disabled-slot");
    }

    public static Component getSharedPetLevelUp(Pet pet, int newLevel) {
        return get("shared-pet-leveled-up",
                Placeholder.parsed("pet_name", pet.getName()),
                Placeholder.parsed("level", String.valueOf(newLevel)));
    }

    public static List<Component> getPetLore(Pet pet) {
        List<Component> lore = new ArrayList<>();

        lore.add(deserialize("<gray>" + formatPetType(pet.getType().toString()) + " Pet"));
        if (pet.isSkinned()) {
            lore.add(deserialize("<gray>" + pet.getPetSkin().name()));
        }

        lore.add(deserialize(""));

        for (PetAttribute attribute : pet.getAttributes()) {
            double value = attribute.getValue(pet.getLevel());
            String formattedValue = value % 1 == 0 ? String.valueOf((int) value) : String.format("%.1f", value);
            String statName = formatStatName(attribute.getStat().getId());

            lore.add(deserialize("<gray>" + statName + ": <green>+" + formattedValue + "</green>"));
        }

        if (!pet.getAbilities().isEmpty()) {
            lore.add(deserialize(""));
        }

        for (PetAbility ability : pet.getAbilities()) {
            lore.add(deserialize("<gold>" + ability.name() + "</gold>"));

            int index = 0;
            for (String line : ability.description()) {
                if (line.contains("<stat>")) {
                    line = line.replace("<stat>",
                            (ability.getStatValueAtLevel(pet.getLevel(), index)) + "");
                    index++;
                }
                lore.add(deserialize("<gray>" + line + "</gray>"));
            }
            lore.add(deserialize(""));
        }

        if (pet.hasItem()) {
            lore.add(deserialize("<gray>Held Item: <gold>" + pet.getEquippedItem().getDisplayName() + "</gold>"));
            for (String itemLoreLine : pet.getEquippedItem().getLore()) {
                lore.add(deserialize("<gray>" + itemLoreLine + "</gray>"));
            }
            lore.add(deserialize(""));
        }

        if (pet.getLevel() < 100) {
            double progressPercent = pet.getProgress() * 100;
            lore.add(deserialize("<gray>Progress to Level " + (pet.getLevel() + 1) + ": <yellow>" +
                    String.format("%.1f", progressPercent) + "%</yellow>"));

            int filled = Math.max(0, (int) (pet.getProgress() * 20.0D));
            String progressBar = "<green>" + "â–°".repeat(filled) + "</green>" + "<gray>" + "â–±".repeat(20 - filled) + "</gray>";
            lore.add(deserialize(progressBar));

            lore.add(deserialize("<gray>" + pet.getCurrentExp() + "/" + pet.getRequiredExp() + " XP"));
            lore.add(deserialize(""));
        }

        if (pet.isActive()) {
            lore.add(deserialize("<green>âœ“ Currently Active</green>"));
        } else {
            lore.add(deserialize("<yellow>Right-click to add this pet to your</yellow>"));
            lore.add(deserialize("<yellow>pet menu!</yellow>"));
        }

        lore.add(deserialize(""));

        lore.add(deserialize("<" + pet.getRarity().getColor() + "><bold>" +
                pet.getRarity().getDisplayName().toUpperCase() + "</bold></" +
                pet.getRarity().getColor() + ">"));

        return lore;
    }

    private static String formatPetType(String type) {
        return Arrays.stream(type.split("_"))
                .map(word -> word.substring(0, 1).toUpperCase() + word.substring(1).toLowerCase())
                .reduce((a, b) -> a + " " + b)
                .orElse(type);
    }

    private static String formatStatName(String statId) {
        String[] parts = statId.split("_");
        StringBuilder formatted = new StringBuilder();

        for (int i = 0; i < parts.length; i++) {
            if (i > 0) formatted.append(" ");
            formatted.append(parts[i].substring(0, 1).toUpperCase())
                    .append(parts[i].substring(1).toLowerCase());
        }

        return formatted.toString();
    }

    public static Component deserialize(String message) {
        return miniMessage.deserialize(message).decoration(TextDecoration.ITALIC, false);
    }

    public static List<Component> deserialize(String... messages) {
        return Arrays.stream(messages).map(Messages::deserialize).toList();
    }

    public static List<Component> deserialize(List<String> messages) {
        return messages.stream().map(Messages::deserialize).toList();
    }

    public static String serialize(Component component) {
        return miniMessage.serialize(component);
    }

    public static void debug(String message) {
        if (!debug) return;
        plugin.getLogger().info("[Debug] " + message);
    }

    public static String formatTime(long seconds) {
        seconds /= 1000;
        long hours = seconds / 3600;
        long minutes = (seconds % 3600) / 60;
        long secs = seconds % 60;

        StringBuilder result = new StringBuilder();
        if (hours > 0) result.append(hours).append("h");
        if (minutes > 0) result.append(minutes).append("m");
        if (secs > 0 || result.isEmpty()) result.append(secs).append("s");

        return result.toString();
    }

    public static long parseTime(String time) {
        long total = 0;
        String[] parts = time.toLowerCase().split("(?<=\\D)(?=\\d)|(?<=\\d)(?=\\D)");

        for (int i = 0; i < parts.length - 1; i += 2) {
            try {
                long value = Long.parseLong(parts[i]);
                switch (parts[i + 1]) {
                    case "h" -> total += value * 3600;
                    case "m" -> total += value * 60;
                    case "s" -> total += value;
                }
            } catch (NumberFormatException | ArrayIndexOutOfBoundsException e) {
                return 0;
            }
        }
        return total * 1000;
    }

    public static double round(double value) {
        return BigDecimal.valueOf(value)
                .setScale(2, RoundingMode.HALF_UP)
                .doubleValue();
    }

    public static boolean isDebugEnabled() {
        return debug;
    }
}
