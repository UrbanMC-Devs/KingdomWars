package net.urbanmc.kingdomwars.command.subs;

import java.util.logging.Level;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

import com.palmergames.bukkit.towny.exceptions.NotRegisteredException;
import com.palmergames.bukkit.towny.object.Nation;
import com.palmergames.bukkit.towny.object.TownyUniverse;

import net.urbanmc.kingdomwars.TownyUtil;
import net.urbanmc.kingdomwars.WarUtil;
import net.urbanmc.kingdomwars.data.War;
import net.urbanmc.kingdomwars.event.WarEndEvent;

public class End {

	public End(Player p, String[] args) {
		if (!p.hasPermission("kingdomwars.end")) {
			p.sendMessage(ChatColor.RED + "You do not have permission to do this!");
			return;
		}

		Nation nation1;

		try {
			nation1 = TownyUniverse.getDataSource().getResident(p.getName()).getTown().getNation();
		} catch (NotRegisteredException ex) {
			p.sendMessage(ChatColor.RED + "You are not in a nation!");
			return;
		}

		if (!WarUtil.inWar(nation1)) {
			p.sendMessage(ChatColor.RED + "Your nation is not in a war!");
			return;
		}

		War war = WarUtil.getWar(nation1);

		if (!war.getDeclaringNation().equalsIgnoreCase(nation1.getName())) {
			p.sendMessage(ChatColor.RED + "Your nation cannot end the war because your nation did not start it!");
			return;
		}

		WarEndEvent event = new WarEndEvent(war);
		Bukkit.getPluginManager().callEvent(event);

		if (event.isCancelled())
			return;

		WarUtil.endWar(war);

		Nation nation2 = TownyUtil.getNation(war.getDeclaredNation());

		if (nation2 == null) {
			Bukkit.getLogger().log(Level.WARNING,
					"[KingdomWars] Error while getting nation " + war.getDeclaredNation());
			return;
		}

		TownyUtil.sendNationMessage(nation1, "Your nation has ended the war against " + nation2.getName() + ".");
		TownyUtil.sendNationMessage(nation2, nation1.getName() + " has ended the war against your nation.");
	}
}
