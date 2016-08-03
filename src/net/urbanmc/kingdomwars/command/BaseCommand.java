package net.urbanmc.kingdomwars.command;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import net.md_5.bungee.api.ChatColor;
import net.urbanmc.kingdomwars.command.subs.End;
import net.urbanmc.kingdomwars.command.subs.Start;
import net.urbanmc.kingdomwars.command.subs.Truce;

public class BaseCommand implements CommandExecutor {

	@Override
	public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
		if (!(sender instanceof Player)) {
			sender.sendMessage("You must be a player to run this command.");
			return true;
		}

		Player p = (Player) sender;

		// Possible subs: start [town], end [town], truce [town]

		if (args.length == 0) {
			infoMessage(p, label);
			return true;
		}

		String sub = args[0].toLowerCase();

		switch (sub) {
		case "start":
			new Start(p, args);
			break;
		case "end":
			new End(p, args);
			break;
		case "truce":
			new Truce(p, args);
			break;
		}

		return true;
	}

	private void infoMessage(Player p, String label) {
		String message = ChatColor.AQUA + "=== Kingdom Wars ===\n\n" + "/" + label + " start (nation)"
				+ ChatColor.WHITE + ": Start a war with another nation!\n" + ChatColor.AQUA + "/" + label + " end"
				+ ChatColor.WHITE + ": End a war you started!\n" + ChatColor.AQUA + "/" + label + " truce"
				+ ChatColor.WHITE + ": Declare a truce with the nation who started a battle!";
		p.sendMessage(message);
	}
}
