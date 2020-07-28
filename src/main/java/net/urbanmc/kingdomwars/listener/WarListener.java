package net.urbanmc.kingdomwars.listener;

import com.palmergames.bukkit.towny.exceptions.NotRegisteredException;
import com.palmergames.bukkit.towny.object.TownBlock;
import com.palmergames.bukkit.towny.object.TownyUniverse;
import net.urbanmc.kingdomwars.manager.ConfigManager;
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
import net.urbanmc.kingdomwars.util.TownyUtil;
import net.urbanmc.kingdomwars.WarBoard;
import net.urbanmc.kingdomwars.data.war.War;
import net.urbanmc.kingdomwars.event.WarPointAddEvent;

public class WarListener implements Listener {

	private KingdomWars plugin;

	public WarListener(KingdomWars plugin) {
		this.plugin = plugin;
	}

	@EventHandler
	public void onDisallowedPVP(DisallowedPVPEvent e) {
		Player attacker = e.getAttacker();
		Player defender = e.getDefender();

		if (attacker == null || defender == null)
			return;

		Nation attackerNation = TownyUtil.getNation(attacker);
		Nation defenderNation = TownyUtil.getNation(defender);

		if (attackerNation == null || defenderNation == null)
			return;

		if (TownyUtil.isSameNation(attackerNation, defenderNation))
			return;

		War war = plugin.getWarManager().getWar(attackerNation);

		if (war == null)
			return;

		if (!war.isInWar(defenderNation.getName()) || war.isOnSameSide(attackerNation.getName(), defenderNation.getName()))
			return;

		if (plugin.getWarManager().checkForceEnd(war))
			return;

		TownBlock tB = TownyUniverse.getTownBlock(e.getAttacker().getLocation());
		if (tB != null && tB.hasTown()) {
			try {
				if (tB.getTown().getMayor().isNPC()) {
					return;
				}
			} catch (NotRegisteredException __) {
			}
		}

		if (plugin.hasEssentials()) {
			User attackerUser = plugin.getEssentials().getUser(attacker);
			User defenderUser = plugin.getEssentials().getUser(defender);

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
		TownyUtil.damageCancelled(plugin, e.getDamager(), e.getEntity());
	}

	@EventHandler
	public void onPlayerDeath(PlayerDeathEvent e) {
		Player p = e.getEntity();
		Player killer = p.getKiller();

		if (killer == null)
			return;

		Nation victimNation = TownyUtil.getNation(p);
		Nation killerNation = TownyUtil.getNation(killer);

		if (victimNation == null || killerNation == null)
			return;

		if (TownyUtil.isSameNation(victimNation, killerNation))
			return;

		War war = plugin.getWarManager().getWar(victimNation);

		if (war == null || !war.isInWar(killerNation.getName()) || war.isOnSameSide(victimNation.getName(), killerNation.getName()))
			return;

		if (KingdomWars.playerIsJailed(p))
			return;

		boolean killerNationDeclaring = war.isOnDeclaringSide(killerNation.getName());

		String nationPointName = killerNationDeclaring ? war.getDeclaringNation() : war.getDeclaredNation();

		Nation recievingNation = TownyUtil.getNation(nationPointName);

		WarPointAddEvent event = new WarPointAddEvent(war, recievingNation, 1);
		Bukkit.getPluginManager().callEvent(event);

		if (event.isCancelled()) return;

		if (TownyUtil.isNationKing(p) && !war.isAlly(victimNation.getName())) {
			// Add the kill to the war
			war.addPoints(recievingNation, 1);
			plugin.getWarManager().win(recievingNation, victimNation);
		}
		else {
			war.addPoints(recievingNation, 1);
			WarBoard.updateBoard(war);
			plugin.getWarManager().checkWin(war);
			plugin.getWarManager().saveCurrentWars();
		}
	}

	@EventHandler
	public void onPlayerJoin(PlayerJoinEvent e) {
		WarBoard.showBoard(plugin, e.getPlayer());
	}
}
