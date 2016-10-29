package net.urbanmc.kingdomwars.command.subs;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

import net.urbanmc.kingdomwars.WarUtil;
import net.urbanmc.kingdomwars.data.leaderboard.Leaderbrd;

public class Leaderboard {

	public Leaderboard (Player p, String[] args) {
			if (!p.hasPermission("kingdomwars.leaderboard")) {
				p.sendMessage(ChatColor.RED + "You do not have permission to do this!");
				return;
			}
		
		String message;
		List<Leaderbrd> leaderboardlist = WarUtil.leaderboardlist;
		if(leaderboardlist.isEmpty()) message = ChatColor.GREEN + "=== Kingdom Wars Leaderboard ===\n"+ ChatColor.GRAY + "No current data!";
		
		else {
			message = ChatColor.GREEN + "=== Kingdom Wars Leaderboard ===\n" + ChatColor.GREEN + "Nation " + ChatColor.AQUA + "Wins "
		    + ChatColor.RED + "Losses\n"; 
			
			leaderboardlist = sortLeaderBoardList(leaderboardlist);
			Leaderbrd ldbr;
			for(int i = 0; i < leaderboardlist.size(); i++) {
				ldbr = leaderboardlist.get(i);
				message = message + "" + ChatColor.YELLOW +  (i+1) +ChatColor.YELLOW + ". " +  ChatColor.GREEN + 
				ldbr.getNation() + " " + ChatColor.AQUA + ldbr.getWins() + " " + ChatColor.RED + ldbr.getLosses() +"\n";
			}		
		}
		p.sendMessage(message);
		return;
	 }

	private List<Leaderbrd> sortLeaderBoardList(List<Leaderbrd> list) {
		Collections.sort(list, new Comparator<Leaderbrd>() {

			@Override
			public int compare(Leaderbrd o1, Leaderbrd o2) {
				return o1.getWins() - o2.getWins();
			}

		});
		Collections.reverse(list);
		return list;
	}

}
