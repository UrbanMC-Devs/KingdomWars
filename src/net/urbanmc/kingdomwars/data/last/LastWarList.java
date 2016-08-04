package net.urbanmc.kingdomwars.data.last;

import java.util.List;

public class LastWarList {

	private List<LastWar> last;

	public LastWarList(List<LastWar> last) {
		this.last = last;
	}

	public List<LastWar> getLast() {
		return this.last;
	}
}
