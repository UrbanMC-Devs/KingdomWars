package net.urbanmc.kingdomwars.data;

import com.palmergames.bukkit.towny.object.Nation;

public class War {

	private int warId;
	private String nation1, nation2;
	private int points1 = 0, points2 = 0;

	public War(int warId, String nation1, String nation2) {
		this.warId = warId;
		this.nation1 = nation1;
		this.nation2 = nation2;
	}

	public int getWarId() {
		return this.warId;
	}

	public String getDeclaringNation() {
		return this.nation1;
	}

	public String getDeclaredNation() {
		return this.nation2;
	}

	public int getDeclaringPoints() {
		return points1;
	}

	public int getDeclaredPoints() {
		return points2;
	}

	public void addPoints(Nation nation, int points) {
		if (nation1.equals(nation.getName())) {
			points1 += points;
		} else if (nation2.equals(nation.getName())) {
			points2 += points;
		}
	}
}
