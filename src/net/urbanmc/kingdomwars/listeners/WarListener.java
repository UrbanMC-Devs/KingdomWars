package net.urbanmc.kingdomwars.listeners;

import org.bukkit.Bukkit;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerJoinEvent;

import com.palmergames.bukkit.towny.object.Nation;

import net.urbanmc.kingdomwars.TownyUtil;
import net.urbanmc.kingdomwars.WarBoard;
import net.urbanmc.kingdomwars.WarUtil;
import net.urbanmc.kingdomwars.data.War;
import net.urbanmc.kingdomwars.event.WarPointAddEvent;

public class WarListener implements Listener {

	@EventHandler
	public void onEntityDamageByEntity(EntityDamageByEntityEvent e) {
		Entity defender = e.getEntity(), attacker = e.getDamager();

		if (e.isCancelled() && TownyUtil.damageCancelled(attacker, defender)) {
			Nation nation1 = TownyUtil.getNation((Player) attacker);

			if (nation1 == null || !WarUtil.inWar(nation1))
				return;

			Nation nation2 = TownyUtil.getNation((Player) defender);

			if (nation2 == null || !WarUtil.inWar(nation2))
				return;

			War war = WarUtil.getWar(nation1);

			if (!war.getDeclaringNation().equals(nation2.getName())
					&& !war.getDeclaredNation().equals(nation2.getName()))
				return;

			e.setCancelled(false);
		}
	}

	@EventHandler
	public void onPlayerDeath(PlayerDeathEvent e) {
		Player p = e.getEntity();
		Player killer = p.getKiller();

		if (killer == null)
			return;

		Nation nation1 = TownyUtil.getNation(p);

		if (nation1 == null || !WarUtil.inWar(nation1))
			return;

		Nation nation2 = TownyUtil.getNation(killer);

		if (nation2 == null || !WarUtil.inWar(nation2))
			return;

		War war = WarUtil.getWar(nation1);

		if (!war.getDeclaringNation().equals(nation2.getName()) && !war.getDeclaredNation().equals(nation2.getName()))
			return;

		WarPointAddEvent event = new WarPointAddEvent(war, nation2, 1);
		Bukkit.getPluginManager().callEvent(event);

		if (event.isCancelled())
			return;

		war.addPoints(nation2, 1);
		WarUtil.updateWar(war);
		WarBoard.updateBoard(war);
		WarUtil.checkWin(war);
	}

	@EventHandler
	public void onPlayerJoin(PlayerJoinEvent e) {
		WarBoard.showBoard(e.getPlayer());
	}
}
