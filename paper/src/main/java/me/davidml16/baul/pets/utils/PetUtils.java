package me.davidml16.baul.pets.utils;

import me.davidml16.baul.Main;
import me.davidml16.baul.pets.pet.Pet;
import me.davidml16.baul.pets.utils.enums.Rarity;
import net.kyori.adventure.text.Component;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Interaction;
import org.bukkit.entity.ItemDisplay;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;

import javax.annotation.Nullable;
import java.util.List;
import java.util.UUID;

public class PetUtils {

    private PetUtils() {
        throw new AssertionError();
    }

    public @Nullable
    static UUID extractUUID(ItemStack itemStack) {
        if (itemStack == null || itemStack.getItemMeta() == null) return null;
        var meta = itemStack.getItemMeta();
        String uuidString = meta.getPersistentDataContainer().get(Keys.PET_PLAYER_KEY, PersistentDataType.STRING);
        if (uuidString == null) return null;
        return UUID.fromString(uuidString);
    }

    public @Nullable
    static UUID extractUUID(Entity entity) {
        if (!(entity instanceof ItemDisplay || entity instanceof Interaction)) return null;

        var meta = entity.getPersistentDataContainer();
        String uuidString = meta.get(Keys.PET_PLAYER_KEY, PersistentDataType.STRING);
        if (uuidString == null) return null;

        return UUID.fromString(uuidString);
    }

    public @Nullable
    static String extractPetId(ItemStack itemStack) {
        if (itemStack == null || itemStack.getItemMeta() == null) return null;
        var meta = itemStack.getItemMeta();
        return meta.getPersistentDataContainer().get(Keys.PET_ID_KEY, PersistentDataType.STRING);
    }

    public @Nullable
    static String extractRarity(ItemStack itemStack) {
        if (itemStack == null || itemStack.getItemMeta() == null) return null;
        var meta = itemStack.getItemMeta();
        return meta.getPersistentDataContainer().get(Keys.RARITY_KEY, PersistentDataType.STRING);
    }

    public static ItemStack petToItem(Pet pet) {
        ItemStack item = InventoryUtils.createTexturedHead(pet.getTextureUrl());
        ItemMeta meta = item.getItemMeta();

        meta.displayName(Messages.getPetNameColored(pet));
        List<Component> lore = Messages.getPetLore(pet);

        meta.getPersistentDataContainer().set(Keys.PET_PLAYER_KEY, PersistentDataType.STRING, String.valueOf(pet.getUuid()));
        meta.getPersistentDataContainer().set(Keys.PET_ID_KEY, PersistentDataType.STRING, pet.getId());
        meta.getPersistentDataContainer().set(Keys.RARITY_KEY, PersistentDataType.STRING, pet.getRarity().name());
        meta.getPersistentDataContainer().set(Keys.PET_LEVEL_KEY, PersistentDataType.INTEGER, pet.getLevel());
        meta.getPersistentDataContainer().set(Keys.PET_EXP_KEY, PersistentDataType.INTEGER, pet.getCurrentExp());
        meta.lore(lore);
        item.setItemMeta(meta);
        return item;
    }

    public static Pet itemToPet(ItemStack item) {
        ItemMeta meta = item.getItemMeta();
        try {
            String id = extractPetId(item);
            String rarityString = extractRarity(item);

            Messages.debug(rarityString);

            Integer level = meta.getPersistentDataContainer().get(Keys.PET_LEVEL_KEY, PersistentDataType.INTEGER);
            Integer exp = meta.getPersistentDataContainer().get(Keys.PET_EXP_KEY, PersistentDataType.INTEGER);

            if (id == null || rarityString == null) return null;

            Pet template = Main.getInstance().getRegistryPetManager().getPet(id, Rarity.valueOf(rarityString.toUpperCase()));
            if (template == null) return null;

            Pet pet = template.clone();
            if (level != null) pet.setLevel(level);
            if (exp != null) pet.setCurrentExp(exp);

            return pet;
        } catch (Exception e) {
            Messages.debug("Error deserializing Pet: " + e.getMessage());
            return null;
        }
    }

    public static boolean isSkinItem(ItemStack item) {
        if (item == null || !item.hasItemMeta()) return false;
        return item.getItemMeta().getPersistentDataContainer().has(Keys.SKIN_ID_KEY);
    }

    public static boolean isPet(ItemStack item) {
        if (item == null || !item.hasItemMeta()) return false;
        return item.getItemMeta().getPersistentDataContainer().has(Keys.PET_ID_KEY);
    }

    public static boolean isPetItem(ItemStack item) {
        if (item == null || !item.hasItemMeta()) return false;
        return item.getItemMeta().getPersistentDataContainer().has(Keys.PET_ITEM_KEY);
    }

    public static String getSkinId(ItemStack item) {
        return item.getItemMeta().getPersistentDataContainer().get(Keys.SKIN_ID_KEY, PersistentDataType.STRING);
    }

    public static String getPetItemId(ItemStack item) {
        return item.getItemMeta().getPersistentDataContainer().get(Keys.PET_ITEM_KEY, PersistentDataType.STRING);
    }
}
