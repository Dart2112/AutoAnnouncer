package net.lapismc.autoannouncer;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.entity.Player;

public class CommandListener
        implements CommandExecutor {
    private final Announcer plugin;

    CommandListener(Announcer plugin) {
        this.plugin = plugin;
    }

    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        String commandName = command.getName();
        if ((sender instanceof Player)) {
            Player player = (Player) sender;
            if (commandName.equalsIgnoreCase("announcer")) {
                if (this.plugin.perm.has(player, player.isOp())) {
                    try {
                        if ((args[0].equalsIgnoreCase("help")) || (args[0].equalsIgnoreCase("?"))) {
                            this.plugin.announcerHelp(player);
                        } else if (args[0].equalsIgnoreCase("off")) {
                            this.plugin.scheduleOff(false, player);
                        } else if (args[0].equalsIgnoreCase("on")) {
                            this.plugin.isScheduling = this.plugin.scheduleOn(player);
                        } else if ((args[0].equalsIgnoreCase("interval")) || (args[0].equalsIgnoreCase("i"))) {
                            this.plugin.setInterval(args, player);
                        } else if ((args[0].equalsIgnoreCase("random")) || (args[0].equalsIgnoreCase("r"))) {
                            this.plugin.setRandom(args, player);
                        } else if ((args[0].equalsIgnoreCase("restart")) || (args[0].equalsIgnoreCase("reload"))) {
                            this.plugin.scheduleRestart(player);
                        } else if (args[0].equalsIgnoreCase("add")) {
                            this.plugin.addAnnounce(args, player);
                        } else if (args[0].equalsIgnoreCase("list")) {
                            this.plugin.listAnnounces(player);
                        } else if (args[0].equalsIgnoreCase("remove")) {
                            this.plugin.removeAnnounce(args, player);
                        }
                        return true;
                    } catch (ArrayIndexOutOfBoundsException ex) {
                        return false;
                    }
                }
                player.sendMessage(ChatColor.RED + "You do not have permission to use this command!");
                return true;
            }
        } else if ((sender instanceof ConsoleCommandSender)) {
            ConsoleCommandSender Console = (ConsoleCommandSender) sender;
            try {
                if ((args[0].equalsIgnoreCase("help")) || (args[0].equalsIgnoreCase("?"))) {
                    this.plugin.announcerHelp(Console);
                } else if (args[0].equalsIgnoreCase("off")) {
                    this.plugin.scheduleOff(false, Console);
                } else if (args[0].equalsIgnoreCase("on")) {
                    this.plugin.isScheduling = this.plugin.scheduleOn(Console);
                } else if ((args[0].equalsIgnoreCase("interval")) || (args[0].equalsIgnoreCase("i"))) {
                    this.plugin.setInterval(args, Console);
                } else if ((args[0].equalsIgnoreCase("random")) || (args[0].equalsIgnoreCase("r"))) {
                    this.plugin.setRandom(args, Console);
                } else if ((args[0].equalsIgnoreCase("restart")) || (args[0].equalsIgnoreCase("reload"))) {
                    this.plugin.scheduleRestart(Console);
                } else if (args[0].equalsIgnoreCase("add")) {
                    this.plugin.addAnnounce(args, Console);
                } else if (args[0].equalsIgnoreCase("list")) {
                    this.plugin.listAnnounces(Console);
                } else if (args[0].equalsIgnoreCase("remove")) {
                    this.plugin.removeAnnounce(args, Console);
                }
                return true;
            } catch (ArrayIndexOutOfBoundsException ex) {
                return false;
            }
        }
        return false;
    }
}
