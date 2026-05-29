package me.davidml16.baul.commands.cubelets.subcommands;

import me.davidml16.baul.Main;
import me.davidml16.baul.api.CubeletsAPI;
import me.davidml16.baul.utils.Utils;
import me.davidml16.baul.utils.MiniMessageUtils;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.sql.SQLException;

public class ExecuteGive {

    private Main main;
    public ExecuteGive(Main main) {
        this.main = main;
    }

    public boolean executeCommand(CommandSender sender, String label, String[] args) {

        if(sender instanceof Player) {
            if (!main.playerHasPermission((Player) sender, "baul.admin")) {
                sender.sendMessage(main.getLanguageHandler().getMessage("Commands.NoPerms"));
                return false;
            }
        }

        if (args.length == 1 || args.length == 2) {
            sender.sendMessage("");
            sender.sendMessage(Utils.translate(main.getLanguageHandler().getPrefix() + " &cUsage: /" + label + " give [player] [typeID] [amount]"));
            sender.sendMessage("");
            return false;
        }

        String player = args[1];

        String id = args[2];
        if (!id.equalsIgnoreCase("random") && !main.getCubeletTypesHandler().getTypes().containsKey(id)) {
            sender.sendMessage(Utils.translate(
                    main.getLanguageHandler().getPrefix() + " &cThis " + label + " type doesn't exists!"));
            return false;
        }

        final int[] amount = {1};

        if(player.equalsIgnoreCase("*") || player.equalsIgnoreCase("all")) {

            if(args.length == 3) {

                for(Player iterator : Bukkit.getOnlinePlayers()) {

                    int finalAmount = amount[0];

                    CubeletsAPI.giveCubelet(iterator.getName(), id, 1, typeGived -> {

                                  String msg = main.getLanguageHandler().getRawMessage("Commands.Cubelets.Give");
                                  msg = msg.replaceAll("%amount%", Integer.toString(finalAmount));
                                  msg = msg.replaceAll("%baul%",  typeGived.getName());
                                  msg = msg.replaceAll("%player%", iterator.getName());
                                  sender.sendMessage(me.davidml16.baul.utils.Colorize.format(msg));

                    });

                }

                return true;

            } else if(args.length == 4) {

                amount[0] = Integer.parseInt(args[3]);

                if(amount[0] > 0) {

                    for(Player iterator : Bukkit.getOnlinePlayers()) {

                        int finalAmount = amount[0];

                        CubeletsAPI.giveCubelet(iterator.getName(), id, finalAmount, typeGived -> {

                            String msg = main.getLanguageHandler().getRawMessage("Commands.Cubelets.Give");
                            msg = msg.replaceAll("%amount%", Integer.toString(finalAmount));
                            msg = msg.replaceAll("%baul%",  typeGived.getName());
                            msg = msg.replaceAll("%player%", iterator.getName());
                            sender.sendMessage(me.davidml16.baul.utils.Colorize.format(msg));

                        });

                    }

                    return true;

                } else {
                    sender.sendMessage(Utils.translate(
                            main.getLanguageHandler().getPrefix() + " &cAmount to give need to be more than 0!"));
                    return false;
                }
            }
        } else {

            try {

                main.getDatabaseHandler().hasName(player, name -> {

                    if(name == null) {

                        sender.sendMessage(Utils.translate(main.getLanguageHandler().getPrefix() + " &cThis player not exists in the database!"));

                    } else {

                        if(args.length == 3) {

                            int finalAmount = amount[0];

                            CubeletsAPI.giveCubelet(player, id, 1, typeGived -> {

                                  String msg = main.getLanguageHandler().getMessage("Commands.Cubelets.Give");
                                  msg = msg.replaceAll("%amount%", Integer.toString(finalAmount));
                                  msg = msg.replaceAll("%baul%",  typeGived.getName());
                                  msg = msg.replaceAll("%player%", name);
                                  sender.sendMessage(MiniMessageUtils.format(msg));

                            });

                        } else if(args.length == 4) {

                            amount[0] = Integer.parseInt(args[3]);

                            if(amount[0] > 0) {

                                int finalAmount = amount[0];

                                CubeletsAPI.giveCubelet(player, id, finalAmount, typeGived -> {

                                    String msg = main.getLanguageHandler().getRawMessage("Commands.Cubelets.Give");
                                    msg = msg.replaceAll("%amount%", Integer.toString(finalAmount));
                                    msg = msg.replaceAll("%baul%",  typeGived.getName());
                                    msg = msg.replaceAll("%player%", name);
                                 sender.sendMessage(me.davidml16.baul.utils.Colorize.format(msg));

                                });

                            } else {

                                sender.sendMessage(Utils.translate(main.getLanguageHandler().getPrefix() + " &cAmount to give need to be more than 0!"));

                            }
                        }

                    }

                });

            } catch (SQLException throwables) {
                throwables.printStackTrace();
            }

        }

        return true;

    }

}
