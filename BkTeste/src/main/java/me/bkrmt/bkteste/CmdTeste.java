package me.bkrmt.bkteste;

import me.bkrmt.bkcore.BkPlugin;
import me.bkrmt.bkcore.command.Executor;
import me.bkrmt.bkcore.guiconfig.GUIConfig;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class CmdTeste extends Executor {
    public CmdTeste(BkPlugin plugin, String langKey, String permission) {
        super(plugin, langKey, permission);
    }

    @Override
    public boolean onCommand(CommandSender commandSender, Command command, String label, String[] args) {
        Player player = (Player) commandSender;

        GUIConfig.openMenu(getPlugin(), (Player) commandSender, getPlugin().getConfigManager().getConfig());
        return true;
    }

    /*public void fill(World world, Material material, Location loc1, Location loc2) {
        int xMax = Integer.max(loc1.getBlockX(), loc2.getBlockX());
        int yMax = Integer.max(loc1.getBlockY(), loc2.getBlockY());
        int zMax = Integer.max(loc1.getBlockZ(), loc2.getBlockZ());
        int xMin = Integer.min(loc1.getBlockX(), loc2.getBlockX());
        int yMin = Integer.min(loc1.getBlockY(), loc2.getBlockY());
        int zMin = Integer.min(loc1.getBlockZ(), loc2.getBlockZ());

        WorkloadThread thread = new WorkloadThread();

        for (int x = xMin; x < xMax; x++) {
            for (int y = yMin; y < yMax; y++) {
                for (int z = zMin; z < zMax; z++) {
                    thread.addLoad(new PlaceableBlock(world.getUID(), x, y, z, material));
                }
            }
        }

        new BukkitRunnable() {
            @Override
            public void run() {
                if (!thread.getWorkloadDeque().isEmpty()) thread.run();
                else cancel();
            }
        }.runTaskTimer(getPlugin(), 0, 1);
    }*/
}
