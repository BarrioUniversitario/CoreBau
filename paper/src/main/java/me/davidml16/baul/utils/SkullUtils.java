package me.davidml16.baul.utils;

import com.cryptomorin.xseries.profiles.builder.XSkull;
import com.cryptomorin.xseries.profiles.exceptions.ProfileChangeException;
import com.cryptomorin.xseries.profiles.objects.ProfileInputType;
import com.cryptomorin.xseries.profiles.objects.Profileable;
import lombok.experimental.UtilityClass;
import me.davidml16.baul.Main;
import org.bukkit.inventory.ItemStack;

import java.util.UUID;

/**
 * @author Florian Ohldag (Fedox)
 * @version 1.0
 * @since 6/23/2025, 8:48 AM
 */
@UtilityClass
public class SkullUtils {

    private static final String SPARE_BASE64_HEAD = "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvM2Y2NWFjZWRmZTU3ZDM1ZjZiY2EzMzgxMjc5NDBlYWZmNGZmYmVjODBhYmQzM2JmZjg1OGRiM2ExYTkzZjgyIn19fQ==";
    private static boolean exceptionAlreadyTraced = false;

    public static void reload() {
        exceptionAlreadyTraced = false;
    }

    public ItemStack itemFromBase64(String base64) {
        return itemFrom(ProfileInputType.BASE64, base64);
    }

    public ItemStack itemFromUUID(UUID uuid) {
        return itemFrom(ProfileInputType.UUID, uuid.toString());
    }

    public ItemStack itemFromName(String name) {
        return itemFrom(ProfileInputType.USERNAME, name);
    }

    private ItemStack itemFrom(ProfileInputType inputType, String input) {
        // Cant use Profilable#fallback(...) because if input invalid, exception will be thrown out
        try {
            // Try to return get player's skull by inputType
            return XSkull.createItem().profile(Profileable.of(inputType, input)).apply();
        } catch (Exception ignored) {
            // If player not found (offline) or others exception thrown
            return spareItem();
        }
    }

    private ItemStack spareItem() {
        // Exception can be throws only once, when configuration properties invalid
        if (exceptionAlreadyTraced) {
            // Guaranteed to return a working head if configuration parameters are incorrect
            return XSkull.createItem().profile(Profileable.of(ProfileInputType.BASE64, SPARE_BASE64_HEAD)).apply();
        }
        // Get properties from config to return spare profilable
        final String inputTypeString = Main.get().getSetting("GiftMenuSpareHeadType");
        final String value = Main.get().getSetting("GiftMenuSpareHeadValue");
        try {
            final ProfileInputType inputType = ProfileInputType.valueOf(inputTypeString);
            return XSkull.createItem().profile(Profileable.of(inputType, value)).apply();
            // Try to return a head by properties from configuration
        } catch (IllegalArgumentException exception) {
            // Throws when inputType (ProfileInputType) invalid
            Main.get().getLogger().severe("GiftMenuSpareHeadType '" + inputTypeString + "' is not allowed. Please, check your configuration file.");
            exceptionAlreadyTraced = true;
            throw exception;
        } catch (ProfileChangeException exception) {
            // Throws when value for ProfileInputType invalid
            Main.get().getLogger().severe("GiftMenuSpareHeadValue '" + value + "' incorrect. Please, check your configuration file.");
            exceptionAlreadyTraced = true;
            throw exception;
        }
    }

}
