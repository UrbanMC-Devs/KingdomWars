package net.urbanmc.kingdomwars;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Scanner;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.palmergames.bukkit.towny.event.DeleteNationEvent;
import com.palmergames.bukkit.towny.event.RenameNationEvent;
import com.palmergames.bukkit.towny.exceptions.EconomyException;
import com.palmergames.bukkit.towny.object.Nation;

import net.urbanmc.kingdomwars.data.last.LastWar;
import net.urbanmc.kingdomwars.data.last.LastWarList;
import net.urbanmc.kingdomwars.data.leaderboard.Leaderboard;
import net.urbanmc.kingdomwars.data.leaderboard.LeaderboardList;
import net.urbanmc.kingdomwars.data.war.War;
import net.urbanmc.kingdomwars.data.war.WarList;
import net.urbanmc.kingdomwars.data.war.WarListSerializer;
import net.urbanmc.kingdomwars.event.WarEndEvent;

public class WarUtil {

	private static Gson gson;

	private static List<War> wars;
	private static List<LastWar> last;
	private static List<Leaderboard> leaderboardList;

	static {
		gson = new GsonBuilder().registerTypeAdapter(WarList.class, new WarListSerializer()).create();
		createFiles();
		loadWars();
		loadLast();
		loadLeaderboard();
	}

	private static void createFiles() {
		File file = new File("plugins/KingdomWars/wars.json");

		if (!file.getParentFile().isDirectory()) {
			file.getParentFile().mkdir();
		}

		if (!file.exists()) {
			try {
				file.createNewFile();
			} catch (IOException ex) {
				ex.printStackTrace();
			}
		}

		File last = new File("plugins/KingdomWars/last.json");

		if (!last.exists()) {
			try {
				last.createNewFile();
			} catch (IOException ex) {
				ex.printStackTrace();
			}
		}

		File leaderboard = new File("plugins/KingdomWars/leaderboard.json");

		if (!leaderboard.exists()) {
			try {
				leaderboard.createNewFile();
			} catch (IOException ex) {
				ex.printStackTrace();
			}
		}

	}

	private static void loadWars() {
		wars = new ArrayList<War>();

		try {
			Scanner scanner = new Scanner(new File("plugins/KingdomWars/wars.json"));

			wars = gson.fromJson(scanner.nextLine(), WarList.class).getWars();

			scanner.close();
		} catch (Exception ex) {
			;
		}
	}

	private static void loadLast() {
		last = new ArrayList<LastWar>();

		try {
			Scanner scanner = new Scanner(new File("plugins/KingdomWars/last.json"));

			Gson gson = new Gson();

			last = gson.fromJson(scanner.nextLine(), LastWarList.class).getLast();

			scanner.close();
		} catch (Exception ex) {
			;
		}

		reloadLast();
	}

	private static void loadLeaderboard() {
		leaderboardList = new ArrayList<Leaderboard>();

		try {
			Scanner scanner = new Scanner(new File("plugins/KingdomWars/leaderboard.json"));

			Gson gson = new Gson();

			leaderboardList = gson.fromJson(scanner.nextLine(), LeaderboardList.class).getLeaderboards();
            if(leaderboardList == null) System.out.println("Loading LList = null");
			scanner.close();
		} catch (Exception ex) {
			;
		}

	}

	private static void reloadLast() {
		long millis = System.currentTimeMillis();

		ArrayList<LastWar> remove = new ArrayList<LastWar>();

		last.stream().filter(t -> t.getMillis() <= millis).forEach(remove::add);

		last.removeAll(remove);

		saveLast();
	}

	public static void startWar(Nation nation1, Nation nation2) {
		startWar(new War(nation1.getName(), nation2.getName()));
	}

	public static void startWar(War war) {
		wars.add(war);
		saveFile();
		WarBoard.createBoard(war);
	}

	public static void endWar(Nation nation) {
		endWar(getWar(nation));
	}

	public static void endWar(War war) {
		wars.remove(war);
		saveFile();
		end(war);
	}

