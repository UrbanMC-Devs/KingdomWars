package net.urbanmc.kingdomwars;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import com.palmergames.bukkit.towny.TownyMessaging;
import com.palmergames.bukkit.towny.exceptions.NotRegisteredException;
import com.palmergames.bukkit.towny.object.Nation;
import com.palmergames.bukkit.towny.object.Resident;
import com.palmergames.bukkit.towny.object.TownyUniverse;

import ca.xshade.questionmanager.Option;
import ca.xshade.questionmanager.Question;
import ca.xshade.questionmanager.QuestionTask;

public class TownyUtil {

	public static void sendNationMessage(Nation nation, String message) {
		TownyMessaging.sendNationMessage(nation, message);
	}

	public static Nation getNation(String name) {
		Nation nation = null;

		try {
			nation = TownyUniverse.getDataSource().getNation(name);
		} catch (NotRegisteredException ex) {
			;
		}

		if (nation == null) {
			for (Nation dataNation : TownyUniverse.getDataSource().getNations()) {
				if (dataNation.getName().equalsIgnoreCase(name)) {
					nation = dataNation;
					break;
				}
			}
		}

		return nation;
	}

	public void truceQuestion(String playerName, String otherNation) {
		List<Option> options = new ArrayList<Option>();

		options.add(new Option("accept", new QuestionTask() {
			public void run() {

			}
		}));

		options.add(new Option("deny", new QuestionTask() {
			public void run() {

			}
		}));

		Question question = new Question(playerName, "Would you like to accept a truce with " + otherNation
				+ "? You will receive %configTruceAmount% from their nation bank.", options);

		try {
			KingdomWars.getQuestioner().getQuestionManager().appendQuestion(question);

		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}

	public static List<Player> getOnlineInNation(Nation nation, String permission) {
		List<Player> players = new ArrayList<Player>();
		List<String> residents = new ArrayList<String>();

		for (Resident res : nation.getResidents()) {
			residents.add(res.getName());
		}

		for (Player p : Bukkit.getOnlinePlayers()) {
			if (residents.contains(p.getName()) && p.hasPermission(permission)) {
				players.add(p);
			}
		}

		return players;
	}
}
