package net.urbanmc.kingdomwars.event;

import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

public class WarDeclareEvent extends Event implements Cancellable {
    private static final HandlerList handlers = new HandlerList();
    private boolean cancelled = false;
    private String declared, declaring;
    private int timeTillWar;
    
    public WarDeclareEvent(String declaring, String declared, int timeTillWar) {
        this.declared = declared;
        this.declaring = declaring;
        this.timeTillWar = timeTillWar;
    }

    @Override
    public HandlerList getHandlers() {
        return handlers;
    }

    public static HandlerList getHandlerList() {
        return handlers;
    }

    public String getDeclared() { return declared; }

    public String getDeclaring() { return declaring; }

    public int getTimeTillWar() { return timeTillWar; }

    public void setTimeTillWar(int time) { timeTillWar = time; }


    @Override
    public boolean isCancelled() {
        return cancelled;
    }

    @Override
    public void setCancelled(boolean b) {
        cancelled = b;
    }
}
