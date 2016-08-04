package net.urbanmc.kingdomwars.command.subs;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

import com.palmergames.bukkit.towny.object.Nation;

import net.urbanmc.kingdomwars.TownyUtil;
import net.urbanmc.kingdomwars.WarUtil;
import net.urbanmc.kingdomwars.data.war.War;
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

		if (nation1.getName().equals(nation2.getName())) {
			p.sendMessage(ChatColor.RED + "This plug-in does not support civil wars!");
			return;
		}

		if (TownyUtil.allied(nation1, nation2)) {
			p.sendMessage(ChatColor.RED + "You cannot have a war with your ally!");
			return;
		}

		if (WarUtil.hasLast(nation1.getName(), nation2.getName())) {
			p.sendMessage(ChatColor.RED + "You cannot have another war with this nation until " + getLast(nation1)
					+ " seconds from now!");
			return;
		}

		War war = new War(nation1.getName(), nation2.getName());

		WarStartEvent event = new WarStartEvent(war);
		Bukkit.getPluginManager().callEvent(event);

		if (event.isCancelled())
			return;

		WarUtil.startWar(war);

		TownyUtil.sendNationMessage(nation1, "Your nation has declared war against " + nation2.getName() + "!");
		TownyUtil.sendNationMessage(nation2, nation1.getName() + " has declared war against your nation!");
	}

	private String getLast(Nation nation) {
		long time = WarUtil.getLast(nation).getMillis() - System.currentTimeMillis();

		return formatTime(time / 1000);
	}

	public static String formatTime(long time) {
		int days = 0, hours = 0, minutes = 0, seconds = 0;

		while (time >= 86400) {
			days++;
			time -= 86400;
		}

		while (time >= 3600) {
			hours++;
			time -= 3600;
		}

		while (time >= 60) {
			minutes++;
			time -= 60;
		}

		seconds = Long.valueOf(time).intValue();

		if (seconds == 60) {
			minutes++;
			seconds = 0;
		}

		String output = "";

		if (days > 1) {
			output += days + " days, ";
		} else {
			output += days + " day, ";
		}

		if (hours > 1) {
			output += hours + " hours, ";
		} else if (hours == 1) {
			output += hours + " hour, ";
		}

		if (minutes > 1) {
			output += minutes + " minutes, ";
		} else if (minutes == 1) {
			output += minutes + " minute, ";
		}

		if (seconds > 1) {
			output += seconds + " seconds";
		} else if (seconds == 1) {
			output += seconds + " second";
		}

		output = output.trim();

		if (output.endsWith(",")) {
			output = output.substring(0, output.length() - 1);
		}

		return output;
	}
}
