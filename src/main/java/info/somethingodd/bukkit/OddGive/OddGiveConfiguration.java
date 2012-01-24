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

import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;

/**
 * @author Gordon Pettey (petteyg359@gmail.com)
 */
public class OddGiveConfiguration {
    private OddGive oddGive;
    private File configurationFile;
    private YamlConfiguration defaultConfiguration;
    private YamlConfiguration configuration;

    public OddGiveConfiguration(OddGive oddGive) {
        this.oddGive = oddGive;
    }

    protected void configure() {
        defaultConfiguration = new YamlConfiguration();
        try {
            defaultConfiguration.load(oddGive.getResource("OddGive.yml"));
        } catch (Exception e) {
            oddGive.log.warning(oddGive.logPrefix + "Error loading default configuration! " + e.getMessage());
            e.printStackTrace();
        }
        configurationFile = new File(oddGive.getDataFolder() + File.separator + "OddGive.yml");
        configuration = new YamlConfiguration();
        configuration.setDefaults(defaultConfiguration);
        try {
            configuration.load(configurationFile);
        } catch (Exception e) {
            oddGive.log.severe("Couldn't open configuration file! " + e.getMessage());
            e.printStackTrace();
        }

    }
}
