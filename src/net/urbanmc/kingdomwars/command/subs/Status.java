package net.urbanmc.kingdomwars.command.subs;

import java.util.UUID;

import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

import com.palmergames.bukkit.towny.object.Nation;

import net.urbanmc.kingdomwars.KingdomWars;
import net.urbanmc.kingdomwars.TownyUtil;
import net.urbanmc.kingdomwars.WarUtil;
import net.urbanmc.kingdomwars.data.War;

public class Status {

	public Status(Player p, String[] args) {
		if (!p.hasPermission("kingdomwars.status")) {
			p.sendMessage(ChatColor.RED + "You do not have permission to do this!");
			return;
		}

		Nation nation = TownyUtil.getNation(p);

		if (nation == null) {
			p.sendMessage(ChatColor.RED + "You are not in a nation!");
			return;
		}

		if (!WarUtil.inWar(nation)) {
			p.sendMessage(ChatColor.RED + "You are not in a war!");
			return;
		}

		War war = WarUtil.getWar(nation);

		UUID id = p.getUniqueId();

		war.setDisabled(id, !war.isDisabled(id));

		boolean disabled = war.isDisabled(id);

		if (!disabled) {
			KingdomWars.getWarBoard().showBoard(p);
		}

		p.sendMessage(ChatColor.GOLD + "War scoreboard has been " + (disabled ? "disabled" : "enabled") + ".");
	}
}
