package net.urbanmc.kingdomwars;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;

import com.palmergames.bukkit.towny.TownyMessaging;
import com.palmergames.bukkit.towny.exceptions.NotRegisteredException;
import com.palmergames.bukkit.towny.object.Nation;
import com.palmergames.bukkit.towny.object.Resident;
import com.palmergames.bukkit.towny.object.TownyUniverse;
import com.palmergames.bukkit.towny.utils.CombatUtil;

import ca.xshade.questionmanager.LinkedQuestion;
import ca.xshade.questionmanager.Option;
import ca.xshade.questionmanager.QuestionTask;

public class TownyUtil {

	public static void sendNationMessage(Nation nation, String message) {
		TownyMessaging.sendNationMessage(nation, message);
	}

	public static Nation getNation(Player p) {
		try {
			return TownyUniverse.getDataSource().getResident(p.getName()).getTown().getNation();
		} catch (NotRegisteredException ex) {
			return null;
		}
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

	public static boolean allied(Nation nation1, Nation nation2) {
		return nation1.hasAlly(nation2) && nation2.hasAlly(nation1);
	}

	public void truceQuestion(Nation recievingNation, String otherNation) {
		List<Option> options = new ArrayList<Option>();

		options.add(new Option("accept", new QuestionTask() {
			public void run() {

			}
		}));

		options.add(new Option("deny", new QuestionTask() {
			public void run() {

			}
		}));

		List<String> targets = new ArrayList<String>();

		for (Player p : getOnlineInNation(recievingNation, "KingdomWars.nationstaff")) {
			targets.add(p.getName());
		}

		LinkedQuestion question = new LinkedQuestion(
				KingdomWars.getQuestioner().getQuestionManager().getNextQuestionId(), targets,
				"Would you like to accept a truce with " + otherNation
						+ "? You will receive %configTruceAmount% from their nation bank.",
				options);

		try {
			KingdomWars.getQuestioner().getQuestionManager().appendLinkedQuestion(question);

		} catch (Exception ex) {
			ex.printStackTrace();
		}

		Player tempp;
		for (String pname : targets) {
			tempp = Bukkit.getPlayer(pname);
			for (String line : KingdomWars.getQuestioner().formatQuestion(question, "New Question"))
				tempp.sendMessage(line);
		}

		return;
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

	public static boolean damageCancelled(Entity attacker, Entity defender) {
		return CombatUtil.preventDamageCall(KingdomWars.getTowny(), attacker, defender);
	}
}
