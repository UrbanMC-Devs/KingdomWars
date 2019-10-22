package net.urbanmc.kingdomwars.data.war;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class WarSerializer {

	public static JsonElement serialize(List<War> wars) {
		JsonArray array = new JsonArray();

		for (War war : wars) {
			array.add(serialize(war));
		}

		return array;
	}

	public static JsonObject serialize(War war) {
		JsonObject obj = new JsonObject();

		obj.addProperty("nation1", war.getDeclaringNation());
		obj.addProperty("nation2", war.getDeclaredNation());

		obj.addProperty("points1", war.getDeclaringPoints());
		obj.addProperty("points2", war.getDeclaredPoints());

		if (war.hasAllies()) {
			JsonArray nation1Allies = new JsonArray(), nation2Allies = new JsonArray();

			war.getAllies(true).forEach(nation1Allies::add);
			war.getAllies(false).forEach(nation2Allies::add);

			obj.add("nation1Allies", nation1Allies);
			obj.add("nation2Allies", nation2Allies);
		}

		obj.addProperty("killsToWin", war.getKillsToWin());

		obj.addProperty("startTime", war.getStarted());

		if (war.getDisabled() != null) {
			JsonArray disabledArray = new JsonArray();

			for (UUID uuid : war.getDisabled()) {
				disabledArray.add(uuid.toString());
			}

			obj.add("scoreboardDisabled", disabledArray);
		}

		return obj;
	}

	public static List<War> deserializeWars(JsonElement element) throws IllegalArgumentException {
		if (!element.isJsonArray())
			throw new IllegalArgumentException("JSON Element is not a json array!");

		JsonArray array = element.getAsJsonArray();

		List<War> wars = new ArrayList<>();

		for (JsonElement warObject : array) {
			wars.add(deserializeWar(warObject));
		}

		return wars;
	}

	public static War deserializeWar(JsonElement element) throws JsonParseException {
		JsonObject obj = element.getAsJsonObject();

		String nation1 = obj.get("nation1").getAsString();
		String nation2 = obj.get("nation2").getAsString();

		int points1 = obj.get("points1").getAsInt();
		int points2 = obj.get("points2").getAsInt();

		War war = new War(nation1, nation2);

		war.setDeclaringPoints(points1);
		war.setDeclaredPoints(points2);

		if (obj.has("nation1Allies")) {
			for (JsonElement el : obj.getAsJsonArray("nation1Allies")) {
				war.addNation1Ally(el.getAsString());
			}
		}

		if (obj.has("nation2Allies")) {
			for (JsonElement el : obj.getAsJsonArray("nation2Allies")) {
				war.addNation2Ally(el.getAsString());
			}
		}

		war.setKills(obj.get("killsToWin").getAsInt());

		if (obj.has("startTime")) {
			war.setStarted(obj.get("startTime").getAsLong());
		} else {
			war.setStarted();
		}

		if (obj.has("scoreboardDisabled")) {
			JsonArray disabledArray = obj.getAsJsonArray("scoreboardDisabled");

			for (JsonElement jsonElement : disabledArray) {
				war.setDisabled(UUID.fromString(jsonElement.getAsString()),  true);
			}
		}

		return war;
	}
}
