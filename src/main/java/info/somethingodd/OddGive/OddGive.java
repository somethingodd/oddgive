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

import org.bukkit.plugin.java.JavaPlugin;

import java.util.logging.Logger;

/**
 * @author Gordon Pettey (petteyg359@gmail.com)
 */
public class OddGive extends JavaPlugin {
    protected String logPrefix;
    protected Integer defaultQuantity;
    protected Logger log;
    protected Boolean blacklist;
    private OddGiveCommandExecutor OGCE;
    private OddGiveConfiguration oddGiveConfiguration;

    @Override
    public void onDisable() {
        log.info(logPrefix + "disabled");
        OGCE = null;
        blacklist = null;
        log = null;
        logPrefix = null;
        defaultQuantity = null;
    }

    @Override
    public void onEnable() {
        log = getServer().getLogger();
        logPrefix = "[" + getDescription().getName() + "] ";
        log.info(logPrefix + getDescription().getVersion() + " enabled");
        oddGiveConfiguration = new OddGiveConfiguration(this);
        oddGiveConfiguration.configure();
        OGCE = new OddGiveCommandExecutor(this);
        getCommand("give").setExecutor(OGCE);
        getCommand("i").setExecutor(OGCE);
        getCommand("i0").setExecutor(OGCE);
        getCommand("oddgive").setExecutor(OGCE);
    }
}
