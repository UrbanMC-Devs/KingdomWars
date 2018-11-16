package net.urbanmc.kingdomwars.data.war;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;

public class WarListSerializer implements JsonSerializer<WarList>, JsonDeserializer<WarList> {

	@Override
	public JsonElement serialize(WarList list, Type type, JsonSerializationContext context) {
		JsonArray array = new JsonArray();

		Gson gson = new GsonBuilder().registerTypeAdapter(War.class, new WarSerializer()).create();

		for (War war : list.getWars()) {
			array.add(gson.toJsonTree(war));
        }

		return array;
	}

	@Override
	public WarList deserialize(JsonElement element, Type type, JsonDeserializationContext context)
			throws JsonParseException {
		List<War> wars = new ArrayList<>();

		Gson gson = new GsonBuilder().registerTypeAdapter(War.class, new WarSerializer()).create();

		for (JsonElement je : element.getAsJsonArray()) {
			wars.add(gson.fromJson(je, War.class));
		}

		return new WarList(wars);
	}
}
