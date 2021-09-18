package me.bkrmt.bkmito;

import me.bkrmt.bkcore.Utils;
import me.bkrmt.bkcore.clickablemessage.Button;
import me.bkrmt.bkcore.clickablemessage.ClickableMessage;
import me.bkrmt.bkcore.clickablemessage.Hover;
import me.bkrmt.bkcore.config.Configuration;
import me.bkrmt.bkmito.api.UpdateReason;
import me.bkrmt.bkmito.api.events.MitoChangeEvent;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.chat.HoverEvent;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;

import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

class MitoManager implements me.bkrmt.bkmito.api.MitoManager {
    private final BkMito plugin;
    private me.bkrmt.bkmito.api.Mito currentMito;

    private final MitoStatsMenu mitoStatsMenu;

    private Configuration mitosConfig;

    private final Map<UUID, me.bkrmt.bkmito.api.Mito> cachedMitos = new HashMap<>();

    MitoManager(BkMito plugin) {
        this.plugin = plugin;
        this.mitoStatsMenu = new MitoStatsMenu();
        currentMito = null;
        File mitosFile = BkMito.getInstance().getFile("", "mitos.yml");
        if (!mitosFile.exists()) {
            try {
                if (mitosFile.createNewFile()) {
                    mitosConfig = new Configuration(BkMito.getInstance(), mitosFile);
                } else {
                    new IllegalStateException("The file \"mitos.yml\" could not be created.").printStackTrace();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else {
            mitosConfig = new Configuration(BkMito.getInstance(), mitosFile);
            loadMitos();
        }
    }

    @Override
    public Map<UUID, me.bkrmt.bkmito.api.Mito> getCachedMitos() {
        return cachedMitos;
    }

    @Override
    public MitoStatsMenu getMitoStatsMenu() {
        return mitoStatsMenu;
    }

    @Override
    public me.bkrmt.bkmito.api.Mito setMito(OfflinePlayer player) {
        me.bkrmt.bkmito.api.Mito cachedMito = getOldMito(player.getUniqueId());
        if (cachedMito == null) cachedMito = getOldMito(player.getName());
        if (cachedMito == null) {
            if (currentMito == null || currentMito.getUUID() != player.getUniqueId()) {
                Mito newMito = new Mito(player, getMitoStats(player.getUniqueId().toString()));
                MitoChangeEvent event = new MitoChangeEvent(newMito, currentMito);
                Bukkit.getPluginManager().callEvent(event);
                if (!event.isCancelled()) {
                    removeCurrentMito();
                    setCurrentMito(newMito);
                    BkMito.getInstance().getNpcManager().setMitoNpc(currentMito, UpdateReason.UPDATE_ALL);
                    return currentMito;
                }
            }
        } else {
            MitoChangeEvent event = new MitoChangeEvent(cachedMito, currentMito);
            Bukkit.getPluginManager().callEvent(event);
            if (!event.isCancelled()) {
                cachedMito.getStats().setMitoTimes(cachedMito.getStats().getMitoTimes() + 1);
                cachedMito.getStats().setStartTime(System.currentTimeMillis());
                removeCurrentMito();
                cachedMito.getStats().setEndTime(0);
                cachedMito.getStats().setKills(0);
                setCurrentMito(cachedMito);
                cachedMitos.remove(cachedMito.getUUID());
                mitosConfig.set(cachedMito.getUUID().toString(), null);
                BkMito.getInstance().getNpcManager().setMitoNpc(currentMito, UpdateReason.UPDATE_ALL);
                return currentMito;
            }
        }
        return null;
    }

    private void setCurrentMito(me.bkrmt.bkmito.api.Mito newMito) {
        runPermissionCommands("permission-commands.set", newMito);
        currentMito = newMito;
    }

    private void removeCurrentMito() {
        if (currentMito != null) {
            runPermissionCommands("permission-commands.unset", currentMito);
            currentMito.getStats().setEndTime(System.currentTimeMillis());
            cachedMitos.put(currentMito.getUUID(), currentMito);
        }
    }

    private void runPermissionCommands(String s, me.bkrmt.bkmito.api.Mito mito) {
        List<String> commands = getPlugin().getConfigManager().getConfig().getStringList(s);
        for (int i = 0; i < commands.size(); i++) {
            int index = i;
            Bukkit.getScheduler().runTaskLater(plugin, () -> {
                String command = commands.get(index)
                        .replace("{mito}", mito.getPlayerName())
                        .replace("/", "");
                plugin.getServer().dispatchCommand(getPlugin().getServer().getConsoleSender(), command);
            }, 15L * i);
        }
    }

    private void loadMitos() {
        if (mitosConfig.get("current-mito") != null) {
            String uuidString = mitosConfig.getString("current-mito.uuid");
            me.bkrmt.bkmito.api.MitoStats stats = getMitoStats("current-mito");
            currentMito = new Mito(Bukkit.getOfflinePlayer(UUID.fromString(uuidString)), stats);
            plugin.getNpcManager().setMitoNpc(currentMito, UpdateReason.UPDATE_ALL);
        }
        Set<String> uuidKeys = mitosConfig.getKeys(false);
        uuidKeys.remove("current-mito");
        for (String uuidKey : uuidKeys) {
            me.bkrmt.bkmito.api.MitoStats stats = getMitoStats(uuidKey);
            UUID mitoUuid = UUID.fromString(uuidKey);
            cachedMitos.put(mitoUuid, new Mito(Bukkit.getOfflinePlayer(mitoUuid), stats));
        }
    }

    private me.bkrmt.bkmito.api.MitoStats getMitoStats(String key) {
        if (mitosConfig.get(key) == null) {
            return new MitoStats(0, System.currentTimeMillis(), 0, 1);
        } else {
            return new MitoStats(
                    mitosConfig.getInt(key + ".kills"),
                    mitosConfig.getLong(key + ".start-time"),
                    mitosConfig.getLong(key + ".end-time"),
                    mitosConfig.getInt(key + ".mito-times")
            );
        }
    }

    @Override
    public me.bkrmt.bkmito.api.Mito getCurrentMito() {
        return currentMito;
    }

    @Override
    public me.bkrmt.bkmito.api.Mito getOldMito(UUID uuid) {
        return cachedMitos.size() > 0 ? cachedMitos.get(uuid) : null;
    }

    @Override
    public me.bkrmt.bkmito.api.Mito getOldMito(String name) {
        for (me.bkrmt.bkmito.api.Mito mito : cachedMitos.values()) {
            if (mito.getPlayerName().equalsIgnoreCase(name)) {
                return mito;
            }
        }
        return null;
    }

    @Override
    public void sendNewMitoMessage(me.bkrmt.bkmito.api.Mito newMito, me.bkrmt.bkmito.api.Mito oldMito) {
        Configuration config = getPlugin().getConfigManager().getConfig();
        List<String> lineList = config.getStringList("broadcast.new-mito.message")
                .stream().map(line -> {
                    line = line
                            .replace("{new-mito}", newMito.getPlayerName())
                            .replace("{new-mito-kills}", String.valueOf(newMito.getStats().getKills()))
                            .replace("{new-mito-duration}", newMito.getStats().getFormattedDuration())
                            .replace("{new-mito-times}", String.valueOf(newMito.getStats().getKills()));
                    if (oldMito != null) {
                        line = line
                                .replace("{old-mito}", oldMito.getPlayerName())
                                .replace("{old-mito-kills}", String.valueOf(oldMito.getStats().getKills()))
                                .replace("{old-mito-duration}", oldMito.getStats().getFormattedDuration())
                                .replace("{old-mito-times}", String.valueOf(oldMito.getStats().getKills()));
                    }
                    return Utils.translateColor(line);
                }).collect(Collectors.toList());
        ClickableMessage message = new ClickableMessage(lineList)
                .addButton(new Button(
                        config.getString("broadcast.new-mito.buttons.new-mito.text").replace("{new-mito}", newMito.getPlayerName()),
                        new Hover(HoverEvent.Action.SHOW_TEXT, new ComponentBuilder(Utils.translateColor(config.getString("broadcast.new-mito.buttons.new-mito.hover"))).create()),
                        plugin.getLangFile().get("commands.mito.command") + " " + plugin.getLangFile().get("commands.mito.subcommands.stats.command") + " " + newMito.getPlayerName(),
                        "{new-mito-button}"
                ));
        if (oldMito != null) {
            message.addButton(new Button(
                    config.getString("broadcast.new-mito.buttons.old-mito.text").replace("{old-mito}", oldMito.getPlayerName()),
                    new Hover(HoverEvent.Action.SHOW_TEXT, new ComponentBuilder(Utils.translateColor(config.getString("broadcast.new-mito.buttons.old-mito.hover"))).create()),
                    plugin.getLangFile().get("commands.mito.command") + " " + plugin.getLangFile().get("commands.mito.subcommands.stats.command") + " " + oldMito.getPlayerName(),
                    "{old-mito-button}"
            ));
        }
        message.buildMessage()
                .addReceivers(plugin.getHandler().getMethodManager().getOnlinePlayers())
                .sendMessage();
    }

    @Override
    public BkMito getPlugin() {
        return plugin;
    }

    @Override
    public void saveMitos() {
        if (currentMito != null) {
            saveMitoValues(currentMito, "current-mito");
            mitosConfig.set("current-mito.uuid", currentMito.getUUID().toString());
        }
        for (me.bkrmt.bkmito.api.Mito mito : cachedMitos.values()) {
            String uuidKey = mito.getUUID().toString();
            saveMitoValues(mito, uuidKey);
        }
        mitosConfig.saveToFile();
    }

    private void saveMitoValues(me.bkrmt.bkmito.api.Mito mito, String key) {
        mitosConfig.set(key + ".player", mito.getPlayerName());
        mitosConfig.set(key + ".kills", mito.getStats().getKills());
        mitosConfig.set(key + ".start-time", mito.getStats().getStartTime());
        mitosConfig.set(key + ".end-time", mito.getStats().getEndTime());
        mitosConfig.set(key + ".mito-times", mito.getStats().getMitoTimes());
    }
}
