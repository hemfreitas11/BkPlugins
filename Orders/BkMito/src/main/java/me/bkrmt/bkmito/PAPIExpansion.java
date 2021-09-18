package me.bkrmt.bkmito;

import me.bkrmt.bkmito.api.Mito;
import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import org.bukkit.OfflinePlayer;

public class PAPIExpansion extends PlaceholderExpansion {
    private final BkMito plugin;

    public PAPIExpansion(BkMito plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean persist() {
        return true;
    }

    @Override
    public boolean canRegister() {
        return true;
    }

    @Override
    public String getAuthor() {
        return plugin.getDescription().getAuthors().toString();
    }

    @Override
    public String getIdentifier() {
        return plugin.getName();
    }

    @Override
    public String getVersion() {
        return plugin.getDescription().getVersion();
    }

    @Override
    public String onRequest(OfflinePlayer player, String identifier) {
        String returnValue = "";
        if (player != null && identifier != null) {
            Mito mito = plugin.getMitoManager().getCurrentMito();

            if (mito != null && mito.getUUID().equals(player.getUniqueId())) {
                if (identifier.equalsIgnoreCase("tag")) {
                    returnValue = plugin.getConfigManager().getConfig().getString("mito-tag");
                } else if (identifier.equalsIgnoreCase("kills")) {
                    returnValue = String.valueOf(mito.getStats().getKills());
                } else if (identifier.equalsIgnoreCase("times")) {
                    returnValue = String.valueOf(mito.getStats().getMitoTimes());
                } else if (identifier.equalsIgnoreCase("duration")) {
                    returnValue = String.valueOf(mito.getStats().getFormattedDuration());
                }
            } else {
                Mito oldMito = plugin.getMitoManager().getOldMito(player.getUniqueId());
                if (oldMito != null) {
                    if (identifier.equalsIgnoreCase("kills")) {
                        returnValue = String.valueOf(oldMito.getStats().getKills());
                    } else if (identifier.equalsIgnoreCase("times")) {
                        returnValue = String.valueOf(oldMito.getStats().getMitoTimes());
                    } else if (identifier.equalsIgnoreCase("duration")) {
                        returnValue = String.valueOf(oldMito.getStats().getFormattedDuration());
                    }
                }
            }
        }
        return returnValue;
    }
}
