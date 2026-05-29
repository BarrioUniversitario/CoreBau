package me.davidml16.baul.menus.player;

import com.cryptomorin.xseries.XMaterial;
import io.github.bananapuncher714.nbteditor.NBTEditor;
import me.davidml16.baul.Main;
import me.davidml16.baul.cosmetics.Cosmetic;
import me.davidml16.baul.enums.CraftType;
import me.davidml16.baul.objects.CraftIngredient;
import me.davidml16.baul.objects.Menu;
import me.davidml16.baul.objects.Profile;
import me.davidml16.baul.utils.ItemBuilder;
import me.davidml16.baul.utils.Utils;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Player-facing crafting menu. Lists every cosmetic that has a recipe in
 * {@code plugins/Baul/cosmetic-crafting.yml}. Click to craft when the player
 * has all ingredients; shift-click previews the cosmetic for 30s without
 * crafting.
 */
public class CosmeticCraftingMenu extends Menu {

    public CosmeticCraftingMenu(Main main, Player player) {
        super(main, player);
        setSize(6);
    }

    @Override
    public void OnPageOpened(int page) {
        Player player = getOwner();
        Profile profile = getMain().getPlayerDataHandler().getData(player);
        if (profile == null) return;

        Map<String, List<CraftIngredient>> recipes = getMain().getCosmeticCraftingHandler().getRecipes();
        List<String> ids = new ArrayList<>(recipes.keySet());
        ids.sort(String::compareTo);

        if (page < 0) { openPage(0); return; }
        if (page > 0 && ids.size() < (page * getPageSize()) + 1) { openPage(getPage() - 1); return; }

        Inventory gui = createInventory(getSize(), Utils.translate("<dark_gray>Crafteo de Cosméticos</dark_gray>"));

        ItemStack hidden = new ItemBuilder(XMaterial.GRAY_STAINED_GLASS_PANE.parseItem()).setName(" ").toItemStack();
        fillTopSide(hidden, 5);

        if (ids.isEmpty()) {
            gui.setItem(22, new ItemBuilder(XMaterial.RED_STAINED_GLASS_PANE.parseItem())
                    .setName(Utils.translate("<red>No hay recetas definidas</red>"))
                    .setLore("", Utils.translate("<gray>Los admins agregan recetas en</gray>"),
                            Utils.translate("<gray>plugins/Baul/crafting.yml</gray>"), "")
                    .toItemStack());
        } else {
            int sub = Math.min(ids.size(), (page + 1) * getPageSize());
            for (String id : ids.subList(page * getPageSize(), sub)) {
                Cosmetic c = getMain().getCosmeticRegistry().getById(id);
                if (c == null) continue;
                gui.addItem(buildTile(profile, c, recipes.get(id)));
            }
        }

        fillTopSide(hidden, 5);

        ItemStack back = new ItemBuilder(XMaterial.OAK_DOOR.parseItem())
                .setName(Utils.translate("<yellow>← Volver a cosméticos</yellow>"))
                .toItemStack();
        back = NBTEditor.set(back, "back", NBTEditor.CUSTOM_DATA, "action");
        gui.setItem(48, back);
        gui.setItem(49, balanceItem(profile));

        if (page > 0) {
            ItemStack prev = new ItemBuilder(XMaterial.ARROW.parseItem()).setName(Utils.translate("<gray>« Página anterior</gray>")).toItemStack();
            prev = NBTEditor.set(prev, "previous", NBTEditor.CUSTOM_DATA, "action");
            gui.setItem(45, prev);
        }
        if (ids.size() > (page + 1) * getPageSize()) {
            ItemStack next = new ItemBuilder(XMaterial.ARROW.parseItem()).setName(Utils.translate("<gray>Página siguiente »</gray>")).toItemStack();
            next = NBTEditor.set(next, "next", NBTEditor.CUSTOM_DATA, "action");
            gui.setItem(53, next);
        }

        openInventory();
    }

