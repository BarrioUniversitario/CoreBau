package me.davidml16.baul.pets.autopet;

import org.bukkit.Material;

public enum TriggerType {
    BREAK_BLOCK(Material.GRASS_BLOCK, "<green>On Block Break", "<green>Break Block", "<gray>You Break a Block"),
    PLACE_BLOCK(Material.GRASS_BLOCK, "<green>On Block Place", "<green>Place Block", "<gray>You Place a Block"),
    SWING_SWORD(Material.IRON_SWORD, "<red>On Sword Swing", "<red>Swing Sword", "<gray>You swing a sword"),
    ;

    private final Material triggerIcon;
    private final String id;
    private final String name;
    private final String description;

    TriggerType(Material itemStack, String id, String name, String description) {
        this.id = id;
        this.triggerIcon = itemStack;
        this.name = name;
        this.description = description;
    }

    public Material getTriggerIcon() {
        return triggerIcon;
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }
}
