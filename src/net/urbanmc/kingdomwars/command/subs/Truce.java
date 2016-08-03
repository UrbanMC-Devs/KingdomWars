package net.urbanmc.kingdomwars.command.subs;

import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

public class Truce {
	
	public Truce(Player p, String[] args) {
		if (!p.hasPermission("kingdomwars.truce")) {
			p.sendMessage(ChatColor.RED + "You do not have permission to do this!");
			return;
		}

		
	}	
}
