package info.somethingodd.bukkit.odd.give;

import info.somethingodd.bukkit.odd.item.OddItem;
import com.nijiko.permissions.PermissionHandler;
import com.nijikokun.bukkit.Permissions.Permissions;
import java.util.logging.Logger;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.PluginDescriptionFile;
import org.bukkit.plugin.java.JavaPlugin;

/**
 * OddGive for Bukkit
 *
 * @author petteyg359
 */

public class OddGive extends JavaPlugin {
	private static Logger log;
	private static PluginDescriptionFile info;
    private static PermissionHandler Permissions = null;

	/**
	 * Gives an item stack to player
	 * 
	 * @param player Destination player
	 * @param item Item type
	 * @param sender Source player
	 * @return true if item successfully given, otherwise false
	 */
	public static boolean give(Player player, String item, Player sender) {
		return give(player, item, 64, sender);
	}

	/**
	 * Gives some quantity of an item to player
	 * 
	 * @param player Destination player
	 * @param item Item type
	 * @param quantity Item quantity
	 * @param sender Source Player
	 * @return true if item successfully given, otherwise false
	 */
	public static boolean give(Player player, String item, int quantity, Player sender) {
		if (player == null)
			return false;
		Inventory i = player.getInventory();
		ItemStack is = null;
		try {
			is = OddItem.getItemStack(item);
		} catch (IllegalArgumentException iae) {
			if (sender instanceof Player) {
				sender.sendMessage("Item " + item + " unknown. Closest match: " + iae.getMessage());
			} else {
				log.info("Item " + item + " unknown. Closest match: " + iae.getMessage());
			}
		}
		if (is == null) {
			log.warning("[" + info.getName() +"] Item \"" + item + "\" not known");
			return true;
		}
		is.setAmount(quantity);
		i.addItem(is);
		return true;
	}

	@java.lang.Override
    public boolean onCommand(CommandSender sender, Command command, String commandLabel, String[] args) {
		int quantity = 0;
		if (Permissions == null && !sender.isOp())
			return true;
        if (Permissions != null && sender instanceof Player && !Permissions.has((Player) sender, "odd.give." + commandLabel.toLowerCase()))
            return true;
		if (commandLabel.toLowerCase().equals("i0")) {
			if (args.length == 0 && sender instanceof Player) {
				Player p = (Player) sender;
				p.getInventory().clear();
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
				return give((Player) sender, args[0], (Player) sender);
			} else if (args.length == 2) {
				try {
					quantity = Integer.decode(args[1]);
				} catch (NumberFormatException nfe) {
					return false;
				}
				return give((Player) sender, args[0], quantity, (Player) sender);
			}
			return false;
		} else if (commandLabel.toLowerCase().equals("give")) {
			if (sender instanceof Player) {
				if (args.length == 2) {
					return give(getServer().getPlayer(args[0]), args[1], (Player) sender);
				} else if (args.length == 3) {
					try {
						quantity = Integer.decode(args[2]);
					} catch (NumberFormatException nfe) {
						return false;
					}
					return give(getServer().getPlayer(args[0]), args[1], quantity, (Player) sender);
				}
				return false;
			} else {
				if (args.length == 2) {
					return give(getServer().getPlayer(args[0]), args[1], null);
				} else if (args.length == 3) {
					try {
						quantity = Integer.decode(args[2]);
					} catch (NumberFormatException nfe) {
						return false;
					}
					return give(getServer().getPlayer(args[0]), args[1], quantity, null);
				}
				return false;
			}
		}
		return false;
	}

	public void onDisable() {
		log.info( "[" + info.getName() + "] disabled" );
	}

	public void onEnable() {
        info = getDescription();
        log = getServer().getLogger();
		log.info( "[" + info.getName() + "] " + info.getVersion() + " enabled" );
        setupPermissions();
	}

    public void setupPermissions() {
        Plugin test = this.getServer().getPluginManager().getPlugin("Permissions");
        if (Permissions == null && test != null) {
                this.getServer().getPluginManager().enablePlugin(test);
                Permissions = ((Permissions) test).getHandler();
        } else {
            log.info("[" + info.getName() + "] Permissions not found. Op-only mode.");
        }
    }

}