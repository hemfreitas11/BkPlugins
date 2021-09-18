package me.bkrmt.bkmito;

import br.com.devpaulo.legendchat.api.events.ChatMessageEvent;
import me.bkrmt.bkcore.BkPlugin;
import me.bkrmt.bkcore.Utils;
import me.bkrmt.bkcore.bkgui.BkGUI;
import me.bkrmt.bkcore.command.CommandModule;
import me.bkrmt.bkcore.command.MainCommand;
import me.bkrmt.bkcore.guiconfig.GUIConfig;
import me.bkrmt.bkcore.textanimator.AnimatorManager;
import me.bkrmt.bkmito.api.Mito;
import me.bkrmt.bkmito.api.UpdateReason;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.HumanEntity;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.plugin.Plugin;
import org.bukkit.util.StringUtil;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public final class BkMito extends BkPlugin {
    private static BkMito plugin;
    private AnimatorManager animatorManager;
    private NPCManager npcManager;
    private MitoManager mitoManager;

    @Override
    public void onEnable() {
        plugin = this;
        BkGUI.INSTANCE.register(this);
        animatorManager = new AnimatorManager(this);
        start(true);
        setRunning(true);

        getCommandMapper()
                .addCommand(new CommandModule(new MainCommand(this, "bkmito.admin", GUIConfig::openMenu), BkMito::mainCompleter))
                .addCommand(new CommandModule(new CmdMito(this, "mito", ""), BkMito::mitoCompleter))
                .registerAll();

        npcManager = new NPCManager(this);
        mitoManager = new MitoManager(this);

        if (getServer().getPluginManager().getPlugin("nChat") != null && getServer().getPluginManager().getPlugin("LegendChat") != null) {
            getServer().getPluginManager().registerEvents(
                    new Listener() {
                        @EventHandler
                        public void onChat(ChatMessageEvent event) {
                            Mito mito = getMitoManager().getCurrentMito();
                            if (mito != null) {
                                if (event.getTags().contains("bkmito") && event.getSender().getUniqueId().equals(mito.getUUID()))
                                    event.setTagValue("bkmito", Utils.translateColor(getConfigManager().getConfig().getString("mito-tag")));
                            }
                        }
                    }, this
            );
        } else {
            sendConsoleMessage("§c[§4BkMito§c] §4Nao foi possivel encontrar o nChat ou LegendChat.");
        }
        getServer().getPluginManager().registerEvents(new ConstantListener(), this);
        Plugin papi = getServer().getPluginManager().getPlugin("PlaceholderAPI");
        if (papi != null && papi.isEnabled()) {
            new PAPIExpansion(this).register();
        }
    }

    private static List<String> mitoCompleter(CommandSender sender, Command command, String label, String[] args) {
        List<String> completions = new ArrayList<>();

        String stats = plugin.getLangFile().get((OfflinePlayer) sender, "commands.mito.subcommands.stats.command");
        String setar = plugin.getLangFile().get((OfflinePlayer) sender, "commands.mito.subcommands.setar.command");
        String npc = plugin.getLangFile().get((OfflinePlayer) sender, "commands.mito.subcommands.npc.command");
        String npc_update = plugin.getLangFile().get((OfflinePlayer) sender, "commands.mito.subcommands.npc.subcommands.update.command");
        String npc_location = plugin.getLangFile().get((OfflinePlayer) sender, "commands.mito.subcommands.npc.subcommands.location.command");

        List<String> subCommands = new ArrayList<>();

        if (sender.hasPermission("bkmito.player") || sender.hasPermission("bkmito.stats"))
            subCommands.add(stats);
        if (sender.hasPermission("bkmito.admin") || sender.hasPermission("bkmito.setar"))
            subCommands.add(setar);
        if (sender.hasPermission("bkmito.admin") || sender.hasPermission("bkmito.npc"))
            subCommands.add(npc);

        if (args.length == 1) {
            String partialCommand = args[0];
            StringUtil.copyPartialMatches(partialCommand, subCommands, completions);
        } else if (args.length == 2) {
            if (subCommands.contains(setar) && (args[0].equalsIgnoreCase(setar))) {
                List<String> players = Arrays.stream(plugin.getHandler().getMethodManager().getOnlinePlayers()).map(HumanEntity::getName).collect(Collectors.toList());
                String partialName = args[1];
                StringUtil.copyPartialMatches(partialName, players, completions);
            } else if (subCommands.contains(stats) && (args[0].equalsIgnoreCase(stats))) {
                List<String> players = new ArrayList<>();
                me.bkrmt.bkmito.api.MitoManager manager = plugin.mitoManager;
                for (Mito mito : manager.getCachedMitos().values()) {
                    players.add(mito.getPlayerName());
                }
                if (manager.getCurrentMito() != null) players.add(manager.getCurrentMito().getPlayerName());
                String partialName = args[1];
                StringUtil.copyPartialMatches(partialName, players, completions);
            } else if (subCommands.contains(npc) && (args[0].equalsIgnoreCase(npc))) {
                String partialSubCommand = args[1];
                StringUtil.copyPartialMatches(partialSubCommand, Arrays.asList(npc_location, npc_update), completions);
            }
        }

        Collections.sort(completions);
        return completions;
    }

    private static List<String> mainCompleter(CommandSender sender, Command command, String label, String[] args) {
        List<String> completions = new ArrayList<>();

        String config = plugin.getLangFile().get((OfflinePlayer) sender, "commands.mito.subcommands.stats.command");
        String reload = plugin.getLangFile().get((OfflinePlayer) sender, "commands.mito.subcommands.setar.command");
        String messages = plugin.getLangFile().get((OfflinePlayer) sender, "commands.mito.subcommands.npc.command");

        List<String> subCommands = new ArrayList<>();

        if (sender.hasPermission("bkmito.admin"))
            subCommands.add(config);
        if (sender.hasPermission("bkmito.admin"))
            subCommands.add(reload);
        if (sender.hasPermission("bkmito.admin"))
            subCommands.add(messages);

        if (args.length == 1) {
            String partialCommand = args[0];
            StringUtil.copyPartialMatches(partialCommand, subCommands, completions);
        }

        Collections.sort(completions);
        return completions;
    }

    @Override
    public void onDisable() {
        mitoManager.saveMitos();
        getNpcManager().wipe(UpdateReason.UPDATE_ALL);
    }

    @Override
    public AnimatorManager getAnimatorManager() {
        return animatorManager;
    }

    public me.bkrmt.bkmito.api.MitoManager getMitoManager() {
        return mitoManager;
    }

    public me.bkrmt.bkmito.api.NPCManager getNpcManager() {
        return npcManager;
    }

    public static BkMito getInstance() {
        return plugin;
    }
}