	public static void updateWar(War war) {
		War oldWar = getWar(TownyUtil.getNation(war.getDeclaringNation()));

		if (oldWar != null) {
			wars.remove(oldWar);
		}

		wars.add(war);
		saveFile();
	}

	public static boolean inWar(Nation nation) {
		String name = nation.getName();

		for (War war : wars) {
			if (war.getDeclaringNation().equals(name))
				return true;
			if (war.getDeclaredNation().equals(name))
				return true;
		}

		return false;
	}

	public static List<War> getWarList() {
		return wars;
	}

	public static War getWar(Nation nation) {
		String name = nation.getName();

		for (War war : wars) {
			if (war.getDeclaringNation().equals(name))
				return war;
			if (war.getDeclaredNation().equals(name))
				return war;
		}

		return null;
	}

	public synchronized static void checkWin(War war) {
		Nation winner = null, loser = null;

		if (war.getDeclaringPoints() == KingdomWars.getWinningKills()) {
			winner = TownyUtil.getNation(war.getDeclaringNation());
			loser = TownyUtil.getNation(war.getDeclaredNation());
		} else if (war.getDeclaredPoints() == KingdomWars.getWinningKills()) {
			winner = TownyUtil.getNation(war.getDeclaredNation());
			loser = TownyUtil.getNation(war.getDeclaringNation());
		}

		if (winner != null && loser != null) {
			win(winner, loser, KingdomWars.getFinishAmount());
		}
	}

	public synchronized static void win(Nation winner, Nation loser, double amount) {
		War war = getWar(winner);

		WarEndEvent event = new WarEndEvent(war);
		Bukkit.getPluginManager().callEvent(event);

		if (event.isCancelled())
			return;

		wars.remove(war);
		saveFile();

		for (Player p : Bukkit.getServer().getOnlinePlayers()) {
			Nation nation = TownyUtil.getNation(p);

			if (nation == null)
				continue;

			if (nation.equals(winner) || nation.equals(loser)) {
				if (p.getScoreboard().equals(war.getScoreBoard())) {
					p.setScoreboard(Bukkit.getScoreboardManager().getNewScoreboard());
				}
			}
		}

		TownyUtil.sendNationMessage(winner, "Your nation has won the war against " + loser.getName() + "!");
		addWinToLeaderBoard(winner.getName(), true);

		TownyUtil.sendNationMessage(loser, "Your nation has lost the war against " + winner.getName() + "!");
		addWinToLeaderBoard(loser.getName(), false);

		LastWar lastWar = new LastWar(winner.getName(), loser.getName(),
				System.currentTimeMillis() + KingdomWars.getMillis());
		addLast(lastWar);
		updateWarInfoInLeaderboard(winner.getName(), loser.getName());
		try {
			winner.setBalance(winner.getHoldingBalance() + amount, "War win");
		} catch (EconomyException ex) {
			ex.printStackTrace();
		}

		double balance = 0;

		try {
			balance = loser.getHoldingBalance();
		} catch (EconomyException ex) {
			ex.printStackTrace();
		}

		if (balance < amount) {
			TownyUtil.sendNationMessage(loser, "Your nation could not pay the war loss fee and has fallen!");
			TownyUtil.deleteNation(loser);
		} else {
			try {
				loser.setBalance(balance - amount, "War loss");
			} catch (EconomyException ex) {
				ex.printStackTrace();
			}
		}
	}

	public static void end(War war) {
		Nation nation1 = TownyUtil.getNation(war.getDeclaringNation());
		Nation nation2 = TownyUtil.getNation(war.getDeclaredNation());

		WarEndEvent event = new WarEndEvent(war);
		Bukkit.getPluginManager().callEvent(event);

		wars.remove(war);
		saveFile();

		if (event.isCancelled())
			return;

		for (Player p : Bukkit.getServer().getOnlinePlayers()) {
			Nation nation = TownyUtil.getNation(p);

			if (nation == null)
				continue;

			if (nation.equals(nation1) || nation.equals(nation2)) {
				if (p.getScoreboard().equals(war.getScoreBoard())) {
					p.setScoreboard(Bukkit.getScoreboardManager().getNewScoreboard());
				}
			}
		}

		LastWar lastWar = new LastWar(nation1.getName(), nation2.getName(),
				System.currentTimeMillis() + KingdomWars.getMillis());
		addLast(lastWar);
	}

