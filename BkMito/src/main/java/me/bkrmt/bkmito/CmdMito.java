package me.bkrmt.bkmito;

import me.bkrmt.bkcore.BkPlugin;
import me.bkrmt.bkcore.PagedItem;
import me.bkrmt.bkcore.PagedList;
import me.bkrmt.bkcore.bkgui.MenuSound;
import me.bkrmt.bkcore.bkgui.gui.GUI;
import me.bkrmt.bkcore.bkgui.gui.Rows;
import me.bkrmt.bkcore.bkgui.item.ItemBuilder;
import me.bkrmt.bkcore.bkgui.page.Page;
import me.bkrmt.bkcore.command.Executor;
import me.bkrmt.bkcore.config.Configuration;
import me.bkrmt.bkcore.xlibs.XMaterial;
import me.bkrmt.bkmito.api.Mito;
import me.bkrmt.bkmito.api.MitoManager;
import me.bkrmt.bkmito.api.UpdateReason;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.ArrayDeque;
import java.util.stream.Collectors;

public class CmdMito extends Executor {

    public CmdMito(BkPlugin plugin, String langKey, String permission) {
        super(plugin, langKey, permission);
    }

    @Override
    public boolean onCommand(CommandSender commandSender, Command command, String label, String[] args) {

        BkMito plugin = BkMito.getInstance();
        if (args.length == 2) {
            if (args[0].equalsIgnoreCase(getPlugin().getLangFile().get("commands.mito.subcommands.npc.command"))) {
                if (!commandSender.hasPermission("bkmito.admin")) {
                    commandSender.sendMessage(getPlugin().getLangFile().get("error.no-permission"));
                    return true;
                }

                if (args[1].equalsIgnoreCase(getPlugin().getLangFile().get("commands.mito.subcommands.npc.subcommands.location.command"))) {
                    Configuration config = getPlugin().getConfigManager().getConfig();
                    config.setLocation("mito-npc.npc.location", ((Player) commandSender).getLocation());
                    config.saveToFile();
                    commandSender.sendMessage(getPlugin().getLangFile().get("info.location-set"));
                } else if (args[1].equalsIgnoreCase(getPlugin().getLangFile().get("commands.mito.subcommands.npc.subcommands.update.command"))) {
                    Mito mito = plugin.getMitoManager().getCurrentMito();
                    if (mito != null) {
                        BkMito.getInstance().getNpcManager().setMitoNpc(mito, UpdateReason.UPDATE_ALL);
                    } else {
                        commandSender.sendMessage(getPlugin().getLangFile().get("error.invalid-mito"));
                    }
                }
            } else {
                if (args[0].equalsIgnoreCase(getPlugin().getLangFile().get("commands.mito.subcommands.stats.command"))) {
                    MitoManager manager = BkMito.getInstance().getMitoManager();
                    Mito mito = manager.getOldMito(args[1]);
                    if (mito == null) mito = manager.getCurrentMito();
                    if (mito != null) {
                        if (!commandSender.hasPermission("bkmito.stats")) {
                            commandSender.sendMessage(getPlugin().getLangFile().get("error.no-permission"));
                            return true;
                        }
                        Page statsMenu = plugin.getMitoManager().getMitoStatsMenu().buildStatsMenu(mito, ((Player) commandSender));
                        if (statsMenu == null) {
                            commandSender.sendMessage(plugin.getLangFile().get("error.no-stats").replace("{player}", args[1]));
                        } else {
                            statsMenu.openGui((Player) commandSender);
                        }
                    } else {
                        commandSender.sendMessage(plugin.getLangFile().get("error.no-stats").replace("{player}", args[1]));
                    }
                } else {
                    Player targetPlayer = Bukkit.getPlayer(args[1]);
                    if (targetPlayer != null) {
                        if (args[0].equalsIgnoreCase(getPlugin().getLangFile().get("commands.mito.subcommands.setar.command"))) {
                            if (!commandSender.hasPermission("bkmito.admin")) {
                                commandSender.sendMessage(getPlugin().getLangFile().get("error.no-permission"));
                                return true;
                            }
                            Mito mito = plugin.getMitoManager().setMito(targetPlayer);
                            if (mito != null) {
                                commandSender.sendMessage(getPlugin().getLangFile().get("info.mito-set").replace("{player}", targetPlayer.getName()));
                            } else {
                                commandSender.sendMessage(getPlugin().getLangFile().get("error.not-set"));
                            }
                        }
                    } else {
                        commandSender.sendMessage(getPlugin().getLangFile().get("error.invalid-player").replace("{player}", args[1]));
                    }
                }
            }
        } else if (args.length == 1) {
            if (args[0].equalsIgnoreCase(getPlugin().getLangFile().get("commands.mito.subcommands.stats.command"))) {
                MitoManager manager = BkMito.getInstance().getMitoManager();
                Mito currentMito = manager.getCurrentMito();
                if (currentMito != null) {
                    if (!commandSender.hasPermission("bkmito.stats")) {
                        commandSender.sendMessage(getPlugin().getLangFile().get("error.no-permission"));
                        return true;
                    }
                    OfflinePlayer player = (OfflinePlayer) commandSender;

                    Page mitoPage = new Page(getPlugin(), getPlugin().getAnimatorManager(), new GUI(BkMito.getInstance().getLangFile().get(player, "gui-buttons.mitos-list.current-mito.title"), Rows.THREE), 1);
                    mitoPage.pageSetHead(
                            13,
                            Bukkit.getOfflinePlayer(currentMito.getUUID()),
                            getPlugin().getLangFile().get(player, "gui-buttons.mitos-list.current-mito.name").replace("{player}", currentMito.getPlayerName()),
                            getPlugin().getLangFile().getStringList(player, "gui-buttons.mitos-list.current-mito.description"),
                            player.getName().toLowerCase() + "-mitos-main-mito-" + currentMito.getPlayerName().toLowerCase(),
                            me.bkrmt.bkmito.Mito.mitoClickStatsResponse(mitoPage, currentMito)
                    );

                    PagedList mitosList = null;
                    if (manager.getCachedMitos().size() > 0) {
                        ArrayDeque<PagedItem> mitos = manager.getCachedMitos().values().stream().map(mito -> (PagedItem) mito).collect(Collectors.toCollection(ArrayDeque::new));
                        mitosList = new PagedList(plugin, (Player) commandSender, "mitos-list", mitos);
                        mitosList
                                .setGuiRows(Rows.THREE)
                                .setListRows(1)
                                .setStartingSlot(11)
                                .setListRowSize(5)
                                .setButtonSlots(9, 17)
                                .setGuiTitle(BkMito.getInstance().getLangFile().get(player, "gui-buttons.mitos-list.old-mitos.title"))
                                .buildMenu();
                        Page firstPage = mitosList.getPages().get(0);
                        firstPage.setBackMenuButton(
                                9,
                                new ItemBuilder(XMaterial.RED_WOOL)
                                        .setName(plugin.getLangFile().get("gui-buttons.mitos-list.current-mito-button.name"))
                                        .setLore(plugin.getLangFile().getStringList("gui-buttons.mitos-list.current-mito-button.description")),
                                player.getName().toLowerCase() + "-back-menu-old-mitos-list",
                                event1 -> {
                                    MenuSound.BACK.play(event1.getWhoClicked());
                                    firstPage.setSwitchingPages(true);
                                    mitoPage.openGui((Player) event1.getWhoClicked());
                                }
                        );
                    }

                    if (mitosList != null) {
                        Page firstPage = mitosList.getPages().get(0);
                        mitoPage.pageSetItem(
                                17,
                                new ItemBuilder(XMaterial.GREEN_WOOL)
                                        .setName(plugin.getLangFile().get("gui-buttons.mitos-list.old-mitos-button.name"))
                                        .setLore(plugin.getLangFile().getStringList("gui-buttons.mitos-list.old-mitos-button.description")),
                                player.getName().toLowerCase() + "-old-mitos-button",
                                event -> {
                                    mitoPage.addNextMenu(firstPage);
                                    firstPage.addPreviousMenu(mitoPage);
                                    mitoPage.setSwitchingPages(true);
                                    firstPage.openGui((Player) event.getWhoClicked());
                                }
                        );
                    }
                    mitoPage.openGui((Player) player);
                } else {
                    commandSender.sendMessage(plugin.getLangFile().get("error.invalid-mito"));
                }
            } else {
                sendUsage(commandSender);
            }
        } else {
            sendUsage(commandSender);
        }
        return true;
    }
}