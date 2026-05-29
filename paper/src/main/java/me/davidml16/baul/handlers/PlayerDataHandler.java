package me.davidml16.baul.handlers;

import me.davidml16.baul.Main;
import me.davidml16.baul.animations.AnimationHandler;
import me.davidml16.baul.cosmetics.Cosmetic;
import me.davidml16.baul.cosmetics.CosmeticCategory;
import me.davidml16.baul.cosmetics.types.Hat;
import me.davidml16.baul.cosmetics.types.JoinEffect;
import me.davidml16.baul.cosmetics.types.Pet;
import me.davidml16.baul.objects.Profile;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.sql.SQLException;
import java.util.HashMap;
import java.util.UUID;

public class PlayerDataHandler {

	public HashMap<UUID, Profile> data = new HashMap<UUID, Profile>();

	public HashMap<UUID, Long> disconnectCacheTime = new HashMap<UUID, Long>();

	private Main main;
	public PlayerDataHandler(Main main) {
		this.main = main;
	}

	public HashMap<UUID, Profile> getPlayersData() {
		return data;
	}

	public HashMap<UUID, Long> getDisconnectCacheTime() {
		return disconnectCacheTime;
	}

	public Profile getData(Player p) {
		if (data.containsKey(p.getUniqueId()))
			return data.get(p.getUniqueId());
		return null;
	}

	public Profile getData(UUID uuid) {
		if (data.containsKey(uuid))
			return data.get(uuid);
		return null;
	}

	public Long getDisconnectTime(UUID uuid) {
		if (disconnectCacheTime.containsKey(uuid))
			return disconnectCacheTime.get(uuid);
		return null;
	}

	public void addDisconnectTime(UUID uuid, Long time) {
		disconnectCacheTime.put(uuid, time);
	}

	public boolean playerExists(Player p) {
		return data.containsKey(p.getUniqueId());
	}

	public void loadPlayerData(Player p) {

		Profile profile = new Profile(main, p.getUniqueId());
		data.put(p.getUniqueId(), profile);

		try {

			main.getDatabaseHandler().hasName(p.getName(), name -> {

				if(name == null) {

					main.getDatabaseHandler().createPlayerData(p);
					profile.setOrderBy("date");
					profile.setLootPoints(0);
					profile.setAnimation("animation2");

				} else {

					main.getDatabaseHandler().updatePlayerName(p);

					main.getDatabaseHandler().getPlayerOrderSetting(p.getUniqueId(), profile::setOrderBy);
					main.getDatabaseHandler().getPlayerLootPoints(p.getUniqueId(), profile::setLootPoints);
					main.getDatabaseHandler().getPlayerCosmeticsVisible(p.getUniqueId(), profile::setCosmeticsVisible);
					main.getDatabaseHandler().getPlayerAnimation(p.getUniqueId(), animation -> {
						if(animation.contains("animation"))
							profile.setAnimation(animation);
						else
							profile.setAnimation(AnimationHandler.DEFAULT_ANIMATION);
					});

				}

				main.getDatabaseHandler().removeExpiredCubelets(p.getUniqueId());

				if (main.getCosmeticRegistry() != null && main.getCosmeticRegistry().isEnabled()) {
					if (main.getEmoteCooldowns() != null) main.getEmoteCooldowns().loadForPlayer(p.getUniqueId());
					main.getDatabaseHandler().getOwnedCosmetics(p.getUniqueId()).thenAccept(owned ->
						Bukkit.getScheduler().runTask(main, () -> profile.setOwnedCosmetics(owned)));
					main.getDatabaseHandler().getEquippedCosmetics(p.getUniqueId()).thenAccept(equipped ->
						Bukkit.getScheduler().runTask(main, () -> {
							profile.setEquippedCosmetics(equipped);
							if (!p.isOnline()) return;
							String hatId = equipped.get(CosmeticCategory.HAT.getId());
							if (hatId != null && main.getHatApplier() != null) {
								Cosmetic c = main.getCosmeticRegistry().getById(hatId);
								if (c instanceof Hat) main.getHatApplier().apply(p, (Hat) c);
							}
							String joinId = equipped.get(CosmeticCategory.JOIN_EFFECT.getId());
							if (joinId != null) {
								Cosmetic c = main.getCosmeticRegistry().getById(joinId);
								if (c instanceof JoinEffect) ((JoinEffect) c).play(p);
							}
							String petId = equipped.get(CosmeticCategory.PET.getId());
							if (petId != null && main.getPetManager() != null) {
								Cosmetic c = main.getCosmeticRegistry().getById(petId);
								if (c instanceof Pet) main.getPetManager().spawn(p, (Pet) c);
							}
						}));
				}

				main.getDatabaseHandler().getCubelets(p.getUniqueId()).thenAccept(cubelets -> {

					profile.setCubelets(cubelets);

					Bukkit.getScheduler().runTaskLater(main, () -> main.getHologramImplementation().reloadHolograms(p), 2L);

					if(main.isSetting("LoginReminder")) {

						Bukkit.getScheduler().runTaskLater(main, () -> {
							if(cubelets.size() > 0) {
								for (String line : main.getLanguageHandler().getRawMessageList("Cubelet.LoginReminder")) {
									line = line.replaceAll("%amount%", Integer.toString(cubelets.size()));
									line = line.replaceAll("%player%", p.getName());
									p.sendMessage(me.davidml16.baul.utils.Colorize.format(line));
								}
							}
						}, 20L);

					}

				});

				main.getDatabaseHandler().getLootHistory(p.getUniqueId()).thenAccept(lootHistory -> profile.setLootHistory(lootHistory));

			});

		} catch (SQLException throwables) {
			throwables.printStackTrace();
		}

	}

	public void loadAllPlayerData() {
		data.clear();
		for (Player p : Bukkit.getServer().getOnlinePlayers()) {
			loadPlayerData(p);
		}
	}

	public void saveAllPlayerDataAsync() {
		for (Player p : Bukkit.getServer().getOnlinePlayers()) {
			main.getDatabaseHandler().saveProfileAsync(getData(p));
		}
	}

	public void saveAllPlayerDataSync() {
		for (Player p : Bukkit.getServer().getOnlinePlayers()) {
			main.getDatabaseHandler().saveProfileSync(getData(p));
		}
	}

}
