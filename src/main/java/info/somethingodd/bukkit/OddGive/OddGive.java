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
import info.somethingodd.bukkit.OddItem.OddItemGroup;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.PluginDescriptionFile;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.util.config.Configuration;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Logger;

/**
 * @author Gordon Pettey (petteyg359@gmail.com)
 */
public class OddGive extends JavaPlugin {
    protected Map<Player, Set<ItemStack>> lists = null;
    protected Map<Integer, OddItemGroup> groups = null;
    protected String logPrefix = null;
    protected String mode = null;
    protected Integer defaultQuantity = null;
    protected static Logger log = null;
    private PermissionHandler ph = null;
    private PluginDescriptionFile info = null;
    private OddGiveCommandExecutor OGCE = null;
    private OddGivePlayerListener OGPL = null;
    private String configurationFile = null;
    private static String permission = null;

    private void configure() {
        File configFile = new File(configurationFile);
        if (!configFile.exists())
            writeConfig();
        Configuration config = new Configuration(configFile);
        defaultQuantity = config.getInt("defaultQuantity", 64);
        mode = config.getString("mode", "blacklist");
        groups = new HashMap<Integer, OddItemGroup>();
        List<String> groupsList = config.getKeys("groups");
        if (groupsList == null || groupsList.isEmpty()) {
            log.warning(logPrefix + "No groups specified - item creation is unrestricted");
        } else {
            for (String n : groupsList) {
                groups.put(config.getInt(n, groups.size()), OddItem.getItemGroup(n));
            }
        }
        permission = config.getString("permission", "bukkit");
    }

    protected void calculate(Player player) {
        if (lists == null) lists = new HashMap<Player, Set<ItemStack>>();
        Set<ItemStack> list = new HashSet<ItemStack>();
        for (int i = 1; i <= groups.size() && player.hasPermission("oddgive.groups."+i); i++) {
            if (mode.equals("blacklist"))
                for (ItemStack x : groups.get(i)) {
                    boolean add = true;
                    boolean remove = false;
                    boolean blacklist = groups.get(i).getData().getBoolean("blacklist", true);
                    for (ItemStack y : list)
                        if (OddItem.compare(x, y)) {
                            if (blacklist) add = false;
                            else remove = true;
                        }
                    if (add) list.add(x);
                    if (remove) list.remove(x);
                }
        }
        lists.put(player, list);
    }

    @Override
    public void onDisable() {
        OGCE = null;
        OGPL = null;
        lists = null;
        log.info(logPrefix + "disabled");
    }

    @Override
    public void onEnable() {
        for (Player player : getServer().getOnlinePlayers()) {
            calculate(player);
        }
        configurationFile = getDataFolder() + System.getProperty("file.separator") + "OddGive.yml";
        OGCE = new OddGiveCommandExecutor(this);
        OGPL = new OddGivePlayerListener(this);
        getCommand("give").setExecutor(OGCE);
        getCommand("i").setExecutor(OGCE);
        getCommand("i0").setExecutor(OGCE);
        getCommand("oddgive").setExecutor(OGCE);
        getServer().getPluginManager().registerEvent(Event.Type.PLAYER_LOGIN, OGPL, Event.Priority.Normal, this);
        getServer().getPluginManager().registerEvent(Event.Type.PLAYER_QUIT, OGPL, Event.Priority.Normal, this);
        Plugin p;
        if (permission.equals("yeti")) {
            p = getServer().getPluginManager().getPlugin("Permissions");
            if (p != null)
                ph = ((Permissions) p).getHandler();
            else
                log.warning("Permissions selected in config, but plugin not found");
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
