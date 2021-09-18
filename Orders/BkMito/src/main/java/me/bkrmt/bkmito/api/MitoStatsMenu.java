package me.bkrmt.bkmito.api;

import me.bkrmt.bkcore.bkgui.page.Page;
import org.bukkit.entity.Player;

public interface MitoStatsMenu {
    Page buildStatsMenu(Player mitoPlayer, Player targetPlayer);

    Page buildStatsMenu(Mito mito, Player targetPlayer);
}
