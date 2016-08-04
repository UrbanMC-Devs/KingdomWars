package net.urbanmc.kingdomwars;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.scoreboard.DisplaySlot;
import org.bukkit.scoreboard.Objective;
import org.bukkit.scoreboard.Score;
import org.bukkit.scoreboard.Scoreboard;

import com.palmergames.bukkit.towny.object.Nation;

import net.md_5.bungee.api.ChatColor;
import net.urbanmc.kingdomwars.data.War;

public class WarBoard {

	public void createBoard(War war) {
		Scoreboard warboard = Bukkit.getScoreboardManager().getNewScoreboard();
		Objective obj = warboard.registerNewObjective("test", "dummy");
		obj.setDisplaySlot(DisplaySlot.SIDEBAR);
		obj.setDisplayName(ChatColor.RED + "WarBoard");

		Score sc = obj.getScore(ChatColor.BLUE + war.getDeclaringNation());
		sc.setScore(10);
		
		Score s1 = obj.getScore("Kills: " + String.valueOf(0));
		s1.setScore(9);

		Score s2 = obj.getScore(ChatColor.AQUA + war.getDeclaredNation());
		s2.setScore(7);
		
		Score s3 = obj.getScore("Kills: " + String.valueOf(0));
		s3.setScore(6);

		Score s4 = obj.getScore(
				ChatColor.ITALIC + String.valueOf(KingdomWars.getWinningKills()) + ChatColor.ITALIC + " kills");
		s4.setScore(4);

		Score s5 = obj.getScore(ChatColor.ITALIC + "to win");
		s5.setScore(3);

		war.setScoreBoard(warboard);
		WarUtil.updateWar(war);

		updateBoard(war);
	}

	public void updateBoard(War war) {
		if (war.getScoreBoard() == null) {
			createBoard(war);
		}

		Scoreboard warboard = war.getScoreBoard();
		Objective obj = warboard.getObjective(DisplaySlot.SIDEBAR);

		Score s1 = obj.getScore("Kills: " + String.valueOf(war.getDeclaringPoints()));
		s1.setScore(9);

		Score s3 = obj.getScore("Kills: " + String.valueOf(war.getDeclaredPoints()));
		s3.setScore(6);

		Nation n1 = TownyUtil.getNation(war.getDeclaringNation());
		Nation n2 = TownyUtil.getNation(war.getDeclaredNation());

		war.setScoreBoard(warboard);
		WarUtil.updateWar(war);

		for (Player p : Bukkit.getServer().getOnlinePlayers()) {
			Nation nation = TownyUtil.getNation(p);
			if (nation.equals(n1) || nation.equals(n2)) {
				p.setScoreboard(warboard);
			}
		}
	}

	public void showBoard(Player p) {
		Nation n = TownyUtil.getNation(p);

		if (n == null)
			return;

		if (!WarUtil.inWar(n))
			return;

		p.setScoreboard(WarUtil.getWar(n).getScoreBoard());
	}
}