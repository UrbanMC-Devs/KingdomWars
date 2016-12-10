package net.urbanmc.kingdomwars.command.subs;

import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

import net.urbanmc.kingdomwars.WarUtil;
import net.urbanmc.kingdomwars.data.war.War;

public class Wars {

	public Wars(Player p, String[] args) {
		if (!p.hasPermission("kingdomwars.wars")) {
			p.sendMessage(ChatColor.RED + "You do not have permission to do this!");
			return;
		}

		WarUtil.checkForceEndAll();

		p.sendMessage(ChatColor.GREEN + " === Current Wars ===");

		if (!WarUtil.getWarList().isEmpty()) {
			for (War war : WarUtil.getWarList()) {
				p.sendMessage(ChatColor.BOLD + war.getDeclaringNation() + ChatColor.RESET + ChatColor.ITALIC + " vs "
						+ ChatColor.RESET + ChatColor.BOLD + war.getDeclaredNation());
			}
		} else {
			p.sendMessage(ChatColor.ITALIC + "No Current Wars!");
		}

		p.sendMessage(ChatColor.GREEN + "=======");
	}
}
