package net.urbanmc.kingdomwars.command;

import net.urbanmc.kingdomwars.KingdomWars;
import net.urbanmc.kingdomwars.command.subs.*;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

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
			case "forcestart":
				new ForceStart(p, args);
				return true;
			case "wars":
				new Wars(p, args);
				return true;
			case "leaderboard":
				new LeaderboardSub(p, args);
				return true;
			case "last":
				new LastSub(p, args);
				return true;
			case "callallies":
				new CallAlliesSub(p);
				return true;
			case "joinwar":
				new JoinWar(p, args);
				return true;
			case "accept":
			case "deny":
				new AcceptDenySub(p, args[0]);
				return true;
		}

		infoMessage(p, label);

		return true;
	}

	private void infoMessage(Player p, String label) {
		StringBuilder message = new StringBuilder();

		message.append(ChatColor.AQUA + "=== Kingdom Wars ===\n");

		label = ChatColor.AQUA + "/" + label + " ";

		message.append(label).append("start (nation)").append(ChatColor.WHITE).append(": Start a war with another nation! ")
				.append(ChatColor.GREEN).append("($").append(KingdomWars.getStartAmount()).append(")\n");

		message.append(label).append("end").append(ChatColor.WHITE).append(": End a war you started!\n");
		message.append(label).append("truce").append(ChatColor.WHITE).append(": Declare a truce with the nation who started a battle!\n");
		message.append(label).append("status").append(ChatColor.WHITE).append(": Toggle the war scoreboard\n");
		message.append(label).append("wars").append(ChatColor.WHITE).append(": View the current wars!\n");
		message.append(label).append("leaderboard").append(ChatColor.WHITE).append(": Check out which nation has the most wins!");
		message.append(label).append("callallies").append(ChatColor.WHITE).append(": Call for allies in a war. Must be before the war starts during the preparation period!!");
		message.append(label).append("joinwar (nation)").append(ChatColor.WHITE).append(": Join an ally's war if they have called for you!");
		p.sendMessage(message.toString());
	}
}
