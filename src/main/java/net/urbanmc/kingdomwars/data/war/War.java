package net.urbanmc.kingdomwars.data.war;

import com.palmergames.bukkit.towny.object.Nation;
import net.urbanmc.kingdomwars.data.WarAbstract;
import net.urbanmc.kingdomwars.data.WarStage;
import net.urbanmc.kingdomwars.util.TownyUtil;
import org.bukkit.scoreboard.Scoreboard;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

public class War extends WarAbstract {

	private int declaringPoints = 0, declaredPoints = 0,
				killsToWin;
	private transient Scoreboard board;
	private Set<UUID> disabled;

	public War(String declaringName, String declaredName) {
		super(declaringName, declaredName);
	}

	public Nation getOtherNation(Nation nation) {
		if (isDeclaringNation(nation.getName()))
			return TownyUtil.getNation(getDeclaredNation());
		else if (isDeclaredNation(nation.getName()))
			return TownyUtil.getNation(getDeclaringNation());
		else
			return null;
	}

	public int getDeclaringPoints() {
		return declaringPoints;
	}

	public int getDeclaredPoints() {
		return declaredPoints;
	}

	public void addPoints(Nation nation, int points) {
		if (isDeclaringNation(nation.getName())) {
			declaringPoints += points;
		} else if (isDeclaredNation(nation.getName())) {
			declaredPoints += points;
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
			this.disabled = new HashSet<>();
		}

		if (disable) {
			this.disabled.add(id);
		} else {
			this.disabled.remove(id);
		}
	}

	@Override
	public WarStage getWarStage() {
		return WarStage.FIGHTING;
	}

	public void setKills(int killsToWin) {
		this.killsToWin = killsToWin;
	}

	public int getKillsToWin() { return killsToWin; }


}