	public static boolean hasLast(String nation1, String nation2) {
		reloadLast();

		for (LastWar lastWar : last) {
			if (lastWar.getDeclaringNation().equals(nation1) && lastWar.getDeclaredNation().equals(nation2))
				return true;
			if (lastWar.getDeclaringNation().equals(nation2) && lastWar.getDeclaredNation().equals(nation1))
				return true;
		}

		return false;
	}

	public static void addLast(LastWar lastWar) {
		last.add(lastWar);
		reloadLast();
	}

	public static LastWar getLast(Nation nation) {
		reloadLast();

		for (LastWar lastWar : last) {
			if (lastWar.getDeclaringNation().equals(nation.getName())
					|| lastWar.getDeclaredNation().equals(nation.getName()))
				return lastWar;
		}

		return null;
	}

	private static void saveFile() {
		try {
			PrintWriter writer = new PrintWriter(new File("plugins/KingdomWars/wars.json"));

			writer.write(gson.toJson(new WarList(wars)));

			writer.close();
		} catch (IOException ex) {
			ex.printStackTrace();
		}
	}

	private static void saveLast() {
		try {
			PrintWriter writer = new PrintWriter(new File("plugins/KingdomWars/last.json"));

			Gson gson = new Gson();

			writer.write(gson.toJson(new LastWarList(last)));

			writer.close();
		} catch (IOException ex) {
			ex.printStackTrace();
		}
	}

	private static void saveLeaderboard() {
		try {
			PrintWriter writer = new PrintWriter(new File("plugins/KingdomWars/leaderboard.json"));

			writer.write(new Gson().toJson(new LeaderboardList(leaderboardList)));

			writer.close();
		} catch (IOException ex) {
			ex.printStackTrace();
		}
	}

	public static List<Leaderboard> getLeaderboards() {
		if(leaderboardList == null) System.out.println("Returning LList = Null");
		return leaderboardList;
	}

	public static void addWinToLeaderBoard(String nation, boolean won) {
		int i = findIndexForNationInLeaderboard(nation);

		Leaderboard lb;

		if (i == -1) {
			lb = new Leaderboard(nation);
			leaderboardList.add(lb);
		} else {
			lb = leaderboardList.get(i);
		}

		i = findIndexForNationInLeaderboard(nation);
		
		if (won) {
			leaderboardList.get(i).setWins(lb.getWins() + 1);
		} else {
			leaderboardList.get(i).setLosses(lb.getLosses() + 1);
		}


		saveLeaderboard();
	}

	private static void updateWarInfoInLeaderboard(String winner, String loser) {
		DateFormat df = new SimpleDateFormat("dd/MM/yy");

		String lastwarinfo = winner + ";" + loser + ";" + df.format(new Date());

		int i = findIndexForNationInLeaderboard(winner);
		leaderboardList.get(i).setLastWarInfo(lastwarinfo);
		saveLeaderboard();

		i = findIndexForNationInLeaderboard(loser);
		leaderboardList.get(i).setLastWarInfo(lastwarinfo);
		saveLeaderboard();
	}

	public static void leaderBoardNationRename(RenameNationEvent e) {
		int i = findIndexForNationInLeaderboard(e.getOldName());

		if (i != -1) {
			leaderboardList.get(i).setNation(e.getNation().getName());
			saveLeaderboard();
		}
	}

	public static void leaderBoardNationDelete(DeleteNationEvent e) {
		int i = findIndexForNationInLeaderboard(e.getNationName());

		if (i != -1) {
			leaderboardList.remove(i);
			saveLeaderboard();
		}
	}

	private static int findIndexForNationInLeaderboard(String nation) {
		if (leaderboardList.isEmpty())
			return -1;

		for (int i = 0; i < leaderboardList.size(); i++) {
			if (leaderboardList.get(i).getNation().equalsIgnoreCase(nation))
				return i;
		}

		return -1;
	}
}
