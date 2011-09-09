package info.somethingodd.bukkit.OddGive;

import info.somethingodd.bukkit.OddItem.OddItem;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

public class OddGiveCommandExecutor implements CommandExecutor {

    private OddGive oddGive = null;

    public OddGiveCommandExecutor(OddGive oddGive) {
        this.oddGive = oddGive;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        Player player = (sender instanceof Player ? (Player) sender : null);
        if (player == null && label.equals("i")) {
            sender.sendMessage(oddGive.logPrefix + "Perhaps you meant to use /give?");
            return true;
        }
        if (player != null && !player.isOp() && !oddGive.uglyPermissions(player, "oddgive." + label)) {
            sender.sendMessage(oddGive.logPrefix + "You are not worthy.");
            return true;
        }
        if (label.equals("i") || label.equals("give")) {
            give(player, args);
        }
        if (label.equals("i0")) {
            take(player, args);
        }

        return false;
    }

    private void give(Player player, String[] args) {
        Set<Player> players = new HashSet<Player>();
        Set<ItemStack> items = new HashSet<ItemStack>();
        Set<ItemStack> list = (oddGive.lists.get(player) != null ? oddGive.lists.get(player) : new HashSet<ItemStack>());
        int i = 0;
        for (; i < args.length; i++) {
            Player p = oddGive.getServer().getPlayer(args[i]);
            if (p != null) {
                players.add(p);
            } else break;
        }
        for (; i < args.length; i++) {
            int q = 0;
            String is = args[i];
            try {
                q = Integer.valueOf(args[i+1]);
                i++;
            } catch (NumberFormatException e) {
                q = oddGive.defaultQuantity;
            }
            try {
                ItemStack y = OddItem.getItemStack(is, q);
                boolean allowed;
                if (!oddGive.blacklist) {
                    allowed = false;
                    for (ItemStack z : list)
                        if (OddItem.compare(y, z)) allowed = true;
                } else {
                    allowed = true;
                    for (ItemStack z : list)
                        if (OddItem.compare(y, z)) allowed = false;
                }
                if (allowed) items.add(y);
                else player.sendMessage(oddGive.logPrefix + "Not allowed: " + is);
            } catch (IllegalArgumentException e) {
                player.sendMessage(oddGive.logPrefix + "Unknown: \"" + is + "\"   Did you mean \"" + e.getMessage() + "\"?");
            }
        }
        for (Player w : players)
            for (ItemStack x : items)
                w.getInventory().addItem(x);
    }

    private void take(Player player, String[] args) {
        Set<Player> players = new HashSet<Player>();
        Set<ItemStack> items = new HashSet<ItemStack>();
        int i = 0;
        for (; i < args.length; i++) {
            Player p = oddGive.getServer().getPlayer(args[i]);
            if (p != null) {
                players.add(p);
            } else break;
        }
        if (!player.isEmpty() && !oddGive.uglyPermissions(player, "oddgive.i0.other")) {
            player.sendMessage(oddGive.logPrefix + "Not allowed");
            return;
        }
        for (; i < args.length; i++) {
            Player p = oddGive.getServer().getPlayer(args[i]);
            if (p != null) {
                players.add(p);
            } else break;
        }
        for (; i < args.length; i++) {
            int q = 0;
            String is = args[i];
            try {
                q = Integer.valueOf(args[i + 1]);
                i++;
            } catch (NumberFormatException e) {
                q = -1;
            }
            try {
                ItemStack y = OddItem.getItemStack(is, q);
                items.add(y);
            } catch (IllegalArgumentException e) {
                player.sendMessage(oddGive.logPrefix + "Unknown: \"" + is + "\"   Did you mean \"" + e.getMessage() + "\"?");
            }
        }
        for (Player w : players)
            for (ItemStack x : items) {
                if (x.getAmount() == -1) {
                    for (ItemStack v : w.getInventory().getContents()) {
                        if (OddItem.compare(x, v)) w.getInventory().remove(v);
                    }
                } else {
                    HashMap<Integer, ItemStack> v = w.getInventory().removeItem(x);
                    if (!v.isEmpty()) {
                        player.sendMessage(oddGive.logPrefix + "Could not remove" + (x.getAmount() - v.keySet().iterator().next()) + " of " + x.getTypeId() + ";" + x.getDurability() + " from " + w.getName() + ".");
                    }
                }
            }
    }
}
