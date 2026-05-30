package me.davidml16.baul.pets.utils;

import me.davidml16.baul.Main;
import org.bukkit.NamespacedKey;

public class Keys {

    public static final NamespacedKey PET_ID_KEY = new NamespacedKey(Main.getInstance(), "pet_id");
    public static final NamespacedKey RARITY_KEY = new NamespacedKey(Main.getInstance(), "rarity");
    public static final NamespacedKey PET_PLAYER_KEY = new NamespacedKey(Main.getInstance(), "pet_uuid");
    public static final NamespacedKey PET_LEVEL_KEY = new NamespacedKey(Main.getInstance(), "pet_level");
    public static final NamespacedKey PET_EXP_KEY = new NamespacedKey(Main.getInstance(), "pet_exp");
    public static final NamespacedKey SKIN_ID_KEY = new NamespacedKey(Main.getInstance(), "pet_skin_id");
    public static final NamespacedKey PET_ITEM_KEY = new NamespacedKey(Main.getInstance(), "pet_item");
    public static final NamespacedKey TRIGGER_KEY = new NamespacedKey(Main.getInstance(), "trigger");
    public static final NamespacedKey MOUNT_SPEED_KEY = new NamespacedKey(Main.getInstance(), "mount_speed");

    private Keys() {
        throw new AssertionError();
    }
}
