package net.urbanmc.kingdomwars.util;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import net.urbanmc.kingdomwars.KingdomWars;
import net.urbanmc.kingdomwars.WarUtil;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;

import com.palmergames.bukkit.towny.TownyMessaging;
import com.palmergames.bukkit.towny.exceptions.EconomyException;
import com.palmergames.bukkit.towny.exceptions.NotRegisteredException;
import com.palmergames.bukkit.towny.object.Nation;
import com.palmergames.bukkit.towny.object.Resident;
import com.palmergames.bukkit.towny.object.TownyUniverse;
import com.palmergames.bukkit.towny.utils.CombatUtil;

import ca.xshade.questionmanager.LinkedQuestion;
import ca.xshade.questionmanager.Option;
import ca.xshade.questionmanager.QuestionManager;
import ca.xshade.questionmanager.QuestionTask;

public class TownyUtil {

	public static void sendNationMessage(Nation nation, String message) {
		TownyMessaging.sendNationMessage(nation, ChatColor.AQUA + message);
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

	public static double getNationBalance(Nation nation) {
		try {
			return nation.getHoldingBalance();
		} catch (EconomyException ex) {
			ex.printStackTrace();
			return 0;
		}
	}

	public static void setNationBalance(Nation nation, double balance, String reason) {
		try {
			nation.setBalance(balance, reason);
		} catch (EconomyException ex) {
			ex.printStackTrace();
		}
	}

	public static boolean isNationKing(Player p) {
		try {
			return TownyUniverse.getDataSource().getResident(p.getName()).isKing();
		} catch (NotRegisteredException ex) {
			ex.printStackTrace();
			return false;
		}
	}

	public static void truceQuestion(Nation receivingNation, Nation otherNation) {
		if (!receivingNation.hasValidUUID())
				receivingNation.setUuid(UUID.randomUUID());

		KingdomWars.getQuestionUtil().askQuestion("Would you like to accept a truce with " + otherNation.getName() + "? You will receive $"
				+ KingdomWars.getTruceAmount() + " from their nation bank.",
				receivingNation.getUuid(),
				() -> { WarUtil.win(receivingNation, otherNation, KingdomWars.getTruceAmount()); },
				() -> {
					sendNationMessage(receivingNation,
							"Your nation has declined the request to truce with " + otherNation.getName() + ".");
					sendNationMessage(otherNation, receivingNation.getName() + " has declined your request to truce.");
				},
				getOnlineInNation(receivingNation, "kingdomwars.nationstaff")
				);
	}

	public static void deleteNation(Nation nation) {
		TownyUniverse.getDataSource().removeNation(nation);
		WarUtil.leaderBoardNationDelete(nation.getName());
	}

	public static List<Player> getOnlineInNation(Nation nation, String permission) {
		List<Player> players = new ArrayList<>();
		List<String> residents = new ArrayList<>();

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
