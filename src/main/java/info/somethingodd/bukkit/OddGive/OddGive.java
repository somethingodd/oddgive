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
import org.bukkit.Bukkit;
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
import java.net.URL;
import java.net.URLConnection;
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
    private Boolean permission = null;
    private PermissionHandler ph = null;
    private PluginDescriptionFile info = null;
    private OddGiveCommandExecutor OGCE = null;
    private OddGivePlayerListener OGPL = null;
    private String configurationFile = null;

    private void configure() {
        File configFile = new File(configurationFile);
        if (!configFile.exists())
            writeConfig();
        Configuration config = new Configuration(configFile);
        blacklist = config.getBoolean("blacklist", true);
        defaultQuantity = config.getInt("defaultQuantity", 64);
        groups = new LinkedList<OddItemGroup>();
        List<String> groupsList = config.getStringList("groups", new ArrayList<String>());
        if (groupsList.isEmpty()) {
            log.warning(logPrefix + "No groups available; blacklist disabled.");
        } else {
            for (String n : groupsList) {
                OddItemGroup adding = OddItem.getItemGroup(n);
                int i = 0;
                while (i < groups.size() && groups.get(i).getData().getInt("priority", -1) < adding.getData().getInt("priority", -1))
                    i++;
                groups.add(i, adding);
            }
        }
        permission = config.getBoolean("bukkitpermissions", true);
        if (config.getBoolean("phonehome", true)) phoneHome();
    }

    protected void calculate(Player player) {
        if (lists == null) lists = new HashMap<Player, Set<ItemStack>>();
        Set<ItemStack> list = new HashSet<ItemStack>();

        for (int i = groups.size() - 1; i >= 0 && player.hasPermission("oddgive.groups." + i); i++) {
            OddItemGroup group = groups.get(i);
            boolean type = group.getData().getBoolean("blacklist", blacklist);
            for (ItemStack x : group) {
                for (ItemStack y : list)
                    if (OddItem.compare(x, y)) {
                        if (type) {
                            if (blacklist) list.add(x);
                        } else list.remove(x);
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
        permission = null;
        blacklist = null;
        info = null;
        log = null;
        logPrefix = null;
        ph = null;
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
        Plugin p;
        if (!permission) {
            p = getServer().getPluginManager().getPlugin("Permissions");
            if (p != null) {
                ph = ((Permissions) p).getHandler();
            } else {
                log.warning("Nijikokun/TheYeti/rcjrrjcr Permissions selected in config, but plugin not found");
                permission = !permission;
            }
        }
    }

    private boolean writeConfig() {
        FileWriter fw;
        try {
            getDataFolder().mkdir();
            fw = new FileWriter(configurationFile);
        } catch (SecurityException e) {
            log.severe(logPrefix + "Couldn't create config dir: " + e.getMessage());
            Bukkit.getPluginManager().disablePlugin(this);
            return false;
        } catch (IOException e) {
            log.severe(logPrefix + "Couldn't write config file: " + e.getMessage());
            Bukkit.getPluginManager().disablePlugin(this);
            return false;
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
                Bukkit.getPluginManager().disablePlugin(this);
            }
        }
        return true;
    }

    protected boolean uglyPermissions(Player player, String permission) {
        if (ph != null)
            return ph.has(player, permission);
        return player.hasPermission(permission);
    }

    private void phoneHome() {
        String url = String.format("http://plugins.blockface.org/usage/update.php?name=%s&build=%s&plugin=%s&port=%s",
                getServer().getName(),
                getDescription().getVersion(),
                getDescription().getName(),
                getServer().getPort());
        try {
            URL blockface = new URL(url);
            URLConnection con = blockface.openConnection();
        } catch (Exception e) {
            return;
        }
    }
}
