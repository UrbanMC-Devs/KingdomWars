package net.urbanmc.kingdomwars.command.subs;

import net.urbanmc.kingdomwars.KingdomWars;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

import com.palmergames.bukkit.towny.TownyMessaging;
import com.palmergames.bukkit.towny.object.Nation;

import net.urbanmc.kingdomwars.util.TownyUtil;
import net.urbanmc.kingdomwars.data.war.War;
import net.urbanmc.kingdomwars.event.WarTruceEvent;

public class Truce {

	public Truce(Player p, String label, final KingdomWars plugin) {
		if (!p.hasPermission("kingdomwars.truce")) {
			p.sendMessage(ChatColor.RED + "You do not have permission to do this!");
			return;
		}

		Nation nation = TownyUtil.getNation(p);

		if (nation == null) {
			p.sendMessage(ChatColor.RED + "You are not in a nation!");
			return;
		}

		if (!plugin.getWarManager().inWar(nation)) {
			p.sendMessage(ChatColor.RED + "You are not in a war!");
			return;
		}

		War war = plugin.getWarManager().getWar(nation);

		String nationName = nation.getName();

		int declaringPoints = war.getDeclaringPoints(), declaredPoints = war.getDeclaredPoints();

		if (war.isDeclaringNation(nationName) && declaringPoints > declaredPoints ||
				!war.isDeclaringNation(nationName) && declaredPoints > declaringPoints) {
			p.sendMessage(
					ChatColor.RED + "Your nation is winning the war! You must do /" + label + " end instead" + ".");
			return;
		} else if (declaringPoints == declaredPoints && war.isDeclaringNation(nationName)) {
			p.sendMessage(
					ChatColor.RED + "You are the nation that started the war! You must do /" + label + " end instead" +
							".");
			return;
		}

		Nation receivingNation = war.getOtherNation(nation);

		WarTruceEvent event = new WarTruceEvent(war);
		Bukkit.getPluginManager().callEvent(event);

		if (event.isCancelled())
			return;

		TownyMessaging.sendNationMessage(nation, "Your nation has requested a truce with " + receivingNation.getName
				());
		TownyUtil.truceQuestion(plugin, receivingNation, nation);
	}
}
