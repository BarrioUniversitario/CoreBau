package me.davidml16.baul.pets.pet;

import me.davidml16.baul.Main;
import me.davidml16.baul.pets.abilities.PetAbility;
import me.davidml16.baul.pets.items.PetItem;
import me.davidml16.baul.pets.runnable.PetTask;
import me.davidml16.baul.pets.utils.IProgress;
import me.davidml16.baul.pets.utils.InventoryUtils;
import me.davidml16.baul.pets.utils.Keys;
import me.davidml16.baul.pets.utils.Messages;
import me.davidml16.baul.pets.utils.PetUtils;
import me.davidml16.baul.pets.utils.enums.PetType;
import me.davidml16.baul.pets.utils.enums.Rarity;
import me.davidml16.baul.pets.utils.enums.XPSource;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Location;
import org.bukkit.entity.Interaction;
import org.bukkit.entity.ItemDisplay;
import org.bukkit.entity.Player;
import org.bukkit.persistence.PersistentDataType;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public abstract class Pet implements Cloneable {

    private String id;
    private UUID uuid;
    private final String name;
    protected Player owner;
    private final PetType type;
    private Rarity rarity;
    private List<PetAbility> abilities;
    private String textureUrl;
    private boolean active;
    private ItemDisplay display;
    private PetTask followTask;
    private PetAttribute[] attributes;
    private PetAttribute[] abilityAttributes;
    private PetSkin petSkin;
    private Interaction interaction;
    private PetUpgrade upgrade;

    private int currentExp;
    private int level;

    private PetItem equippedItem;
    private double expMultiplier = 1.0;

    public Pet(String id, String name, PetType type, Rarity rarity, String textureUrl) {
        this.id = id;
        this.name = name;
        this.type = type;
        this.rarity = rarity;
        this.textureUrl = textureUrl;
        this.active = false;
        this.level = 1;
        this.currentExp = 0;

        this.abilities = initializeAbilities();
        this.attributes = initializeAttributes();
        this.upgrade = initializeUpgrade();
    }

    protected abstract List<PetAbility> initializeAbilities();

    public abstract PetAttribute[] initializeAttributes();

    protected abstract PetUpgrade initializeUpgrade();

    public String getId() {
        return id;
    }

    public UUID getUuid() {
        return uuid;
    }

    public String getName() {
        return name;
    }

    public String getColoredName() {
        return "<" + getRarity().getColor() + ">" + getName() + "</" + getRarity().getColor() + ">";
    }

    public PetType getType() {
        return type;
    }

    public int getLevel() {
        return level;
    }

    public Rarity getRarity() {
        return rarity;
    }

    public void setRarity(Rarity rarity) {
        this.rarity = rarity;
    }

    public void setId(String id) {
        this.id = id;
    }

    public void setOwner(Player owner) {
        this.owner = owner;
    }

    public void setLevel(int level) {
        this.level = level;
    }

    public void setCurrentExp(int currentExp) {
        this.currentExp = currentExp;
    }

    public List<PetAbility> getAbilities() {
        return abilities;
    }

    public String getTextureUrl() {
        return textureUrl;
    }

    public void setPetSkin(PetSkin petSkin) {
        this.petSkin = petSkin;
        this.textureUrl = petSkin.texture();
        if (display != null && !display.isDead()) {
            display.setItemStack(InventoryUtils.createTexturedHead(textureUrl));
        }
    }

    public PetSkin getPetSkin() {
        return petSkin;
    }

    public boolean isSkinned() {
        return petSkin != null && !petSkin.texture().isEmpty();
    }

    public boolean hasItem() {
        return equippedItem != null;
    }

    public void startFollowing(Player player) {
        if (followTask != null) {
            followTask.cancel();
        }
        followTask = new PetTask(this, player);
        followTask.runTaskTimer(Main.getInstance(), 0L, 2L);
    }

    public void stopFollowing() {
        if (followTask != null) {
            followTask.cancel();
            followTask = null;
        }
    }

    public void removeAbilities() {
        if (abilities == null || abilities.isEmpty()) {
            Messages.debug("No abilities to remove for pet: " + name);
            return;
        }

        if (owner == null) {
            Messages.debug("Cannot remove abilities for pet " + name + ": owner is null");
            return;
        }

        for (PetAbility petAbility : abilities) {
            if (petAbility == null) {
                Messages.debug("Skipping null ability for pet: " + name);
                continue;
            }
            petAbility.onUnequip(owner);
        }
    }

    public void applyAbilities() {
        if (abilities == null || abilities.isEmpty()) {
            Messages.debug("No abilities to apply for pet: " + name);
            return;
        }

        if (owner == null) {
            Messages.debug("Cannot apply abilities for pet " + name + ": owner is null");
            return;
        }
        removeAbilities();
        for (PetAbility petAbility : abilities) {
            if (petAbility == null) {
                Messages.debug("Skipping null ability for pet: " + name);
                continue;
            }
            petAbility.onEquip(owner);
        }
    }

    public void setActive(boolean active) {
        this.active = active;
        if (active) {
            applyAbilities();
            applyAttributes();
        } else {
            removeAbilities();
            removeAttributes();
            stopFollowing();
            remove();
        }
    }

    public boolean isActive() {
        return active;
    }

    public void spawnHead(Location location, Player player) {
        if (location == null || location.getWorld() == null) {
            Messages.debug("Cannot spawn pet " + name + ": invalid location");
            return;
        }

        if (player == null) {
            Messages.debug("Cannot spawn pet " + name + ": player is null");
            return;
        }

        if (uuid == null) {
            uuid = UUID.randomUUID();
            Messages.debug("Generated new UUID for pet " + name);
        }
        remove();

        try {
            display = location.getWorld().spawn(location, ItemDisplay.class);
            display.setItemStack(InventoryUtils.createTexturedHead(textureUrl));

            interaction = location.getWorld().spawn(location, Interaction.class);
            interaction.setInteractionHeight(0.6f);
            interaction.setInteractionWidth(0.6f);
            interaction.setResponsive(true);

            interaction.setInvisible(true);
            interaction.setInvulnerable(true);

            interaction.getPersistentDataContainer().set(
                    Keys.PET_PLAYER_KEY, PersistentDataType.STRING, uuid.toString());

            org.bukkit.util.Transformation transformation = new org.bukkit.util.Transformation(
                    new org.joml.Vector3f(),
                    new org.joml.AxisAngle4f(),
                    new org.joml.Vector3f(0.7f),
                    new org.joml.AxisAngle4f()
            );
            display.setTransformation(transformation);
            display.customName(Messages.getPetName(this));
            display.setCustomNameVisible(true);
            display.getPersistentDataContainer().set(
                    Keys.PET_PLAYER_KEY, PersistentDataType.STRING, uuid.toString());
            display.setTeleportDuration(2);

            startFollowing(player);

            Messages.debug("Successfully spawned pet " + name + " for player " + player.getName());
        } catch (Exception e) {
            Messages.debug("Failed to spawn pet " + name + ": " + e.getMessage());
            remove(); // Clean up on failure
        }
    }

    public void teleport(Location location) {
        if (location == null || location.getWorld() == null) {
            Messages.debug("Attempted to teleport pet " + name + " to invalid location");
            return;
        }

        if (display != null && !display.isDead()) {
            if (interaction != null && !interaction.isDead()) {
                try {
                    interaction.teleport(location.clone().subtract(0, 0.5, 0));
                } catch (Exception e) {
                    Messages.debug("Failed to teleport pet interaction entity: " + e.getMessage());
                }
            }

            try {
                display.teleport(location);
            } catch (Exception e) {
                Messages.debug("Failed to teleport pet display entity: " + e.getMessage());
            }
        }
    }

    public void remove() {
        if (display != null && !display.isDead()) {
            if (interaction != null) {
                interaction.remove();
                interaction = null;
            }
            display.remove();
            display = null;
        }
    }

    public Interaction getInteraction() {
        return interaction;
    }

    public ItemDisplay getDisplay() {
        return display;
    }

    public void gainExp(int amount, XPSource source) {
        if (amount <= 0 || source == null) {
            Messages.debug("Invalid exp gain parameters for pet " + name + ": amount=" + amount + ", source=" + source);
            return;
        }

        int baseAmount = calculateExpGain(amount, source);
        int finalAmount = (int) Math.round(baseAmount * (1 + expMultiplier / 100.0));
        this.currentExp += finalAmount;

        CompletableFuture.runAsync(() -> checkLevelUp(source));
    }

    private int calculateExpGain(int amount, XPSource source) {
        if (this.type == null || this.type.getSource() == source || source == XPSource.COMMAND) {
            return amount;
        }
        return amount / 3;
    }

    private void checkLevelUp(XPSource source) {
        while (level < 100) {
            int requiredExp = getRequiredExp();
            if (requiredExp == -1) {
                currentExp = 0;
                break;
            }

            if (currentExp >= requiredExp) {
                currentExp -= requiredExp;
                level++;

                if (source == XPSource.EXP_SHARE) {
                    owner.sendMessage(Messages.getSharedPetLevelUp(this, level));
                } else {
                    owner.sendMessage(Messages.getPetLevelUp(this, level));
                }

                if (active) {
                    display.customName(Messages.getPetName(this));
                    applyAttributes();
                    applyAbilities();
                }
            } else {
                break;
            }
        }

        if (level >= 100) {
            level = 100;
            currentExp = 0;
        }
    }

    public int getCurrentExp() {
        return currentExp;
    }

    public int getRequiredExp() {
        return IProgress.getRequiredExp(level, rarity);
    }

    public double getProgress() {
        int required = getRequiredExp();
        if (required <= 0) return 1.0;
        return (double) currentExp / required;
    }

    public PetAttribute[] getAttributes() {
        return attributes != null ? attributes : new PetAttribute[0];
    }

    public PetAttribute[] getAbilityAttributes() {
        return abilityAttributes;
    }

    public void setAttributes(PetAttribute[] attributes) {
        this.attributes = attributes;
    }

    public void setAbilityAttributes(PetAttribute[] abilityAttributes) {
        this.abilityAttributes = abilityAttributes;
    }

    public void applyAttributes() {
        // EcoSkills integration removed - stat modifiers no longer applied
        Messages.debug("Attributes apply disabled (EcoSkills removed)");
    }

    public void removeAttributes() {
        // EcoSkills integration removed - stat modifiers no longer removed
        Messages.debug("Attributes remove disabled (EcoSkills removed)");
    }

    public List<Component> getAttributeLore() {
        // Attributes display disabled (EcoSkills removed)
        return new ArrayList<>();
    }

    public PetItem getEquippedItem() {
        return equippedItem;
    }

    public void equipItem(PetItem item) {
        if (this.equippedItem != null) {
            this.equippedItem.removeEffect(this);
        }
        this.equippedItem = item;
        if (item != null) {
            item.applyEffect(this);
        }
    }

    public void unequipItem() {
        if (this.equippedItem != null) {
            this.equippedItem.removeEffect(this);
            this.equippedItem = null;
        }
    }

    public void finishUpgrade() {
        if (upgrade != null) {
            Pet upgradedPet = Main.getInstance().getRegistryPetManager().getPet(id, upgrade.getRarity());
            if (upgradedPet != null) {
                rarity = upgradedPet.getRarity();
                abilities = upgradedPet.getAbilities();
                if (upgradedPet.getUpgrade() == null) {
                    upgrade = null;
                    Messages.debug("NULL");
                } else {
                    this.upgrade = upgradedPet.getUpgrade();
                }
            } else {
                Messages.debug("Failed to find upgraded pet: " + upgradedPet.getColoredName());
            }
        }
    }

    public double getExpMultiplier() {
        return expMultiplier;
    }

    public void setExpMultiplier(double expMultiplier) {
        this.expMultiplier = expMultiplier;
    }

    public void setUpgrade(PetUpgrade upgrade) {
        this.upgrade = upgrade;
    }

    public PetUpgrade getUpgrade() {
        return upgrade;
    }

    public Map<String, Object> serialize() {
        Map<String, Object> data = new HashMap<>();
        data.put("id", id);
        data.put("uuid", uuid.toString());
        data.put("rarity", rarity);
        data.put("level", level);
        data.put("currentExp", currentExp);
        if (equippedItem != null) {
            data.put("petItem", equippedItem.getId());
        }
        if (petSkin != null) {
            data.put("petSkin", petSkin.id());
        }
        return data;
    }

    public static Pet deserialize(Map<String, Object> data) {
        String id = (String) data.get("id");
        Rarity rarity = Rarity.valueOf((String) data.get("rarity"));
        Pet pet = Main.getInstance().getRegistryPetManager().getPet(id, rarity);
        if (pet == null) return null;

        pet.uuid = UUID.fromString((String) data.get("uuid"));
        pet.level = (int) data.get("level");
        pet.currentExp = (int) data.get("currentExp");

        if (data.containsKey("petItem")) {
            String itemId = (String) data.get("petItem");
            pet.equipItem(Main.getInstance().getPetItemLoader().getItem(itemId));
        }

        if (data.containsKey("petSkin")) {
            String skinId = (String) data.get("petSkin");
            pet.setPetSkin(Main.getInstance().getPetSkinLoader().getSkinById(skinId));
        }
        return pet;
    }

    @Override
    public Pet clone() {
        try {
            Pet clonedPet = (Pet) super.clone();
            clonedPet.abilities = new ArrayList<>(this.abilities);
            clonedPet.active = false;
            clonedPet.currentExp = 0;
            clonedPet.level = 1;
            clonedPet.owner = null;

            clonedPet.uuid = UUID.randomUUID();

            clonedPet.display = null;
            clonedPet.followTask = null;
            clonedPet.interaction = null;

            return clonedPet;
        } catch (CloneNotSupportedException e) {
            throw new RuntimeException("Failed to clone pet", e);
        }
    }

    @Override
    public String toString() {
        return "Pet{" +
                "id='" + id + '\'' +
                ", uuid=" + uuid +
                ", name='" + name + '\'' +
                ", owner=" + owner +
                ", type=" + type +
                ", rarity=" + rarity +
                ", abilities=" + abilities +
                ", textureUrl='" + textureUrl + '\'' +
                ", active=" + active +
                ", display=" + display +
                ", followTask=" + followTask +
                ", attributes=" + Arrays.toString(attributes) +
                ", petSkin=" + petSkin +
                ", interaction=" + interaction +
                ", currentExp=" + currentExp +
                ", level=" + level +
                ", equippedItem=" + equippedItem +
                ", expMultiplier=" + expMultiplier +
                '}';
    }
}
