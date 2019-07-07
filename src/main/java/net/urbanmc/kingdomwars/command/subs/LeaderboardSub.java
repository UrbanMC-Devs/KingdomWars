package net.urbanmc.kingdomwars.command.subs;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

import net.urbanmc.kingdomwars.WarUtil;
import net.urbanmc.kingdomwars.data.leaderboard.Leaderboard;

public class LeaderboardSub {

	public LeaderboardSub(Player p, String[] args) {
		if (!p.hasPermission("kingdomwars.leaderboard")) {
			p.sendMessage(ChatColor.RED + "You do not have permission to do this!");
			return;
		}

		List<Leaderboard> leaderboardList = WarUtil.getLeaderboards();

		String message;

		if (leaderboardList.isEmpty()) {
			message = ChatColor.GREEN + "=== Kingdom Wars Leaderboard ===\n" + ChatColor.GRAY + "No current data!";
		} else {
			message = ChatColor.GREEN + "=== Kingdom Wars Leaderboard ===\n" + ChatColor.GREEN + "Nation "
					+ ChatColor.AQUA + "Wins " + ChatColor.RED + "Losses\n";

			leaderboardList = sortLeaderBoardList(leaderboardList);

			for (int i = 0; i < leaderboardList.size(); i++) {
				Leaderboard lb = leaderboardList.get(i);

				message = message + "" + ChatColor.YELLOW + (i + 1) + ChatColor.YELLOW + ". " + ChatColor.GREEN
						+ lb.getNation() + " " + ChatColor.AQUA + lb.getWins() + " " + ChatColor.RED + lb.getLosses()
						+ "\n";
			}
		}

		p.sendMessage(message);
	}

	private List<Leaderboard> sortLeaderBoardList(List<Leaderboard> list) {
		list.sort(Comparator.comparingInt(Leaderboard::getWins));

		Collections.reverse(list);
		return list;
	}

}
