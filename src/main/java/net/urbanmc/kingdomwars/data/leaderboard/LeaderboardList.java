package net.urbanmc.kingdomwars.data.leaderboard;

import java.util.List;

public class LeaderboardList {

	private List<Leaderboard> leaderboards;

	public LeaderboardList(List<Leaderboard> leaderboards) {
		this.leaderboards = leaderboards;
	}

	public List<Leaderboard> getLeaderboards() {
		return this.leaderboards;
	}
}
