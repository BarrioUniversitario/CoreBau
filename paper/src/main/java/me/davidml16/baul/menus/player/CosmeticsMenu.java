package me.davidml16.baul.menus.player;

import com.cryptomorin.xseries.XMaterial;
import io.github.bananapuncher714.nbteditor.NBTEditor;
import me.davidml16.baul.Main;
import me.davidml16.baul.cosmetics.Cosmetic;
import me.davidml16.baul.cosmetics.CosmeticCategory;
import me.davidml16.baul.cosmetics.types.Emote;
import me.davidml16.baul.cosmetics.types.Hat;
import me.davidml16.baul.cosmetics.types.Pet;
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
import org.bukkit.inventory.meta.SkullMeta;

import java.util.ArrayList;
import java.util.List;

public class CosmeticsMenu extends Menu {

    private CosmeticCategory filter;

    public CosmeticsMenu(Main main, Player player) {
        super(main, player);
        setSize(6);
        this.filter = null;
    }

    public CosmeticsMenu(Main main, Player player, CosmeticCategory filter) {
        super(main, player);
        setSize(6);
        this.filter = filter;
    }

    @Override
    public void OnPageOpened(int page) {
        Player player = getOwner();
        Profile profile = getMain().getPlayerDataHandler().getData(player);
        if (profile == null) return;

        List<Cosmetic> all = new ArrayList<>();
        for (Cosmetic c : getMain().getCosmeticRegistry().getAll().values()) {
            if (filter == null || c.getCategory() == filter) all.add(c);
        }
        all.sort((a, b) -> {
            // Rarity desc (mythic first), then category, then id.
            int rr = Integer.compare(rarityOrder(b.getRarity()), rarityOrder(a.getRarity()));
            if (rr != 0) return rr;
            int cat = a.getCategory().getId().compareTo(b.getCategory().getId());
            return cat != 0 ? cat : a.getId().compareTo(b.getId());
        });

        if (page < 0) { openPage(0); return; }
        if (page > 0 && all.size() < (page * getPageSize()) + 1) { openPage(getPage() - 1); return; }

        String title = filter == null
                ? "Cosméticos"
                : ("Cosméticos — " + categoryDisplayName(filter));
        Inventory gui = createInventory(getSize(), Utils.translate("<dark_gray>" + title + "</dark_gray>"));

        ItemStack hidden = new ItemBuilder(XMaterial.GRAY_STAINED_GLASS_PANE.parseItem()).setName(" ").toItemStack();
        fillTopSide(hidden, 5);
        gui.setItem(8, buildVisibilityButton(profile));

        int sub = Math.min(all.size(), (page + 1) * getPageSize());
        List<Cosmetic> pageItems = all.subList(page * getPageSize(), sub);
        for (Cosmetic c : pageItems) {
            gui.addItem(buildCosmeticItem(profile, c));
        }

        // Top row primary actions (override the filler put down by fillTopSide).
        gui.setItem(1, buildShopButton());
        gui.setItem(4, buildHeaderItem(player, profile));
        gui.setItem(7, buildCraftButton());

        // Fila inferior: filtros + visibilidad + paginación.
        gui.setItem(46, buildFilterItem(null, "Todos", XMaterial.NETHER_STAR, profile));
        gui.setItem(47, buildFilterItem(CosmeticCategory.TRAIL, "Rastros", XMaterial.BLAZE_POWDER, profile));
        gui.setItem(48, buildFilterItem(CosmeticCategory.HAT, "Sombreros", XMaterial.CARVED_PUMPKIN, profile));
        gui.setItem(49, buildFilterItem(CosmeticCategory.WING, "Alas", XMaterial.FEATHER, profile));
        gui.setItem(50, buildFilterItem(CosmeticCategory.PET, "Mascotas", XMaterial.BONE, profile));
        gui.setItem(51, buildFilterItem(CosmeticCategory.JOIN_EFFECT, "Entrada", XMaterial.WHITE_WOOL, profile));
        gui.setItem(52, buildFilterItem(CosmeticCategory.EMOTE, "Emotes", XMaterial.PAPER, profile));
        // Visibility toggle is available in the GUI at the top row now.

        if (page > 0) {
            ItemStack prev = new ItemBuilder(XMaterial.ARROW.parseItem())
                    .setName(Utils.translate("<gray>« Página anterior</gray>"))
                    .toItemStack();
            prev = NBTEditor.set(prev, "previous", NBTEditor.CUSTOM_DATA, "action");
            gui.setItem(45, prev);
        }
        if (all.size() > (page + 1) * getPageSize()) {
            ItemStack next = new ItemBuilder(XMaterial.ARROW.parseItem())
                    .setName(Utils.translate("<gray>Página siguiente »</gray>"))
                    .toItemStack();
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
            case "shop":
                new MarketplaceMenu(getMain(), player).open();
                return;
            case "craft":
                new CosmeticCraftingMenu(getMain(), player).open();
                return;
            case "visibility": {
                boolean newVal = !profile.isCosmeticsVisible();
                profile.setCosmeticsVisible(newVal);
                getMain().getDatabaseHandler().saveProfileAsync(profile, player.getName());
                if (getMain().getPetManager() != null) getMain().getPetManager().refreshVisibilityFor(player, newVal);
                player.sendMessage(Utils.translate(getMain().getLanguageHandler().getPrefix()
                        + " <green>Cosméticos de otros jugadores:</green> "
                        + (newVal ? "<aqua>visibles</aqua>" : "<gray>ocultos</gray>")));
                playSound(SoundType.CLICK);
                reloadMyMenu();
                return;
            }
            case "filter": {
                String catId = NBTEditor.getString(clicked, NBTEditor.CUSTOM_DATA, "category");
                this.filter = (catId == null || catId.isEmpty()) ? null : CosmeticCategory.fromId(catId);
                playSound(SoundType.CLICK);
                openPage(0);
                return;
            }
            case "cosmetic":
                handleCosmeticClick(event, player, profile,
                        NBTEditor.getString(clicked, NBTEditor.CUSTOM_DATA, "cosmeticId"));
                return;
        }
    }

