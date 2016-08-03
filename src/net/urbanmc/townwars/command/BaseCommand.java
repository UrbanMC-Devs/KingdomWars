package net.urbanmc.townwars.command;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import net.md_5.bungee.api.ChatColor;
import net.urbanmc.townwars.command.subs.End;
import net.urbanmc.townwars.command.subs.Start;
import net.urbanmc.townwars.command.subs.Truce;

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
			infoMessage(p);
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

	private void infoMessage(Player p) {
		p.sendMessage(ChatColor.AQUA + "=== Kingdom Wars ===");
		p.sendMessage(ChatColor.AQUA + "Developed by: Elian & Silverwolfg11");
		p.sendMessage("");
		p.sendMessage(
				ChatColor.AQUA + "/twars start (nation)" + ChatColor.WHITE + ": Start a war with another nation!");
		p.sendMessage(ChatColor.AQUA + "/twars end" + ChatColor.WHITE + ": End a war you started!");
		p.sendMessage(ChatColor.AQUA + "/twars truce" + ChatColor.WHITE
				+ ": Declare a truce with the nation who started a battle!");
	}
}
