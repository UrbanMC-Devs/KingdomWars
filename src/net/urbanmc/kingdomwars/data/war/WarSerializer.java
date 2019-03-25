package net.urbanmc.kingdomwars.data.war;

import java.lang.reflect.Type;

import com.google.gson.*;

public class WarSerializer implements JsonSerializer<War>, JsonDeserializer<War> {

	@Override
	public JsonElement serialize(War war, Type type, JsonSerializationContext context) {
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

		return obj;
	}

	@Override
	public War deserialize(JsonElement element, Type type, JsonDeserializationContext context) throws JsonParseException {
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

		return war;
	}
}
