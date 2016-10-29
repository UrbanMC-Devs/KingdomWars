package net.urbanmc.kingdomwars.data.leaderboard;

import java.util.List;

import net.urbanmc.kingdomwars.data.last.LastWar;

public class LeaderboardList {

	private List<Leaderbrd> leaderboard;

	public LeaderboardList(List<Leaderbrd> leaderboard) {
		this.leaderboard = leaderboard;
	}

	public List<Leaderbrd> getLeaderboards() {
		return this.leaderboard;
	}
	
}
