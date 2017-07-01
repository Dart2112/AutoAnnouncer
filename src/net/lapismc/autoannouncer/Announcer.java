package net.lapismc.autoannouncer;

import org.apache.commons.lang.StringUtils;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.plugin.PluginDescriptionFile;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Random;

public class Announcer extends JavaPlugin {
    private static List<String> strings;
    private static List<String> Groups;
    private final String DIR = "plugins" + File.separator + "AutoAnnouncer" + File.separator;
    boolean isScheduling;
    AnnouncerLog logger = new AnnouncerLog(this);
    AnnouncerPerm perm;
    private PluginDescriptionFile pdfFile;
    private YamlConfiguration Settings;
    private String Tag;
    private int Interval;
    private int taskId = -1;
    private int counter = 0;
    private boolean isRandom;
    private boolean InSeconds;
    private boolean permission;
    private boolean toGroups;
    private AnnouncerUtils utils = new AnnouncerUtils();

    public Announcer() {
        isScheduling = false;
        InSeconds = false;
        perm = null;
    }

    @Override
    public void onEnable() {
        pdfFile = getDescription();
        File fDir = new File(DIR);
        if (!fDir.exists()) {
            fDir.mkdir();
        }
        try {
            File configFile = new File(DIR + "settings.yml");
            if (!configFile.exists()) {
                configFile.getParentFile().mkdirs();
                utils.copy(getResource("settings.yml"), configFile);
            }
        } catch (Exception e) {
            logger.severe(e);
        }
        loadSettings();
        perm = new AnnouncerPerm(this);
        if (permission) {
            perm.enablePermissions();
        } else {
            logger.warning("No permission system enabled!");
        }
        getCommand("announcer").setExecutor(new CommandListener(this));

        logger.info("Settings Loaded (" + strings.size() + " announces).");
        isScheduling = scheduleOn(null);
        logger.info("v" + pdfFile.getVersion() + " is enabled!");
        logger.info("Developed by: " + pdfFile.getAuthors());
    }

    @Override
    public void onDisable() {
        scheduleOff(true, null);
        logger.info("v" + pdfFile.getVersion() + " is disabled!.");
    }

    void scheduleOff(boolean Disabling, CommandSender sender) {
        if (isScheduling) {
            getServer().getScheduler().cancelTask(taskId);
            if (sender != null) {
                sender.sendMessage(ChatColor.DARK_GREEN + "Scheduling finished!");
            }
            logger.info("Scheduling finished!");
            isScheduling = false;
        } else if (!Disabling) {
            if (sender != null) {
                sender.sendMessage(ChatColor.DARK_RED + "No schedule running!");
            }
            logger.info("No schedule running!");
        }
    }

    boolean scheduleOn(CommandSender sender) {
        if (!isScheduling) {
            if (strings.size() > 0) {
                int TimeToTicks = InSeconds ? 20 : 1200;
                taskId = getServer().getScheduler().scheduleSyncRepeatingTask(this, runnable(), Interval * TimeToTicks, Interval * TimeToTicks);
                if (taskId == -1) {
                    if (sender != null) {
                        sender.sendMessage(ChatColor.DARK_RED + "Scheduling failed!");
                    }
                    logger.warning("Scheduling failed!");
                    return false;
                }
                counter = 0;
                if (sender != null) {
                    sender.sendMessage(ChatColor.DARK_GREEN + "Scheduled every " + Interval + (InSeconds ? " seconds!" : " minutes!"));
                }
                logger.info("Scheduled every " + Interval + (InSeconds ? " seconds!" : " minutes!"));
                return true;
            }
            if (sender != null) {
                sender.sendMessage(ChatColor.DARK_RED + "Scheduling failed! There are no announcements to do.");
            }
            logger.warning("Scheduling failed! There are no announcements to do.");
            return false;
        }
        if (sender != null) {
            sender.sendMessage(ChatColor.DARK_RED + "Scheduler already running.");
        }
        logger.info("Scheduler already running.");
        return true;
    }

    void scheduleRestart(CommandSender sender) {
        if (isScheduling) {
            scheduleOff(false, null);
            loadSettings();
            sender.sendMessage(ChatColor.DARK_GREEN + "Settings Loaded (" + strings.size() + " announces).");
            isScheduling = scheduleOn(sender);
        } else {
            sender.sendMessage(ChatColor.DARK_RED + "No schedule running!");
        }
    }

    void setInterval(String[] args, CommandSender sender) {
        if (args.length == 2) {
            try {
                int interval = Integer.parseInt(args[1], 10);
                Settings.set("Settings.Interval", interval);
                saveSettings();
                sender.sendMessage(ChatColor.DARK_GREEN + "Interval changed successfully to " + args[1] + (InSeconds ? " seconds." : " minutes."));
                if (isScheduling) {
                    scheduleRestart(sender);
                }
            } catch (NumberFormatException err) {
                sender.sendMessage(ChatColor.DARK_RED + "Error! Usage: /announcer interval 5");
            }
        } else {
            sender.sendMessage(ChatColor.DARK_RED + "Error! Usage: /announcer interval 5");
        }
    }

    void setRandom(String[] args, CommandSender sender) {
        if (args.length == 2) {
            switch (args[1]) {
                case "on":
                    Settings.set("Settings.Random", true);
                    saveSettings();
                    sender.sendMessage(ChatColor.DARK_GREEN + "Changed to random transition.");
                    if (isScheduling) {
                        scheduleRestart(sender);
                    }
                    break;
                case "off":
                    Settings.set("Settings.Random", false);
                    saveSettings();
                    sender.sendMessage(ChatColor.DARK_GREEN + "Changed to consecutive transition.");
                    if (isScheduling) {
                        scheduleRestart(sender);
                    }
                    break;
                default:
                    sender.sendMessage(ChatColor.DARK_RED + "Error! Usage: /announcer random off");
                    break;
            }
        } else {
            sender.sendMessage(ChatColor.DARK_RED + "Error! Usage: /announcer random off");
        }
    }

