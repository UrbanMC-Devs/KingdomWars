package net.urbanmc.kingdomwars.data;

public class Leaderboard implements Comparable<Leaderboard> {

	private int wins, losses;
	private String nation;

	public Leaderboard(String nation) {
		this.nation = nation;
		this.wins = 0;
		this.losses = 0;
	}

	public Leaderboard(String nation, int wins, int losses) {
		this.nation = nation;
		this.wins = wins;
		this.losses = losses;
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

	public String getNation() {
		return this.nation;
	}

	public void setNation(String nation) {
		this.nation = nation;
	}

	@Override
	public int compareTo(Leaderboard lb) {
		if (this.wins == lb.wins) {
			return this.losses - lb.losses;
		}
		return lb.wins - this.wins;
	}
}
