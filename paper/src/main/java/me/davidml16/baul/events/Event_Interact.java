package me.davidml16.baul.events;

import io.github.bananapuncher714.nbteditor.NBTEditor;
import me.davidml16.baul.Main;
import me.davidml16.baul.interfaces.CubeletDateComparator;
import me.davidml16.baul.menus.player.CubeletsMenu;
import me.davidml16.baul.menus.player.rewards.RewardsPreviewMenu;
import me.davidml16.baul.objects.*;
import me.davidml16.baul.utils.MiniMessageUtils;
import org.bukkit.Bukkit;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerArmorStandManipulateEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;

import java.util.List;
import java.util.Optional;

public class Event_Interact implements Listener {

	private final Main main;

	public Event_Interact(Main main) {
		this.main = main;
	}

	@EventHandler
	public void onInteract(PlayerInteractEvent e) {

		Player player = e.getPlayer();
		Action action = e.getAction();

		ItemStack item = e.getItem();

		if (action == Action.RIGHT_CLICK_AIR || action == Action.LEFT_CLICK_AIR || action == Action.RIGHT_CLICK_BLOCK || action == Action.LEFT_CLICK_BLOCK) {

			if (item != null && !item.getType().isAir()) {

				if (NBTEditor.contains(item, NBTEditor.CUSTOM_DATA, "keyType")) e.setCancelled(true);
			}

		}

		if (item == null || item.getType().isAir() || !NBTEditor.contains(item, NBTEditor.CUSTOM_DATA, "keyType") || !main.isSetting("UseKeys")) {

			if (main.getCubeletBoxHandler().isClickType(action)) {

				if (main.getCubeletBoxHandler().getMachines().containsKey(e.getClickedBlock().getLocation())) {

					e.setCancelled(true);

					if (e.getHand() != EquipmentSlot.HAND) return;

					CubeletMachine box = main.getCubeletBoxHandler()
							.getMachineByLocation(e.getClickedBlock().getLocation());

					if (box.isWaiting()) {
						main.getPlayerDataHandler().getData(player).setBoxOpened(box);

						if (!main.isSetting("NoGuiMode")) {

							new CubeletsMenu(main, player).open();

						} else {

							Profile profile = main.getPlayerDataHandler().getData(player.getUniqueId());
							List<Cubelet> cubelets = profile.getCubelets();
							cubelets.sort(new CubeletDateComparator());

							Optional<Cubelet> optionalCubelet = cubelets.stream().findFirst();

							if (optionalCubelet.isPresent()) {

								Cubelet cubelet = optionalCubelet.get();

								if (cubelet.getExpire() > System.currentTimeMillis() || cubelet.getExpire() == -1) {

									CubeletType type = main.getCubeletTypesHandler().getTypeBydId(cubelet.getType());

									if (type.getAllRewards().size() > 0) {

										main.getCubeletOpenHandler()
												.openAnimation(player, profile.getBoxOpened(), type, false);

										main.getDatabaseHandler()
												.removeCubelet(player.getUniqueId(), cubelet.getUuid());

										profile.getCubelets().remove(cubelet);
										main.getHologramImplementation().reloadHolograms(player);

									}

								}

							}

						}

					} else {

						if (box.getPlayerOpening().getUuid() == player.getUniqueId()) {
                      if (!main.getLanguageHandler().isEmptyMessage("Cubelet.BoxInUse.Me"))
                          player.sendMessage(MiniMessageUtils.format(main.getLanguageHandler().getMessage("Cubelet.BoxInUse.Me")));
						} else {
                      if (!main.getLanguageHandler().isEmptyMessage("Cubelet.BoxInUse.Other"))
                          player.sendMessage(MiniMessageUtils.format(main.getLanguageHandler()
                                  .getMessage("Cubelet.BoxInUse.Other")
                                  .replaceAll("%player%", box.getPlayerOpening().getName())));
						}
					}

				}

			}

		} else {

			if (main.getCubeletBoxHandler().isClickType(action)) {

				if (main.getCubeletBoxHandler().getMachines().containsKey(e.getClickedBlock().getLocation())) {

					e.setCancelled(true);

					if (e.getHand() != EquipmentSlot.HAND) return;

					String typeID = NBTEditor.getString(item, NBTEditor.CUSTOM_DATA, "keyType");

					if (action == Action.LEFT_CLICK_BLOCK) {

						if (main.isSetting("Rewards.Preview.Enabled")) {

							RewardsPreviewMenu rewardsPreviewMenu = new RewardsPreviewMenu(Main.get(), player);
							rewardsPreviewMenu.setAttribute(Menu.AttrType.CUSTOM_ID_ATTR, typeID);
							rewardsPreviewMenu.setAttribute(Menu.AttrType.OPENED_EXTERNALLY_ATTR, Boolean.TRUE);
							rewardsPreviewMenu.open();

						}

					} else {

						CubeletMachine box = main.getCubeletBoxHandler()
								.getMachineByLocation(e.getClickedBlock().getLocation());

						if (box.isWaiting()) {

							CubeletType type = main.getCubeletTypesHandler().getTypeBydId(typeID);

							if (type.getAllRewards().size() > 0) {

								main.getCubeletOpenHandler().openAnimation(player, box, type, true);

								item.setAmount(item.getAmount() - 1);

								Bukkit.getScheduler().runTaskLater(main, () -> player.updateInventory(), 20L);

								main.getHologramImplementation().reloadHolograms(player);

							}

						} else {
							if (box.getPlayerOpening().getUuid() == player.getUniqueId()) {
								if (!main.getLanguageHandler().isEmptyMessage("Cubelet.BoxInUse.Me"))
									player.sendMessage(main.getLanguageHandler().getMessage("Cubelet.BoxInUse.Me"));
							} else {
								if (!main.getLanguageHandler().isEmptyMessage("Cubelet.BoxInUse.Other"))
									player.sendMessage(main.getLanguageHandler()
											.getMessage("Cubelet.BoxInUse.Other")
											.replaceAll("%player%", box.getPlayerOpening().getName()));
							}
						}

					}

				}

			}

		}

	}

	@EventHandler
	public void onArmorStand(PlayerArmorStandManipulateEvent e) {

		ArmorStand armorStand = e.getRightClicked();

		if (!armorStand.hasMetadata("BAUL")) return;

		e.setCancelled(true);

	}

}
