package net.urbanmc.kingdomwars;

import com.palmergames.bukkit.towny.TownyMessaging;
import com.palmergames.bukkit.towny.exceptions.NotRegisteredException;
import com.palmergames.bukkit.towny.object.Nation;
import com.palmergames.bukkit.towny.object.TownyUniverse;

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
}
