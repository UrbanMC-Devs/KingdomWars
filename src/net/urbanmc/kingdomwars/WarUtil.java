package net.urbanmc.kingdomwars;

import java.util.ArrayList;
import java.util.List;

import com.palmergames.bukkit.towny.object.Nation;

public class WarUtil {

	private static List<War> wars;

	static {
		loadWars();
	}

	private static void loadWars() {
		wars = new ArrayList<War>();

		// TODO: load wars from file
	}

	public static void startWar(Nation nation1, Nation nation2) {
		startWar(new War(wars.size(), nation1.getName(), nation2.getName()));
	}

	public static void startWar(War war) {
		wars.add(war);
	}

	public static void endWar(Nation nation) {
		endWar(getWar(nation));
	}

	public static void endWar(War war) {
		wars.remove(war);
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
}
