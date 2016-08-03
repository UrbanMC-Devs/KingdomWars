package net.urbanmc.kingdomwars.command.subs;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

import com.palmergames.bukkit.towny.TownyMessaging;
import com.palmergames.bukkit.towny.exceptions.NotRegisteredException;
import com.palmergames.bukkit.towny.object.Nation;
import com.palmergames.bukkit.towny.object.TownyUniverse;

import net.urbanmc.kingdomwars.WarUtil;
import net.urbanmc.kingdomwars.data.War;
import net.urbanmc.kingdomwars.event.WarStartEvent;

public class Start {

	public Start(Player p, String[] args) {
		if (!p.hasPermission("kingdomwars.start")) {
			p.sendMessage(ChatColor.RED + "You do not have permission to do this!");
			return;
		}

		Nation nation1;

		try {
			nation1 = TownyUniverse.getDataSource().getResident(p.getName()).getTown().getNation();
		} catch (NotRegisteredException ex) {
			p.sendMessage(ChatColor.RED + "You are not in a nation!");
			return;
		}

		if (args.length == 1) {
			p.sendMessage(ChatColor.RED + "Please specify a nation to declare war against.");
			return;
		}

		Nation nation2;

		try {
			nation2 = TownyUniverse.getDataSource().getNation(args[1]);
		} catch (NotRegisteredException ex) {
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

		War war = WarUtil.createWar(nation1, nation2);

		WarStartEvent event = new WarStartEvent(war);
		Bukkit.getPluginManager().callEvent(event);

		if (event.isCancelled())
			return;

		WarUtil.startWar(war);

		TownyMessaging.sendNationMessage(nation1, "Your nation has declared war against " + nation2.getName() + "!");
		TownyMessaging.sendNationMessage(nation2, nation1.getName() + " has declared war against your nation!");
	}
}
