package net.urbanmc.kingdomwars.command.subs;

import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

import com.palmergames.bukkit.towny.object.Nation;

import net.urbanmc.kingdomwars.TownyUtil;
import net.urbanmc.kingdomwars.WarUtil;

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

		WarUtil.end(WarUtil.getWar(nation));
		p.sendMessage(ChatColor.GOLD + "Ended war.");
	}
}
