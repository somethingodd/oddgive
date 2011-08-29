/* This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package info.somethingodd.bukkit.OddGive;

import com.nijiko.permissions.PermissionHandler;
import com.nijikokun.bukkit.Permissions.Permissions;
import info.somethingodd.bukkit.OddItem.OddItem;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.PluginDescriptionFile;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.util.config.Configuration;

import java.io.*;
import java.util.*;
import java.util.logging.Logger;

/**
 * @author Gordon Pettey (petteyg359@gmail.com)
 */
public class OddGive extends JavaPlugin {
    private Logger log = null;
    private PluginDescriptionFile info = null;
    private PermissionHandler ph = null;
    private Boolean blacklist = null;
    private Integer defaultQuantity = null;
    private String logPrefix;
    private OddGiveCommandExecutor OGCE = null;
    private OddGivePlayerListener OGPL = null;
    protected Map<Player, Map<String, Boolean>> lists = null;
    private final String configurationFile = "plugins" + File.separator + "OddGive.yml";

    private void configure() {
        File configurationFile = new File(this.configurationFile);
        if (!configurationFile.exists())
            writeConfig();
        Configuration config = new Configuration(configurationFile);

    }

    @Override
    public void onDisable() {
        lists = null;
        OGCE = null;
        OGPL = null;
        getServer().getPluginManager().
        log.info(logPrefix + "disabled");
    }

    @Override
    public void onEnable() {
        lists = new HashMap<Player, Map<String, Boolean>>();
        OGCE = new OddGiveCommandExecutor(this);
        OGPL = new OddGivePlayerListener(this);
        getCommand("give").setExecutor(OGCE);
        getCommand("i").setExecutor(OGCE);
        getCommand("i0").setExecutor(OGCE);
        getCommand("og").setExecutor(OGCE);
        getServer().getPluginManager().registerEvent(Event.Type.PLAYER_LOGIN, OGPL, Event.Priority.Normal, this);
        getServer().getPluginManager().registerEvent(Event.Type.PLAYER_QUIT, OGPL, Event.Priority.Normal, this);
        try {
            ph = ((Permissions) getServer().getPluginManager().getPlugin("Permissions")).getHandler();
        } catch (NullPointerException e) {
        }
        log.info(logPrefix + info.getVersion() + " enabled");
    }

    @Override
    public void onLoad() {
        info = getDescription();
        log = getServer().getLogger();
        logPrefix = "[" + info.getName() + "] ";
    }

    private void writeConfig() {
        FileWriter fw;
        try {
            fw = new FileWriter(configurationFile);
        } catch (IOException e) {
            log.severe(logPrefix + "Couldn't write config file: " + e.getMessage());
            getServer().getPluginManager().disablePlugin(this);
            return;
        }
        BufferedReader i = new BufferedReader(new InputStreamReader(getClass().getResourceAsStream("/OddGive.yml")));
        BufferedWriter o = new BufferedWriter(fw);
        try {
            String line = i.readLine();
            while (line != null) {
                o.write(line + System.getProperty("line.separator"));
                line = i.readLine();
            }
            log.info(logPrefix + "Wrote default config");
        } catch (IOException e) {
            log.severe(logPrefix + "Error writing config: " + e.getMessage());
        } finally {
            try {
                o.close();
                i.close();
            } catch (IOException e) {
                log.severe(logPrefix + "Error saving config: " + e.getMessage());
                getServer().getPluginManager().disablePlugin(this);
            }
        }
    }

    protected boolean uglyPermissions(Player player, String permission) {
        if (ph != null)
            return ph.has(player, permission);
        return player.hasPermission(permission);
    }
}
