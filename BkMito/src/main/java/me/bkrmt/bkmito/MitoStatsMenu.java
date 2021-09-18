package me.bkrmt.bkmito;

import me.bkrmt.bkcore.bkgui.gui.GUI;
import me.bkrmt.bkcore.bkgui.gui.Rows;
import me.bkrmt.bkcore.bkgui.item.ItemBuilder;
import me.bkrmt.bkcore.bkgui.page.Page;
import me.bkrmt.bkcore.xlibs.XMaterial;
import me.bkrmt.bkmito.api.Mito;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;

import java.util.stream.Collectors;

class MitoStatsMenu implements me.bkrmt.bkmito.api.MitoStatsMenu {
    @Override
    public Page buildStatsMenu(Player mitoPlayer, Player targetPlayer) {
        BkMito plugin = BkMito.getInstance();
        Mito mito;
        if (plugin.getMitoManager().getCurrentMito() != null && plugin.getMitoManager().getCurrentMito().getUUID().equals(mitoPlayer.getUniqueId())) {
            mito = plugin.getMitoManager().getCurrentMito();
        } else {
            mito = plugin.getMitoManager().getOldMito(mitoPlayer.getUniqueId());
        }
        return buildStatsMenu(mito, targetPlayer);
    }

    @Override
    public Page buildStatsMenu(Mito mito, Player targetPlayer) {
        BkMito plugin = BkMito.getInstance();

        if (mito == null) {
            return null;
        }

        OfflinePlayer target = plugin.getServer().getOfflinePlayer(mito.getUUID());

        Page page = new Page(plugin, plugin.getAnimatorManager(), new GUI(plugin.getLangFile().get(target, "info.player-profile-title"), Rows.FIVE), 1);

        boolean currentMito = mito.getUUID().equals(plugin.getMitoManager().getCurrentMito().getUUID());

        ItemBuilder kills = new ItemBuilder(XMaterial.IRON_SWORD)
                .setName(plugin.getLangFile().get(target, "gui-buttons.player-profile.kills.name"))
                .setLore(
                        plugin.getLangFile().getStringList(target, "gui-buttons.player-profile.kills.description" + (currentMito ? ".is-mito" : ".not-mito"))
                                .stream().map(line -> line.replace("{kills}", String.valueOf(mito.getStats().getKills()))).collect(Collectors.toList())
                )
                .hideTags();

        ItemBuilder mitoAmount = new ItemBuilder(XMaterial.NETHER_STAR)
                .setName(plugin.getLangFile().get(target, "gui-buttons.player-profile.mito-amount.name"))
                .setLore(
                        plugin.getLangFile().getStringList(target, "gui-buttons.player-profile.mito-amount.description" + (currentMito ? ".is-mito" : ".not-mito"))
                                .stream().map(line -> line.replace("{mito-amount}", String.valueOf(mito.getStats().getMitoTimes()))).collect(Collectors.toList())
                )
                .hideTags();


        ItemBuilder mitoTime = new ItemBuilder(XMaterial.EXPERIENCE_BOTTLE)
                .setName(plugin.getLangFile().get(target, "gui-buttons.player-profile.mito-time.name"))
                .setLore(
                        plugin.getLangFile().getStringList(target, "gui-buttons.player-profile.mito-time.description" + (currentMito ? ".is-mito" : ".not-mito"))
                                .stream().map(line -> line.replace("{mito-time}", mito.getStats().getFormattedDuration())).collect(Collectors.toList())
                )
                .hideTags();

        page.pageSetHead(
                13,
                target,
                plugin.getLangFile().get(target, "gui-buttons.player-profile.head.name").replace("{player}", mito.getPlayerName()),
                    plugin.getLangFile().getStringList(target, "gui-buttons.player-profile.head.description" + (currentMito ? ".is-mito" : ".not-mito"))
                        .stream().map(line -> line.replace("{player}", mito.getPlayerName())).collect(Collectors.toList()),
                "player-profile-head-button", null
        );
        page.setItemOnXY(5, 3, mitoTime, "player-profile-duels-button", null);
        page.setItemOnXY(4, 3, kills, "player-profile-wins-button", null);
        page.setItemOnXY(6, 3, mitoAmount, "player-profile-defeats-button", null);
        return page;
    }
}
