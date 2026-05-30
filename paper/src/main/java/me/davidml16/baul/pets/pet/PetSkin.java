package me.davidml16.baul.pets.pet;

import me.davidml16.baul.pets.utils.InventoryUtils;
import me.davidml16.baul.pets.utils.Keys;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;

import java.util.ArrayList;
import java.util.List;

public record PetSkin(String id, String name, String petId, String texture) {

    public ItemStack createSkinItem() {
        ItemStack item = InventoryUtils.createTexturedHead(texture);
        ItemMeta meta = item.getItemMeta();

        meta.displayName(Component.text("Pet Skin: " + name)
                .color(NamedTextColor.GOLD));

        List<Component> lore = new ArrayList<>();
        lore.add(Component.empty());
        lore.add(Component.text("Right-click your pet while")
                .color(NamedTextColor.GRAY));
        lore.add(Component.text("holding this item to apply!")
                .color(NamedTextColor.GRAY));

        meta.lore(lore);
        meta.getPersistentDataContainer().set(Keys.SKIN_ID_KEY, PersistentDataType.STRING, id);

        item.setItemMeta(meta);
        return item;
    }
}
