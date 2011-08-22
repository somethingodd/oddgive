package info.somethingodd.bukkit.OddGive;

import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerListener;
import org.bukkit.event.player.PlayerLoginEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import java.util.HashMap;

public class OddGivePlayerListener extends PlayerListener {
    private OddGive oddGive;

    public OddGivePlayerListener(OddGive oddGive) {
        this.oddGive = oddGive;
    }

    @Override
    public void onPlayerLogin(PlayerLoginEvent event) {
        oddGive.lists.put(event.getPlayer(), new HashMap<String, Boolean>());
    }

    @Override
    public void onPlayerQuit(PlayerQuitEvent event) {
        oddGive.lists.remove(event.getPlayer());
    }
}
