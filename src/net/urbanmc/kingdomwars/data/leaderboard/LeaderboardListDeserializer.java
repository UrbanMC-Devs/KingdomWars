package net.urbanmc.kingdomwars.data.leaderboard;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

import com.google.gson.Gson;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;

import net.urbanmc.kingdomwars.data.last.LastWar;
import net.urbanmc.kingdomwars.data.last.LastWarList;

public class LeaderboardListDeserializer implements JsonDeserializer<LeaderboardList> {

	@Override
	public LeaderboardList deserialize(JsonElement element, Type type, JsonDeserializationContext context) throws JsonParseException {
		List<Leaderbrd> leaderboard = new ArrayList<Leaderbrd>();
		
		Gson gson = new Gson();

		for (JsonElement je : element.getAsJsonObject().getAsJsonArray("leaderboard")) {
			leaderboard.add(gson.fromJson(je, Leaderbrd.class));
		}

		return new LeaderboardList(leaderboard);
	}

}
