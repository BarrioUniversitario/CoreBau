package me.davidml16.baul.animations;

import com.cryptomorin.xseries.XItemStack;
import com.cryptomorin.xseries.XMaterial;
import lombok.Getter;
import lombok.Setter;
import me.davidml16.baul.Main;
import me.davidml16.baul.animations.animation.animation2.Animation2_Task;
import me.davidml16.baul.utils.ConfigUpdater;
import me.davidml16.baul.utils.Utils;
import org.bukkit.Material;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.*;
import java.util.concurrent.ThreadLocalRandom;

public class AnimationHandler {

	private static final List<ItemStack> animationItems;
	public static String DEFAULT_ANIMATION = "animation2";
	// TODO: Make this dynamic
	public static int ANIMATION_COUNT = 25;

	static {

		animationItems = Arrays.asList(XMaterial.BARRIER.parseItem(), XMaterial.FIREWORK_STAR.parseItem(), XMaterial.CRAFTING_TABLE.parseItem(), XMaterial.FIREWORK_ROCKET.parseItem(), XMaterial.NETHERRACK.parseItem(), XMaterial.SANDSTONE.parseItem(), XMaterial.LIME_WOOL.parseItem(), XMaterial.PUMPKIN.parseItem(), XMaterial.RED_WOOL.parseItem(), XMaterial.LADDER.parseItem(), XMaterial.FIRE_CHARGE.parseItem(), XMaterial.ENDER_EYE.parseItem(), XMaterial.CAULDRON.parseItem(), XMaterial.PRISMARINE_SHARD.parseItem(), XMaterial.POPPY.parseItem(), XMaterial.BLAZE_POWDER.parseItem(), XMaterial.PINK_WOOL.parseItem(), XMaterial.SNOWBALL.parseItem(), XMaterial.DIAMOND.parseItem(), XMaterial.ANVIL.parseItem(), XMaterial.ENDER_PEARL.parseItem(), XMaterial.HEART_OF_THE_SEA.parseItem(), XMaterial.PURPUR_BLOCK.parseItem(), XMaterial.RED_CONCRETE.parseItem(), XMaterial.BLACK_CONCRETE.parseItem(), XMaterial.JACK_O_LANTERN.parseItem());

	}

	private final Main main;
	@Getter
	private final Map<String, AnimationSettings> animations;
	@Setter
	@Getter
	private List<Animation> tasks;
	@Setter
	@Getter
	private List<Entity> entities;
	@Setter
	@Getter
	private File file;
	@Setter
	@Getter
	private YamlConfiguration config;

	public AnimationHandler(Main main) {

		this.main = main;
		this.animations = new HashMap<>();
		this.tasks = new ArrayList<>();
		this.entities = new ArrayList<>();

	}

