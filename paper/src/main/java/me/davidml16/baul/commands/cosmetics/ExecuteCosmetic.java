package me.davidml16.baul.commands.cosmetics;

import me.davidml16.baul.Main;
import me.davidml16.baul.cosmetics.Cosmetic;
import me.davidml16.baul.cosmetics.CosmeticCategory;
import me.davidml16.baul.cosmetics.types.Emote;
import me.davidml16.baul.cosmetics.types.Hat;
import me.davidml16.baul.cosmetics.types.Pet;
import me.davidml16.baul.menus.player.CosmeticCraftingMenu;
import me.davidml16.baul.menus.player.CosmeticsMenu;
import me.davidml16.baul.menus.player.MarketplaceMenu;
import me.davidml16.baul.objects.Profile;
import me.davidml16.baul.utils.Utils;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.Map;
import java.util.UUID;

public class ExecuteCosmetic {

    private final Main main;

    public ExecuteCosmetic(Main main) {
        this.main = main;
    }

    public boolean executeCommand(CommandSender sender, String label, String[] args) {
        if (main.getCosmeticRegistry() == null || !main.getCosmeticRegistry().isEnabled()) {
            sender.sendMessage(Utils.translate(main.getLanguageHandler().getPrefix() + " <red>Los cosméticos están desactivados. Activa Cosmetics.Enabled en el config.yml.</red>"));
            return true;
        }

        if (args.length < 2) {
            return openMenu(sender);
        }

        switch (args[1].toLowerCase()) {
            case "menu":
            case "open":
                return openMenu(sender);
            case "list":
                return listCosmetics(sender, args);
            case "equip":
                return equip(sender, label, args);
            case "unequip":
                return unequip(sender, label, args);
            case "use":
                return useEmote(sender, label, args);
            case "preview":
                return previewCosmetic(sender, label, args);
            case "shop":
            case "market":
            case "marketplace":
                return openMarketplace(sender);
            case "craft":
            case "crafting":
                return openCrafting(sender);
            case "visibility":
            case "visible":
                return toggleVisibility(sender, args);
            case "grant":
                return grant(sender, label, args);
            case "revoke":
                return revoke(sender, label, args);
        }

        sender.sendMessage("");
        sender.sendMessage(Utils.translate(main.getLanguageHandler().getPrefix() + " <red>Subcomando desconocido. Usa /" + label + " cosmetic para ver la ayuda.</red>"));
        sender.sendMessage("");
        return false;
    }

