package net.urbanmc.kingdomwars.command.subs;

import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

public class Status {

	public Status(Player p, String[] args) {
		if (!p.hasPermission("kingdomwars.status")) {
			p.sendMessage(ChatColor.RED + "You do not have permission to do this!");
			return;
		}

		
	}
}
