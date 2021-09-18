package me.bkrmt.bkmito.api;

import me.bkrmt.bkmito.BkMito;
import org.bukkit.OfflinePlayer;

import java.util.Map;
import java.util.UUID;

public interface MitoManager {
    Map<UUID, Mito> getCachedMitos();

    MitoStatsMenu getMitoStatsMenu();

    Mito setMito(OfflinePlayer player);

    Mito getCurrentMito();

    Mito getOldMito(UUID uuid);

    Mito getOldMito(String name);

    void sendNewMitoMessage(Mito newMito, Mito oldMito);

    BkMito getPlugin();

    void saveMitos();
}
