package net.urbanmc.kingdomwars.listeners;

import org.bukkit.entity.Entity;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;

import net.urbanmc.kingdomwars.TownyUtil;

public class WarDamageListener implements Listener {

	@EventHandler
	public void onEntityDamageByEntity(EntityDamageByEntityEvent e) {
		Entity defender = e.getEntity(), attacker = e.getDamager();

		if (e.isCancelled() && TownyUtil.damageCancelled(attacker, defender)) {
			e.setCancelled(false);
		}
	}
}
