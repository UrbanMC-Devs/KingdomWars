package net.urbanmc.kingdomwars.listener;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerJoinEvent;

import com.earth2me.essentials.User;
import com.palmergames.bukkit.towny.event.DisallowedPVPEvent;
import com.palmergames.bukkit.towny.object.Nation;

import net.urbanmc.kingdomwars.KingdomWars;
import net.urbanmc.kingdomwars.TownyUtil;
import net.urbanmc.kingdomwars.WarBoard;
import net.urbanmc.kingdomwars.WarUtil;
import net.urbanmc.kingdomwars.data.war.War;
import net.urbanmc.kingdomwars.event.WarPointAddEvent;

public class WarListener implements Listener {

	@EventHandler
	public void onDisallowedPVP(DisallowedPVPEvent e) {
		Player attacker = e.getAttacker();
		Player defender = e.getDefender();

		if (attacker == null || defender == null)
			return;

		Nation nation1 = TownyUtil.getNation(attacker);
		Nation nation2 = TownyUtil.getNation(defender);

		if (nation1 == null || nation2 == null)
			return;

		War war = WarUtil.getWar(nation1);

		if (war == null)
			return;

		if (!war.getDeclaringNation().equals(nation2.getName()) && !war.getDeclaredNation().equals(nation2.getName()))
			return;

		if (nation1.getName().equals(nation2.getName()))
			return;

		if (WarUtil.checkForceEnd(war))
			return;

		if (KingdomWars.hasEssentials()) {
			User attackerUser = KingdomWars.getEssentials().getUser(attacker);
			User defenderUser = KingdomWars.getEssentials().getUser(defender);

			if (attackerUser.isGodModeEnabled()) {
				attackerUser.setGodModeEnabled(false);
			}

			if (defenderUser.isGodModeEnabled()) {
				defenderUser.setGodModeEnabled(false);
			}
		}

		if (attacker.getAllowFlight() || attacker.isFlying()) {
			attacker.setFlying(false);
			attacker.setAllowFlight(false);
		}

		if (defender.getAllowFlight() || defender.isFlying()) {
			defender.setFlying(false);
			defender.setAllowFlight(false);
		}

		e.setCancelled(true);
	}

	@EventHandler
	public void onEntityDamage(EntityDamageByEntityEvent e) {
		TownyUtil.damageCancelled(e.getDamager(), e.getEntity());
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

		if (nation1.getName().equals(nation2.getName()))
			return;

		if (WarUtil.checkForceEnd(war))
			return;

		WarPointAddEvent event = new WarPointAddEvent(war, nation2, 1);
		Bukkit.getPluginManager().callEvent(event);

		if (event.isCancelled())
			return;

		if (TownyUtil.isNationKing(p)) {
			WarUtil.win(nation2, nation1, KingdomWars.getFinishAmount());
			return;
		}

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
