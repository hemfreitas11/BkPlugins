package me.bkrmt.bkmito.api;

import java.util.UUID;

public interface Mito {
    MitoStats getStats();

    String getPlayerName();

    UUID getUUID();
}
