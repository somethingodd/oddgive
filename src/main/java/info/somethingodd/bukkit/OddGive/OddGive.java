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

import info.somethingodd.bukkit.OddItem.OddItemGroup;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

/**
 * @author Gordon Pettey (petteyg359@gmail.com)
 */
public class OddGive extends JavaPlugin {
    protected Map<Player, OddItemGroup> lists;
    protected List<OddItemGroup> groups;
    protected Map<String, OddItemGroup> kits;
    protected String logPrefix;
    protected Integer defaultQuantity;
    protected Logger log;
    protected Boolean blacklist;
    private OddGiveCommandExecutor OGCE;
    private OddGiveListener OGPL;
    private OddGiveConfiguration oddGiveConfiguration;

    protected void list(OddItemGroup oddItemGroup) {
        if (groups == null) groups = new ArrayList<OddItemGroup>();
        int i = 0;
        while (i < groups.size()) {
            if (groups.get(i).getData().getInt("oddgive.priority", 0) >= oddItemGroup.getData().getInt("oddgive.priority", 0)) break;
            i++;
        }
        groups.add(i, oddItemGroup);
    }

    protected void calculate(Player player) {
        if (lists == null) lists = new HashMap<Player, OddItemGroup>();
        OddItemGroup oddItemGroup = new OddItemGroup();
        for (int i = groups.size() - 1; i >= 0; i--) {
            OddItemGroup currentGroup = groups.get(i);
            List<String> types = currentGroup.getData().getStringList("oddgive.type");
            boolean blacklist = true;
            if (types.contains("blacklist")) {
                blacklist = true;
            } else if (types.contains("whitelist")) {
                blacklist = false;
            }
            for (ItemStack itemStack : currentGroup) {
                if (oddItemGroup.contains(itemStack) && this.blacklist && !blacklist) {
                    oddItemGroup.remove(itemStack);
                } else if (!oddItemGroup.contains(itemStack) && this.blacklist && blacklist) {
                    oddItemGroup.add(itemStack);
                }
            }
        }
        lists.put(player, oddItemGroup);
    }

    @Override
    public void onDisable() {
        log.info(logPrefix + "disabled");
        OGCE = null;
        OGPL = null;
        groups = null;
        lists = null;
        blacklist = null;
        log = null;
        logPrefix = null;
        defaultQuantity = null;
        kits = null;
    }

    @Override
    public void onEnable() {
        log = getServer().getLogger();
        logPrefix = "[" + getDescription().getName() + "] ";
        log.info(logPrefix + getDescription().getVersion() + " enabled");
        oddGiveConfiguration.configure();
        for (Player player : getServer().getOnlinePlayers()) {
            calculate(player);
        }
        OGCE = new OddGiveCommandExecutor(this);
        OGPL = new OddGiveListener(this);
        getCommand("give").setExecutor(OGCE);
        getCommand("i").setExecutor(OGCE);
        getCommand("i0").setExecutor(OGCE);
        getCommand("oddgive").setExecutor(OGCE);
        getServer().getPluginManager().registerEvents(OGPL, this);
    }
}
