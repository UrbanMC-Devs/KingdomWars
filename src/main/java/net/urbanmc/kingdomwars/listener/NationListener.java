package net.urbanmc.kingdomwars.listener;

import com.palmergames.bukkit.towny.TownyAPI;
import com.palmergames.bukkit.towny.event.NewNationEvent;
import com.palmergames.bukkit.towny.object.Nation;
import net.urbanmc.kingdomwars.KingdomWars;
import net.urbanmc.kingdomwars.data.PreWar;
import net.urbanmc.kingdomwars.data.war.War;
import net.urbanmc.kingdomwars.util.TownyUtil;
import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;

import com.palmergames.bukkit.towny.event.DeleteNationEvent;
import com.palmergames.bukkit.towny.event.RenameNationEvent;

import net.urbanmc.kingdomwars.WarUtil;

public class NationListener implements Listener {

	private KingdomWars plugin;

	public NationListener(KingdomWars plugin) {
		this.plugin = plugin;
	}

	@EventHandler (priority = EventPriority.MONITOR, ignoreCancelled = true)
	public void onNationCreate(NewNationEvent event) {
		WarUtil.createNation(event.getNation().getName());
	}


	@EventHandler
	public void renameNation(RenameNationEvent e) {
		String oldName = e.getOldName(), newName = e.getNation().getName();

		WarUtil.renameGraceNation(oldName, newName);

		if (WarUtil.inWar(oldName)) {
			War war = WarUtil.getWar(oldName);

			int ally = war.isDeclaringAlly(oldName);

			if (ally != -1)
				war.renameAlly(oldName, newName, ally == 1);

			else WarUtil.warNationRename(oldName, newName);
		}

		if (WarUtil.alreadyScheduledForWar(oldName)) {
			PreWar preWar = WarUtil.getPreWar(oldName);

			preWar.renameNation(oldName, newName);
		}

		WarUtil.lastNationRename(oldName, newName);
		WarUtil.leaderBoardNationRename(oldName, newName);
	}

	@EventHandler
	public void deleteNation(DeleteNationEvent e) {
		String nation = e.getNationName();

		if (WarUtil.inWar(nation)) {
			War war = WarUtil.getWar(nation);

			int ally = war.isDeclaringAlly(nation);

			if (ally != -1)
				war.removeAlly(nation, ally == 1);

			else {
				Bukkit.getScheduler().runTask(plugin, () -> {
					WarUtil.endWar(WarUtil.getWar(nation));
				});
			}
		}

		if (WarUtil.alreadyScheduledForWar(nation)) {
			PreWar preWar = WarUtil.getPreWar(nation);

			if (preWar.isMainNation(nation)) {
				Nation otherNation = TownyUtil.getNation(preWar.getOtherNation(nation));

				if (otherNation != null)
					TownyUtil.sendNationMessage(otherNation, "The war against " + nation + " has been cancelled because they were disbanded!");

				preWar.cancelTask();

				WarUtil.removePreWar(preWar);
			}

		}

		WarUtil.removeAllLast(nation);

		WarUtil.leaderBoardNationDelete(nation);
	}
}
