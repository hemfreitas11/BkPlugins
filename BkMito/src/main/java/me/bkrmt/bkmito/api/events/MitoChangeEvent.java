package me.bkrmt.bkmito.api.events;

import me.bkrmt.bkmito.api.Mito;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

public class MitoChangeEvent extends Event implements Cancellable {
    private static final HandlerList handlers = new HandlerList();

    private boolean cancelled;
    private final Mito newMito;
    private final Mito oldMito;

    public MitoChangeEvent(Mito newMito, Mito oldMito) {
        this.newMito = newMito;
        this.oldMito = oldMito;
    }

    public Mito getNewMito() {
        return newMito;
    }

    public Mito getOldMito() {
        return oldMito;
    }

    @Override
    public HandlerList getHandlers() {
        return handlers;
    }

    public static HandlerList getHandlerList() {
        return handlers;
    }

    @Override
    public boolean isCancelled() {
        return cancelled;
    }

    @Override
    public void setCancelled(boolean cancelled) {
        this.cancelled = cancelled;
    }
}
