package info.somethingodd.bukkit.OddGive;

import com.nijiko.permissions.PermissionHandler;
import com.nijikokun.bukkit.Permissions.Permissions;
import info.somethingodd.bukkit.OddItem.OddItem;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.PluginDescriptionFile;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.logging.Logger;

/**
 * OddGive for Bukkit
 *
 * @author petteyg359
 */

public class OddGive extends JavaPlugin {
    private static Logger log;
    private static PluginDescriptionFile info;
    private PermissionHandler Permissions = null;
    private OddItem oddItem = null;
    private static String dataDir = "plugins" + File.separator + "Odd";
    private static String config = dataDir + File.separator + "give.txt";
    private static Set<String> itemlist = new HashSet<String>();
    private static boolean blacklist;
    private static int defaultQuantity;
    private static String logPrefix;

    /**
     * Gives some quantity of an item to player
     *
     * @param player Destination player
     * @param item Item type
     * @param quantity Item quantity
     * @param sender Source Player
     */
    private void give(Player player, String item, int quantity, CommandSender sender) {
        if (player == null)
            return;
        Inventory i = player.getInventory();
        ItemStack is;
        try {
            is = oddItem.getItemStack(item);
            is.setAmount(quantity);
            i.addItem(is);
        } catch (IllegalArgumentException iae) {
            if (sender instanceof Player) {
                sender.sendMessage("Item " + item + " unknown. Closest match: " + iae.getMessage());
            } else {
                log.info(logPrefix + "Item " + item + " unknown. Closest match: " + iae.getMessage());
            }
        }
    }

    private void take(CommandSender sender, String[] args)
    {
        Set<Player> players = new HashSet<Player>();
        Set<Material> items = new HashSet<Material>();
        if (args.length == 0) {
            if (!(sender instanceof Player))
                sender.sendMessage("You have no inventory, silly!");
            else
                ((Player) sender).getInventory().clear();
            return;
        }
        if (args[0].equals("*")) {
            if (args.length > 1) {
                players.addAll(Arrays.asList(getServer().getOnlinePlayers()));
            } else {
                if (!(sender instanceof Player))
                    sender.sendMessage("You have no inventory, silly!");
                else
                    ((Player) sender).getInventory().clear();
            }
        } else {
            int i = 0;
            while (getServer().getPlayer(args[i]) != null) {
                players.add(getServer().getPlayer(args[i]));
                i++;
            }
            if (args[i].equals("*")) {
                for (Player p : players)
                    p.getInventory().clear();
                return;
            } else {
                for (; i < args.length; i++) {
                    ItemStack is;
                    try {
                        is = oddItem.getItemStack(args[i]);
                        items.add(is.getType());
                    } catch (IllegalArgumentException iae) {
                        sender.sendMessage(logPrefix + "Inavlid item: " + args[i]);
                    }
                }
            }
            if (players.isEmpty())
                players.add((Player) sender);
            for (Player p : players) {
                Inventory inventory = p.getInventory();
                for (Material m : items)
                    inventory.remove(m);
            }
        }
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String commandLabel, String[] args) {
        String item = "";
        Player player = null;
        int quantity = defaultQuantity;
        if (Permissions == null && !sender.isOp())
            return true;
        if (!sender.isOp() && sender instanceof Player && Permissions != null && !Permissions.has((Player) sender, "odd.give." + commandLabel))
            return true;
        if (commandLabel.equals("i0")) {
            if (args.length > 0 && getServer().getPlayer(args[0]) != null)
                if (!sender.isOp() && sender instanceof Player && Permissions != null && !Permissions.has((Player) sender, "odd.give.i0.other"))
                    return true;
            take(sender, args);
        } else if (commandLabel.equals("i")) {
            if (!(sender instanceof Player))
                return false;
            if (args.length == 1) {
                item = args[0];
                player = (Player) sender;
            } else if (args.length == 2) {
                try {
                    quantity = Integer.decode(args[1]);
                } catch (NumberFormatException nfe) {
                    return false;
                }
                item = args[0];
                player = (Player) sender;
            }
        } else if (commandLabel.equals("give")) {
            if (sender instanceof Player) {
                if (args.length == 2) {
                    player = getServer().getPlayer(args[0]);
                    item = args[1];
                } else if (args.length == 3) {
                    try {
                        quantity = Integer.decode(args[2]);
                    } catch (NumberFormatException nfe) {
                        return false;
                    }
                }
            } else {
                if (args.length == 2) {
                    player = getServer().getPlayer(args[0]);
                    item = args[1];
                } else if (args.length == 3) {
                    player = getServer().getPlayer(args[0]);
                    item = args[1];
                    try {
                        quantity = Integer.decode(args[2]);
                    } catch (NumberFormatException nfe) {
                        return false;
                    }
                }
            }
        }
        boolean deny = false;
        if ((!blacklist && !itemlist.contains(item)) || (blacklist && itemlist.contains(item)))
            deny = true;
        if (deny && !(sender.isOp() || (Permissions != null && Permissions.has((Player) sender, "odd.give.override"))))
            return true;
        give(player, item, quantity, sender);
        return true;
    }

