package net.urbanmc.kingdomwars.data.war;

import com.palmergames.bukkit.towny.object.Nation;
import net.urbanmc.kingdomwars.util.TownyUtil;
import org.bukkit.scoreboard.Scoreboard;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

public class War {

	private String nation1, nation2;
	private Set<String> nation1Allies = new HashSet<>(), nation2Allies = new HashSet<>();
	private int points1 = 0, points2 = 0, killsToWin;
	private Scoreboard board;
	private Set<UUID> disabled;
	private long started;

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

	public String getOtherNation(String nation) {
		if (nation1.equals(nation))
			return nation2;
		else if (nation2.equals(nation))
			return nation1;
		else
			return "";
	}

	public Nation getOtherNation(Nation nation) {
		if (nation1.equals(nation.getName()))
			return TownyUtil.getNation(nation2);
		else if (nation2.equals(nation.getName()))
			return TownyUtil.getNation(nation1);
		else
			return null;
	}

	public int getDeclaringPoints() {
		return points1;
	}

	public int getDeclaredPoints() {
		return points2;
	}

	public void setDeclaringNation(String nation) {
		this.nation1 = nation;
	}

	public void setDeclaredNation(String nation) {
		this.nation2 = nation;
	}

	public boolean isDeclaringNation(String nation) {
		return this.nation1.equals(nation);
	}

	void setDeclaringPoints(int points) {
		points1 = points;
	}

	void setDeclaredPoints(int points) {
		points2 = points;
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
			this.disabled = new HashSet<UUID>();
		}

		if (disable) {
			this.disabled.add(id);
		} else {
			this.disabled.remove(id);
		}
	}

	public Set<UUID> getDisabled() { return disabled; }

	public void setStarted() {
		this.started = System.currentTimeMillis();
	}

	public void setStarted(long started) {
		this.started = started;
	}

	public long getStarted() {
		return this.started;
	}

	public boolean hasAllies() {
		return !(nation1Allies.isEmpty() && nation2Allies.isEmpty());
	}

	public Set<String> getAllies(boolean declaring) {
		return declaring ? nation1Allies : nation2Allies;
	}

	public void addNation1Ally(String ally) {
			nation1Allies.add(ally);
	}

	public void addNation2Ally(String ally) {
			nation2Allies.add(ally);
	}

	public boolean isAllied(String nation) {
		return nation1Allies.contains(nation) || nation2Allies.contains(nation);
	}

	//1 = true; 0 = false; -1 = Not an ally
	public int isDeclaringAlly(String nation) {
		if (nation1Allies.contains(nation)) return 1;

		if (nation2Allies.contains(nation)) return 0;

		return -1;
	}

	public void renameAlly(String oldName, String newName, boolean declaring) {
		Set<String> ally = declaring ? nation1Allies : nation2Allies;

		ally.remove(oldName);
		ally.add(newName);
	}

	public void removeAlly(String allyName, boolean declaring) {
		Set<String> ally = declaring ? nation1Allies : nation2Allies;

		ally.remove(allyName);
	}

	//1 = true; 0 = false; -1 = One nation is not in the same war
	public int onSameSide(String nation1, String nation2) {
		int nation1DeclaringSide = isDeclaringSide(nation1);

		int nation2DeclaringSide = isDeclaringSide(nation2);

		if (nation1DeclaringSide == -1 || nation2DeclaringSide == -1) return -1;

		if (nation1DeclaringSide == nation2DeclaringSide) return 1;

		return 0;
	}

	//1 = true; 0 = false; -1 = One nation is not in the same war
	public int isDeclaringSide(String nation) {
		if (isDeclaringNation(nation)) return 1;

		int declaringAllyNation = isDeclaringAlly(nation);

		if (declaringAllyNation == 1) return 1;

		if (this.nation2.equalsIgnoreCase(nation) || declaringAllyNation == 0) return 0;

		return -1;
	}

	public void setKills(int killsToWin) {
		this.killsToWin = killsToWin;
	}

	public int getKillsToWin() { return killsToWin; }

	public boolean isInWar(String nation) {
		return nation1.equals(nation) || nation2.equals(nation) || (hasAllies() && isAllied(nation));
	}

	public Set<String> getAllNationNames() {
		final Set<String> nationNames = new HashSet<>();
		nationNames.add(nation1);
		nationNames.add(nation2);
		nationNames.addAll(nation1Allies);
		nationNames.addAll(nation2Allies);

		return nationNames;
	}


}
