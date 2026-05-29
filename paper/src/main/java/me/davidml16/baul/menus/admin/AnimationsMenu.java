package me.davidml16.baul.menus.admin;

import com.cryptomorin.xseries.XMaterial;
import io.github.bananapuncher714.nbteditor.NBTEditor;
import me.davidml16.baul.Main;
import me.davidml16.baul.animations.AnimationSettings;
import me.davidml16.baul.menus.admin.type.TypeConfigMenu;
import me.davidml16.baul.objects.CubeletType;
import me.davidml16.baul.objects.GUILayout;
import me.davidml16.baul.objects.Menu;
import me.davidml16.baul.utils.ItemBuilder;
import me.davidml16.baul.utils.Utils;
import org.bukkit.Material;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class AnimationsMenu extends Menu {

	public AnimationsMenu(Main main, Player player) {

		super(main, player);

	}

	@Override
	public void OnPageOpened(int page) {

		Player player = getOwner();

		player.updateInventory();

		List<AnimationSettings> animations = new ArrayList<>(getMain().getAnimationHandler().getAnimations().values());
		Collections.sort(animations);

		if (page < 0) {
			openPage(0);
			return;
		}

		if (page > 0 && animations.size() < (page * 21) + 1) {
			openPage(getPage() - 1);
			return;
		}

		if (animations.size() > 21)
			animations = animations.subList(page * 21, Math.min(((page * 21) + 21), animations.size()));

		GUILayout guiLayout = getMain().getLayoutHandler().getLayout("animations");

		CubeletType cubeletType = (CubeletType) getAttribute(AttrType.CUBELET_TYPE_ATTR);

		Inventory inventory = createInventory(45, "%baul_type% | Animations".replaceAll("%baul_type%", cubeletType.getId()));

		setInventory(inventory);

		ItemStack back = new ItemBuilder(XMaterial.ARROW.parseItem()).setName(Utils.translate("&aBack to config"))
				.toItemStack();
		back = NBTEditor.set(back, "back", NBTEditor.CUSTOM_DATA, "action");

		ItemStack filler = XMaterial.GRAY_STAINED_GLASS_PANE.parseItem();
		fillBorders(filler);

		if (page > 0) {

			ItemStack item = new ItemBuilder(XMaterial.ENDER_PEARL.parseMaterial(), 1).setName(Utils.translate("&aPrevious page"))
					.toItemStack();

			item = NBTEditor.set(item, "previous", NBTEditor.CUSTOM_DATA, "action");

			inventory.setItem(18, item);

		}

		if (getMain().getAnimationHandler().getAnimations().size() > (page + 1) * 21) {

			ItemStack item = new ItemBuilder(XMaterial.ENDER_PEARL.parseMaterial(), 1).setName(Utils.translate("&aaNext page"))
					.toItemStack();

			item = NBTEditor.set(item, "next", NBTEditor.CUSTOM_DATA, "action");

			inventory.setItem(26, item);

		}

		inventory.setItem(40, back);

		for (AnimationSettings animation : animations)
			inventory.addItem(getAnimationItem(cubeletType, animation.getId()));

		ItemStack randomAnimation = getRandomAnimationItem(cubeletType, guiLayout);
		randomAnimation = NBTEditor.set(randomAnimation, "animation", NBTEditor.CUSTOM_DATA, "action");
		inventory.setItem(38, randomAnimation);

		openInventory();

	}

	@Override
	public void OnMenuClick(InventoryClickEvent event) {

		if (event.getCurrentItem() == null) return;
		if (event.getCurrentItem().getType() == Material.AIR) return;
		if (event.getClick() == ClickType.DOUBLE_CLICK) return;

		String action = NBTEditor.getString(event.getCurrentItem(), NBTEditor.CUSTOM_DATA, "action");

		if (event.getClick() == ClickType.DOUBLE_CLICK) return;

		if (action == null) return;

		Player player = getOwner();
		CubeletType cubeletType = (CubeletType) getAttribute(AttrType.CUBELET_TYPE_ATTR);

		switch (action) {

			case "previous":
				previousPage();
				playSound(SoundType.CLICK);
				break;

			case "next":
				nextPage();
				playSound(SoundType.CLICK);
				break;

			case "animation":

				String status = NBTEditor.getString(event.getCurrentItem(), NBTEditor.CUSTOM_DATA, "status");
				String animation = NBTEditor.getString(event.getCurrentItem(), NBTEditor.CUSTOM_DATA, "animation");

				if (status.equalsIgnoreCase("unlocked")) {

					FileConfiguration config = getMain().getCubeletTypesHandler().getConfig(cubeletType.getId());

					config.set("type.animation", animation);
					cubeletType.setAnimation(animation);
					playSound(SoundType.CLICK);

					cubeletType.saveType();
					reloadMenu();

				}

				break;

			case "back":
				TypeConfigMenu typeConfigMenu = new TypeConfigMenu(getMain(), player);
				typeConfigMenu.setAttribute(AttrType.CUSTOM_ID_ATTR, cubeletType.getId());
				typeConfigMenu.open();
				break;

		}

	}

	@Override
	public void OnMenuClosed() {
	}

	private ItemStack getAnimationItem(CubeletType cubeletType, String animation) {

		AnimationSettings animationSettings = getMain().getAnimationHandler().getAnimationSetting(animation);

		ItemStack item = animationSettings.getDisplayItem().clone();

		if (!cubeletType.getAnimation().equalsIgnoreCase(animation))
			item = getItem(animationSettings, "Unlocked", item);
		else item = getItem(animationSettings, "Selected", item);

		item = NBTEditor.set(item, animation, NBTEditor.CUSTOM_DATA, "animation");
		item = NBTEditor.set(item, "animation", NBTEditor.CUSTOM_DATA, "action");

		return item;

	}

	private ItemStack getItem(AnimationSettings animationSettings, String status, ItemStack itemStack) {

		String name = Utils.translate((status.equalsIgnoreCase("Selected") ? "&a" : "&c") + animationSettings.getDisplayName());
		List<String> lore = new ArrayList<>();
		lore.add("");
		lore.add(Utils.translate("&eLeft-Click » " + (status.equalsIgnoreCase("Unlocked") ? "&a" : "&c") + "Enable animation "));

		ItemStack item;
		if (status.equalsIgnoreCase("Selected"))
			item = new ItemBuilder(itemStack).setName(name).setLore(lore).addGlow().hideAttributes().toItemStack();
		else item = new ItemBuilder(itemStack).setName(name).setLore(lore).hideAttributes().toItemStack();

		return NBTEditor.set(item, status.toLowerCase(), NBTEditor.CUSTOM_DATA, "status");
	}

	private ItemStack getRandomAnimationItem(CubeletType cubeletType, GUILayout guiLayout) {

		ItemStack item = new ItemBuilder(XMaterial.ENDER_PEARL.parseItem()).toItemStack();

		String status;

		if (!cubeletType.getAnimation().equalsIgnoreCase("random")) {
			String name = guiLayout.getMessage("Items.RandomAnimation.NoSelected.Name");
			List<String> lore = guiLayout.getMessageList("Items.RandomAnimation.NoSelected.Lore");
			item = new ItemBuilder(item).setName(name).setLore(lore).toItemStack();
			status = "Unlocked";
		} else {
			String name = guiLayout.getMessage("Items.RandomAnimation.Selected.Name");
			List<String> lore = guiLayout.getMessageList("Items.RandomAnimation.Selected.Lore");
			item = new ItemBuilder(item).setName(name).setLore(lore).addGlow().toItemStack();
			status = "Selected";
		}

		item = NBTEditor.set(item, "random", NBTEditor.CUSTOM_DATA, "animation");
		item = NBTEditor.set(item, status.toLowerCase(), NBTEditor.CUSTOM_DATA, "status");

		return item;

	}

}