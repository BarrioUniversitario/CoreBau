package me.davidml16.baul.menus.player;

import com.cryptomorin.xseries.XMaterial;
import io.github.bananapuncher714.nbteditor.NBTEditor;
import me.davidml16.baul.Main;
import me.davidml16.baul.cosmetics.Cosmetic;
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
import java.util.Arrays;
import java.util.List;

/**
 * Player-facing marketplace where any cosmetic with price > 0 can be bought
 * with loot points. Click purchases instantly (no confirmation submenu by
 * design — every cosmetic shows its price clearly and the deduction is one
 * action away from undoing via /baul cosmetic preview to check first).
 */
public class MarketplaceMenu extends Menu {

    public MarketplaceMenu(Main main, Player player) {
        super(main, player);
        setSize(6);
    }

    @Override
    public void OnPageOpened(int page) {
        Player player = getOwner();
        Profile profile = getMain().getPlayerDataHandler().getData(player);
        if (profile == null) return;

        List<Cosmetic> forSale = new ArrayList<>();
        for (Cosmetic c : getMain().getCosmeticRegistry().getAll().values()) {
            if (c.isForSale()) forSale.add(c);
        }
        forSale.sort((a, b) -> {
            int rc = Long.compare(a.getPrice(), b.getPrice());
            return rc != 0 ? rc : a.getId().compareTo(b.getId());
        });

        if (page < 0) { openPage(0); return; }
        if (page > 0 && forSale.size() < (page * getPageSize()) + 1) { openPage(getPage() - 1); return; }

        String title = Utils.translate("<dark_gray>Tienda de Cosméticos <gray>(" + profile.getLootPoints() + "p)</gray></dark_gray>");
        Inventory gui = createInventory(getSize(), title);

        ItemStack hidden = new ItemBuilder(XMaterial.GRAY_STAINED_GLASS_PANE.parseItem()).setName(" ").toItemStack();
        fillTopSide(hidden, 5);

        if (forSale.isEmpty()) {
            gui.setItem(22, new ItemBuilder(XMaterial.RED_STAINED_GLASS_PANE.parseItem())
                    .setName(Utils.translate("<red>No hay cosméticos a la venta</red>"))
                    .setLore("", Utils.translate("<gray>Los admins deben poner price > 0</gray>"),
                            Utils.translate("<gray>en cosmetics/*.yml para que aparezcan acá.</gray>"), "")
                    .toItemStack());
        } else {
            int sub = Math.min(forSale.size(), (page + 1) * getPageSize());
            for (Cosmetic c : forSale.subList(page * getPageSize(), sub)) {
                gui.addItem(buildTile(profile, c));
            }
        }

        fillTopSide(hidden, 5);

        // Bottom row: balance, back-to-cosmetics, prev/next
        gui.setItem(49, buildBalanceItem(profile));
        ItemStack back = new ItemBuilder(XMaterial.OAK_DOOR.parseItem())
                .setName(Utils.translate("<yellow>← Volver a cosméticos</yellow>"))
                .toItemStack();
        back = NBTEditor.set(back, "back", NBTEditor.CUSTOM_DATA, "action");
        gui.setItem(48, back);

        if (page > 0) {
            ItemStack prev = new ItemBuilder(XMaterial.ARROW.parseItem()).setName(Utils.translate("<gray>« Página anterior</gray>")).toItemStack();
            prev = NBTEditor.set(prev, "previous", NBTEditor.CUSTOM_DATA, "action");
            gui.setItem(45, prev);
        }
        if (forSale.size() > (page + 1) * getPageSize()) {
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
            case "buy": break;
            default: return;
        }

        String id = NBTEditor.getString(clicked, NBTEditor.CUSTOM_DATA, "cosmeticId");
        Cosmetic cosmetic = getMain().getCosmeticRegistry().getById(id);
        if (cosmetic == null || !cosmetic.isForSale()) return;

        // Shift-click = vista previa en vez de comprar
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
        long price = cosmetic.getPrice();
        if (profile.getLootPoints() < price) {
            long missing = price - profile.getLootPoints();
            player.sendMessage(Utils.translate(getMain().getLanguageHandler().getPrefix()
                    + " <red>No te alcanzan los puntos.</red> <gray>Te faltan " + missing + ".</gray>"));
            playSound(SoundType.NOTE_PLING);
            return;
        }

        // Comprar: descontar puntos, otorgar cosmético, persistir + sincronizar.
        profile.setLootPoints(profile.getLootPoints() - price);
        getMain().getDatabaseHandler().setPlayerLootPoints(player.getUniqueId(), profile.getLootPoints());
        getMain().getSyncManager().syncPointsChange(player.getUniqueId());

        profile.getOwnedCosmetics().add(id);
        getMain().getDatabaseHandler().addOwnedCosmetic(player.getUniqueId(), id, null);
        getMain().getSyncManager().syncCosmeticUnlock(player.getUniqueId());

        player.sendMessage(Utils.translate(getMain().getLanguageHandler().getPrefix()
                + " <green>Compraste</green> " + cosmetic.getDisplayName()
                + " <gray>por " + price + " puntos de botín.</gray>"));
        playSound(SoundType.ANVIL_USE);
        reloadMyMenu();
    }

    @Override
    public void OnMenuClosed() { }

    private ItemStack buildTile(Profile profile, Cosmetic c) {
        boolean owned = profile.ownsCosmetic(c.getId());
        boolean affordable = profile.getLootPoints() >= c.getPrice();

        XMaterial xm = XMaterial.matchXMaterial(c.getIconMaterial()).orElse(XMaterial.PAPER);
        ItemStack base = xm.parseItem();
        if (!owned && !affordable) base = XMaterial.GRAY_DYE.parseItem();

        String status = owned
                ? "<green>✔ Ya lo tienes"
                : (affordable ? "<yellow>Clic para comprar" : "<red>No te alcanzan los puntos");
        List<String> lore = new ArrayList<>(Arrays.asList(
                Utils.translate("<gray>Categoría: <white>" + c.getCategory().getId() + "</white></gray>"),
                Utils.translate("<gray>Rareza: <white>" + c.getRarity() + "</white></gray>"),
                Utils.translate("<gray>Precio: <gold>" + c.getPrice() + "p</gold></gray>"),
                "",
                Utils.translate("<gray>" + status + "</gray>"),
                Utils.translate("<aqua>Shift+clic para previsualizar (30s)</aqua>")
        ));

        ItemBuilder builder = new ItemBuilder(base)
                .setName(Utils.translate(c.getDisplayName()))
                .setLore(lore)
                .hideAttributes();
        if (owned) builder.addGlow();

        ItemStack item = builder.toItemStack();
        item = NBTEditor.set(item, "buy", NBTEditor.CUSTOM_DATA, "action");
        item = NBTEditor.set(item, c.getId(), NBTEditor.CUSTOM_DATA, "cosmeticId");
        return item;
    }

    private ItemStack buildBalanceItem(Profile profile) {
        return new ItemBuilder(XMaterial.GOLD_NUGGET.parseItem())
                .setName(Utils.translate("<gold>Saldo: " + profile.getLootPoints() + "p</gold>"))
                .setLore(Utils.translate("<gray>Ganas más abriendo baúles.</gray>"))
                .toItemStack();
    }
}