    private void handleCosmeticClick(InventoryClickEvent event, Player player, Profile profile, String id) {
        Cosmetic cosmetic = getMain().getCosmeticRegistry().getById(id);
        if (cosmetic == null) return;

        // Shift-click = vista previa (funciona con poseídos y no poseídos)
        if (event.getClick() == ClickType.SHIFT_LEFT || event.getClick() == ClickType.SHIFT_RIGHT) {
            getMain().getPreviewManager().preview(player, cosmetic);
            player.closeInventory();
            player.sendMessage(Utils.translate(getMain().getLanguageHandler().getPrefix() + " <aqua>Vista previa</aqua> " + cosmetic.getDisplayName() + " <gray>(30s)</gray>"));
            return;
        }
        if (!profile.ownsCosmetic(id)) {
            player.sendMessage(Utils.translate(getMain().getLanguageHandler().getPrefix() + " <red>Todavía no tienes este cosmético.</red> <gray>(Shift+clic para previsualizar)</gray>"));
            playSound(SoundType.NOTE_PLING);
            return;
        }
        if (cosmetic.requiresPermission() && !getMain().playerHasPermission(player, cosmetic.getPermission())) {
            player.sendMessage(Utils.translate(getMain().getLanguageHandler().getPrefix() + " <red>No tienes permiso para equipar esto.</red>"));
            return;
        }

        // Los emotes se activan, no se equipan
        if (cosmetic instanceof Emote) {
            Emote emote = (Emote) cosmetic;
            long remaining = getMain().getEmoteCooldowns().remaining(player.getUniqueId(), id, emote.getCooldownMillis());
            if (remaining > 0) {
                player.sendMessage(Utils.translate(getMain().getLanguageHandler().getPrefix() + " <yellow>En cooldown: " + (remaining / 1000.0) + "s</yellow>"));
                return;
            }
            getMain().getEmoteCooldowns().mark(player.getUniqueId(), id);
            player.closeInventory();
            emote.trigger(player);
            return;
        }

        // Cancel any preview state before applying a real equip/unequip action.
        if (getMain().getPreviewManager().isPreviewing(player.getUniqueId())) {
            getMain().getPreviewManager().revert(player.getUniqueId());
        }

        String catId = cosmetic.getCategory().getId();
        boolean alreadyEquipped = id.equals(profile.getEquipped(catId));

        if (event.getClick() == ClickType.RIGHT || alreadyEquipped) {
            profile.getEquippedCosmetics().remove(catId);
            getMain().getDatabaseHandler().unequipCosmetic(player.getUniqueId(), catId, null);
            getMain().getSyncManager().syncCosmeticEquip(player.getUniqueId());
            if (cosmetic.getCategory() == CosmeticCategory.HAT && getMain().getHatApplier() != null) {
                getMain().getHatApplier().restore(player);
            }
            if (cosmetic.getCategory() == CosmeticCategory.PET && getMain().getPetManager() != null) {
                getMain().getPetManager().despawn(player.getUniqueId());
            }
            player.sendMessage(Utils.translate(getMain().getLanguageHandler().getPrefix() + " <green>Quitaste:</green> " + cosmetic.getDisplayName()));
        } else {
            profile.getEquippedCosmetics().put(catId, id);
            getMain().getDatabaseHandler().setEquippedCosmetic(player.getUniqueId(), catId, id, null);
            getMain().getSyncManager().syncCosmeticEquip(player.getUniqueId());
            if (cosmetic instanceof Hat && getMain().getHatApplier() != null) {
                getMain().getHatApplier().restore(player);
                getMain().getHatApplier().apply(player, (Hat) cosmetic);
            }
            if (cosmetic instanceof Pet && getMain().getPetManager() != null) {
                getMain().getPetManager().spawn(player, (Pet) cosmetic);
            }
            player.sendMessage(Utils.translate(getMain().getLanguageHandler().getPrefix() + " <green>Equipado:</green> " + cosmetic.getDisplayName()));
        }
        playSound(SoundType.CLICK);
        reloadMyMenu();
    }

