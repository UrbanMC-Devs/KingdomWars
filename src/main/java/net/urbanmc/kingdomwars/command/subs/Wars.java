package net.urbanmc.kingdomwars.command.subs;

import net.urbanmc.kingdomwars.WarUtil;
import net.urbanmc.kingdomwars.data.war.War;
import net.urbanmc.kingdomwars.util.JSONMessage;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

import java.util.ArrayList;

public class Wars {

	public Wars(Player p, String[] args) {
		if (!p.hasPermission("kingdomwars.wars")) {
			p.sendMessage(ChatColor.RED + "You do not have permission to do this!");
			return;
		}

		WarUtil.checkForceEndAll();

		p.sendMessage(ChatColor.GREEN + " === Current Wars ===");

		if (!WarUtil.getWarList().isEmpty()) {
			for (War war : new ArrayList<>(WarUtil.getWarList())) {
				JSONMessage.create(war.getDeclaringNation()).style(ChatColor.BOLD).tooltip(""+ war.getDeclaringPoints())
						.then(" vs ").style(ChatColor.ITALIC)
						.then(war.getDeclaredNation()).style(ChatColor.BOLD).tooltip("" + war.getDeclaredPoints())
						.send(p);
			}
		} else {
			p.sendMessage(ChatColor.ITALIC + "No Current Wars!");
		}

		p.sendMessage(ChatColor.GREEN + "=======");
	}
}
