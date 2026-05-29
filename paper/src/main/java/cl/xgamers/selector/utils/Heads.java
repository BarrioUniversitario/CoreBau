package cl.xgamers.selector.utils;

import com.destroystokyo.paper.profile.PlayerProfile;
import com.destroystokyo.paper.profile.ProfileProperty;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.SkullMeta;

import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.UUID;
import java.util.regex.Pattern;

public final class Heads {

    private static final Pattern TEXTURE_HASH = Pattern.compile("^[a-f0-9]{64}$");

    private Heads() {
    }

    public static ItemStack fromIcon(String icon) {
        if (icon == null || icon.isBlank()) {
            return new ItemStack(Material.PLAYER_HEAD);
        }

        String value = icon.trim();
        String lower = value.toLowerCase();

        if (lower.startsWith("base64:")) {
            return fromBase64(value.substring(7));
        }
        if (lower.startsWith("texture:")) {
            return fromTextureHash(value.substring(8));
        }
        if (lower.startsWith("http://") || lower.startsWith("https://")) {
            return fromUrl(value);
        }
        if (TEXTURE_HASH.matcher(value).matches()) {
            return fromTextureHash(value);
        }
        if (lower.startsWith("player:")) {
            return fromUsername(value.substring(7));
        }

        try {
            return new ItemStack(Material.valueOf(value.toUpperCase()));
        } catch (IllegalArgumentException ignored) {
            return new ItemStack(Material.PLAYER_HEAD);
        }
    }

    public static ItemStack fromPlayer(Player player) {
        ItemStack head = new ItemStack(Material.PLAYER_HEAD);
        SkullMeta meta = (SkullMeta) head.getItemMeta();
        if (meta == null) {
            return head;
        }

        PlayerProfile profile = player.getPlayerProfile();
        if (profile.getUniqueId() == null) {
            profile = Bukkit.createProfile(player.getUniqueId(), player.getName());
        }
        meta.setPlayerProfile(profile);
        head.setItemMeta(meta);
        return head;
    }

    public static ItemStack fromUrl(String url) {
        String json = String.format("{\"textures\":{\"SKIN\":{\"url\":\"%s\"}}}", url);
        return applyTexturesProperty(encodeBase64(json));
    }

    public static ItemStack fromTextureHash(String hash) {
        return fromUrl("http://textures.minecraft.net/texture/" + hash);
    }

    public static ItemStack fromBase64(String base64Value) {
        return applyTexturesProperty(base64Value.trim());
    }

    public static ItemStack fromUsername(String username) {
        ItemStack head = new ItemStack(Material.PLAYER_HEAD);
        SkullMeta meta = (SkullMeta) head.getItemMeta();
        if (meta == null) {
            return head;
        }

        PlayerProfile profile = Bukkit.createProfile(username);
        meta.setPlayerProfile(profile);
        head.setItemMeta(meta);
        return head;
    }

    private static ItemStack applyTexturesProperty(String texturesBase64) {
        ItemStack head = new ItemStack(Material.PLAYER_HEAD);
        SkullMeta meta = (SkullMeta) head.getItemMeta();
        if (meta == null) {
            return head;
        }

        PlayerProfile profile = Bukkit.createProfile(UUID.randomUUID(), null);
        profile.setProperty(new ProfileProperty("textures", texturesBase64, null));
        meta.setPlayerProfile(profile);
        head.setItemMeta(meta);
        return head;
    }

    private static String encodeBase64(String json) {
        return Base64.getEncoder().encodeToString(json.getBytes(StandardCharsets.UTF_8));
    }

    /** @deprecated Usar {@link #fromUrl(String)} o {@link #fromIcon(String)} */
    @Deprecated
    public static ItemStack getSkull(String url) {
        return fromUrl(url);
    }
}
