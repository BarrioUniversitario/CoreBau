package me.davidml16.baul.handlers;

import me.davidml16.baul.Main;
import me.davidml16.baul.animations.Animation;
import me.davidml16.baul.animations.AnimationHandler;
import me.davidml16.baul.animations.AnimationSettings;
import me.davidml16.baul.objects.CubeletMachine;
import me.davidml16.baul.objects.CubeletOpener;
import me.davidml16.baul.objects.CubeletType;
import me.davidml16.baul.objects.Profile;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

public class CubeletOpenHandler {

	private final Main main;

	public CubeletOpenHandler(Main main) {
		this.main = main;
	}

	public void openAnimation(Player p, CubeletMachine box, CubeletType type, boolean openedByKey) {
		if (box.isWaiting()) {

			CubeletOpener cubeletOpener = new CubeletOpener(p.getUniqueId(), p.getName());

			box.setPlayerOpening(cubeletOpener);

			main.getHologramImplementation().clearLines(box);

			Animation animation;

			if (!main.isSetting("AnimationsByPlayer") || openedByKey) {

				if (!type.getAnimation().equalsIgnoreCase("random")) {

					animation = main.getAnimationHandler().getAnimation(type.getAnimation());

				} else {

					animation = main.getAnimationHandler()
							.getAnimation(main.getAnimationHandler().getRandomAnimation().getId());

				}

			} else {

				Profile profile = main.getPlayerDataHandler().getData(p);

				if (!profile.getAnimation().equalsIgnoreCase("random")) {

					AnimationSettings animationSetting = main.getAnimationHandler()
							.getAnimationSetting(profile.getAnimation());

					if (animationSetting == null) {
						main.getLogger()
								.warning("Animation setting for " + profile.getAnimation() + " not found for player " + p.getName());
						profile.setAnimation(AnimationHandler.DEFAULT_ANIMATION);
						animationSetting = main.getAnimationHandler()
								.getAnimationSetting(AnimationHandler.DEFAULT_ANIMATION);
					}

					if (animationSetting.isNeedPermission()) {
						if (!main.getAnimationHandler().haveAnimationPermission(p, animationSetting))
							profile.setAnimation(AnimationHandler.DEFAULT_ANIMATION);
					}

					animation = main.getAnimationHandler().getAnimation(profile.getAnimation());

				} else {

					animation = main.getAnimationHandler()
							.getAnimation(main.getAnimationHandler().getRandomAnimation(p).getId());

				}

			}

			animation.setCubeletBox(box);
			animation.setCubeletType(type);
			animation.start();

		} else {
             if (box.getPlayerOpening().getUuid() == p.getUniqueId()) {
                 p.sendMessage(me.davidml16.baul.utils.Colorize.format(main.getLanguageHandler().getRawMessage("Cubelet.BoxInUse.Me")));
             } else {
                 String boxMsg = main.getLanguageHandler().getRawMessage("Cubelet.BoxInUse.Other")
                         .replaceAll("%player%", box.getPlayerOpening().getName());
                 p.sendMessage(me.davidml16.baul.utils.Colorize.format(boxMsg));
             }
		}
	}

}
