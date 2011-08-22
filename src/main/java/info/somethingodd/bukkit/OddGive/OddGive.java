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
    protected OddItem oddItem = null;

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
        log.info(logPrefix + "disabled" );
    }

    @Override
    public void onEnable() {
        lists = new HashMap<Player, Map<String, Boolean>>();
        OGCE = new OddGiveCommandExecutor(this);
        OGPL = new OddGivePlayerListener(this);
        //getCommand("give")
        getCommand("give").setExecutor(OGCE);
        getCommand("i").setExecutor(OGCE);
        getCommand("i0").setExecutor(OGCE);
        getCommand("og").setExecutor(OGCE);
        getServer().getPluginManager().registerEvent(Event.Type.PLAYER_LOGIN, OGPL, Event.Priority.Normal, this);
        getServer().getPluginManager().registerEvent(Event.Type.PLAYER_QUIT, OGPL, Event.Priority.Normal, this);
        oddItem = (OddItem) getServer().getPluginManager().getPlugin("OddItem");
        Plugin p = getServer().getPluginManager().getPlugin("Permissions");
        if (p != null) ph = ((Permissions) p).getHandler();
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
        BufferedReader i = new BufferedReader(new InputStreamReader(getClass().getResourceAsStream("/OddItem.yml")));
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
