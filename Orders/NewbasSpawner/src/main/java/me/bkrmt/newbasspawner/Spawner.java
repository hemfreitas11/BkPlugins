package me.bkrmt.newbasspawner;

import me.bkrmt.bkcore.PagedItem;
import me.bkrmt.bkcore.PagedList;
import me.bkrmt.bkcore.Utils;
import me.bkrmt.bkcore.bkgui.MenuSound;
import me.bkrmt.bkcore.bkgui.event.ElementResponse;
import me.bkrmt.bkcore.bkgui.item.ItemBuilder;
import me.bkrmt.bkcore.bkgui.menus.ConfirmationMenu;
import me.bkrmt.bkcore.bkgui.page.Page;
import me.bkrmt.bkcore.bkgui.page.PageItem;
import me.bkrmt.bkcore.xlibs.XMaterial;
import net.milkbowl.vault.economy.EconomyResponse;
import org.bukkit.ChatColor;
import org.bukkit.block.Block;
import org.bukkit.block.CreatureSpawner;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;

import java.util.ArrayList;
import java.util.List;

public class Spawner implements PagedItem {
    private final List<Group> groups;
    private final String displayName;
    private final String mob;
    private List<String> lore;
    private Block targetBlock;
    private Page confirmMenu;

    public Spawner(String name, String mob, List<Group> groups, List<String> lore) {
        this.groups = groups;
        this.mob = mob;
        this.displayName = name;
        confirmMenu = null;
        this.lore = new ArrayList<>();
        for (String line : lore) {
            this.lore.add(NewbasSpawner.getInstance().translatePrices(line, groups));
        }
    }

    public Page getConfirmMenu() {
        return confirmMenu;
    }

    public void setLore(List<String> lore) {
        this.lore = lore;
    }

    public Block getTargetBlock() {
        return targetBlock;
    }

    public void setTargetBlock(Block targetBlock) {
        this.targetBlock = targetBlock;
    }

    public List<Group> getGroups() {
        return groups;
    }

    public String getMob() {
        return mob;
    }

    @Override
    public String getDisplayName(PagedList list, Page currentPage) {
        return displayName.replace("{spawner}", Utils.capitalize(mob.replace("_", " ")));
    }

    @Override
    public int getSlot() {
        return -1;
    }

    @Override
    public int getPage() {
        return -1;
    }
///////
    @Override
    public void setIgnorePage(boolean ignorePage) {

    }

    @Override
    public void setIgnoreSlot(boolean ignoreSlot) {

    }

    @Override
    public boolean isIgnorePage() {
        return false;
    }

    @Override
    public boolean isIgnoreSlot() {
        return false;
    }

    @Override
    public void setPage(int slot) {
    }

    @Override
    public void setSlot(int slot) {
    }
//////
    @Override
    public List<String> getLore(PagedList list, Page currentPage) {
        return lore;
    }

    @Override
    public Object getDisplayItem(PagedList list, Page currentPage) {
        return XMaterial.SPAWNER.parseItem();
    }

    @Override
    public ElementResponse getElementResponse(PagedList list, Page currentPage) {
        return event -> {
            NewbasSpawner plugin = NewbasSpawner.getInstance();
            Player player = (Player) event.getWhoClicked();
            buildConfirmMenu(currentPage, event, plugin, player);
            
            currentPage.setSwitchingPages(true);
            confirmMenu.addPreviousMenu(currentPage);
            MenuSound.CLICK.play(player);
            confirmMenu.openGui(player);
        };
    }

    @Override
    public void assignID(long id) {

    }

    @Override
    public long getID() {
        return 0;
    }

    private void buildConfirmMenu(Page currentPage, InventoryClickEvent event, NewbasSpawner plugin, Player player) {
        PageItem confirmButton = new PageItem(
                new ItemBuilder(XMaterial.GREEN_STAINED_GLASS_PANE).setName(plugin.getLangFile().get("mensagens.confirmar.aceitar")),
                player.getName().toLowerCase() + "-" + getMob() + "-confirm-button",
                false,
                null,
                confirmEvent -> {
                    player.closeInventory();
                    EntityType newType = EntityType.valueOf(getMob().toUpperCase());
                    double playerMoney = plugin.getEconomy().getBalance(player);
                    double cost = plugin.getCost(player, newType.toString().toLowerCase());
                    if (playerMoney >= cost) {
                        EconomyResponse response = plugin.getEconomy().withdrawPlayer(player, cost);
                        if (response.transactionSuccess()) {
                            EntityType oldType = ((CreatureSpawner) getTargetBlock().getState()).getSpawnedType();
                            plugin.getSpawnerManager().setSpawner(getTargetBlock(), newType, player);
                            MenuSound.SUCCESS.play(player);
                            player.sendMessage(
                                    plugin.getLangFile().get("mensagens.spawner-trocado")
                                            .replace("{player}", player.getName())
                                            .replace("{preco}", String.valueOf((int) cost))
                                            .replace("{antigo}", oldType.toString())
                                            .replace("{novo}", newType.toString())
                            );
                        } else {
                            MenuSound.ERROR.play(player);
                            player.sendMessage(String.format("Aconteceu um erro: %s", response.errorMessage));
                        }
                    } else {
                        MenuSound.ERROR.play(player);
                        player.sendMessage(plugin.getLangFile().get("mensagens.sem-dinheiro"));
                    }
                }
        );
        PageItem declineButton = new PageItem(
                new ItemBuilder(XMaterial.RED_STAINED_GLASS_PANE).setName(plugin.getLangFile().get("mensagens.confirmar.recusar")),
                player.getName().toLowerCase() + "-" + getMob() + "-decline-button",
                false,
                null,
                declineEvent -> {
                    confirmMenu.setWipeOnlySelf(true);
                    currentPage.openGui(player);
                    MenuSound.WARN.play(player);
                    currentPage.displayItemMessage(event.getSlot(), 3, ChatColor.RED, plugin.getLangFile().get("mensagens.confirmar.cancelado"), null);
                }
        );
        PageItem infoButton = new PageItem(
                new ItemBuilder(XMaterial.WRITABLE_BOOK).setName(plugin.getLangFile().get("mensagens.confirmar.descricao")),
                player.getName().toLowerCase() + "-" + getMob() + "-info-button",
                false,
                null,
                infoEvent -> {
                    MenuSound.CLICK.play(player);
                }
        );
        confirmMenu = new ConfirmationMenu(
                plugin.getLangFile().get("mensagens.confirmar.titulo"),
                infoButton,
                confirmButton,
                declineButton
        ).getMenu();
    }
}
