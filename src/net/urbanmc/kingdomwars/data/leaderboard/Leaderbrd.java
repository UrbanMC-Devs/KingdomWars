package net.urbanmc.kingdomwars.data.leaderboard;

public class Leaderbrd {

	private int wins;
	private int losses;
	private String nation;
	private String lastwarinfo;
	
	public Leaderbrd(String nation) {
		this.nation = nation;
		wins = 0;
		losses = 0;
	}
	
	public int getWins() {
		return wins;
	}
	
	public void setWins(int wins) {
		this.wins = wins;
		return;
	}
	
	public int getLosses() {
		return losses;
	}
	
	public void setLosses(int losses) {
		this.losses = losses;
		return;
	}
	
	public String getLastWarInfo() {
		return lastwarinfo;
	}
	
	public void setLastWarInfo(String lastwarinfo) {
		this.lastwarinfo = lastwarinfo;
		return;
	}
	
	public String getNation() {
		return nation;
	}
	
	public void setNation(String nation) {
		this.nation = nation;
	}
	
}