    @Override
    public void OnMenuClosed() { }

    // -------- item builders --------

    private ItemStack buildCosmeticItem(Profile profile, Cosmetic c) {
        boolean owned = profile.ownsCosmetic(c.getId());
        boolean equipped = c.getId().equals(profile.getEquipped(c.getCategory().getId()));

        XMaterial xm = XMaterial.matchXMaterial(c.getIconMaterial()).orElse(XMaterial.PAPER);
        ItemStack base = xm.parseItem();
        if (!owned) base = XMaterial.GRAY_DYE.parseItem();

        String rarityTag = rarityColor(c.getRarity());
        List<String> lore = new ArrayList<>();
        lore.add(Utils.translate("<gray>Categoría: <white>" + categoryDisplayName(c.getCategory()) + "</white></gray>"));
        lore.add(Utils.translate("<gray>Rareza: " + rarityTag + c.getRarity() + "</" + stripTag(rarityTag) + "></gray>"));
        if (c.isForSale()) {
            lore.add(Utils.translate("<gray>Precio: <gold>" + c.getPrice() + "p</gold></gray>"));
        }
        if (c instanceof Emote) {
            lore.add(Utils.translate("<gray>Cooldown: <white>" + (((Emote) c).getCooldownMillis() / 1000.0) + "s</white></gray>"));
        }
        lore.add("");
        if (equipped) {
            lore.add(Utils.translate("<green>✔ Equipado</green>"));
        } else if (owned) {
            lore.add(Utils.translate("<yellow>● Lo tienes</yellow>"));
        } else {
            lore.add(Utils.translate("<dark_gray>✘ Bloqueado</dark_gray>"));
        }
        lore.add("");
        if (owned) {
            if (c instanceof Emote) lore.add(Utils.translate("<yellow>Clic para activar</yellow>"));
            else lore.add(Utils.translate("<yellow>" + (equipped ? "Clic para quitar" : "Clic para equipar") + "</yellow>"));
            if (!(c instanceof Emote) && equipped) lore.add(Utils.translate("<yellow>Clic derecho para quitar el equipado</yellow>"));
        } else {
            lore.add(Utils.translate("<dark_gray>Consíguelo en un baúl, la tienda o crafteando</dark_gray>"));
        }
        lore.add(Utils.translate("<aqua>Shift+clic para previsualizar (30s)</aqua>"));

        ItemBuilder builder = new ItemBuilder(base)
                .setName(Utils.translate(c.getDisplayName()))
                .setLore(lore)
                .hideAttributes();
        // Glow on equipped OR on locked legendary/mythic to tease.
        boolean rare = rarityOrder(c.getRarity()) >= 3; // LEGENDARY or higher
        if (equipped || (rare && !owned)) builder.addGlow();

        ItemStack item = builder.toItemStack();
        item = NBTEditor.set(item, "cosmetic", NBTEditor.CUSTOM_DATA, "action");
        item = NBTEditor.set(item, c.getId(), NBTEditor.CUSTOM_DATA, "cosmeticId");
        return item;
    }

