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
package info.somethingodd.OddGive;

import info.somethingodd.OddItem.OddItem;
import info.somethingodd.OddItem.configuration.OddItemGroup;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.ItemStack;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * @author Gordon Pettey (petteyg359@gmail.com)
 */
public class OddGiveLists implements Listener {
    private final Set<OddItemGroup> groups;
    private final Map<String, Set<ItemStack>> kits;
    private final Map<Player, Integer> levels;
    private final Map<Integer, Set<ItemStack>> whitelist;
    private final Map<Integer, Set<ItemStack>> blacklist;
    private final Map<Player, Set<ItemStack>> lists;
    private final OddGive oddGive;
    private int maxPriority;

    public OddGiveLists(OddGive oddGive) {
        this.oddGive = oddGive;
        kits = new HashMap<String, Set<ItemStack>>();
        whitelist = new HashMap<Integer, Set<ItemStack>>();
        blacklist = new HashMap<Integer, Set<ItemStack>>();
        groups = new HashSet<OddItemGroup>();
        levels = new HashMap<Player, Integer>();
        maxPriority = 0;
        getGroups();
        getLevels();
        lists = new HashMap<Player, Set<ItemStack>>();

        oddGive.log.info("Loaded " + kits.size() + " kits");
        oddGive.log.info(whitelist.size() + " items whitelisted");
        oddGive.log.info(blacklist.size() + " items blacklisted");
        oddGive.log.info("Maximum group priority level is " + maxPriority + ".");
        if (Collections.max(levels.values()) != maxPriority) {
            oddGive.log.warning("Maxmimum group priority (" + maxPriority + ") does not match maximum permission level (" + Collections.max(levels.values()) + ").");
        }
    }

    private void getGroups() {
        groups.addAll(OddItem.getItemGroups("oddgive"));
        for (OddItemGroup oddItemGroup : groups) {
            Integer currentPriority = oddItemGroup.getInt("oddgive", "priority");
            if (currentPriority != null && currentPriority > maxPriority) {
                maxPriority = currentPriority;
            }
            if (whitelist.get(currentPriority) == null) {
                whitelist.put(currentPriority, new HashSet<ItemStack>());
            }
            if (blacklist.get(currentPriority) == null) {
                blacklist.put(currentPriority, new HashSet<ItemStack>());
            }
            List<String> type = oddItemGroup.getStringList("oddgive", "type");
            if (type != null) {
                if (type.contains("kit")) {
                    for (String alias : oddItemGroup.getAliases())
                        kits.put(alias, new HashSet<ItemStack>(oddItemGroup.getItems()));
                }
                if (type.contains("whitelist") && type.contains("blacklist")) {
                    oddGive.log.warning("Error: Group \"" + oddItemGroup.getAliases().iterator().next() + "\" can't be both blacklist and whitelist!");
                    continue;
                } else if (type.contains("whitelist")) {
                    whitelist.get(currentPriority).addAll(oddItemGroup.getItems());
                } else if (type.contains("blacklist")) {
                    blacklist.get(currentPriority).addAll(oddItemGroup.getItems());
                }
            }
        }
    }

    private void getLevels() {
        for (Player player : oddGive.getServer().getOnlinePlayers()) {
            getLevel(player);
        }
    }

    private void getLevel(Player player) {
        for (int i = 0; i <= maxPriority; i++) {
            if (player.hasPermission("oddgive.priority." + i)) {
                levels.put(player, i);
            }
        }
    }

    public void calculate(Player player) {

    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        getLevel(event.getPlayer());
        if (levels.get(event.getPlayer()) > maxPriority) {
            oddGive.log.warning("Player " + event.getPlayer().getName() + " has permission level (" + levels.get(event.getPlayer()) + ") higher than maximum group priority (" + maxPriority + ").");
        }
        calculate(event.getPlayer());
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        levels.remove(event.getPlayer());
        lists.remove(event.getPlayer());
    }
}
