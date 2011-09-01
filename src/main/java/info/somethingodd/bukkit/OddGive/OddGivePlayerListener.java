package info.somethingodd.bukkit.OddGive;

import org.bukkit.event.player.PlayerListener;
import org.bukkit.event.player.PlayerLoginEvent;
import org.bukkit.event.player.PlayerQuitEvent;

public class OddGivePlayerListener extends PlayerListener {
    private OddGive oddGive = null;

    public OddGivePlayerListener(OddGive oddGive) {
        this.oddGive = oddGive;
    }

    @Override
    public void onPlayerLogin(PlayerLoginEvent event) {
        oddGive.calculate(event.getPlayer());
    }

    @Override
    public void onPlayerQuit(PlayerQuitEvent event) {
        if (oddGive.lists != null) oddGive.lists.remove(event.getPlayer());
    }
}
