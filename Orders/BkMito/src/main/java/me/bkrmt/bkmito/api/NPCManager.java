package me.bkrmt.bkmito.api;

public interface NPCManager {
    void setMitoNpc(Mito mito, UpdateReason reason);

    void wipe(UpdateReason reason);
}