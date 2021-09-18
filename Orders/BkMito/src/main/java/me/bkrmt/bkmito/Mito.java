package me.bkrmt.bkmito;

import me.bkrmt.bkcore.AbstractItem;
import me.bkrmt.bkcore.HeadDisplay;
import me.bkrmt.bkcore.PagedList;
import me.bkrmt.bkcore.bkgui.MenuSound;
import me.bkrmt.bkcore.bkgui.event.ElementResponse;
import me.bkrmt.bkcore.bkgui.item.ItemBuilder;
import me.bkrmt.bkcore.bkgui.page.Page;
import me.bkrmt.bkcore.xlibs.XMaterial;
import me.bkrmt.bkmito.api.MitoStats;
import org.bukkit.ChatColor;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;

import java.util.Collections;
import java.util.UUID;

class Mito extends AbstractItem implements me.bkrmt.bkmito.api.Mito {
    private final String playerName;
    private final UUID uuid;
    private final me.bkrmt.bkmito.api.MitoStats stats;

    Mito(OfflinePlayer player, me.bkrmt.bkmito.api.MitoStats stats) {
        super(-1, -1,
            () -> "",
            () -> Collections.singletonList(""),
            () -> new HeadDisplay(
                player,
                BkMito.getInstance().getLangFile().get("gui-buttons.mitos-list.old-mitos.name").replace("{player}", player.getName()),
                BkMito.getInstance().getLangFile().getStringList("gui-buttons.mitos-list.old-mitos.description")
            )
        );
        this.playerName = player.getName();
        this.uuid = player.getUniqueId();
        this.stats = stats;
    }

    @Override
    public MitoStats getStats() {
        return stats;
    }

    @Override
    public String getPlayerName() {
        return playerName;
    }

    @Override
    public UUID getUUID() {
        return uuid;
    }

    @Override
    public ElementResponse getElementResponse(PagedList list, Page currentPage) {
        return mitoClickStatsResponse(currentPage, this);
    }

    static ElementResponse mitoClickStatsResponse(Page currentPage, me.bkrmt.bkmito.api.Mito mito) {
        return event -> {
            Page statsMenu = BkMito.getInstance().getMitoManager().getMitoStatsMenu().buildStatsMenu(mito, ((Player) event.getWhoClicked()));
            if (statsMenu == null) {
                currentPage.displayItemMessage(event.getSlot(), 2, ChatColor.RED, BkMito.getInstance().getLangFile().get("error.no-stats").replace("{player}", mito.getPlayerName()), null);
            } else {
                statsMenu.addPreviousMenu(currentPage);
                currentPage.addNextMenu(statsMenu);
                statsMenu.setBackMenuButton(
                        18,
                        new ItemBuilder(XMaterial.RED_WOOL).setName("asdasd").setLore("ausdhkajsd"),
                        event.getWhoClicked().getName().toLowerCase() + "-back-menu-stats-mito-" + mito.getPlayerName().toLowerCase(),
                        event1 -> {
                            MenuSound.BACK.play(event.getWhoClicked());
                            statsMenu.setSwitchingPages(true);
                            currentPage.openGui((Player) event.getWhoClicked());
                        }
                );
                currentPage.setSwitchingPages(true);
                MenuSound.CLICK.play(event.getWhoClicked());
                statsMenu.openGui((Player) event.getWhoClicked());
            }
        };
    }
}
