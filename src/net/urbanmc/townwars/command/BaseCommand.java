package net.urbanmc.townwars.command;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import net.urbanmc.townwars.command.subs.Start;

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
			String message = "";
			p.sendMessage(message);

			return true;
		}

		String sub = args[0].toLowerCase();

		switch (sub) {
		case "start":
			new Start(p, args);
			break;
		}

		return true;
	}	
}
