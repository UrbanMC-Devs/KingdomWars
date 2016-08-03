package net.urbanmc.kingdomwars.command.subs;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

import com.palmergames.bukkit.towny.object.Nation;

import net.urbanmc.kingdomwars.TownyUtil;
import net.urbanmc.kingdomwars.WarUtil;
import net.urbanmc.kingdomwars.data.War;
import net.urbanmc.kingdomwars.event.WarStartEvent;

public class Start {

	public Start(Player p, String[] args) {
		if (!p.hasPermission("kingdomwars.start")) {
			p.sendMessage(ChatColor.RED + "You do not have permission to do this!");
			return;
		}

		Nation nation1 = TownyUtil.getNation(p);

		if (nation1 == null) {
			p.sendMessage(ChatColor.RED + "You are not in a nation!");
			return;
		}

		if (args.length == 1) {
			p.sendMessage(ChatColor.RED + "Please specify a nation to declare war against.");
			return;
		}

		Nation nation2 = TownyUtil.getNation(args[1]);

		if (nation2 == null) {
			p.sendMessage(ChatColor.RED + "You have not specified a valid nation.");
			return;
		}

		if (WarUtil.inWar(nation1)) {
			p.sendMessage(ChatColor.RED + "You are already in a war!");
			return;
		}

		if (WarUtil.inWar(nation2)) {
			p.sendMessage(ChatColor.RED + "That nation is already in a war!");
			return;
		}

		if (TownyUtil.allied(nation1, nation2)) {
			p.sendMessage(ChatColor.RED + "You cannot have a war with your ally!");
			return;
		}

		War war = WarUtil.createWar(nation1, nation2);

		WarStartEvent event = new WarStartEvent(war);
		Bukkit.getPluginManager().callEvent(event);

		if (event.isCancelled())
			return;

		WarUtil.startWar(war);

		TownyUtil.sendNationMessage(nation1, "Your nation has declared war against " + nation2.getName() + "!");
		TownyUtil.sendNationMessage(nation2, nation1.getName() + " has declared war against your nation!");
	}
}
