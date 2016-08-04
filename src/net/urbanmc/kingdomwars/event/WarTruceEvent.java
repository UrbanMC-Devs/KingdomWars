package net.urbanmc.kingdomwars.event;

import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

import net.urbanmc.kingdomwars.data.war.War;

public class WarTruceEvent extends Event implements Cancellable {

	private War war;

	private boolean cancelled = false;

	private static final HandlerList handlers = new HandlerList();

	public WarTruceEvent(War war) {
		this.war = war;
	}

	public War getWar() {
		return this.war;
	}

	@Override
	public boolean isCancelled() {
		return this.cancelled;
	}

	@Override
	public void setCancelled(boolean cancel) {
		this.cancelled = cancel;
	}

	@Override
	public HandlerList getHandlers() {
		return handlers;
	}

	public static HandlerList getHandlerList() {
		return handlers;
	}
}
