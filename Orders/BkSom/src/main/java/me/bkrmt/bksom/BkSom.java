package me.bkrmt.bksom;

import me.bkrmt.bkcore.BkPlugin;
import me.bkrmt.bkcore.command.CommandModule;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.util.StringUtil;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public final class BkSom extends BkPlugin {
    public static BkSom plugin;

    public void onEnable() {
        plugin = this;
        start(true);
        setRunning(true);
        getCommandMapper().addCommand(new CommandModule(new CmdSom(this, "bksom", "bksom.usar"), (sender, b, c, args) -> {
            List<String> completions = new ArrayList<>();
            if (sender.hasPermission("bksom.usar")) {
                List<String> newCompletions = new ArrayList<>();
                if (args.length == 1) {
                    String partialCommand = args[0];
                    newCompletions.add("todos");
                    newCompletions.add("parar");
                    for (Player player : getHandler().getMethodManager().getOnlinePlayers()) {
                        newCompletions.add(player.getName());
                    }
                    StringUtil.copyPartialMatches(partialCommand, newCompletions, completions);
                } else if (args.length == 2) {
                    String partialCommand = args[1];
                    for (Sound sound : Sound.values()) {
                        newCompletions.add(sound.toString());
                    }
                    StringUtil.copyPartialMatches(partialCommand, newCompletions, completions);
                }
            }
            Collections.sort(completions);

            return completions;
        }))
                .registerAll();
    }

    public static BkSom getInstance() {
        return plugin;
    }
}
