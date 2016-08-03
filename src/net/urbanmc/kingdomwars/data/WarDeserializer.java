package net.urbanmc.kingdomwars.data;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

import com.google.gson.Gson;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;

public class WarDeserializer implements JsonDeserializer<WarList> {

	@Override
	public WarList deserialize(JsonElement element, Type type, JsonDeserializationContext context)
			throws JsonParseException {
		List<War> wars = new ArrayList<War>();

		Gson gson = new Gson();

		for (JsonElement je : element.getAsJsonObject().getAsJsonArray("wars")) {
			wars.add(gson.fromJson(je, War.class));
		}

		return new WarList(wars);
	}
}
