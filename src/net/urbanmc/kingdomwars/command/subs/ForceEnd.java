package net.urbanmc.kingdomwars.command.subs;

import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

import com.palmergames.bukkit.towny.object.Nation;

import net.urbanmc.kingdomwars.util.TownyUtil;
import net.urbanmc.kingdomwars.WarUtil;
import net.urbanmc.kingdomwars.data.war.War;

public class ForceEnd {

	public ForceEnd(Player p, String[] args) {
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

		if (!WarUtil.inWar(nation)) {
			p.sendMessage(ChatColor.RED + "That nation is not in a war!");
			return;
		}

		War war = WarUtil.getWar(nation);

		WarUtil.end(war);
		p.sendMessage(ChatColor.GOLD + "Ended war.");

		Nation otherNation = war.getOtherNation(nation);

		TownyUtil.sendNationMessage(nation,
				"Your war against " + otherNation.getName() + " has been ended by an admin.");
		TownyUtil.sendNationMessage(otherNation,
				"Your war against " + nation.getName() + " has been ended by an admin.");
	}
}
