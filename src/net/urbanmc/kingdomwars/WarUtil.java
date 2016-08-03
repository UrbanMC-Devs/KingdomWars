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
		// TODO: Start war method
	}

	public static void endWar(Nation nation1, Nation nation2) {
		// TODO: End war method
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
