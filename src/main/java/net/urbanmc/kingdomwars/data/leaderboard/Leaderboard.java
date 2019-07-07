package net.urbanmc.kingdomwars.data.leaderboard;

public class Leaderboard {

	private int wins, losses;
	private String nation, lastWarInfo;

	public Leaderboard(String nation) {
		this.nation = nation;
		this.wins = 0;
		this.losses = 0;
	}

	public int getWins() {
		return this.wins;
	}

	public void setWins(int wins) {
		this.wins = wins;
	}

	public int getLosses() {
		return this.losses;
	}

	public void setLosses(int losses) {
		this.losses = losses;
	}

	public String getLastWarInfo() {
		return this.lastWarInfo;
	}

	public void setLastWarInfo(String lastWarInfo) {
		this.lastWarInfo = lastWarInfo;
	}

	public String getNation() {
		return this.nation;
	}

	public void setNation(String nation) {
		this.nation = nation;
	}
}
