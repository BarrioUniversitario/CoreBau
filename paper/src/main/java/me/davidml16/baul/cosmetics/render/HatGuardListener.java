package me.davidml16.baul.cosmetics.render;

import com.destroystokyo.paper.event.player.PlayerArmorChangeEvent;
import me.davidml16.baul.Main;
import me.davidml16.baul.cosmetics.Cosmetic;
import me.davidml16.baul.cosmetics.CosmeticCategory;
import me.davidml16.baul.cosmetics.types.Hat;
import me.davidml16.baul.objects.Profile;
import me.davidml16.baul.utils.Utils;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.inventory.ItemStack;

import java.util.UUID;

/**
 * Hooks Paper's {@link PlayerArmorChangeEvent} to detect when a player picks up
 * a real helmet while wearing a cosmetic hat. The cosmetic is unequipped (state
 * + DB + sync cleared) and the real helmet wins. Stash is dropped without
 * restoring, because the current helmet IS the real helmet now.
 *
 * The {@code applying} guard in {@link HatApplier} prevents us from reacting to
 * our own setHelmet calls.
 */
public class HatGuardListener implements Listener {

    private final Main main;

    public HatGuardListener(Main main) {
        this.main = main;
    }

    @EventHandler(ignoreCancelled = true)
    public void onArmorChange(PlayerArmorChangeEvent e) {
        if (e.getSlotType() != PlayerArmorChangeEvent.SlotType.HEAD) return;

        Player p = e.getPlayer();
        HatApplier applier = main.getHatApplier();
        if (applier == null) return;

        UUID uuid = p.getUniqueId();
        if (!applier.isApplied(uuid) || applier.isApplying(uuid)) return;

        // Real change came from the player (not from us applying the cosmetic).
        Bukkit.getScheduler().runTask(main, () -> verify(p));
    }

    private void verify(Player p) {
        Profile profile = main.getPlayerDataHandler().getData(p);
        if (profile == null) return;

        String hatId = profile.getEquipped(CosmeticCategory.HAT.getId());
        if (hatId == null) return;

        Cosmetic c = main.getCosmeticRegistry().getById(hatId);
        if (!(c instanceof Hat)) return;
        Hat hat = (Hat) c;

        ItemStack helmet = p.getInventory().getHelmet();
        if (helmet != null && helmet.getType() == hat.getMaterial()) return; // still our hat, no-op

        profile.getEquippedCosmetics().remove(CosmeticCategory.HAT.getId());
        main.getDatabaseHandler().unequipCosmetic(p.getUniqueId(), CosmeticCategory.HAT.getId(), null);
        main.getSyncManager().syncCosmeticEquip(p.getUniqueId());
        main.getHatApplier().clear(p.getUniqueId());

        p.sendMessage(Utils.translate(main.getLanguageHandler().getPrefix() + " <gray>Cosmetic hat unequipped.</gray>"));
    }
}
