package net.urbanmc.kingdomwars;

import net.urbanmc.kingdomwars.util.TownyUtil;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.scoreboard.DisplaySlot;
import org.bukkit.scoreboard.Objective;
import org.bukkit.scoreboard.Score;
import org.bukkit.scoreboard.Scoreboard;

import com.palmergames.bukkit.towny.object.Nation;

import net.urbanmc.kingdomwars.data.war.War;

public class WarBoard {

	public static void createBoard(War war) {
		Scoreboard warboard = Bukkit.getScoreboardManager().getNewScoreboard();
		Objective obj = warboard.registerNewObjective("test", "dummy");
		obj.setDisplaySlot(DisplaySlot.SIDEBAR);
		obj.setDisplayName(ChatColor.RED + "WarBoard");

		Score sc = obj.getScore(ChatColor.BLUE + "" + ChatColor.BOLD + war.getDeclaringNation());
		sc.setScore(10);

		Score s1 = obj.getScore(ChatColor.BLUE + "Kills: " + String.valueOf(0));
		s1.setScore(9);

		Score s6 = obj.getScore("");
		s6.setScore(8);

		Score s2 = obj.getScore(ChatColor.AQUA + "" + ChatColor.BOLD + war.getDeclaredNation());
		s2.setScore(7);

		Score s3 = obj.getScore(ChatColor.AQUA + "Kills: " + String.valueOf(0));
		s3.setScore(6);

		Score s7 = obj.getScore(" ");
		s7.setScore(5);

		Score s4 = obj.getScore(
				ChatColor.ITALIC + String.valueOf(KingdomWars.getWinningKills()) + ChatColor.ITALIC + " kills");
		s4.setScore(4);

		Score s5 = obj.getScore(ChatColor.ITALIC + "to win");
		s5.setScore(3);

		war.setScoreBoard(warboard);
		WarUtil.updateWar(war);

		updateBoard(war);
	}

	public static void updateBoard(War war) {
		if (war.getScoreBoard() == null) {
			createBoard(war);
		}

		Scoreboard warboard = war.getScoreBoard();
		Objective obj = warboard.getObjective(DisplaySlot.SIDEBAR);

		warboard.resetScores(ChatColor.BLUE + "Kills: " + String.valueOf(war.getDeclaringPoints() - 1));
		warboard.resetScores(ChatColor.AQUA + "Kills: " + String.valueOf(war.getDeclaredPoints() - 1));

		Score s1 = obj.getScore(ChatColor.BLUE + "Kills: " + String.valueOf(war.getDeclaringPoints()));
		s1.setScore(9);

		Score s3 = obj.getScore(ChatColor.AQUA + "Kills: " + String.valueOf(war.getDeclaredPoints()));
		s3.setScore(6);

		Nation n1 = TownyUtil.getNation(war.getDeclaringNation());
		Nation n2 = TownyUtil.getNation(war.getDeclaredNation());

		war.setScoreBoard(warboard);
		WarUtil.updateWar(war);

		for (Player p : Bukkit.getServer().getOnlinePlayers()) {
			Nation nation = TownyUtil.getNation(p);

			if (nation == null)
				continue;

			if (nation.equals(n1) || nation.equals(n2)) {
				p.setScoreboard(warboard);
			}
		}
	}

	public static void updateNationNames(War war, String oldName, boolean isDeclaring) {
		if (war.getScoreBoard() == null) {
			updateBoard(war);
		}

		Scoreboard warboard = war.getScoreBoard();
		Objective obj = warboard.getObjective(DisplaySlot.SIDEBAR);

		ChatColor color = (isDeclaring) ? ChatColor.BLUE : ChatColor.AQUA;
		String newNationName = (isDeclaring) ? war.getDeclaringNation() : war.getDeclaredNation();

		warboard.resetScores(color + "" + ChatColor.BOLD + oldName);
		obj.getScore(color + "" + ChatColor.BOLD + newNationName).setScore((isDeclaring) ? 10 : 7);

		Nation n1 = TownyUtil.getNation(war.getDeclaringNation());
		Nation n2 = TownyUtil.getNation(war.getDeclaredNation());

		war.setScoreBoard(warboard);
		WarUtil.updateWar(war);

		for (Player p : Bukkit.getServer().getOnlinePlayers()) {
			Nation nation = TownyUtil.getNation(p);

			if (nation == null)
				continue;

			if (nation.equals(n1) || nation.equals(n2)) {
				p.setScoreboard(warboard);
			}
		}
	}


	public static void showBoard(Player p) {
		Nation n = TownyUtil.getNation(p);

		if (n == null)
			return;

		if (!WarUtil.inWar(n))
			return;

		War war = WarUtil.getWar(n);

		if (war.isDisabled(p.getUniqueId()))
			return;

		if (war.getScoreBoard() == null) {
			createBoard(war);
		}

		p.setScoreboard(war.getScoreBoard());
	}
}
