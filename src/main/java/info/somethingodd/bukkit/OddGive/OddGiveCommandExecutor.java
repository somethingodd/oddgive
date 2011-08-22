package info.somethingodd.bukkit.OddGive;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

public class OddGiveCommandExecutor implements CommandExecutor {
    private OddGive oddGive = null;

    public OddGiveCommandExecutor(OddGive oddGive) {
        this.oddGive = oddGive;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {

        return false;
    }
}
