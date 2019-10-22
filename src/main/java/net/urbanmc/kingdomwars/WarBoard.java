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

import java.util.Set;

public class WarBoard {

	public static void createBoard(War war) {
		Scoreboard warboard = Bukkit.getScoreboardManager().getNewScoreboard();
		Objective obj = warboard.registerNewObjective("test", "dummy");
		obj.setDisplaySlot(DisplaySlot.SIDEBAR);
		obj.setDisplayName(ChatColor.RED + "" + ChatColor.BOLD +  "WarBoard");

		Score sc = obj.getScore(ChatColor.GOLD + "" + ChatColor.BOLD + war.getDeclaringNation());
		sc.setScore(10);

		Score s1 = obj.getScore(ChatColor.GOLD + "Kills: " + (war.getDeclaringPoints()  > 0 ? (war.getDeclaringPoints() - 1) : 0));
		s1.setScore(9);


		Score s6 = obj.getScore("");
		s6.setScore(8);

		Score s2 = obj.getScore(ChatColor.DARK_AQUA + "" + ChatColor.BOLD + war.getDeclaredNation());
		s2.setScore(7);

		Score s3 = obj.getScore(ChatColor.DARK_AQUA + "Kills: "  + (war.getDeclaredPoints()  > 0 ? (war.getDeclaredPoints() - 1) : 0));
		s3.setScore(6);

		Score s7 = obj.getScore(" ");
		s7.setScore(5);

		int counter = 4;

		if (war.hasAllies()) {
			obj.getScore(translateColor("&b\u25EF Allies:")).setScore(counter);
			counter--;

			for (String nation1Ally : war.getAllies(true)) {
				obj.getScore(translateColor("&6 - " + nation1Ally)).setScore(counter);
				counter--;
			}

			for (String nation2Ally : war.getAllies(false)) {
				obj.getScore(translateColor("&3 - " + nation2Ally)).setScore(counter);
				counter--;
			}

			counter--;
		}

		Score s5 = obj.getScore(translateColor("&o" + war.getKillsToWin() + " kills to win!"));
		s5.setScore(counter);

		war.setScoreBoard(warboard);

		updateBoard(war);
	}

	public static void updateBoard(War war) {
		if (war.getScoreBoard() == null) {
			createBoard(war);
		}

		Scoreboard warboard = war.getScoreBoard();
		Objective obj = warboard.getObjective(DisplaySlot.SIDEBAR);

		warboard.resetScores(ChatColor.GOLD + "Kills: " + (war.getDeclaringPoints() - 1));
		warboard.resetScores(ChatColor.DARK_AQUA + "Kills: " + (war.getDeclaredPoints() - 1));

		Score s1 = obj.getScore(ChatColor.GOLD + "Kills: " + war.getDeclaringPoints());
		s1.setScore(9);

		Score s3 = obj.getScore(ChatColor.DARK_AQUA + "Kills: " + war.getDeclaredPoints());
		s3.setScore(6);

		war.setScoreBoard(warboard);
		displayScoreboard(war, warboard);
	}

	public static void updateNationNames(War war, String oldName, boolean isDeclaring) {
		if (war.getScoreBoard() == null) {
			updateBoard(war);
		}

		Scoreboard warboard = war.getScoreBoard();
		Objective obj = warboard.getObjective(DisplaySlot.SIDEBAR);

		ChatColor color = (isDeclaring) ? ChatColor.GOLD : ChatColor.DARK_AQUA;
		String newNationName = (isDeclaring) ? war.getDeclaringNation() : war.getDeclaredNation();

		warboard.resetScores(color + "" + ChatColor.BOLD + oldName);
		obj.getScore(color + "" + ChatColor.BOLD + newNationName).setScore((isDeclaring) ? 10 : 7);

		Nation n1 = TownyUtil.getNation(war.getDeclaringNation());
		Nation n2 = TownyUtil.getNation(war.getDeclaredNation());

		war.setScoreBoard(warboard);

		displayScoreboard(war, warboard);
	}

	public static void removeAllyFromBoard(War war, String allyNation, boolean declaringAlly) {
		Scoreboard warboard = war.getScoreBoard();

		char c = declaringAlly ? '6' : 3;

		warboard.resetScores(translateColor("&" + c + " - "  + allyNation));

		displayScoreboard(war, warboard);
	}


	public static void showBoard(KingdomWars plugin, Player p) {
		Nation n = TownyUtil.getNation(p);

		if (n == null)
			return;

		if (!plugin.getWarManager().inWar(n)) return;

		War war = plugin.getWarManager().getWar(n);

		if (war.isDisabled(p.getUniqueId()))
			return;

		if (war.getScoreBoard() == null) {
			createBoard(war);
		}

		p.setScoreboard(war.getScoreBoard());
	}

	private static String translateColor(String message) {
		return ChatColor.translateAlternateColorCodes('&', message);
	}

	private static void displayScoreboard(War war, Scoreboard board) {
		Set<String> nationNames = war.getAllNationNames();

		for (Player p : Bukkit.getServer().getOnlinePlayers()) {
			Nation nation = TownyUtil.getNation(p);

			if (nation == null)
				continue;

			if (nationNames.contains(nation.getName())) {
				p.setScoreboard(board);
			}
		}
	}
}
