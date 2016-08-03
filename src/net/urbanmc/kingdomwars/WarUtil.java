package net.urbanmc.kingdomwars;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

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
		startWar(createWar(nation1, nation2));
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

	public static War createWar(Nation nation1, Nation nation2) {
		return new War(wars.size(), nation1.getName(), nation2.getName());
	}

	private static void saveFile() {
		try {
			PrintWriter writer = new PrintWriter(new File("plugins/KindomWars/wars.json"));

			WarList list = new WarList(wars);

			writer.write(new Gson().toJson(list));
		} catch (IOException ex) {
			ex.printStackTrace();
		}
	}
}