	public void loadAnimations() {

		File file = new File(main.getDataFolder(), "animations.yml");
		if (!file.exists()) {
			try {
				file.createNewFile();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}

		try {
			ConfigUpdater.update(main, "animations.yml", new File(main.getDataFolder(), "animations.yml"), Collections.emptyList());
		} catch (IOException e) {
			e.printStackTrace();
		}

		YamlConfiguration config = YamlConfiguration.loadConfiguration(file);

		this.file = file;
		this.config = config;

		this.animations.clear();

		for (int index = 0; index <= ANIMATION_COUNT; index++) {

			AnimationSettings animation = new AnimationSettings("animation" + index);

			String animationId = "animation_" + index;

			animation.setAnimationNumber(index);

			animation.setEnabled(config.getBoolean("animations." + animationId + ".Enabled", true));

			animation.setDisplayName(config.getString("animations." + animationId + ".DisplayName", "Unknown"));

			animation.setOutlineParticles(config.getBoolean("animations." + animationId + ".OutlineParticles", true));

			animation.setFloorParticles(config.getBoolean("animations." + animationId + ".FloorParticles", true));

			animation.setAroundParticles(config.getBoolean("animations." + animation + ".AroundParticles", true));

			animation.setNeedPermission(config.getBoolean("animations." + animation + ".NeedPermission", true));

			animation.setChangeBlocks(config.getBoolean("animations." + animationId + ".ChangeBlocks", true));

			if (config.contains("animations." + animationId + ".Icon") && config.getConfigurationSection("animations." + animationId + ".Icon") != null) {

				ItemStack item = XItemStack.deserialize(Utils.getConfigurationSection(config, "animations." + animationId + ".Icon"));

				animation.setDisplayItem(item);

			} else {

				animation.setDisplayItem(animationItems.get(index));

			}

			this.animations.put("animation" + index, animation);

		}

		DEFAULT_ANIMATION = config.getString("default_animation", "animation2");

		saveAnimations();

	}

	public void saveAnimations() {

		config.set("default_animation", DEFAULT_ANIMATION);

		for (AnimationSettings animationSettings : animations.values()) {

			String animationId = "animation_" + animationSettings.getAnimationNumber();

			config.set("animations." + animationId, null);

			config.set("animations." + animationId + ".Enabled", animationSettings.isEnabled());

			config.set("animations." + animationId + ".DisplayName", animationSettings.getDisplayName());

			config.set("animations." + animationId + ".OutlineParticles", animationSettings.isOutlineParticles());

			config.set("animations." + animationId + ".FloorParticles", animationSettings.isFloorParticles());

			config.set("animations." + animationId + ".AroundParticles", animationSettings.isAroundParticles());

			config.set("animations." + animationId + ".NeedPermission", animationSettings.isNeedPermission());

			config.set("animations." + animationId + ".ChangeBlocks", animationSettings.isChangeBlocks());

			ItemStack bukkit = new ItemStack(Material.BUCKET);
			XItemStack.serialize(bukkit);

			XItemStack.serialize(animationSettings.getDisplayItem(), Utils.getConfigurationSection(config, "animations." + animationId + ".Icon"));
		}

		try {
			config.save(file);
		} catch (IOException e) {
			e.printStackTrace();
		}

		try {
			ConfigUpdater.update(main, "animations.yml", new File(main.getDataFolder(), "animations.yml"), Collections.emptyList());
		} catch (IOException e) {
			e.printStackTrace();
		}

	}

	public Animation getAnimation(String animation) {

		AnimationSettings animationSettings = getAnimationSetting(animation);

		Animation animationObject = null;

		if (animationSettings != null) {

			int index = animationSettings.getAnimationNumber();

			try {

				Class<?> clazz = Class.forName("me.davidml16.baul.animations.animation.animation" + index + ".Animation" + index + "_Task");
				Constructor<?> constructor = clazz.getConstructor(Main.class, AnimationSettings.class);

				animationObject = (Animation) constructor.newInstance(main, animationSettings);

			} catch (ClassNotFoundException |
					 NoSuchMethodException |
					 InvocationTargetException |
					 InstantiationException |
					 IllegalAccessException e) {
				e.printStackTrace();
			}

		}

		return animationObject != null ? animationObject : new Animation2_Task(main, getAnimationSetting("animation2"));

	}

	public AnimationSettings getAnimationSetting(String id) {
		return animations.containsKey(id) ? animations.get(id) : animations.get(DEFAULT_ANIMATION);
	}

	public List<AnimationSettings> getAnimationSettings() {

		List<AnimationSettings> animationSettings = new ArrayList<>();

		for (AnimationSettings animation : animations.values()) {

			if (!animation.getId()
				.equalsIgnoreCase(DEFAULT_ANIMATION) && !animation.isEnabled())
				continue;

			animationSettings.add(animation);

		}

		return animationSettings;

	}

	public AnimationSettings getRandomAnimation(Player player) {
		List<AnimationSettings> animations = getAnimationsAvailable(player);
		return animations.get(ThreadLocalRandom.current()
			.nextInt(animations.size()) % animations.size());
	}

	public List<AnimationSettings> getAnimationsAvailable(Player player) {

		List<AnimationSettings> animations = new ArrayList<>();

		for (AnimationSettings animation : this.getAnimations()
			.values()) {
			if (animation.getAnimationNumber() == 0)
				continue;

			if (!animation.isEnabled()) continue;
			if (animation.isNeedPermission())
				if (!haveAnimationPermission(player, animation))
					continue;

			animations.add(animation);
		}

		return animations;

	}

	public boolean haveAnimationPermission(Player player, AnimationSettings animationSettings) {
		return (animationSettings.getId()
			.equalsIgnoreCase(DEFAULT_ANIMATION) || player.hasPermission("baul.animations.*") || player.hasPermission("baul.animations.animation" + animationSettings.getAnimationNumber()));
	}

	public AnimationSettings getRandomAnimation() {
		List<AnimationSettings> animations = new ArrayList<>(getAnimations().values());
		animations.removeIf(animation -> !animation.isEnabled());
		return animations.get(ThreadLocalRandom.current()
			.nextInt(animations.size()) % animations.size());
	}

}