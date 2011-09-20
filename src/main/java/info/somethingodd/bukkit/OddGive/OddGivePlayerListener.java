package info.somethingodd.bukkit.OddGive;

import info.somethingodd.bukkit.OddItem.OddItem;
import org.bukkit.event.player.PlayerListener;
import org.bukkit.event.player.PlayerLoginEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.ItemStack;

public class OddGivePlayerListener extends PlayerListener {
    private OddGive oddGive = null;

    public OddGivePlayerListener(OddGive oddGive) {
        this.oddGive = oddGive;
    }

    @Override
    public void onPlayerLogin(PlayerLoginEvent event) {
        oddGive.calculate(event.getPlayer());
        ItemStack itemStack = null;
        try {
            itemStack = OddItem.getItemStack(event.getPlayer().getName());
        } catch (IllegalArgumentException e) {
        }
        if (itemStack != null)
            oddGive.log.warning("Joining player name matches an OddItemBase alias! This may be confusing.");
    }

    @Override
    public void onPlayerQuit(PlayerQuitEvent event) {
        if (oddGive.lists != null) oddGive.lists.remove(event.getPlayer());
    }
}
