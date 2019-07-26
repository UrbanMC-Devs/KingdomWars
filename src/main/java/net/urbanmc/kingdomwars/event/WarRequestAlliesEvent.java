package net.urbanmc.kingdomwars.event;

import net.urbanmc.kingdomwars.data.PreWar;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

public class WarRequestAlliesEvent extends Event implements Cancellable {

    private PreWar preWar;
    private int newPrepTime; //In minutes
    private boolean cancelled;

    private final static HandlerList handlers = new HandlerList();

    public WarRequestAlliesEvent(PreWar preWar) {
        this.preWar = preWar;
    }

    @Override
    public boolean isCancelled() {
        return cancelled;
    }

    @Override
    public void setCancelled(boolean b) {
        cancelled = b;
    }

    @Override
    public HandlerList getHandlers() {
        return handlers;
    }

    public static HandlerList getHandlerList() {
        return handlers;
    }

    public PreWar getPreWar() {
        return preWar;
    }

    public int getPreparationTime() { return newPrepTime; }

    public void setPreparationTime(int time) {
        this.newPrepTime = time;
    }
}
