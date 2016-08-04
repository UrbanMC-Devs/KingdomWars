package net.urbanmc.kingdomwars.command.subs;

import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

import com.palmergames.bukkit.towny.TownyMessaging;
import com.palmergames.bukkit.towny.object.Nation;

import net.urbanmc.kingdomwars.TownyUtil;
import net.urbanmc.kingdomwars.WarUtil;
import net.urbanmc.kingdomwars.data.war.War;

public class Truce {

	public Truce(Player p, String label, String[] args) {
		if (!p.hasPermission("kingdomwars.truce")) {
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

		if (!war.getDeclaredNation().equals(nation.getName())) {
			p.sendMessage(
					ChatColor.RED + "You are the nation that started the war! You must do /" + label + " end instead.");
			return;
		}

		Nation receivingNation = TownyUtil.getNation(war.getDeclaringNation());

		TownyMessaging.sendNationMessage(nation, "Your nation has requested a truce with " + receivingNation.getName());
		TownyUtil.truceQuestion(receivingNation, nation);
	}
}
