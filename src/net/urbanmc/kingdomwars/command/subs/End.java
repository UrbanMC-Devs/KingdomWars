package net.urbanmc.kingdomwars.command.subs;

import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

public class End {

	public End(Player p, String[] args) {
		if (!p.hasPermission("kingdomwars.end")) {
			p.sendMessage(ChatColor.RED + "You do not have permission to do this!");
			return;
		}

		
	}
}
