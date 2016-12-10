package net.urbanmc.kingdomwars.listener;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

import com.palmergames.bukkit.towny.event.DeleteNationEvent;
import com.palmergames.bukkit.towny.event.RenameNationEvent;

import net.urbanmc.kingdomwars.WarUtil;

public class NationListener implements Listener {

	@EventHandler
	public void renameNation(RenameNationEvent e) {
		String oldName = e.getOldName(), newName = e.getNation().getName();

		if (WarUtil.inWar(oldName)) {
			WarUtil.warNationRename(oldName, newName);
		}

		WarUtil.lastNationRename(oldName, newName);
		WarUtil.leaderBoardNationRename(oldName, newName);
	}

	@EventHandler
	public void deleteNation(DeleteNationEvent e) {
		String nation = e.getNationName();

		if (WarUtil.inWar(nation)) {
			WarUtil.endWar(WarUtil.getWar(nation));
		}

		WarUtil.removeAllLast(nation);

		WarUtil.leaderBoardNationDelete(nation);
	}
}
