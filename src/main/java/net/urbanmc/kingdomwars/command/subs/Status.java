package net.urbanmc.kingdomwars.command.subs;

import java.util.UUID;

import net.urbanmc.kingdomwars.KingdomWars;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

import com.palmergames.bukkit.towny.object.Nation;

import net.urbanmc.kingdomwars.util.TownyUtil;
import net.urbanmc.kingdomwars.WarBoard;
import net.urbanmc.kingdomwars.data.war.War;

public class Status {

	public Status(Player p, final KingdomWars plugin) {
		if (!p.hasPermission("kingdomwars.status")) {
			p.sendMessage(ChatColor.RED + "You do not have permission to do this!");
			return;
		}

		Nation nation = TownyUtil.getNation(p);

		if (nation == null) {
			p.sendMessage(ChatColor.RED + "You are not in a nation!");
			return;
		}

		if (!plugin.getWarManager().inWar(nation)) {
			p.sendMessage(ChatColor.RED + "You are not in a war!");
			return;
		}

		War war = plugin.getWarManager().getWar(nation);

		UUID id = p.getUniqueId();

		boolean disabled = war.isDisabled(id);

		war.setDisabled(id, !disabled);

		if (disabled) {
			WarBoard.showBoard(plugin, p);
		} else {
			p.setScoreboard(Bukkit.getScoreboardManager().getNewScoreboard());
		}

		p.sendMessage(ChatColor.GOLD + "War scoreboard has been " + (disabled ? "enabled" : "disabled") + ".");
	}
}