    private boolean openMenu(CommandSender sender) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(Utils.translate("&cSolo los jugadores pueden abrir el menú de cosméticos."));
            return true;
        }
        new CosmeticsMenu(main, (Player) sender).open();
        return true;
    }

    private boolean toggleVisibility(CommandSender sender, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(Utils.translate("&cSolo los jugadores pueden cambiar la visibilidad de cosméticos."));
            return true;
        }
        Player p = (Player) sender;
        Profile profile = main.getPlayerDataHandler().getData(p);
        if (profile == null) return true;

        boolean newValue;
        if (args.length >= 3) {
            String mode = args[2].toLowerCase();
            if (mode.equals("on") || mode.equals("show") || mode.equals("true")) newValue = true;
            else if (mode.equals("off") || mode.equals("hide") || mode.equals("false")) newValue = false;
            else newValue = !profile.isCosmeticsVisible();
        } else {
            newValue = !profile.isCosmeticsVisible();
        }

        profile.setCosmeticsVisible(newValue);
        main.getDatabaseHandler().saveProfileAsync(profile, p.getName());
        if (main.getPetManager() != null) main.getPetManager().refreshVisibilityFor(p, newValue);

        sender.sendMessage(Utils.translate(main.getLanguageHandler().getPrefix()
                + " <green>Cosméticos de otros jugadores:</green> "
                + (newValue ? "<aqua>visibles</aqua>" : "<gray>ocultos</gray>")));
        return true;
    }

    private boolean openMarketplace(CommandSender sender) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(Utils.translate("&cSolo los jugadores pueden abrir la tienda."));
            return true;
        }
        new MarketplaceMenu(main, (Player) sender).open();
        return true;
    }

    private boolean openCrafting(CommandSender sender) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(Utils.translate("&cSolo los jugadores pueden abrir el menú de crafteo."));
            return true;
        }
        new CosmeticCraftingMenu(main, (Player) sender).open();
        return true;
    }

    private boolean showEquipped(CommandSender sender, String label) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(Utils.translate("&cSolo los jugadores pueden ver sus cosméticos equipados."));
            return true;
        }
        Player p = (Player) sender;
        Profile profile = main.getPlayerDataHandler().getData(p);
        if (profile == null) return true;

        sender.sendMessage("");
        sender.sendMessage(Utils.translate("<gold><bold>═══ Cosméticos ═══</bold></gold>"));
        Map<String, String> equipped = profile.getEquippedCosmetics();
        if (equipped.isEmpty()) {
            sender.sendMessage(Utils.translate("<gray>No tienes ningún cosmético equipado.</gray>"));
        } else {
            for (Map.Entry<String, String> e : equipped.entrySet()) {
                Cosmetic c = main.getCosmeticRegistry().getById(e.getValue());
                String name = c != null ? c.getDisplayName() : e.getValue();
                sender.sendMessage(Utils.translate("<gray>-</gray> <yellow>" + e.getKey() + ":</yellow> " + name));
            }
        }
        sender.sendMessage(Utils.translate("<gray>Usa <green>/" + label + " cosmetic list</green> para explorar, <green>equip <id></green> o <green>unequip <categoría></green>.</gray>"));
        sender.sendMessage("");
        return true;
    }

    private boolean listCosmetics(CommandSender sender, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(Utils.translate("&cSolo los jugadores pueden listar cosméticos."));
            return true;
        }
        Player p = (Player) sender;
        Profile profile = main.getPlayerDataHandler().getData(p);
        if (profile == null) return true;

        CosmeticCategory filter = null;
        if (args.length >= 3) {
            filter = CosmeticCategory.fromId(args[2]);
            if (filter == null) {
                sender.sendMessage(Utils.translate(main.getLanguageHandler().getPrefix() + " <red>Categoría desconocida: " + args[2] + "</red>"));
                return true;
            }
        }

        sender.sendMessage("");
        sender.sendMessage(Utils.translate("<gold><bold>═══ Cosméticos " + (filter != null ? "(" + filter.getId() + ") " : "") + "═══</bold></gold>"));
        int shown = 0;
        for (Cosmetic c : main.getCosmeticRegistry().getAll().values()) {
            if (filter != null && c.getCategory() != filter) continue;
            boolean owned = profile.ownsCosmetic(c.getId());
            String mark = owned ? "<green>✔</green>" : "<dark_gray>✘</dark_gray>";
            sender.sendMessage(Utils.translate(mark + " <yellow>" + c.getId() + "</yellow> " + c.getDisplayName() + " <gray>[" + c.getRarity() + "]</gray>"));
            shown++;
        }
        if (shown == 0) sender.sendMessage(Utils.translate("<gray>No hay cosméticos registrados.</gray>"));
        sender.sendMessage("");
        return true;
    }

    private boolean equip(CommandSender sender, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(Utils.translate("&cSolo los jugadores pueden equipar cosméticos."));
            return true;
        }
        if (args.length < 3) {
            sender.sendMessage(Utils.translate(main.getLanguageHandler().getPrefix() + " <red>Uso: /" + label + " cosmetic equip <id></red>"));
            return false;
        }
        Player p = (Player) sender;
        Profile profile = main.getPlayerDataHandler().getData(p);
        if (profile == null) return true;

        String id = args[2].toLowerCase();
        Cosmetic cosmetic = main.getCosmeticRegistry().getById(id);
        if (cosmetic == null) {
            sender.sendMessage(Utils.translate(main.getLanguageHandler().getPrefix() + " <red>Cosmético desconocido: " + id + "</red>"));
            return true;
        }
        if (!profile.ownsCosmetic(id)) {
            sender.sendMessage(Utils.translate(main.getLanguageHandler().getPrefix() + " <red>No tienes este cosmético.</red>"));
            return true;
        }
        if (cosmetic.requiresPermission() && !main.playerHasPermission(p, cosmetic.getPermission())) {
            sender.sendMessage(Utils.translate(main.getLanguageHandler().getPrefix() + " <red>No tienes permiso para equipar este cosmético.</red>"));
            return true;
        }

        String categoryId = cosmetic.getCategory().getId();
        profile.getEquippedCosmetics().put(categoryId, id);
        main.getDatabaseHandler().setEquippedCosmetic(p.getUniqueId(), categoryId, id, null);
        main.getSyncManager().syncCosmeticEquip(p.getUniqueId());

        if (cosmetic instanceof Hat && main.getHatApplier() != null) {
            main.getHatApplier().restore(p);
            main.getHatApplier().apply(p, (Hat) cosmetic);
        }
        if (cosmetic instanceof Pet && main.getPetManager() != null) {
            main.getPetManager().spawn(p, (Pet) cosmetic);
        }

        sender.sendMessage(Utils.translate(main.getLanguageHandler().getPrefix() + " <green>Equipado:</green> " + cosmetic.getDisplayName()));
        return true;
    }

    private boolean unequip(CommandSender sender, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(Utils.translate("&cSolo los jugadores pueden sacar cosméticos."));
            return true;
        }
        if (args.length < 3) {
            sender.sendMessage(Utils.translate(main.getLanguageHandler().getPrefix() + " <red>Uso: /" + label + " cosmetic unequip <categoría></red>"));
            return false;
        }
        Player p = (Player) sender;
        Profile profile = main.getPlayerDataHandler().getData(p);
        if (profile == null) return true;

        CosmeticCategory category = CosmeticCategory.fromId(args[2]);
        if (category == null) {
            sender.sendMessage(Utils.translate(main.getLanguageHandler().getPrefix() + " <red>Categoría desconocida: " + args[2] + "</red>"));
            return true;
        }
        String previous = profile.getEquippedCosmetics().remove(category.getId());
        main.getDatabaseHandler().unequipCosmetic(p.getUniqueId(), category.getId(), null);
        main.getSyncManager().syncCosmeticEquip(p.getUniqueId());

        if (category == CosmeticCategory.HAT && main.getHatApplier() != null) {
            main.getHatApplier().restore(p);
        }
        if (category == CosmeticCategory.PET && main.getPetManager() != null) {
            main.getPetManager().despawn(p.getUniqueId());
        }

        if (previous != null) {
            sender.sendMessage(Utils.translate(main.getLanguageHandler().getPrefix() + " <green>Quitaste el cosmético de " + category.getId() + ".</green>"));
        } else {
            sender.sendMessage(Utils.translate(main.getLanguageHandler().getPrefix() + " <gray>No había nada equipado en " + category.getId() + ".</gray>"));
        }
        return true;
    }

    private boolean previewCosmetic(CommandSender sender, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(Utils.translate("&cSolo los jugadores pueden probar cosméticos."));
            return true;
        }
        if (args.length < 3) {
            sender.sendMessage(Utils.translate(main.getLanguageHandler().getPrefix() + " <red>Uso: /" + label + " cosmetic preview <id></red>"));
            return false;
        }
        Player p = (Player) sender;
        String id = args[2].toLowerCase();
        Cosmetic cosmetic = main.getCosmeticRegistry().getById(id);
        if (cosmetic == null) {
            sender.sendMessage(Utils.translate(main.getLanguageHandler().getPrefix() + " <red>Cosmético desconocido: " + id + "</red>"));
            return true;
        }
        main.getPreviewManager().preview(p, cosmetic);
        sender.sendMessage(Utils.translate(main.getLanguageHandler().getPrefix() + " <aqua>Vista previa</aqua> " + cosmetic.getDisplayName() + " <gray>(30s)</gray>"));
        return true;
    }

    private boolean useEmote(CommandSender sender, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(Utils.translate("&cSolo los jugadores pueden usar emotes."));
            return true;
        }
        if (args.length < 3) {
            sender.sendMessage(Utils.translate(main.getLanguageHandler().getPrefix() + " <red>Uso: /" + label + " cosmetic use <id></red>"));
            return false;
        }
        Player p = (Player) sender;
        Profile profile = main.getPlayerDataHandler().getData(p);
        if (profile == null) return true;

        String id = args[2].toLowerCase();
        Cosmetic cosmetic = main.getCosmeticRegistry().getById(id);
        if (!(cosmetic instanceof Emote)) {
            sender.sendMessage(Utils.translate(main.getLanguageHandler().getPrefix() + " <red>Este id no es un emote: " + id + "</red>"));
            return true;
        }
        Emote emote = (Emote) cosmetic;
        if (!profile.ownsCosmetic(id)) {
            sender.sendMessage(Utils.translate(main.getLanguageHandler().getPrefix() + " <red>No tienes este emote.</red>"));
            return true;
        }
        if (emote.requiresPermission() && !main.playerHasPermission(p, emote.getPermission())) {
            sender.sendMessage(Utils.translate(main.getLanguageHandler().getPrefix() + " <red>No tienes permiso.</red>"));
            return true;
        }
        long remaining = main.getEmoteCooldowns().remaining(p.getUniqueId(), id, emote.getCooldownMillis());
        if (remaining > 0) {
            sender.sendMessage(Utils.translate(main.getLanguageHandler().getPrefix() + " <yellow>En cooldown: " + (remaining / 1000.0) + "s</yellow>"));
            return true;
        }
        main.getEmoteCooldowns().mark(p.getUniqueId(), id);
        emote.trigger(p);
        return true;
    }

    private boolean grant(CommandSender sender, String label, String[] args) {
        if (sender instanceof Player && !main.playerHasPermission((Player) sender, "baul.admin")) {
            sender.sendMessage(main.getLanguageHandler().getMessage("Commands.NoPerms"));
            return false;
        }
        if (args.length < 4) {
            sender.sendMessage(Utils.translate(main.getLanguageHandler().getPrefix() + " <red>Uso: /" + label + " cosmetic grant <jugador> <id></red>"));
            return false;
        }
        String targetName = args[2];
        String id = args[3].toLowerCase();
        Cosmetic cosmetic = main.getCosmeticRegistry().getById(id);
        if (cosmetic == null) {
            sender.sendMessage(Utils.translate(main.getLanguageHandler().getPrefix() + " <red>Cosmético desconocido: " + id + "</red>"));
            return true;
        }
        Player target = Bukkit.getPlayerExact(targetName);
        if (target == null) {
            sender.sendMessage(Utils.translate(main.getLanguageHandler().getPrefix() + " <red>Jugador no conectado: " + targetName + "</red>"));
            return true;
        }
        UUID targetUuid = target.getUniqueId();
        Profile profile = main.getPlayerDataHandler().getData(target);
        if (profile != null && profile.ownsCosmetic(id)) {
            sender.sendMessage(Utils.translate(main.getLanguageHandler().getPrefix() + " <yellow>" + target.getName() + " ya tiene " + id + ".</yellow>"));
            return true;
        }
        main.getDatabaseHandler().addOwnedCosmetic(targetUuid, id, () -> {
            if (profile != null) profile.getOwnedCosmetics().add(id);
            main.getSyncManager().syncCosmeticUnlock(targetUuid);
            sender.sendMessage(Utils.translate(main.getLanguageHandler().getPrefix() + " <green>Has dado " + id + " a " + target.getName() + ".</green>"));
        });
        return true;
    }

    private boolean revoke(CommandSender sender, String label, String[] args) {
        if (sender instanceof Player && !main.playerHasPermission((Player) sender, "baul.admin")) {
            sender.sendMessage(main.getLanguageHandler().getMessage("Commands.NoPerms"));
            return false;
        }
        if (args.length < 4) {
            sender.sendMessage(Utils.translate(main.getLanguageHandler().getPrefix() + " <red>Uso: /" + label + " cosmetic revoke <jugador> <id></red>"));
            return false;
        }
        String targetName = args[2];
        String id = args[3].toLowerCase();
        Player target = Bukkit.getPlayerExact(targetName);
        if (target == null) {
            sender.sendMessage(Utils.translate(main.getLanguageHandler().getPrefix() + " <red>Jugador no conectado: " + targetName + "</red>"));
            return true;
        }
        UUID targetUuid = target.getUniqueId();
        main.getDatabaseHandler().removeOwnedCosmetic(targetUuid, id);
        Profile profile = main.getPlayerDataHandler().getData(target);
        if (profile != null) {
            profile.getOwnedCosmetics().remove(id);
            CosmeticCategory cat = CosmeticCategory.fromId(id.split("_", 2)[0]);
            if (cat != null && id.equals(profile.getEquippedCosmetics().get(cat.getId()))) {
                profile.getEquippedCosmetics().remove(cat.getId());
                main.getDatabaseHandler().unequipCosmetic(targetUuid, cat.getId(), null);
            }
        }
        main.getSyncManager().syncCosmeticUnlock(targetUuid);
        sender.sendMessage(Utils.translate(main.getLanguageHandler().getPrefix() + " <green>Has quitado " + id + " a " + target.getName() + ".</green>"));
        return true;
    }
}
