package net.urbanmc.kingdomwars.data.last;

public class LastWar {

	private String nation1;
	private String nation2;
	private long millis;

	public LastWar(String nation1, String nation2, long millis) {
		this.nation1 = nation1;
		this.nation2 = nation2;
		this.millis = millis;
	}

	public String getDeclaringNation() {
		return this.nation1;
	}

	public String getDeclaredNation() {
		return this.nation2;
	}

	public long getMillis() {
		return this.millis;
	}
}
