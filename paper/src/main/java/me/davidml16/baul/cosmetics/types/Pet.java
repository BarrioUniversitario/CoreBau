package me.davidml16.baul.cosmetics.types;

import me.davidml16.baul.cosmetics.Cosmetic;
import me.davidml16.baul.cosmetics.CosmeticCategory;
import org.bukkit.entity.EntityType;

public class Pet extends Cosmetic {

    private final EntityType entityType;
    private final String customName;

    public Pet(String key, String displayName, String rarity, String iconMaterial, String permission,
               EntityType entityType, String customName, long price) {
        super(key, CosmeticCategory.PET, displayName, rarity, iconMaterial, permission, price);
        this.entityType = entityType;
        this.customName = customName;
    }

    public EntityType getEntityType() { return entityType; }
    public String getCustomName() { return customName; }
}
