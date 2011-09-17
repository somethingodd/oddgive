package info.somethingodd.bukkit.OddGive;

import info.somethingodd.bukkit.OddItem.OddItem;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.Arrays;
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
            sender.sendMessage(oddGive.logPrefix + "Try /give");
            return true;
        }
        if (player != null && !player.isOp() && player.hasPermission("oddgive." + label)) {
            sender.sendMessage(oddGive.logPrefix + "You are not worthy.");
            return true;
        }
        if (label.equals("i") || label.equals("give")) {
            give(label, player, args);
            return true;
        }
        if (label.equals("i0")) {
            take(player, args);
            return true;
        }
        return false;
    }

    private Set<ItemStack> getItemStacks(String[] args) {
        Set<ItemStack> itemStacks = new HashSet<ItemStack>();
        ItemStack itemStack;
        Integer quantity = oddGive.defaultQuantity;
        for (int i = 0; i < args.length; i++) {
            try {
                itemStack = OddItem.getItemStack(args[i]);
                quantity = Integer.parseInt(args[i + 1]);
                itemStack.setAmount(quantity);
                itemStacks.add(itemStack);
            } catch (Exception e) {
            }
        }
        return itemStacks;
    }

    private Set<Player> getPlayers(String[] args) {
        Set<Player> players = new HashSet<Player>();
        for (int i = 0; i < args.length; i++) {
            Player player = oddGive.getServer().getPlayer(args[i]);
            if (player != null)
                players.add(player);
            else
                break;
        }
        return players;
    }

    private void give(String label, Player player, String[] args) {
        Set<Player> players = new HashSet<Player>();
        Set<ItemStack> itemStacks = new HashSet<ItemStack>();
        if (label.equals("give"))
            players = getPlayers(args);
        itemStacks = getItemStacks(Arrays.copyOfRange(args, players.size(), args.length));
        for (ItemStack x : itemStacks) {
            boolean remove = false;
            for (ItemStack y : oddGive.lists.get(player)) {
                if (OddItem.compare(x, y)) {
                    if (oddGive.blacklist) remove = true;
                    else remove = false;
                }
            }
            if (remove) itemStacks.remove(x);
        }
    }

    private void take(Player player, String[] args) {

    }
}