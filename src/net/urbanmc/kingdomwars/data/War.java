package net.urbanmc.kingdomwars.data;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.bukkit.scoreboard.Scoreboard;

import com.palmergames.bukkit.towny.object.Nation;

public class War {

	private String nation1, nation2;
	private int points1 = 0, points2 = 0;
	private Scoreboard board;
	private List<UUID> disabled;

	public War(String nation1, String nation2) {
		this.nation1 = nation1;
		this.nation2 = nation2;
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

	public void setScoreBoard(Scoreboard board) {
		this.board = board;
		return;
	}

	public Scoreboard getScoreBoard() {
		return this.board;
	}

	public boolean isDisabled(UUID id) {
		if (this.disabled == null)
			return false;

		return this.disabled.contains(id);
	}

	public void setDisabled(UUID id, boolean disable) {
		if (this.disabled == null) {
			this.disabled = new ArrayList<UUID>();
		}

		if (disable) {
			this.disabled.add(id);
		} else {
			this.disabled.remove(id);
		}
	}
}
