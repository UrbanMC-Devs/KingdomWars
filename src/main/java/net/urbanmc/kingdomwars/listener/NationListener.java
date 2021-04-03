package net.urbanmc.kingdomwars.listener;

import com.palmergames.bukkit.towny.event.DeleteNationEvent;
import com.palmergames.bukkit.towny.event.NewNationEvent;
import com.palmergames.bukkit.towny.event.PreDeleteNationEvent;
import com.palmergames.bukkit.towny.event.RenameNationEvent;
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

import java.util.UUID;

public class NationListener implements Listener {

	private KingdomWars plugin;

	public NationListener(KingdomWars plugin) {
		this.plugin = plugin;
	}

	@EventHandler
	public void createNation(NewNationEvent event) {
		Nation nat = event.getNation();
		plugin.getArchiveManager().createNation(nat.getUUID(), nat.getName());
	}

	@EventHandler
	public void renameNation(RenameNationEvent e) {
		String oldName = e.getOldName(), newName = e.getNation().getName();

		plugin.updateNationName(e.getNation().getUUID(), oldName, newName);
	}

	@EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
	public void preDeleteNation(PreDeleteNationEvent e) {
		String nation = e.getNationName();

		if (plugin.getWarManager().inWar(nation)) {
			War war = plugin.getWarManager().getWar(nation);

			if (war.isAlly(nation)) {
				war.removeAlly(nation);
				WarBoard.removeAllyFromBoard(war, nation);
				plugin.getWarManager().saveCurrentWars();
			}
			else {
				// We have to run a task because DeleteNation can be ran async.
				Bukkit.getScheduler().runTask(plugin, () -> plugin.getWarManager().winByDeletion(war, nation));
			}
		}

		if (plugin.getWarManager().alreadyScheduledForWar(nation)) {
			PreWar preWar = plugin.getWarManager().getPreWar(nation);

			if (preWar.isMainNation(nation)) {
				Nation otherNation = TownyUtil.getNation(preWar.getOtherNation(nation));

				if (otherNation != null)
					TownyUtil.sendNationMessage(otherNation, "The war against " + nation + " has been cancelled because they were disbanded!");

				preWar.cancelTask();
				plugin.getWarManager().cancelDeclaredWar(preWar);
			}
		}
	}

	@EventHandler
	public void deleteNation(DeleteNationEvent event) {
		final String nationName = event.getNationName();
		final UUID nationUUID = event.getNationUUID();

		plugin.getArchiveManager().removeRecentWars(nationName);
		plugin.getLeaderboard().deleteNationFromLeaderboard(nationUUID, nationName);
	}
}
