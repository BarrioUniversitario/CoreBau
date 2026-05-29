package me.davidml16.baul.commands.baul;

import me.davidml16.baul.Main;
import me.davidml16.baul.commands.cubelets.subcommands.ExecuteBox;
import me.davidml16.baul.commands.cubelets.subcommands.ExecuteClear;
import me.davidml16.baul.commands.cubelets.subcommands.ExecuteGift;
import me.davidml16.baul.commands.cubelets.subcommands.ExecuteGive;
import me.davidml16.baul.commands.cubelets.subcommands.ExecuteGiveKey;
import me.davidml16.baul.commands.cubelets.subcommands.ExecuteInfo;
import me.davidml16.baul.commands.cubelets.subcommands.ExecuteLootHistory;
import me.davidml16.baul.commands.cubelets.subcommands.ExecuteOptions;
import me.davidml16.baul.commands.cubelets.subcommands.ExecutePreview;
import me.davidml16.baul.commands.cubelets.subcommands.ExecuteReload;
import me.davidml16.baul.commands.cubelets.subcommands.ExecuteRemove;
import me.davidml16.baul.commands.cubelets.subcommands.ExecuteSetup;
import me.davidml16.baul.commands.cubelets.subcommands.ExecuteType;
import me.davidml16.baul.commands.cosmetics.ExecuteCosmetic;
import me.davidml16.baul.cosmetics.CosmeticCategory;
import me.davidml16.baul.utils.Utils;
import me.davidml16.baul.utils.MiniMessageUtils;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class CoreCommand extends Command {

    private final Main main = Main.get();

    private final ExecuteGive executeGive = new ExecuteGive(main);
    private final ExecuteRemove executeRemove = new ExecuteRemove(main);
    private final ExecuteBox executeBox = new ExecuteBox(main);
    private final ExecuteType executeType = new ExecuteType(main);
    private final ExecuteReload executeReload = new ExecuteReload(main);
    private final ExecuteSetup executeSetup = new ExecuteSetup(main);
    private final ExecuteOptions executeOptions = new ExecuteOptions(main);
    private final ExecuteInfo executeInfo = new ExecuteInfo(main);
    private final ExecuteClear executeClear = new ExecuteClear(main);
    private final ExecuteGift executeGift = new ExecuteGift(main);
    private final ExecuteGiveKey executeGiveKey = new ExecuteGiveKey(main);
    private final ExecutePreview executePreview = new ExecutePreview(main);
    private final ExecuteLootHistory executeLootHistory = new ExecuteLootHistory(main);
    private final ExecuteCosmetic executeCosmetic = new ExecuteCosmetic(main);

    private final me.davidml16.baul.commands.points.subcommands.ExecuteGive executePointsGive = new me.davidml16.baul.commands.points.subcommands.ExecuteGive(main);
    private final me.davidml16.baul.commands.points.subcommands.ExecuteRemove executePointsRemove = new me.davidml16.baul.commands.points.subcommands.ExecuteRemove(main);
    private final me.davidml16.baul.commands.points.subcommands.ExecuteSet executePointsSet = new me.davidml16.baul.commands.points.subcommands.ExecuteSet(main);
    private final me.davidml16.baul.commands.points.subcommands.ExecuteInfo executePointsInfo = new me.davidml16.baul.commands.points.subcommands.ExecuteInfo(main);

    public CoreCommand(String name) {
        super(name);
    }

    @Override
    public boolean execute(CommandSender sender, String label, String[] args) {

        if(args.length == 0) {
            if(sender instanceof Player) {
                  if(!main.getLanguageHandler().isEmptyMessage("Commands.Balance.Cubelets"))
                      sender.sendMessage(MiniMessageUtils.format(main.getLanguageHandler().getMessage("Commands.Balance.Cubelets").replaceAll("%baul_available%", String.valueOf(main.getPlayerDataHandler().getData((Player) sender).getCubelets().size()))));
                return true;
            } else {
                return sendCommandHelp(sender, label);
            }
        }

        if(sender instanceof Player) {
            if(main.getConversationHandler().haveConversation((Player) sender)) return true;
        }

        switch (args[0].toLowerCase()) {
            case "help":
                return sendCommandHelp(sender, label);
            case "machine":
                return executeBox.executeCommand(sender, label, args);
            case "give":
                return executeGive.executeCommand(sender, label, args);
            case "givekey":
                return executeGiveKey.executeCommand(sender, label, args);
            case "remove":
                return executeRemove.executeCommand(sender, label, args);
            case "type":
                return executeType.executeCommand(sender, label, args);
            case "setup":
                return executeSetup.executeCommand(sender, label, args);
            case "options":
                return executeOptions.executeCommand(sender, label, args);
            case "reload":
                return executeReload.executeCommand(sender, label, args);
            case "info":
                return executeInfo.executeCommand(sender, label, args);
            case "clear":
                return executeClear.executeCommand(sender, label, args);
            case "gift":
                return executeGift.executeCommand(sender, label, args);
            case "preview":
                return executePreview.executeCommand(sender, label, args);
            case "history":
                return executeLootHistory.executeCommand(sender, label, args);
            case "points":
                return executePoints(sender, label, args);
            case "cosmetic":
            case "cosmetics":
                return executeCosmetic.executeCommand(sender, label, args);
        }

        sender.sendMessage("");
        sender.sendMessage(Utils.translate(main.getLanguageHandler().getPrefix() + " <red>Argumento inválido, usa /" + label + " help para ver comandos disponibles</red>"));
        sender.sendMessage("");
        return false;
    }

    private boolean executePoints(CommandSender sender, String label, String[] args) {
        if (args.length < 2) {
            if(sender instanceof Player) {
              if(!main.getLanguageHandler().isEmptyMessage("Commands.Balance.Points"))
                      sender.sendMessage(MiniMessageUtils.format(main.getLanguageHandler().getMessage("Commands.Balance.Points").replaceAll("%baul_points%", String.valueOf(main.getPlayerDataHandler().getData((Player) sender).getLootPoints()))));
                return true;
            }
            return sendCommandHelp(sender, label);
        }

        switch (args[1].toLowerCase()) {
            case "give":
                return executePointsGive.executeCommand(sender, label, shiftArgs(args));
            case "remove":
                return executePointsRemove.executeCommand(sender, label, shiftArgs(args));
            case "set":
                return executePointsSet.executeCommand(sender, label, shiftArgs(args));
            case "info":
                return executePointsInfo.executeCommand(sender, label, shiftArgs(args));
        }

        sender.sendMessage("");
        sender.sendMessage(Utils.translate(main.getLanguageHandler().getPrefix() + " <red>Argumento inválido, usa /" + label + " help</red>"));
        sender.sendMessage("");
        return false;
    }

    private String[] shiftArgs(String[] args) {
        String[] shifted = new String[args.length - 1];
        System.arraycopy(args, 1, shifted, 0, args.length - 1);
        return shifted;
    }

    private boolean sendCommandHelp(CommandSender sender, String label) {

        sender.sendMessage("");
        sender.sendMessage(Utils.translate("<gold><bold>═══ Baúl - Comandos ═══</bold></gold>"));

        if(sender instanceof Player) {
            sender.sendMessage(Utils.translate("<gray>-</gray> <green>/" + label + "</green> <gray>- Ver baúles disponibles</gray>"));

            if(main.isSetting("GiftCubeletsCommand")) {
                sender.sendMessage(Utils.translate("<gray>-</gray> <green>/" + label + " gift [player]</green> <gray>- Regalar baúles</gray>"));
            }

            if(main.isSetting("Rewards.Preview.Enabled")) {
                sender.sendMessage(Utils.translate("<gray>-</gray> <green>/" + label + " preview [typeID]</green> <gray>- Vista previa de recompensas</gray>"));
            }

            sender.sendMessage(Utils.translate("<gray>-</gray> <green>/" + label + " history</green> <gray>- Historial de botín</gray>"));
            sender.sendMessage(Utils.translate("<gray>-</gray> <green>/" + label + " points</green> <gray>- Ver tus puntos de botín</gray>"));

            if (main.getCosmeticRegistry() != null && main.getCosmeticRegistry().isEnabled()) {
                sender.sendMessage("");
                sender.sendMessage(Utils.translate("<gold><bold>═══ Cosméticos ═══</bold></gold>"));
                sender.sendMessage(Utils.translate("<gray>-</gray> <green>/" + label + " cosmetic</green> <gray>- Abrir menú de cosméticos</gray>"));
                sender.sendMessage(Utils.translate("<gray>-</gray> <green>/" + label + " cosmetic equip [id]</green> <gray>- Equipar un cosmético</gray>"));
                sender.sendMessage(Utils.translate("<gray>-</gray> <green>/" + label + " cosmetic unequip [categoría]</green> <gray>- Quitar cosmético</gray>"));
                sender.sendMessage(Utils.translate("<gray>-</gray> <green>/" + label + " cosmetic use [emote]</green> <gray>- Activar un emote</gray>"));
                sender.sendMessage(Utils.translate("<gray>-</gray> <green>/" + label + " cosmetic preview [id]</green> <gray>- Vista previa 30s</gray>"));
                sender.sendMessage(Utils.translate("<gray>-</gray> <green>/" + label + " cosmetic shop</green> <gray>- Comprar cosméticos con puntos</gray>"));
                sender.sendMessage(Utils.translate("<gray>-</gray> <green>/" + label + " cosmetic craft</green> <gray>- Craftear cosméticos con ingredientes</gray>"));
                sender.sendMessage(Utils.translate("<gray>-</gray> <green>/" + label + " cosmetic visibility [on/off]</green> <gray>- Mostrar/ocultar cosméticos de otros</gray>"));
            }

            if (main.playerHasPermission((Player) sender, "baul.admin")) {
                sender.sendMessage("");
                sender.sendMessage(Utils.translate("<gray>-</gray> <green>/" + label + " give [player] [typeID] [amount]</green> <gray>- Dar baúles</gray>"));
                sender.sendMessage(Utils.translate("<gray>-</gray> <green>/" + label + " remove [player] [typeID] [amount]</green> <gray>- Quitar baúles</gray>"));
                sender.sendMessage(Utils.translate("<gray>-</gray> <green>/" + label + " info [player]</green> <gray>- Info de baúles</gray>"));
                sender.sendMessage(Utils.translate("<gray>-</gray> <green>/" + label + " clear [player]</green> <gray>- Limpiar baúles</gray>"));

                if(main.isSetting("UseKeys"))
                    sender.sendMessage(Utils.translate("<gray>-</gray> <green>/" + label + " givekey [player] [typeID] [amount]</green> <gray>- Dar llaves</gray>"));

                sender.sendMessage("");
                sender.sendMessage(Utils.translate("<gold><bold>═══ Puntos ═══</bold></gold>"));
                sender.sendMessage(Utils.translate("<gray>-</gray> <green>/" + label + " points give [player] [amount]</green> <gray>- Dar puntos</gray>"));
                sender.sendMessage(Utils.translate("<gray>-</gray> <green>/" + label + " points remove [player] [amount]</green> <gray>- Quitar puntos</gray>"));
                sender.sendMessage(Utils.translate("<gray>-</gray> <green>/" + label + " points set [player] [amount]</green> <gray>- Establecer puntos</gray>"));
                sender.sendMessage(Utils.translate("<gray>-</gray> <green>/" + label + " points info [player]</green> <gray>- Ver puntos de otro jugador</gray>"));

                if (main.getCosmeticRegistry() != null && main.getCosmeticRegistry().isEnabled()) {
                    sender.sendMessage(Utils.translate("<gray>-</gray> <green>/" + label + " cosmetic grant [player] [id]</green> <gray>- Otorgar cosmético</gray>"));
                    sender.sendMessage(Utils.translate("<gray>-</gray> <green>/" + label + " cosmetic revoke [player] [id]</green> <gray>- Revocar cosmético</gray>"));
                }

                sender.sendMessage("");
                sender.sendMessage(Utils.translate("<gold><bold>═══ Administración ═══</bold></gold>"));
                sender.sendMessage(Utils.translate("<gray>-</gray> <green>/" + label + " machine [create/remove/edit]</green> <gray>- Administrar máquinas</gray>"));
                sender.sendMessage(Utils.translate("<gray>-</gray> <green>/" + label + " type</green> <gray>- Administrar tipos</gray>"));
                sender.sendMessage(Utils.translate("<gray>-</gray> <green>/" + label + " setup [typeID]</green> <gray>- Configurar tipo</gray>"));
                sender.sendMessage(Utils.translate("<gray>-</gray> <green>/" + label + " options</green> <gray>- Opciones</gray>"));
                sender.sendMessage(Utils.translate("<gray>-</gray> <green>/" + label + " reload</green> <gray>- Recargar plugin</gray>"));
            }

        } else {
            sender.sendMessage(Utils.translate("<gray>-</gray> <green>/" + label + " give [player] [typeID] [amount]</green>"));
            sender.sendMessage(Utils.translate("<gray>-</gray> <green>/" + label + " remove [player] [typeID] [amount]</green>"));
            sender.sendMessage(Utils.translate("<gray>-</gray> <green>/" + label + " info [player]</green>"));
            sender.sendMessage(Utils.translate("<gray>-</gray> <green>/" + label + " clear [player]</green>"));

            if(main.isSetting("UseKeys"))
                sender.sendMessage(Utils.translate("<gray>-</gray> <green>/" + label + " givekey [player] [typeID] [amount]</green>"));

            sender.sendMessage(Utils.translate("<gray>-</gray> <green>/" + label + " points give [player] [amount]</green>"));
            sender.sendMessage(Utils.translate("<gray>-</gray> <green>/" + label + " points remove [player] [amount]</green>"));
            sender.sendMessage(Utils.translate("<gray>-</gray> <green>/" + label + " points set [player] [amount]</green>"));
            sender.sendMessage(Utils.translate("<gray>-</gray> <green>/" + label + " points info [player]</green>"));
            sender.sendMessage(Utils.translate("<gray>-</gray> <green>/" + label + " reload</green>"));
        }

        sender.sendMessage("");
        return true;
    }

    @Override
    public List<String> tabComplete(CommandSender sender, String alias, String[] args) {
        if (!(sender instanceof Player)) {
            return null;
        }

        Player p = (Player) sender;

        List<String> list = new ArrayList<String>();
        List<String> auto = new ArrayList<String>();

        if (args.length == 1) {
            list.add("help");
            list.add("points");
            if (main.getCosmeticRegistry() != null && main.getCosmeticRegistry().isEnabled())
                list.add("cosmetic");
            if(main.isSetting("GiftCubeletsCommand"))
                list.add("gift");
            if(main.isSetting("Rewards.Preview.Enabled"))
                list.add("preview");
            list.add("history");
            if (main.playerHasPermission(p, "baul.admin")) {
                list.add("give");
                if(main.isSetting("UseKeys"))
                    list.add("givekey");
                list.add("info");
                list.add("clear");
                list.add("remove");
                list.add("machine");
                list.add("type");
                list.add("setup");
                list.add("options");
                list.add("reload");
            }
        }

        if (args[0].equalsIgnoreCase("points") && args.length == 2) {
            if (main.playerHasPermission(p, "baul.admin")) {
                list.add("give");
                list.add("remove");
                list.add("set");
                list.add("info");
            }
        }

        if ((args[0].equalsIgnoreCase("give") || args[0].equalsIgnoreCase("givekey")) && args.length >= 2) {
            if (main.playerHasPermission(p, "baul.admin")) {
                if (args.length == 2) {
                    for (Player target : main.getServer().getOnlinePlayers()) {
                        list.add(target.getName());
                    }
                    list.add("*");
                } else if (args.length == 3) {
                    list.addAll(main.getCubeletTypesHandler().getTypes().keySet());
                } else if (args.length == 4) {
                    list.add("1");
                }
            }
        } else if (args[0].equalsIgnoreCase("remove") && args.length >= 2) {
            if (main.playerHasPermission(p, "baul.admin")) {
                if (args.length == 2) {
                    for (Player target : main.getServer().getOnlinePlayers()) {
                        list.add(target.getName());
                    }
                } else if (args.length == 3) {
                    list.addAll(main.getCubeletTypesHandler().getTypes().keySet());
                }
            }
        } else if ((args[0].equalsIgnoreCase("info") || args[0].equalsIgnoreCase("clear")) && args.length == 2) {
            if (main.playerHasPermission(p, "baul.admin")) {
                for (Player target : main.getServer().getOnlinePlayers()) {
                    list.add(target.getName());
                }
            }
        } else if (args[0].equalsIgnoreCase("points") && args.length >= 3) {
            if (main.playerHasPermission(p, "baul.admin")) {
                switch (args[1].toLowerCase()) {
                    case "give":
                    case "remove":
                    case "set":
                    case "info":
                        if (args.length == 3) {
                            for (Player target : main.getServer().getOnlinePlayers()) {
                                list.add(target.getName());
                            }
                        } else if (args.length == 4 && !args[1].equalsIgnoreCase("info")) {
                            list.add("1");
                        }
                        break;
                }
            }
        } else if (args[0].equalsIgnoreCase("setup") && args.length == 2) {
            if (main.playerHasPermission(p, "baul.admin")) {
                for (File file : Objects.requireNonNull(new File(main.getDataFolder(), "types").listFiles())) {
                    list.add(file.getName().replace(".yml", ""));
                }
            }
        } else if (args[0].equalsIgnoreCase("machine") && args.length == 2) {
            if (main.playerHasPermission(p, "baul.admin")) {
                list.add("create");
                list.add("remove");
                list.add("edit");
            }
        } else if (args[0].equalsIgnoreCase("type")) {
            if (main.playerHasPermission(p, "baul.admin")) {
                if(args.length == 2) {
                    list.add("create");
                    list.add("remove");
                    list.add("template");
                    list.add("list");
                } else if(args.length == 3) {
                    if(args[1].equalsIgnoreCase("remove")) {
                        for (String type : main.getCubeletTypesHandler().getTypes().keySet()) {
                            list.add(type.toLowerCase());
                        }
                    } else if(args[1].equalsIgnoreCase("template")) {
                        list.addAll(main.getTemplates());
                        list.add("*");
                    }
                }
            }
        } else if (args[0].equalsIgnoreCase("gift") && args.length == 2) {
            for (Player target : main.getServer().getOnlinePlayers()) {
                if(!target.getName().equalsIgnoreCase(p.getName()))
                    list.add(target.getName());
            }
        } else if (args[0].equalsIgnoreCase("preview") && args.length == 2) {
            list.addAll( main.getCubeletTypesHandler().getTypes().keySet());
        } else if ((args[0].equalsIgnoreCase("cosmetic") || args[0].equalsIgnoreCase("cosmetics"))
                && main.getCosmeticRegistry() != null && main.getCosmeticRegistry().isEnabled()) {
            if (args.length == 2) {
                list.add("menu");
                list.add("list");
                list.add("equip");
                list.add("unequip");
                list.add("use");
                list.add("preview");
                list.add("shop");
                list.add("craft");
                list.add("visibility");
                if (main.playerHasPermission(p, "baul.admin")) {
                    list.add("grant");
                    list.add("revoke");
                }
            } else if (args.length == 3) {
                String sub = args[1].toLowerCase();
                if (sub.equals("equip") || sub.equals("use") || sub.equals("preview")) {
                    list.addAll(main.getCosmeticRegistry().getAll().keySet());
                } else if (sub.equals("visibility") || sub.equals("visible")) {
                    list.add("on");
                    list.add("off");
                    list.add("toggle");
                } else if (sub.equals("unequip") || sub.equals("list")) {
                    for (CosmeticCategory c : CosmeticCategory.values()) list.add(c.getId());
                } else if ((sub.equals("grant") || sub.equals("revoke")) && main.playerHasPermission(p, "baul.admin")) {
                    for (Player target : main.getServer().getOnlinePlayers()) list.add(target.getName());
                }
            } else if (args.length == 4
                    && (args[1].equalsIgnoreCase("grant") || args[1].equalsIgnoreCase("revoke"))
                    && main.playerHasPermission(p, "baul.admin")) {
                list.addAll(main.getCosmeticRegistry().getAll().keySet());
            }
        }

        for (String s : list) {
            if (s.startsWith(args[args.length - 1])) {
                auto.add(s);
            }
        }

        return auto.isEmpty() ? list : auto;
    }

}
