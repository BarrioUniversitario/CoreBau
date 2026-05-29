package me.davidml16.baul.menus.admin.rewards;

import com.cryptomorin.xseries.XMaterial;
import me.davidml16.baul.Main;
import me.davidml16.baul.cosmetics.Cosmetic;
import me.davidml16.baul.objects.CubeletType;
import me.davidml16.baul.objects.Menu;
import me.davidml16.baul.objects.rewards.CosmeticUnlockObject;
import me.davidml16.baul.objects.rewards.Reward;
import me.davidml16.baul.utils.ItemBuilder;
import me.davidml16.baul.utils.MiniMessageUtils;
import me.davidml16.baul.utils.Utils;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Admin menu for managing which cosmetics a cubelet reward grants on win.
 * Single-list UX: every registered cosmetic is shown as a tile, glow indicates
 * "currently in this reward", click toggles membership.
 *
 * Reached from RewardsMenu via {@link org.bukkit.event.inventory.ClickType#DROP}
 * (Q key) — the other five click types are already wired in RewardsMenu.
 */
public class EditRewardCosmeticsMenu extends Menu {

    private static final int PAGE_SIZE = 21;

    public EditRewardCosmeticsMenu(Main main, Player player) {
        super(main, player);
    }

    @Override
    public void OnPageOpened(int page) {
        Reward reward = (Reward) getAttribute(AttrType.REWARD_ATTR);
        if (reward == null) return;

        if (getMain().getCosmeticRegistry() == null || !getMain().getCosmeticRegistry().isEnabled()) {
            getOwner().sendMessage(Utils.translate(getMain().getLanguageHandler().getPrefix() + " &cCosmetics module is disabled in config.yml."));
            getOwner().closeInventory();
            return;
        }

        List<Cosmetic> all = new ArrayList<>(getMain().getCosmeticRegistry().getAll().values());
        all.sort((a, b) -> {
            int c = a.getCategory().getId().compareTo(b.getCategory().getId());
            return c != 0 ? c : a.getId().compareTo(b.getId());
        });

        if (page > 0 && all.size() < (page * PAGE_SIZE) + 1) {
            openPage(getPage() - 1);
            return;
        }

        Inventory gui = createInventory(45, "%reward% | Cosmetics".replace("%reward%", reward.getId()));

        ItemStack edge = new ItemBuilder(XMaterial.GRAY_STAINED_GLASS_PANE.parseItem()).setName(" ").toItemStack();
        ItemStack back = new ItemBuilder(XMaterial.ARROW.parseItem()).setName(Utils.translate("&aBack to rewards")).toItemStack();

        fillBorders(edge);
        for (int i = 10; i <= 16; i++) gui.setItem(i, null);
        for (int i = 19; i <= 25; i++) gui.setItem(i, null);
        for (int i = 28; i <= 34; i++) gui.setItem(i, null);

        if (page > 0) {
            gui.setItem(18, new ItemBuilder(XMaterial.ENDER_PEARL.parseItem()).setName(Utils.translate("&aPrevious page")).toItemStack());
        }
        if (all.size() > (page + 1) * PAGE_SIZE) {
            gui.setItem(26, new ItemBuilder(XMaterial.ENDER_PEARL.parseItem()).setName(Utils.translate("&aNext page")).toItemStack());
        }
        gui.setItem(41, back);

        if (all.isEmpty()) {
            gui.setItem(22, new ItemBuilder(XMaterial.RED_STAINED_GLASS_PANE.parseItem())
                    .setName(Utils.translate("&cNo cosmetics registered"))
                    .setLore("", Utils.translate(" &7Add some in plugins/Baul/cosmetics/"), "")
                    .toItemStack());
            openInventory();
            return;
        }

        int from = page * PAGE_SIZE;
        int to = Math.min(from + PAGE_SIZE, all.size());
        for (Cosmetic c : all.subList(from, to)) {
            gui.addItem(buildTile(reward, c));
        }

        openInventory();
    }

    private ItemStack buildTile(Reward reward, Cosmetic c) {
        boolean assigned = reward.getCosmetics().stream().anyMatch(cu -> cu.getCosmeticId().equalsIgnoreCase(c.getId()));
        XMaterial xm = XMaterial.matchXMaterial(c.getIconMaterial()).orElse(XMaterial.PAPER);
        ItemStack base = xm.parseItem();

        List<String> lore = new ArrayList<>(Arrays.asList(
                "",
                Utils.translate(" &7Category: &6" + c.getCategory().getId() + " "),
                Utils.translate(" &7Rarity: &6" + c.getRarity() + " "),
                Utils.translate(" &7Status: " + (assigned ? "&aIn reward" : "&7Not in reward") + " "),
                "",
                Utils.translate(assigned ? "&eClick » &cRemove from reward " : "&eClick » &aAdd to reward ")
        ));

        ItemBuilder b = new ItemBuilder(base)
                .setName(Utils.translate("&a" + c.getId()))
                .setLore(lore)
                .hideAttributes();
        if (assigned) b.addGlow();
        return b.toItemStack();
    }

    @Override
    public void OnMenuClick(InventoryClickEvent event) {
        if (event.getCurrentItem() == null) return;
        if (event.getCurrentItem().getType() == Material.AIR) return;

        int slot = event.getRawSlot();
        Reward reward = (Reward) getAttribute(AttrType.REWARD_ATTR);
        if (reward == null) return;
        CubeletType cubeletType = reward.getParentCubelet();
        Player player = getOwner();

        if (slot == 18 && event.getCurrentItem().getType() == XMaterial.ENDER_PEARL.parseMaterial()) {
            previousPage();
            return;
        }
        if (slot == 26 && event.getCurrentItem().getType() == XMaterial.ENDER_PEARL.parseMaterial()) {
            nextPage();
            return;
        }
        if (slot == 41) {
            RewardsMenu rewardsMenu = new RewardsMenu(getMain(), player);
            rewardsMenu.setAttribute(AttrType.CUSTOM_ID_ATTR, cubeletType.getId());
            rewardsMenu.open();
            return;
        }
        if ((slot < 10 || slot > 16) && (slot < 19 || slot > 25) && (slot < 28 || slot > 34)) return;

        String cosmeticId = ChatColor.stripColor(event.getCurrentItem().getItemMeta().getDisplayName());
        Cosmetic cosmetic = getMain().getCosmeticRegistry().getById(cosmeticId);
        if (cosmetic == null) return;

        CosmeticUnlockObject existing = reward.getCosmetics().stream()
                .filter(cu -> cu.getCosmeticId().equalsIgnoreCase(cosmeticId))
                .findFirst().orElse(null);

        if (existing != null) {
            reward.getCosmetics().remove(existing);
            reward.recreateCosmetics();
            player.sendMessage(Utils.translate(getMain().getLanguageHandler().getPrefix()
                    + " &aRemoved &e" + cosmeticId + " &afrom reward &e" + reward.getId()));
        } else {
            int idx = reward.getCosmetics().size();
            reward.getCosmetics().add(new CosmeticUnlockObject("cosmetic-" + idx, cosmeticId));
            player.sendMessage(Utils.translate(getMain().getLanguageHandler().getPrefix()
                    + " &aAdded &e" + cosmeticId + " &ato reward &e" + reward.getId()
                    + " &7(" + MiniMessageUtils.format(cosmetic.getDisplayName()) + "&7)"));
        }
        playSound(SoundType.CLICK);
        reloadMyMenu();
    }

    @Override
    public void OnMenuClosed() {
        Reward reward = (Reward) getAttribute(AttrType.REWARD_ATTR);
        if (reward != null) reward.getParentCubelet().saveType();
    }
}
