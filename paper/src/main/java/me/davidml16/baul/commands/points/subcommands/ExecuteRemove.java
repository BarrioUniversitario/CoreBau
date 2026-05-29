package me.davidml16.baul.commands.points.subcommands;

import me.davidml16.baul.Main;
import me.davidml16.baul.api.PointsAPI;
import me.davidml16.baul.objects.Profile;
import me.davidml16.baul.utils.Utils;
import me.davidml16.baul.utils.MiniMessageUtils;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.sql.SQLException;
import java.util.UUID;

public class ExecuteRemove {

    private Main main;
    
    public ExecuteRemove(Main main) {
        this.main = main;
    }

    public boolean executeCommand(CommandSender sender, String label, String[] args) {
        // Check permissions
        if(sender instanceof Player) {
            if (!main.playerHasPermission((Player) sender, "baul.admin")) {
                if(!main.getLanguageHandler().isEmptyMessage("Commands.NoPerms"))
                    sender.sendMessage(main.getLanguageHandler().getMessage("Commands.NoPerms"));
                return false;
            }
        }

        // Check arguments
        if (args.length == 1) {
            sender.sendMessage("");
            sender.sendMessage(Utils.translate(main.getLanguageHandler().getPrefix() + " &cUsage: /" + label + " remove [player] [amount]"));
            sender.sendMessage("");
            return false;
        }

        String player = args[1];

        try {
            main.getDatabaseHandler().hasName(player, name -> {
                if(name == null) {
                    sender.sendMessage(Utils.translate(main.getLanguageHandler().getPrefix() + " &cThis player not exists in the database!"));
                } else {
                    if(Bukkit.getPlayer(player) != null) {
                        Profile profile = main.getPlayerDataHandler().getData(Bukkit.getPlayer(player));
                        long actualBalance = profile.getLootPoints();

                        if(args.length == 2) {
                            sender.sendMessage(Utils.translate(main.getLanguageHandler().getPrefix() + " &cUsage: /" + label + " remove [player] [amount]"));
                        } else if(args.length == 3) {
                            int amount = Integer.parseInt(args[2]);

                            if(amount > 0) {
                                if(actualBalance >= amount) {
                                    PointsAPI.remove(player, amount);

                                     String msg = main.getLanguageHandler().getRawMessage("Commands.Points.Remove.Removed");
                                     msg = msg.replaceAll("%amount%", Integer.toString(amount));
                                     msg = msg.replaceAll("%player%", args[1]);
                                     sender.sendMessage(me.davidml16.baul.utils.Colorize.format(msg));
                                } else {
                                     String msg = main.getLanguageHandler().getRawMessage("Commands.Points.Remove.NoBalance");
                                     msg = msg.replaceAll("%amount%", Integer.toString(amount));
                                     msg = msg.replaceAll("%balance%", Long.toString(actualBalance));
                                     msg = msg.replaceAll("%player%", name);
                                     sender.sendMessage(me.davidml16.baul.utils.Colorize.format(msg));
                                }
                            } else {
                                sender.sendMessage(Utils.translate(
                                        main.getLanguageHandler().getPrefix() + " &cAmount to give need to be more than 0!"));
                            }
                        }
                    }
                }
            });
        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }

        return false;
    }
}