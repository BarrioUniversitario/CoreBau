package me.davidml16.baul.events;

import me.davidml16.baul.Main;
import me.davidml16.baul.menus.player.gifts.GiftPlayerMenu;
import me.davidml16.baul.objects.Profile;
import me.davidml16.baul.utils.Utils;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerChangedWorldEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.plugin.PluginDescriptionFile;

public class Event_JoinQuit implements Listener {

    private Main main;
    public Event_JoinQuit(Main main) {
        this.main = main;
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent e) {

        Player player = e.getPlayer();

        main.getHologramImplementation().loadHolograms(player);
        main.getPlayerDataHandler().loadPlayerData(player);
        main.setPlayerCount(main.getPlayerCount() + 1);

        main.getPlayerDataHandler().getDisconnectCacheTime().remove(player.getUniqueId());

        main.getMenuHandler().reloadAllMenus(GiftPlayerMenu.class);

    }

    @EventHandler
    public void onQuit(PlayerQuitEvent e) {

        Player player = e.getPlayer();

        if (main.getHatApplier() != null) main.getHatApplier().restore(player);
        if (main.getPetManager() != null) main.getPetManager().despawn(player.getUniqueId());

        main.getMenuHandler().forceRemoveMenu(player);
        main.getHologramImplementation().removeHolograms(player);

        Profile profile = main.getPlayerDataHandler().getData(player);
        main.getDatabaseHandler().saveProfileAsync(profile, player.getName());

        main.setPlayerCount(main.getPlayerCount() - 1);

        main.getPlayerDataHandler().addDisconnectTime(player.getUniqueId(), System.currentTimeMillis());

        main.getDatabaseHandler().removeExpiredCubelets(player.getUniqueId());

        if (main.getPlayerTrailTask() != null) main.getPlayerTrailTask().forgetPlayer(player.getUniqueId());
        if (main.getEmoteCooldowns() != null) main.getEmoteCooldowns().forgetPlayer(player.getUniqueId());
        if (main.getPreviewManager() != null) main.getPreviewManager().forgetPlayer(player.getUniqueId());

        main.getMenuHandler().reloadAllMenus(GiftPlayerMenu.class);

    }

    @EventHandler
    public void worldChange(PlayerChangedWorldEvent e) {

        Bukkit.getScheduler().runTaskLater(main, () -> {

            main.getHologramImplementation().removeHolograms(e.getPlayer());
            main.getHologramImplementation().loadHolograms(e.getPlayer());

        }, 40L);

        if (main.getPetManager() != null && main.getCosmeticRegistry() != null && main.getCosmeticRegistry().isEnabled()) {
            org.bukkit.entity.Player player = e.getPlayer();
            main.getPetManager().despawn(player.getUniqueId());
            Bukkit.getScheduler().runTaskLater(main, () -> {
                if (!player.isOnline()) return;
                me.davidml16.baul.objects.Profile profile = main.getPlayerDataHandler().getData(player);
                if (profile == null) return;
                String petId = profile.getEquipped(me.davidml16.baul.cosmetics.CosmeticCategory.PET.getId());
                if (petId == null) return;
                me.davidml16.baul.cosmetics.Cosmetic c = main.getCosmeticRegistry().getById(petId);
                if (c instanceof me.davidml16.baul.cosmetics.types.Pet) {
                    main.getPetManager().spawn(player, (me.davidml16.baul.cosmetics.types.Pet) c);
                }
            }, 20L);
        }

    }

}
