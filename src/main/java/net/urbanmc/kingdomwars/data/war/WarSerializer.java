package net.urbanmc.kingdomwars.data.war;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import net.urbanmc.kingdomwars.KingdomWars;
import net.urbanmc.kingdomwars.data.LastWar;
import net.urbanmc.kingdomwars.data.PreWar;
import net.urbanmc.kingdomwars.data.WarAbstract;
import net.urbanmc.kingdomwars.data.WarStage;
import org.bukkit.Bukkit;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class WarSerializer {

	public static JsonElement serialize(Gson gson, Collection<WarAbstract> wars) {
		JsonArray array = new JsonArray();

		for (WarAbstract war : wars) {
			JsonElement element = gson.toJsonTree(war);
			element.getAsJsonObject().addProperty("stage", war.getWarStage().name());
			array.add(element);
		}

		return array;
	}

	public static Collection<WarAbstract> deserializeWars(Gson gson, JsonElement element) throws IllegalArgumentException {
		if (!element.isJsonArray())
			throw new IllegalArgumentException("JSON Element is not a json array!");

		JsonArray array = element.getAsJsonArray();

		List<WarAbstract> wars = new ArrayList<>();

		for (JsonElement warObject : array) {
			WarAbstract war = deserializeWar(gson, warObject);
			if (war != null) {
				wars.add(war);
			}
		}

		return wars;
	}

	public static WarAbstract deserializeWar(Gson gson, JsonElement element) throws JsonParseException {
		JsonObject obj = element.getAsJsonObject();

		if (obj.has("stage")) {
			WarStage stage = WarStage.valueOf(obj.get("stage").getAsString());

			switch (stage) {
				case DECLARED:
					return gson.fromJson(obj, PreWar.class);
				case FIGHTING:
					return gson.fromJson(obj, War.class);
				case ARCHIVED:
					return gson.fromJson(obj, LastWar.class);
			}
		}
		else {
			KingdomWars.logger().warning("Unable to deserialize a war because no war stage was found! Json: " + element.toString());
		}
		return null;
	}
}
