package me.bkrmt.bksom;

import me.bkrmt.bkcore.BkPlugin;
import me.bkrmt.bkcore.Utils;
import me.bkrmt.bkcore.command.Executor;
import org.bukkit.Bukkit;
import org.bukkit.Sound;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

import java.util.concurrent.ConcurrentHashMap;

public class CmdSom extends Executor {

    public CmdSom(BkPlugin plugin, String langKey, String permission) {
        super(plugin, langKey, permission);
    }

    private static final ConcurrentHashMap<Integer, BukkitTask> sons = new ConcurrentHashMap<>();

    @Override
    public boolean onCommand(CommandSender commandSender, Command command, String label, String[] args) {
        if (hasPermission(commandSender)) {
            if (args.length == 1) {
                if (args[0].equalsIgnoreCase("parar")) {
                    for (int id : sons.keySet()) {
                        sons.get(id).cancel();
                        sons.remove(id).cancel();
                    }
                    commandSender.sendMessage(getPlugin().getLangFile().get("info.sons-parados"));
                }
            } else if (args.length >= 4 && args.length < 7) {
                Player targetPlayer = null;
                if (!args[0].equalsIgnoreCase("todos")) {
                    targetPlayer = Utils.getPlayer(args[0]);
                }
                if (processArgs(commandSender, args, targetPlayer)) return true;
            } else {
                sendUsage(commandSender);
            }
        } else {
            commandSender.sendMessage(getPlugin().getLangFile().get("error.no-permission"));
        }
        return true;
    }

    private boolean processArgs(CommandSender commandSender, String[] args, Player targetPlayer) {
        Sound som;
        try {
            som = Sound.valueOf(args[1]);
        } catch (Exception e) {
            commandSender.sendMessage(getPlugin().getLangFile().get("error.som-invalido"));
            return true;
        }
        float volume;
        try {
            volume = Float.parseFloat(args[2]);
        } catch (Exception e) {
            commandSender.sendMessage(getPlugin().getLangFile().get("error.volume-invalido"));
            return true;
        }
        float pitch;
        try {
            pitch = Float.parseFloat(args[3]);
        } catch (Exception e) {
            commandSender.sendMessage(getPlugin().getLangFile().get("error.grave-invalido"));
            return true;
        }

        if (args.length == 4) {
            if (targetPlayer == null) {
                for (Player onlinePlayer : Bukkit.getOnlinePlayers()) {
                    onlinePlayer.playSound(onlinePlayer.getLocation(), som, volume, pitch);
                }
            } else {
                targetPlayer.playSound(targetPlayer.getLocation(), som, volume, pitch);
            }
        } else {
            final float[] repetir = new float[1];
            try {
                repetir[0] = Float.parseFloat(args[4]);
            } catch (Exception e) {
                commandSender.sendMessage(getPlugin().getLangFile().get("error.repetir-invalido"));
                return true;
            }
            long espera;
            try {
                espera = Long.parseLong(args[5]);
            } catch (Exception e) {
                commandSender.sendMessage(getPlugin().getLangFile().get("error.espera-invalido"));
                return true;
            }
            int id = sons.size();
            sons.put(id, new BukkitRunnable() {
                @Override
                public void run() {
                    if (repetir[0] == 0) {
                        sons.remove(id);
                        cancel();
                    } else {
                        if (targetPlayer == null) {
                            for (Player onlinePlayer : Bukkit.getOnlinePlayers()) {
                                onlinePlayer.playSound(onlinePlayer.getLocation(), som, volume, pitch);
                            }
                        } else {
                            targetPlayer.playSound(targetPlayer.getLocation(), som, volume, pitch);
                        }
                        repetir[0]--;
                    }
                }
            }.runTaskTimerAsynchronously(BkSom.getInstance(), 0, espera));
        }
        return false;
    }
}