    @Override
    public void OnMenuClick(InventoryClickEvent event) {
        event.setCancelled(true);
        ItemStack clicked = event.getCurrentItem();
        if (clicked == null || clicked.getType() == Material.AIR) return;
        if (event.getClick() == ClickType.DOUBLE_CLICK) return;

        String action = NBTEditor.getString(clicked, NBTEditor.CUSTOM_DATA, "action");
        if (action == null) return;

        Player player = getOwner();
        Profile profile = getMain().getPlayerDataHandler().getData(player);
        if (profile == null) return;

        switch (action) {
            case "previous": previousPage(); return;
            case "next": nextPage(); return;
            case "back":
                new CosmeticsMenu(getMain(), player).open();
                return;
            case "craft": break;
            default: return;
        }

        String id = NBTEditor.getString(clicked, NBTEditor.CUSTOM_DATA, "cosmeticId");
        Cosmetic cosmetic = getMain().getCosmeticRegistry().getById(id);
        if (cosmetic == null) return;
        List<CraftIngredient> recipe = getMain().getCosmeticCraftingHandler().getRecipe(id);
        if (recipe == null) return;

        // Shift-click = vista previa antes de craftear
        if (event.getClick() == ClickType.SHIFT_LEFT || event.getClick() == ClickType.SHIFT_RIGHT) {
            getMain().getPreviewManager().preview(player, cosmetic);
            player.closeInventory();
            player.sendMessage(Utils.translate(getMain().getLanguageHandler().getPrefix()
                    + " <aqua>Vista previa</aqua> " + cosmetic.getDisplayName() + " <gray>(30s)</gray>"));
            return;
        }

        if (profile.ownsCosmetic(id)) {
            player.sendMessage(Utils.translate(getMain().getLanguageHandler().getPrefix() + " <yellow>Ya lo tienes.</yellow>"));
            playSound(SoundType.NOTE_PLING);
            return;
        }
        if (!getMain().getCosmeticCraftingHandler().hasIngredients(player, recipe)) {
            player.sendMessage(Utils.translate(getMain().getLanguageHandler().getPrefix() + " <red>Te faltan ingredientes.</red>"));
            playSound(SoundType.NOTE_PLING);
            return;
        }

        getMain().getCosmeticCraftingHandler().consumeIngredients(player, recipe);

        profile.getOwnedCosmetics().add(id);
        getMain().getDatabaseHandler().addOwnedCosmetic(player.getUniqueId(), id, null);
        if (getMain().getSyncManager() != null) getMain().getSyncManager().syncCosmeticUnlock(player.getUniqueId());

        player.sendMessage(Utils.translate(getMain().getLanguageHandler().getPrefix()
                + " <green>Has crafteado</green> " + cosmetic.getDisplayName()));
        playSound(SoundType.ANVIL_USE);
        reloadMyMenu();
    }

    @Override
    public void OnMenuClosed() { }

    private ItemStack buildTile(Profile profile, Cosmetic c, List<CraftIngredient> recipe) {
        boolean owned = profile.ownsCosmetic(c.getId());
        boolean haveAll = getMain().getCosmeticCraftingHandler().hasIngredients(profile == null ? null : getOwner(), recipe);

        XMaterial xm = XMaterial.matchXMaterial(c.getIconMaterial()).orElse(XMaterial.PAPER);
        ItemStack base = xm.parseItem();
        if (!owned && !haveAll) base = XMaterial.GRAY_DYE.parseItem();

        List<String> lore = new ArrayList<>();
        lore.add(Utils.translate("<gray>Categoría: <white>" + c.getCategory().getId() + "</white></gray>"));
        lore.add(Utils.translate("<gray>Rareza: <white>" + c.getRarity() + "</white></gray>"));
        lore.add("");
        lore.add(Utils.translate("<gray>Ingredientes:</gray>"));
        for (CraftIngredient ing : recipe) {
            String name;
            if (ing.getCraftType() == CraftType.POINTS) name = "Puntos de botín";
            else if (ing.getCraftType() == CraftType.MONEY) name = "Monedas";
            else name = "Baúl: " + ing.getName();
            lore.add(Utils.translate("<gray> · <white>" + ing.getAmount() + "</white> " + name + "</gray>"));
        }
        lore.add("");
        if (owned) {
            lore.add(Utils.translate("<green>✔ Ya lo tienes</green>"));
        } else if (haveAll) {
            lore.add(Utils.translate("<yellow>Clic para craftear</yellow>"));
        } else {
            lore.add(Utils.translate("<red>Te faltan ingredientes</red>"));
        }
        lore.add(Utils.translate("<aqua>Shift+clic para previsualizar (30s)</aqua>"));

        ItemBuilder builder = new ItemBuilder(base)
                .setName(Utils.translate(c.getDisplayName()))
                .setLore(lore)
                .hideAttributes();
        if (owned) builder.addGlow();
        ItemStack item = builder.toItemStack();
        item = NBTEditor.set(item, "craft", NBTEditor.CUSTOM_DATA, "action");
        item = NBTEditor.set(item, c.getId(), NBTEditor.CUSTOM_DATA, "cosmeticId");
        return item;
    }

    private ItemStack balanceItem(Profile profile) {
        return new ItemBuilder(XMaterial.GOLD_NUGGET.parseItem())
                .setName(Utils.translate("<gold>Saldo: " + profile.getLootPoints() + "p</gold>"))
                .toItemStack();
    }
}
