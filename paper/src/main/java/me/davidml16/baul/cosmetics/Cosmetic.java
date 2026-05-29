package me.davidml16.baul.cosmetics;

public abstract class Cosmetic {

    private final String id;
    private final String key;
    private final CosmeticCategory category;
    private final String displayName;
    private final String rarity;
    private final String iconMaterial;
    private final String permission;
    private final long price;

    protected Cosmetic(String key, CosmeticCategory category, String displayName,
                       String rarity, String iconMaterial, String permission) {
        this(key, category, displayName, rarity, iconMaterial, permission, 0L);
    }

    protected Cosmetic(String key, CosmeticCategory category, String displayName,
                       String rarity, String iconMaterial, String permission, long price) {
        this.key = key;
        this.category = category;
        this.id = category.getId() + "_" + key;
        this.displayName = displayName;
        this.rarity = rarity;
        this.iconMaterial = iconMaterial;
        this.permission = permission;
        this.price = Math.max(0, price);
    }

    public String getId() {
        return id;
    }

    public String getKey() {
        return key;
    }

    public CosmeticCategory getCategory() {
        return category;
    }

    public String getDisplayName() {
        return displayName;
    }

    public String getRarity() {
        return rarity;
    }

    public String getIconMaterial() {
        return iconMaterial;
    }

    public String getPermission() {
        return permission;
    }

    public boolean requiresPermission() {
        return permission != null && !permission.isEmpty();
    }

    public long getPrice() {
        return price;
    }

    public boolean isForSale() {
        return price > 0;
    }
}
