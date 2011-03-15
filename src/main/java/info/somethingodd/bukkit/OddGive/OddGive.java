package info.somethingodd.bukkit.OddGive;

import com.nijiko.permissions.PermissionHandler;
import com.nijikokun.bukkit.Permissions.Permissions;
import info.somethingodd.bukkit.OddItem.OddItem;
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
import java.util.HashMap;
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
    private static String dataDir = "plugins" + File.separator + "Odd";
    private static String config = dataDir + File.separator + "give.txt";
    private static Set<String> itemlist = new HashSet<String>();
    private static boolean blacklist;
    private static int defaultQuantity = 64;

    /**
     * Gives an item stack to player
     *
     * @param player Destination player
     * @param item Item type
     * @param sender Source player
     */
    public static void give(Player player, String item, CommandSender sender) {
        give(player, item, 64, sender);
    }

    /**
     * Gives some quantity of an item to player
     * 
     * @param player Destination player
     * @param item Item type
     * @param quantity Item quantity
     * @param sender Source Player
     */
    public static void give(Player player, String item, int quantity, CommandSender sender) {
        if (player == null)
            return;
        Inventory i = player.getInventory();
        ItemStack is = null;
		try {
			is = OddItem.getItemStack(item);
		} catch (IllegalArgumentException iae) {
			if (sender instanceof Player) {
				sender.sendMessage("Item " + item + " unknown. Closest match: " + iae.getMessage());
			} else {
				log.info("[" + info.getName() + "] Item " + item + " unknown. Closest match: " + iae.getMessage());
			}
            return;
		}
		is.setAmount(quantity);
		i.addItem(is);
	}

	@java.lang.Override
    public boolean onCommand(CommandSender sender, Command command, String commandLabel, String[] args) {
        String item = "";
        Player player = null;
        int quantity = defaultQuantity;
		if (Permissions == null && !sender.isOp())
			return true;
        if (Permissions != null && !sender.isOp() && sender instanceof Player && !Permissions.has((Player) sender, "odd.give." + commandLabel.toLowerCase()))
            return true;
		if (commandLabel.toLowerCase().equals("i0")) {
			if (args.length == 0 && sender instanceof Player) {
				player = (Player) sender;
				player.getInventory().clear();
	            return true;
			} else if (args.length == 1) {
				Player p = getServer().getPlayer(args[0]);
				if (p == null)
					sender.sendMessage("Invalid player: " + args[0]);
				else
					p.getInventory().clear();
				return true;
			}
			return false;
		}
		if (commandLabel.toLowerCase().equals("i")) {
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
		} else if (commandLabel.toLowerCase().equals("give")) {
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
        if ( sender.isOp() || (Permissions != null && Permissions.has((Player) sender, "odd.give.override")) || (blacklist && !itemlist.contains(item)) || (!blacklist && itemlist.contains(item)) )
            give(player, item, quantity, sender);
		return true;
	}

    @Override
	public void onDisable() {
		log.info( "[" + info.getName() + "] disabled" );
	}

    @Override
	public void onEnable() {
        info = getDescription();
        log = getServer().getLogger();
		log.info( "[" + info.getName() + "] " + info.getVersion() + " enabled" );
        setupPermissions();
        parseConfig(readConfig());
	}

    @Override
    public void onLoad() {
    }

    private static void parseConfig(String s) {
        HashMap<String, String> it = new HashMap<String, String>();
        String[] l = s.split(System.getProperty("line.separator"));
        for (String Al : l) {
            String[] m = Al.split(" ", 2);
            if (m[0].equals("type:")) {
                if (m[1].equals("blacklist"))
                    blacklist = true;
                else if (m[1].equals("whitelist"))
                    blacklist = false;
                else
                    log.warning("[" + info.getName() + "] Invalid value for type: " + m[1]);
            } else if (m[0].equals("quantity:")) {
                try {
                    defaultQuantity = Integer.decode(m[1]);
                } catch (NumberFormatException nfe) {
                    log.warning("[" + info.getName() + "] Invalid quantity: " + m[1]);
                }
            } else if (m[0].equals("items:")) {
                String[] n = m[1].split(" ");
                log.info("[" + info.getName() + "] " + (blacklist ? "Black" : "White") + "listing " + n.length + " items.");
                for (String An : n) {
                    try {
                        itemlist.add(An);
                    } catch (Exception e) {
                        log.info("[" + info.getName() + "] Exception:");
                        e.printStackTrace();
                    }
                }
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
                log.warning("Reading config: " + ioe.getMessage());
            } finally {
                input.close();
            }
        } catch (IOException ie) {
            log.severe(ie.getMessage());
        }
        return contents.toString();
    }

    public void setupPermissions() {
        Plugin test = this.getServer().getPluginManager().getPlugin("Permissions");
        if (this.Permissions == null && test != null) {
                this.getServer().getPluginManager().enablePlugin(test);
                this.Permissions = ((Permissions) test).getHandler();
        } else {
            log.info("[" + info.getName() + "] Permissions not found. Op-only mode.");
        }
    }

}
