package me.bkrmt.newbasspawner;

import me.bkrmt.bkcore.PagedItem;
import me.bkrmt.bkcore.PagedList;
import me.bkrmt.bkcore.bkgui.gui.Rows;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.List;

public class MenuTroca {
    private PagedList menuList;
    private final Player player;
    private final Block targetBlock;
    private final NewbasSpawner plugin;

    public MenuTroca(NewbasSpawner plugin, Player player, Block block) {
        this.plugin = plugin;
        this.player = player;
        this.targetBlock = block;
        buildMenu();
    }

    private void buildMenu() {
        ArrayDeque<PagedItem> spawners = new ArrayDeque<>();
        for (Spawner spawner : plugin.getSpawnerManager().getSpawners().values().toArray(new Spawner[0])) {
            spawner.setTargetBlock(targetBlock);
            List<String> translatedLore = new ArrayList<>();
            for (String line : spawner.getLore(null, null)) {
                translatedLore.add(line
                    .replace("{player}", player.getName())
                    .replace("{mais-barato}", String.valueOf(plugin.getCost(player, spawner.getMob())))
                );
            }
            spawner.setLore(translatedLore);
            spawners.add(spawner);
        }

        menuList = new PagedList(plugin, player, player.getName().toLowerCase() + "-spawner-troca", spawners);
        menuList
                .setStartingSlot(10)
                .setListRowSize(7)
                .setGuiRows(Rows.SIX)
                .setGuiTitle(plugin.getLangFile().get("titulo-do-menu"))
                .setListRows(4)
                .buildMenu();
    }

    public void open() {
        menuList.openPage(0);
    }
}
