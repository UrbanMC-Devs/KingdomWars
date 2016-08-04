package net.urbanmc.kingdomwars.data;

import java.lang.reflect.Type;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;

public class WarSerializer implements JsonSerializer<War>, JsonDeserializer<War> {

	@Override
	public JsonElement serialize(War war, Type type, JsonSerializationContext context) {
		JsonObject obj = new JsonObject();

		obj.addProperty("nation1", war.getDeclaringNation());
		obj.addProperty("nation2", war.getDeclaredNation());

		obj.addProperty("points1", war.getDeclaringPoints());
		obj.addProperty("points2", war.getDeclaredPoints());

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

		return war;
	}
}