    @Override
    public void onDisable() {
        log.info(logPrefix + "disabled" );
    }

    @Override
    public void onEnable() {
        Plugin p;
        p = getServer().getPluginManager().getPlugin("OddItem");
        if (p != null) {
            oddItem = (OddItem) p;
        } else {
            log.severe(logPrefix + "Couldn't find OddItem.");
            getServer().getPluginManager().disablePlugin(this);
        }
        log.info(logPrefix + info.getVersion() + " enabled");
        p = getServer().getPluginManager().getPlugin("Permissions");
        if (p != null) {
            getServer().getPluginManager().enablePlugin(p);
            Permissions = ((Permissions) p).getHandler();
        } else {
            log.info(logPrefix + "Permissions not found. Using op-only mode.");
        }
        parseConfig(readConfig());
    }

    @Override
    public void onLoad() {
        info = getDescription();
        log = getServer().getLogger();
        logPrefix = "[" + info.getName() + "] ";
    }

    private void parseConfig(String s) {
        defaultQuantity = 64;
        blacklist = true;
        String[] l = s.split(System.getProperty("line.separator"));
        for (String Al : l) {
            String[] m = Al.split(" ", 2);
            if (m[0].equals("type:")) {
                if (m[1].equals("blacklist"))
                    blacklist = true;
                else if (m[1].equals("whitelist"))
                    blacklist = false;
                else
                    log.warning(logPrefix + "Invalid value for type: " + m[1]);
            } else if (m[0].equals("quantity:")) {
                try {
                    defaultQuantity = Integer.decode(m[1]);
                } catch (NumberFormatException nfe) {
                    log.warning(logPrefix + "Invalid quantity: " + m[1]);
                }
            } else if (m[0].equals("items:")) {
                String[] n = m[1].split(" ");
                for (String An : n) {
                    itemlist.add(An);
                    String[] aliases = {};
                    aliases = oddItem.getAliases(An).toArray(aliases);
                    itemlist.addAll(Arrays.asList(aliases));
                }
                log.info(logPrefix + (blacklist ? "Black" : "White") + "listed " + itemlist.size() + " items.");
            }
        }
    }

    private static String readConfig() {
        boolean dirExists = new File(dataDir).exists();
        if (!dirExists) {
            try {
                new File(dataDir).mkdir();
            } catch (SecurityException se) {
                log.severe(se.getMessage());
                return null;
            }
        }
        boolean fileExists = new File(config).exists();
        if (!fileExists) {
            try {
                new File(config).createNewFile();
            } catch (IOException ioe) {
                log.severe(ioe.getMessage());
                return null;
            }
        }
        File file = new File(config);
        StringBuilder contents = new StringBuilder();
        try {
            BufferedReader input = new BufferedReader(new FileReader(file));
            try {
                String line = input.readLine();
                while (line != null) {
                    contents.append(line);
                    contents.append(System.getProperty("line.separator"));
                    line = input.readLine();
                }
            } catch (IOException ioe) {
                log.warning(logPrefix + "Error reading config: " + ioe.getMessage());
            } finally {
                input.close();
            }
        } catch (IOException ie) {
            log.severe(ie.getMessage());
        }
        return contents.toString();
    }

}
