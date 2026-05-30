package me.davidml16.baul.pets.utils;

import com.destroystokyo.paper.profile.PlayerProfile;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;
import org.bukkit.profile.PlayerTextures;

import java.net.URL;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

public class InventoryUtils {

    public static final ItemStack NEXT_PAGE;
    public static final ItemStack PREVIOUS_PAGE;
    public static final ItemStack BLACK_FILLER;
    public static final ItemStack INFO;
    public static final ItemStack EXP_SHARE;
    public static final ItemStack AUTO_PET_RULE;
    public static final ItemStack CLOSE;
    public static final ItemStack SEARCH;
    public static final ItemStack VISIBILITY;
    public static final ItemStack SORT;
    public static final int[] PAGINATION_SLOTS;

    static {
        NEXT_PAGE = new ItemStack(Material.ARROW);
        ItemMeta nextMeta = NEXT_PAGE.getItemMeta();
        nextMeta.displayName(Messages.deserialize("<green>Next Page</green>"));
        NEXT_PAGE.setItemMeta(nextMeta);

        PREVIOUS_PAGE = new ItemStack(Material.ARROW);
        ItemMeta prevMeta = PREVIOUS_PAGE.getItemMeta();
        prevMeta.displayName(Messages.deserialize("<green>Previous Page</green>"));
        PREVIOUS_PAGE.setItemMeta(prevMeta);

        BLACK_FILLER = new ItemStack(Material.BLACK_STAINED_GLASS_PANE);
        ItemMeta blackFillerMeta = BLACK_FILLER.getItemMeta();
        blackFillerMeta.displayName(Component.text(" "));
        BLACK_FILLER.setItemMeta(blackFillerMeta);

        INFO = new ItemStack(Material.BONE);
        ItemMeta infoMeta = INFO.getItemMeta();
        infoMeta.displayName(Messages.deserialize("<green>Pets</green>"));
        infoMeta.lore(List.of(
                Messages.deserialize("<gray>View and manage all your pets</gray>"),
                Messages.deserialize("<gray>Level up your pets faster by gaining</gray>"),
                Messages.deserialize("<gray>XP in their favourite skill!</gray>"),
                Messages.deserialize(""),
                Messages.deserialize("<gray>Selected Pet: <selected_pet></gray>")
        ));
        INFO.setItemMeta(infoMeta);

        EXP_SHARE = new ItemStack(Material.YELLOW_DYE);
        ItemMeta expShareMeta = EXP_SHARE.getItemMeta();
        expShareMeta.displayName(Messages.deserialize("<gold>Exp Sharing</gold>"));
        expShareMeta.lore(List.of(
                Messages.deserialize("<gray>Share your pet's experience with others</gray>"),
                Messages.deserialize("<gray>Click to toggle sharing</gray>")
        ));
        EXP_SHARE.setItemMeta(expShareMeta);

        AUTO_PET_RULE = createTexturedHead("https://textures.minecraft.net/texture/648c16d0488cf1f1767b413e0a1a1e845d523bfc633da0c8b45e2d7706e79673",
                Messages.deserialize("<red>Auto Pet Rule</red>"),
                List.of(
                        Messages.deserialize("<gray>Automatically activate your pet when you join the server</gray>"),
                        Messages.deserialize("<gray>Click to toggle this rule</gray>")
                ));

        CLOSE = new ItemStack(Material.BARRIER);
        ItemMeta closeMeta = CLOSE.getItemMeta();
        closeMeta.displayName(Messages.deserialize("<red>Close</red>"));
        closeMeta.lore(List.of(Messages.deserialize("<red>Close the menu</red>")));
        CLOSE.setItemMeta(closeMeta);

        SEARCH = new ItemStack(Material.OAK_SIGN);
        ItemMeta searchMeta = SEARCH.getItemMeta();
        searchMeta.displayName(Messages.deserialize("<green>Search</green>"));
        searchMeta.lore(List.of(Messages.deserialize("<gray>Search for pets by name</gray>")));
        SEARCH.setItemMeta(searchMeta);

        VISIBILITY = new ItemStack(Material.STONE_BUTTON);
        ItemMeta visibilityMeta = VISIBILITY.getItemMeta();
        visibilityMeta.displayName(Messages.deserialize("<yellow>Pet Visibility</yellow>"));
        VISIBILITY.setItemMeta(visibilityMeta);

        SORT = new ItemStack(Material.HOPPER);
        ItemMeta sortMeta = SORT.getItemMeta();
        sortMeta.displayName(Messages.deserialize("<green>Sort Pet"));
        SORT.setItemMeta(sortMeta);

        PAGINATION_SLOTS = new int[]{
                0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 17, 18,
                26, 27, 35, 36, 44, 45, 46, 47,
                48, 49, 50, 51, 52, 53};

    }


    public static ItemStack createGuiItem(Material material, Component name, Component... lore) {
        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();

        List<Component> loreList = Arrays.asList(lore);

        if (name != null) meta.displayName(name);
        if (!loreList.isEmpty()) meta.lore(loreList);

        item.setItemMeta(meta);
        return item;
    }

    public static ItemStack createTexturedHead(String textureUrl, Component name, List<Component> lore) {
        ItemStack skull = new ItemStack(Material.PLAYER_HEAD);
        SkullMeta meta = (SkullMeta) skull.getItemMeta();

        meta.displayName(name);
        meta.lore(lore);

        PlayerProfile profile = Bukkit.createProfile(UUID.randomUUID());
        PlayerTextures textures = profile.getTextures();

        try {
            textures.setSkin(new URL(textureUrl));
        } catch (Exception ignored) {
        }

        profile.setTextures(textures);
        meta.setPlayerProfile(profile);
        skull.setItemMeta(meta);

        return skull;
    }

    public static ItemStack createTexturedHead(String textureUrl) {
        return createTexturedHead(textureUrl, null, null);
    }

    public static ItemStack createTexturedHead(String textureUrl, Component name) {
        return createTexturedHead(textureUrl, name, null);
    }
}
