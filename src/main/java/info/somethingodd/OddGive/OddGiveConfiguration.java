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

import org.bukkit.configuration.file.YamlConfiguration;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;

/**
 * @author Gordon Pettey (petteyg359@gmail.com)
 */
public class OddGiveConfiguration {
    private OddGive oddGive;
    private YamlConfiguration yamlConfiguration;

    public OddGiveConfiguration(OddGive oddGive) {
        this.oddGive = oddGive;
    }

    protected void configure() {
        String[] filenames = {"config.yml"};
        try {
            initialConfig(filenames);
        } catch (Exception e) {
            oddGive.log.warning("Exception writing initial configuration files: " + e.getMessage());
            e.printStackTrace();
        }
        yamlConfiguration = (YamlConfiguration) oddGive.getConfig();
        oddGive.defaultQuantity = yamlConfiguration.getInt("defaultQuantity");
    }

    private void initialConfig(String[] filenames) throws IOException {
        for (String filename : filenames) {
            File file = new File(oddGive.getDataFolder(), filename);
            if (!file.exists()) {
                BufferedReader src = null;
                BufferedWriter dst = null;
                try {
                    file.getParentFile().mkdirs();
                    file.createNewFile();
                    src = new BufferedReader(new InputStreamReader(getClass().getResourceAsStream("/" + filename)));
                    dst = new BufferedWriter(new FileWriter(file));
                    String line = src.readLine();
                    while (line != null) {
                        dst.write(line + "\n");
                        line = src.readLine();
                    }
                    src.close();
                    dst.close();
                    oddGive.log.info("Wrote default " + filename);
                } catch (IOException e) {
                    oddGive.log.warning("Error writing default " + filename);
                } finally {
                    try {
                        src.close();
                        dst.close();
                    } catch (Exception e) {
                    }
                }
            }
        }
    }
}
