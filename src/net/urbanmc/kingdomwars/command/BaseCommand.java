package net.urbanmc.kingdomwars.command;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import net.urbanmc.kingdomwars.command.subs.End;
import net.urbanmc.kingdomwars.command.subs.ForceEnd;
import net.urbanmc.kingdomwars.command.subs.Leaderboard;
import net.urbanmc.kingdomwars.command.subs.Start;
import net.urbanmc.kingdomwars.command.subs.Status;
import net.urbanmc.kingdomwars.command.subs.Truce;
import net.urbanmc.kingdomwars.command.subs.Wars;

public class BaseCommand implements CommandExecutor {

	@Override
	public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
		if (!(sender instanceof Player)) {
			sender.sendMessage("You must be a player to run this command.");
			return true;
		}

		Player p = (Player) sender;

		if (args.length == 0) {
			infoMessage(p, label);
			return true;
		}

		String sub = args[0].toLowerCase();

		switch (sub) {
		case "start":
			new Start(p, args);
			return true;
		case "end":
			new End(p, args);
			return true;
		case "truce":
			new Truce(p, label, args);
			return true;
		case "status":
			new Status(p, args);
			return true;
		case "forceend":
			new ForceEnd(p, args);
			return true;
		case "wars":
			new Wars(p, args);
			return true;
		case "leaderboard":
			new Leaderboard(p, args);
			return true;
		}

		infoMessage(p, label);

		return true;
	}

	private void infoMessage(Player p, String label) {
		String message = ChatColor.AQUA + "=== Kingdom Wars ===\n" + "/" + label + " start (nation)" + ChatColor.WHITE
				+ ": Start a war with another nation!\n" + ChatColor.AQUA + "/" + label + " end" + ChatColor.WHITE
				+ ": End a war you started!\n" + ChatColor.AQUA + "/" + label + " truce" + ChatColor.WHITE
				+ ": Declare a truce with the nation who started a battle!\n" + ChatColor.AQUA + "/" + label + " status"
				+ ChatColor.WHITE + ": Toggle the war scoreboard\n" + ChatColor.AQUA + "/" + label + " wars"
						+ ChatColor.WHITE + ": View the current wars!\n" + ChatColor.AQUA + "/" + label + " leaderboard"
								+ ChatColor.WHITE + ": Check out which nation has the most wins!";;
		p.sendMessage(message);
	}
}
