package me.davidml16.baul.cosmetics.types;

import me.davidml16.baul.cosmetics.Cosmetic;
import me.davidml16.baul.cosmetics.CosmeticCategory;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

public class Hat extends Cosmetic {

    private final Material material;

    public Hat(String key, String displayName, String rarity, String iconMaterial, String permission, Material material, long price) {
        super(key, CosmeticCategory.HAT, displayName, rarity, iconMaterial, permission, price);
        this.material = material;
    }

    public Material getMaterial() {
        return material;
    }

    public ItemStack toItemStack() {
        return new ItemStack(material);
    }
}
