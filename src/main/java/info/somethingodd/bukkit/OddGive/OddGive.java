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

import info.somethingodd.bukkit.OddItem.OddItem;
import info.somethingodd.bukkit.OddItem.OddItemGroup;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.PluginDescriptionFile;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.util.config.Configuration;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Logger;

/**
 * @author Gordon Pettey (petteyg359@gmail.com)
 */
public class OddGive extends JavaPlugin {
    protected Map<Player, Set<ItemStack>> lists = null;
    protected List<OddItemGroup> groups = null;
    protected String logPrefix = null;
    protected Integer defaultQuantity = null;
    protected Logger log = null;
    protected Boolean blacklist = null;
    private PluginDescriptionFile info = null;
    private OddGiveCommandExecutor OGCE = null;
    private OddGivePlayerListener OGPL = null;
    private String configurationFile = null;

    private void configure() {
        File configFile = new File(configurationFile);
        if (!configFile.exists())
            if (!writeConfig()) return;
        Configuration config = new Configuration(configFile);
        config.load();
        blacklist = config.getBoolean("blacklist", true);
        defaultQuantity = config.getInt("defaultQuantity", 64);
        groups = new LinkedList<OddItemGroup>();
        log.info(logPrefix + "configKeys: " + config.getKeys());
        List<String> groups = config.getStringList("groups", new ArrayList<String>());
        log.info(logPrefix + "groups: " + groups.toString());
        if (groups.isEmpty()) {
            log.warning(logPrefix + "No groups available; blacklist disabled.");
        } else {
            for (String g : groups) {
                OddItemGroup group = OddItem.getItemGroup(g);
                if (group.getData().getProperty("oddgive") != null) {
                    int i = 0;
                    while (i < groups.size() && this.groups.get(i).getData().getInt("oddgive.priority", -1) < group.getData().getInt("oddgive.priority", -1))
                        i++;
                    this.groups.add(i, group);
                }
            }
        }
        //configuration.setHeader("See https://github.com/petteyg/OddGive/blob/master/src/main/resources/OddGive.yml for a commented example");
        config.save();
    }

    protected void calculate(Player player) {
        if (lists == null) lists = new HashMap<Player, Set<ItemStack>>();
        Set<ItemStack> list = new HashSet<ItemStack>();
        for (int i = groups.size() - 1; i >= 0 && player.hasPermission("oddgive.groups." + i); i++) {
            OddItemGroup group = groups.get(i);
            Boolean type = null;
            if (group.getData().getStringList("oddgive.type", new ArrayList<String>()).contains("blacklist"))
                type = true;
            else if (group.getData().getStringList("oddgive.type", new ArrayList<String>()).contains("whitelist"))
                type = false;
            if (type != null) {
                for (ItemStack x : group) {
                    for (ItemStack y : list)
                        if (OddItem.compare(x, y)) {
                            if (type) {
                                if (blacklist) list.add(x);
                            } else list.remove(x);
                        }
                }
            }
        }
        lists.put(player, list);
    }

    @Override
    public void onDisable() {
        log.info(logPrefix + "disabled");
        OGCE = null;
        OGPL = null;
        groups = null;
        lists = null;
        blacklist = null;
        info = null;
        log = null;
        logPrefix = null;
        defaultQuantity = null;
    }

    @Override
    public void onEnable() {
        info = getDescription();
        log = getServer().getLogger();
        logPrefix = "[" + info.getName() + "] ";
        log.info(logPrefix + info.getVersion() + " enabled");
        configurationFile = getDataFolder() + System.getProperty("file.separator") + "OddGive.yml";
        configure();
        for (Player player : getServer().getOnlinePlayers()) {
            calculate(player);
        }
        OGCE = new OddGiveCommandExecutor(this);
        OGPL = new OddGivePlayerListener(this);
        getCommand("give").setExecutor(OGCE);
        getCommand("i").setExecutor(OGCE);
        getCommand("i0").setExecutor(OGCE);
        getCommand("oddgive").setExecutor(OGCE);
        getServer().getPluginManager().registerEvent(Event.Type.PLAYER_LOGIN, OGPL, Event.Priority.Normal, this);
        getServer().getPluginManager().registerEvent(Event.Type.PLAYER_QUIT, OGPL, Event.Priority.Normal, this);
    }

    private boolean writeConfig() {
        FileWriter fw;
        File config = new File(configurationFile);
        if (!config.getParentFile().exists()) config.getParentFile().mkdir();
        try {
            fw = new FileWriter(configurationFile);
        } catch (IOException e) {
            log.severe(logPrefix + "Couldn't write config file: " + e.getMessage());
            getServer().getPluginManager().disablePlugin(this);
            return false;
        }
        BufferedReader i = new BufferedReader(new InputStreamReader(getClass().getResourceAsStream("/" + info.getName() + ".yml")));
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
                getServer().getPluginManager().disablePlugin(getServer().getPluginManager().getPlugin("OddGive"));
            }
        }
        return true;
    }
}
