package net.urbanmc.kingdomwars;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.palmergames.bukkit.towny.object.Nation;

import net.urbanmc.kingdomwars.data.War;
import net.urbanmc.kingdomwars.data.WarDeserializer;
import net.urbanmc.kingdomwars.data.WarList;

public class WarUtil {

	private static List<War> wars;

	static {
		createFile();
		loadWars();
	}

	private static void createFile() {
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
	}

	private static void loadWars() {
		wars = new ArrayList<War>();

		try {
			Scanner scanner = new Scanner(new File("plugins/KingdomWars/wars.json"));

			Gson gson = new GsonBuilder().registerTypeAdapter(WarList.class, new WarDeserializer()).create();

			wars = gson.fromJson(scanner.nextLine(), WarList.class).getWars();

			scanner.close();
		} catch (Exception ex) {
			;
		}
	}

	public static void startWar(Nation nation1, Nation nation2) {
		startWar(new War(nation1.getName(), nation2.getName()));
	}

	public static void startWar(War war) {
		wars.add(war);
		saveFile();
		KingdomWars.getWarBoard().createBoard(war);
	}

	public static void endWar(Nation nation) {
		endWar(getWar(nation));
	}

	public static void endWar(War war) {
		wars.remove(war);
		saveFile();
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

	@SuppressWarnings("deprecation")
	public synchronized static void win(Nation winner, Nation loser, double amount) {
		War war = getWar(winner);

		wars.remove(war);
		saveFile();

		for (Player p : Bukkit.getServer().getOnlinePlayers()) {
			Nation nation = TownyUtil.getNation(p);
			if (nation.equals(winner) || nation.equals(loser)) {
				if (p.getScoreboard().equals(war.getScoreBoard())) {
					p.setScoreboard(null);
				}
			}
		}

		TownyUtil.sendNationMessage(winner, "Your nation has won the war against " + loser.getName() + "!");
		TownyUtil.sendNationMessage(loser, "Your nation has lost the war against " + loser.getName() + "!");

		KingdomWars.getEcon().withdrawPlayer("nation_" + winner.getName(), amount);

		double balance = KingdomWars.getEcon().getBalance("nation_" + loser.getName());

		if (balance < amount) {
			TownyUtil.sendNationMessage(loser, "Your nation could not pay the war loss fee and has fallen!");
			TownyUtil.deleteNation(loser);
		}

		KingdomWars.getEcon().withdrawPlayer("nation_" + loser.getName(), amount);
	}

	public static void end(War war) {
		wars.remove(war);
		saveFile();

		Nation nation1 = TownyUtil.getNation(war.getDeclaringNation());
		Nation nation2 = TownyUtil.getNation(war.getDeclaredNation());

		for (Player p : Bukkit.getServer().getOnlinePlayers()) {
			Nation nation = TownyUtil.getNation(p);
			if (nation.equals(nation1) || nation.equals(nation2)) {
				if (p.getScoreboard().equals(war.getScoreBoard())) {
					p.setScoreboard(null);
				}
			}
		}

		TownyUtil.sendNationMessage(nation1, "Your war against " + nation2.getName() + " has been ended by an admin.");
		TownyUtil.sendNationMessage(nation2, "Your war against " + nation1.getName() + " has been ended by an admin.");
	}

	private static void saveFile() {
		try {
			PrintWriter writer = new PrintWriter(new File("plugins/KingdomWars/wars.json"));

			writer.write(new Gson().toJson(new WarList(wars)));

			writer.close();
		} catch (IOException ex) {
			ex.printStackTrace();
		}
	}
}
