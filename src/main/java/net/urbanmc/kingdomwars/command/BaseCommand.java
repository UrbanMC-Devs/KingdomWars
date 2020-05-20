package net.urbanmc.kingdomwars.command;

import net.urbanmc.kingdomwars.KingdomWars;
import net.urbanmc.kingdomwars.command.subs.*;
import net.urbanmc.kingdomwars.manager.ConfigManager;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class BaseCommand implements CommandExecutor {

	private KingdomWars plugin;

	public BaseCommand(KingdomWars plugin) {
		this.plugin = plugin;
	}

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
				new Start(p, args, plugin);
				break;
			case "end":
				new End(p, plugin);
				break;
			case "truce":
				new Truce(p, label, plugin);
				break;
			case "status":
				new Status(p, plugin);
				break;
			case "forceend":
				new ForceEnd(p, args, plugin);
				break;
			case "forcestart":
				new ForceStart(p, args, plugin);
				break;
			case "wars":
				new Wars(p, plugin);
				break;
			case "leaderboard":
				new LeaderboardSub(p, plugin);
				break;
			case "last":
				new LastSub(p, args, plugin);
				break;
			case "reload":
				new ReloadSub(p, plugin);
				break;
			case "save":
				new SaveSub(p, plugin);
				break;
			case "callallies":
				new CallAlliesSub(p, plugin);
				break;
			case "joinwar":
				new JoinWar(p, args, plugin);
				break;
			case "warblocks":
				new WarBlocksSub(p, args);
				break;
			case "accept":
			case "deny":
				new AcceptDenySub(p, args[0], plugin);
				break;
			default:
				infoMessage(p, label);
				break;
		}

		return true;
	}

	private void infoMessage(Player p, String label) {
		StringBuilder message = new StringBuilder();

		message.append(ChatColor.AQUA + "=== Kingdom Wars ===\n");

		label = ChatColor.AQUA + "/" + label + " ";

		message.append(label).append("start (nation)").append(ChatColor.WHITE).append(": Start a war with another nation! ")
				.append(ChatColor.GREEN).append("($").append(ConfigManager.getStartAmount()).append(")\n");

		message.append(label).append("end").append(ChatColor.WHITE).append(": End a war you started!\n");
		message.append(label).append("truce").append(ChatColor.WHITE).append(": Declare a truce with the nation who started a battle!\n");
		message.append(label).append("status").append(ChatColor.WHITE).append(": Toggle the war scoreboard\n");
		message.append(label).append("wars").append(ChatColor.WHITE).append(": View the current wars!\n");
		message.append(label).append("leaderboard").append(ChatColor.WHITE).append(": Check out which nation has the most wins!\n");
		message.append(label).append("callallies").append(ChatColor.WHITE).append(": Call for allies in a war. Must be before the war starts during the preparation period!\n");
		message.append(label).append("joinwar (nation)").append(ChatColor.WHITE).append(": Join an ally's war if they have called for you!");
		p.sendMessage(message.toString());
	}
}
