package net.urbanmc.kingdomwars.listener;

import com.palmergames.bukkit.towny.event.NewNationEvent;
import com.palmergames.bukkit.towny.object.Nation;
import net.urbanmc.kingdomwars.KingdomWars;
import net.urbanmc.kingdomwars.WarBoard;
import net.urbanmc.kingdomwars.data.PreWar;
import net.urbanmc.kingdomwars.data.war.War;
import net.urbanmc.kingdomwars.util.TownyUtil;
import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;

import com.palmergames.bukkit.towny.event.DeleteNationEvent;
import com.palmergames.bukkit.towny.event.RenameNationEvent;

public class NationListener implements Listener {

	private KingdomWars plugin;

	public NationListener(KingdomWars plugin) {
		this.plugin = plugin;
	}

	@EventHandler (priority = EventPriority.MONITOR, ignoreCancelled = true)
	public void onNationCreate(NewNationEvent event) {
		plugin.getWarManager().createNation(event.getNation().getName());
	}


	@EventHandler
	public void renameNation(RenameNationEvent e) {
		String oldName = e.getOldName(), newName = e.getNation().getName();

		plugin.getWarManager().renameGraceNation(oldName, newName);

		if (plugin.getWarManager().inWar(oldName)) {
			War war = plugin.getWarManager().getWar(oldName);

			int ally = war.isDeclaringAlly(oldName);

			if (ally != -1)
				war.renameAlly(oldName, newName, ally == 1);

			else plugin.getWarManager().renameWarNation(oldName, newName);
		}

		if (plugin.getWarManager().alreadyScheduledForWar(oldName)) {
			PreWar preWar = plugin.getWarManager().getPreWar(oldName);

			preWar.renameNation(oldName, newName);
		}

		plugin.getLastWarManager().lastNationRename(oldName, newName);

		plugin.getLeaderboard().renameNation(oldName, newName);

	}

	@EventHandler
	public void deleteNation(DeleteNationEvent e) {
		String nation = e.getNationName();

		if (plugin.getWarManager().inWar(nation)) {
			War war = plugin.getWarManager().getWar(nation);

			int ally = war.isDeclaringAlly(nation);

			if (ally != -1) {
				war.removeAlly(nation, ally == 1);
				WarBoard.removeAllyFromBoard(war, nation, ally == 1);
				plugin.getWarManager().saveCurrentWars();
			}

			else {
				// We have to run a task because DeleteNation can be ran async.
				Bukkit.getScheduler().runTask(plugin, () -> {
					plugin.getWarManager().winByDeletion(war, nation);
				});
			}
		}

		if (plugin.getWarManager().alreadyScheduledForWar(nation)) {
			PreWar preWar = plugin.getWarManager().getPreWar(nation);

			if (preWar.isMainNation(nation)) {
				Nation otherNation = TownyUtil.getNation(preWar.getOtherNation(nation));

				if (otherNation != null)
					TownyUtil.sendNationMessage(otherNation, "The war against " + nation + " has been cancelled because they were disbanded!");

				preWar.cancelTask();

				plugin.getWarManager().removePreWar(preWar);
			}
		}

		plugin.getLastWarManager().removeAllLastWars(nation);

		plugin.getLeaderboard().deleteNationFromLeaderboard(nation);
	}
}