    void addAnnounce(String[] args, CommandSender sender) {
        if (args.length > 1) {
            String com = StringUtils.join(args, " ", 1, args.length);
            strings.add(com);
            Settings.set("Announcer.Strings", strings);
            saveSettings();
            sender.sendMessage(ChatColor.DARK_GREEN + "New announce added!");
        } else {
            sender.sendMessage(ChatColor.DARK_RED + "Error! Usage: /announcer add [announce here]");
        }
    }

    void listAnnounces(CommandSender sender) {
        sender.sendMessage(ChatColor.DARK_GREEN + "List of announces with ids: (Total: " + strings.size() + ")");
        int i = 0;
        int j = 0;
        for (String announce : strings) {
            j++;
            for (String line : announce.split("&NEW_LINE;")) {
                i++;
                if (i == 1) {
                    sender.sendMessage(ChatColor.GOLD + "[" + j + "] " + ChatColor.RESET + utils.colorize(line));
                } else {
                    sender.sendMessage(utils.colorize(line));
                }
            }
            i = 0;
        }
    }

    void removeAnnounce(String[] args, CommandSender sender) {
        if (args.length == 2) {
            try {
                int announceid = Integer.parseInt(args[1]);
                if ((announceid < 1) || (announceid > strings.size())) {
                    sender.sendMessage(ChatColor.DARK_RED + "Error! Usage: /announcer remove [announce id]");
                } else {
                    strings.remove(announceid - 1);
                    Settings.set("Announcer.Strings", strings);
                    saveSettings();
                    sender.sendMessage(ChatColor.DARK_GREEN + "Announce deleted!");
                    if (isScheduling) {
                        scheduleRestart(sender);
                    }
                }
            } catch (NumberFormatException e) {
                sender.sendMessage(ChatColor.DARK_RED + "Error! Usage: /announcer remove [announce id]");
            }
        } else {
            sender.sendMessage(ChatColor.DARK_RED + "Error! Usage: /announcer remove [announce id]");
        }
    }

    void announcerHelp(CommandSender sender) {
        String or = ChatColor.WHITE + " | ";
        String auctionStatusColor = ChatColor.DARK_GREEN.toString();
        String helpMainColor = ChatColor.GOLD.toString();
        String helpCommandColor = ChatColor.AQUA.toString();
        String helpObligatoryColor = ChatColor.DARK_RED.toString();
        sender.sendMessage(helpMainColor + " -----[ " + auctionStatusColor + "Help for AutoAnnouncer" + helpMainColor + " ]----- ");
        sender.sendMessage(helpCommandColor + "/announcer help" + or + helpCommandColor + "?" + helpMainColor + " - Show this message.");
        sender.sendMessage(helpCommandColor + "/announcer on" + helpMainColor + " - Start AutoAnnouncer.");
        sender.sendMessage(helpCommandColor + "/announcer off" + helpMainColor + " - Stop AutoAnnouncer.");
        sender.sendMessage(helpCommandColor + "/announcer restart" + helpMainColor + " - Restart AutoAnnouncer.");
        sender.sendMessage(helpCommandColor + "/announcer interval" + or + helpCommandColor + "i" + helpObligatoryColor + " <minutes|seconds>" + helpMainColor + " - Set the interval time.");
        sender.sendMessage(helpCommandColor + "/announcer random" + or + helpCommandColor + "r" + helpObligatoryColor + " <on|off>" + helpMainColor + " - Set random or consecutive.");
    }

    private void loadSettings() {
        Settings = YamlConfiguration.loadConfiguration(new File(DIR + "settings.yml"));
        Interval = Settings.getInt("Settings.Interval", 5);
        InSeconds = Settings.getBoolean("Settings.InSeconds", false);
        isRandom = Settings.getBoolean("Settings.Random", false);
        permission = Settings.getBoolean("Settings.Permission", true);
        strings = Settings.getStringList("Announcer.Strings");
        Tag = utils.colorize(Settings.getString("Announcer.Tag", "&GOLD;[AutoAnnouncer]"));
        toGroups = Settings.getBoolean("Announcer.ToGroups", true);
        Groups = Settings.getStringList("Announcer.Groups");
    }

    private void saveSettings() {
        try {
            Settings.save(new File(DIR + "settings.yml"));
        } catch (IOException e) {
            logger.warning("Failed to save config!");
        }
    }

    private Runnable runnable() {
        return () -> {
            String announce;
            if (isRandom) {
                Random randomise = new Random();
                int selection = randomise.nextInt(strings.size());
                announce = strings.get(selection);
            } else {
                announce = strings.get(counter);
            }
            counter++;
            if (counter == strings.size()) {
                counter = 0;
            }
            if ((permission) && (toGroups)) {
                for (Player p : getServer().getOnlinePlayers()) {
                    for (String group : Groups) {
                        if (perm.group(p, group)) {
                            for (String line : announce.split("&NEW_LINE;")) {
                                p.sendMessage(Tag + " " + utils.colorize(line));
                            }
                            break;
                        }
                    }
                }
            } else {
                for (String line : announce.split("&NEW_LINE;")) {
                    for (Player p : getServer().getOnlinePlayers()) {
                        p.sendMessage(Tag + " " + utils.colorize(line));
                    }
                }
            }
        };

    }
}
