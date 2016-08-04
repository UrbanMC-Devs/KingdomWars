package net.urbanmc.kingdomwars.data.last;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

import com.google.gson.Gson;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;

public class LastWarListDeserializer implements JsonDeserializer<LastWarList> {

	@Override
	public LastWarList deserialize(JsonElement element, Type type, JsonDeserializationContext context) throws JsonParseException {
		List<LastWar> last = new ArrayList<LastWar>();

		Gson gson = new Gson();

		for (JsonElement je : element.getAsJsonObject().getAsJsonArray("last")) {
			last.add(gson.fromJson(je, LastWar.class));
		}

		return new LastWarList(last);
	}
}
