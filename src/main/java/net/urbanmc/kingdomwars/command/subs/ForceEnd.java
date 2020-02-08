package net.urbanmc.kingdomwars.command.subs;

import net.urbanmc.kingdomwars.KingdomWars;
import net.urbanmc.kingdomwars.data.PreWar;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

import com.palmergames.bukkit.towny.object.Nation;

import net.urbanmc.kingdomwars.util.TownyUtil;
import net.urbanmc.kingdomwars.data.war.War;

public class ForceEnd {

	public ForceEnd(Player p, String[] args, final KingdomWars plugin) {
		if (!p.hasPermission("kingdomwars.forceend")) {
			p.sendMessage(ChatColor.RED + "You do not have permission to do this!");
			return;
		}

		if (args.length == 1) {
			p.sendMessage(ChatColor.RED + "Please specify a nation whose war to end.");
			return;
		}

		Nation nation = TownyUtil.getNation(args[1]);

		if (nation == null) {
			p.sendMessage(ChatColor.RED + "You have not specified a valid nation.");
			return;
		}

		if (plugin.getWarManager().alreadyScheduledForWar(nation.getName())) {
			PreWar preWar = plugin.getWarManager().getPreWar(nation.getName());

			if (preWar.isMainNation(nation.getName())) {
				p.sendMessage(ChatColor.RED + "This nation is scheduled to be an ally to another nation in a war!");
				return;
			}

			p.sendMessage(ChatColor.GOLD + "You have cancelled the scheduled war.");

			preWar.cancelTask();

			Nation otherNation = TownyUtil.getNation(preWar.getOtherNation(nation.getName()));

			if (otherNation != null)
			TownyUtil.sendNationMessage(nation,
					"Your war against " + otherNation.getName() + " has been cancelled by an admin.");

			TownyUtil.sendNationMessage(otherNation,
					"Your war against " + nation.getName() + " has been cancelled by an admin.");

			return;
		}


		if (!plugin.getWarManager().inWar(nation)) {
			p.sendMessage(ChatColor.RED + "That nation is not in a war!");
			return;
		}

		War war = plugin.getWarManager().getWar(nation);

		plugin.getWarManager().forceEnd(war);

		p.sendMessage(ChatColor.GOLD + "Ended war.");

		Nation otherNation = war.getOtherNation(nation);

		TownyUtil.sendNationMessage(nation,
				"Your war against " + otherNation.getName() + " has been ended by an admin.");
		TownyUtil.sendNationMessage(otherNation,
				"Your war against " + nation.getName() + " has been ended by an admin.");
	}
}
