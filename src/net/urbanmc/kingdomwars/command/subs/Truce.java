package net.urbanmc.kingdomwars.command.subs;

import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

import com.palmergames.bukkit.towny.TownyMessaging;
import com.palmergames.bukkit.towny.object.Nation;

import net.urbanmc.kingdomwars.TownyUtil;
import net.urbanmc.kingdomwars.WarUtil;

public class Truce {

	public Truce(Player p, String[] args) {
		if (!p.hasPermission("kingdomwars.truce")) {
			p.sendMessage(ChatColor.RED + "You do not have permission to do this!");
			return;
		}

		Nation nation1 = TownyUtil.getNation(p);

		if (nation1 == null) {
			p.sendMessage(ChatColor.RED + "You are not in a nation!");
			return;
		}

		if (!WarUtil.inWar(nation1)) {
			p.sendMessage(ChatColor.RED + "You are not in a war!");
			return;
		}
		
		String onation = (WarUtil.getWar(nation1).getDeclaringNation().equalsIgnoreCase(nation1.getName()))
				? nation1.getName() : WarUtil.getWar(nation1).getDeclaredNation();
				
		TownyMessaging.sendNationMessage(nation1, "Your nation has requested a truce with " + onation);
		TownyUtil.truceQuestion(TownyUtil.getNation(onation), nation1);
	}
}