    private ItemStack buildFilterItem(CosmeticCategory category, String label, XMaterial icon, Profile profile) {
        boolean active = (filter == null && category == null) || (filter != null && filter == category);

        int owned = 0, total = 0;
        for (Cosmetic c : getMain().getCosmeticRegistry().getAll().values()) {
            if (category != null && c.getCategory() != category) continue;
            total++;
            if (profile.ownsCosmetic(c.getId())) owned++;
        }

        String name = (active ? "<green>" : "<white>") + label + " <gray>(" + owned + "/" + total + ")";
        ItemBuilder b = new ItemBuilder(icon.parseItem())
                .setName(Utils.translate(name))
                .hideAttributes();
        if (active) b.addGlow();
        ItemStack item = b.toItemStack();
        item = NBTEditor.set(item, "filter", NBTEditor.CUSTOM_DATA, "action");
        item = NBTEditor.set(item, category == null ? "" : category.getId(), NBTEditor.CUSTOM_DATA, "category");
        return item;
    }

    private ItemStack buildHeaderItem(Player player, Profile profile) {
        int owned = profile.getOwnedCosmetics().size();
        int total = getMain().getCosmeticRegistry().getAll().size();
        int equipped = profile.getEquippedCosmetics().size();

        ItemStack head = XMaterial.PLAYER_HEAD.parseItem();
        try {
            SkullMeta meta = (SkullMeta) head.getItemMeta();
            meta.setOwningPlayer(player);
            head.setItemMeta(meta);
        } catch (Throwable ignored) { /* fall back to generic head */ }

        List<String> lore = new ArrayList<>();
        lore.add(Utils.translate("<gray>Saldo: <gold>" + profile.getLootPoints() + "p</gold></gray>"));
        lore.add(Utils.translate("<gray>Tienes: <white>" + owned + " / " + total + "</white></gray>"));
        lore.add(Utils.translate("<gray>Equipados: <white>" + equipped + "</white></gray>"));
        lore.add("");
        lore.add(Utils.translate("<dark_gray>Haz clic en un cosmético para equipar o quitar</dark_gray>"));
        lore.add(Utils.translate("<dark_gray>Shift+clic en cualquier ítem para previsualizarlo</dark_gray>"));

        return new ItemBuilder(head)
                .setName(Utils.translate("<aqua><bold>" + player.getName() + "</bold></aqua>"))
                .setLore(lore)
                .hideAttributes()
                .toItemStack();
    }

