package net.urbanmc.kingdomwars;

public class War {

	private int warId;
	private String nation1, nation2;

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
}