    private ItemStack buildShopButton() {
        ItemStack shop = new ItemBuilder(XMaterial.EMERALD.parseItem())
                .setName(Utils.translate("<green><bold>Tienda</bold></green>"))
                .setLore(
                        Utils.translate("<gray>Gasta puntos de botín para</gray>"),
                        Utils.translate("<gray>desbloquear cosméticos directamente.</gray>"),
                        "",
                        Utils.translate("<yellow>Clic para abrir</yellow>"))
                .hideAttributes()
                .toItemStack();
        return NBTEditor.set(shop, "shop", NBTEditor.CUSTOM_DATA, "action");
    }

    private ItemStack buildCraftButton() {
        ItemStack craft = new ItemBuilder(XMaterial.CRAFTING_TABLE.parseItem())
                .setName(Utils.translate("<aqua><bold>Crafteo</bold></aqua>"))
                .setLore(
                        Utils.translate("<gray>Combina baúles, puntos y</gray>"),
                        Utils.translate("<gray>monedas para desbloquear cosméticos.</gray>"),
                        "",
                        Utils.translate("<yellow>Clic para abrir</yellow>"))
                .hideAttributes()
                .toItemStack();
        return NBTEditor.set(craft, "craft", NBTEditor.CUSTOM_DATA, "action");
    }

    private ItemStack buildVisibilityButton(Profile profile) {
        boolean visible = profile.isCosmeticsVisible();
        XMaterial mat = visible ? XMaterial.ENDER_EYE : XMaterial.ENDER_PEARL;
        ItemBuilder b = new ItemBuilder(mat.parseItem())
                .setName(Utils.translate(visible
                        ? "<aqua>Ver cosméticos ajenos: ACTIVADO</aqua>"
                        : "<dark_gray>Ver cosméticos ajenos: APAGADO</dark_gray>"))
                .setLore(
                        Utils.translate("<gray>Mostrar u ocultar los rastros y</gray>"),
                        Utils.translate("<gray>mascotas de otros jugadores.</gray>"),
                        "",
                        Utils.translate("<yellow>Clic para cambiar</yellow>"))
                .hideAttributes();
        if (visible) b.addGlow();
        ItemStack item = b.toItemStack();
        return NBTEditor.set(item, "visibility", NBTEditor.CUSTOM_DATA, "action");
    }

    /** Nombre mostrable de una categoría en español. */
    private String categoryDisplayName(CosmeticCategory c) {
        switch (c) {
            case TRAIL: return "Rastros";
            case HAT: return "Sombreros";
            case JOIN_EFFECT: return "Entrada";
            case EMOTE: return "Emotes";
            case PET: return "Mascotas";
            default: return capitalize(c.getId());
        }
    }

    // -------- helpers --------

    private String rarityColor(String rarity) {
        if (rarity == null) return "<white>";
        switch (rarity.toUpperCase()) {
            case "COMMON": return "<gray>";
            case "RARE": return "<aqua>";
            case "EPIC": return "<light_purple>";
            case "LEGENDARY": return "<gold>";
            case "MYTHIC": return "<red>";
            default: return "<white>";
        }
    }

    private int rarityOrder(String rarity) {
        if (rarity == null) return 0;
        switch (rarity.toUpperCase()) {
            case "COMMON": return 0;
            case "RARE": return 1;
            case "EPIC": return 2;
            case "LEGENDARY": return 3;
            case "MYTHIC": return 4;
            default: return 0;
        }
    }

    /** Returns the tag name (e.g. "gray") without angle brackets, for the close tag. */
    private String stripTag(String openTag) {
        return openTag.replace("<", "").replace(">", "");
    }

    private String capitalize(String s) {
        if (s == null || s.isEmpty()) return s;
        return Character.toUpperCase(s.charAt(0)) + s.substring(1);
    }
}
