package net.urbanmc.kingdomwars.command.subs;

import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

import com.palmergames.bukkit.towny.object.Nation;

import net.urbanmc.kingdomwars.KingdomWars;
import net.urbanmc.kingdomwars.TownyUtil;
import net.urbanmc.kingdomwars.WarUtil;
import net.urbanmc.kingdomwars.data.war.War;

public class Develop {
  public Develop(Player p, String[] args) {
		if (!p.hasPermission("kingdomwars.develop")) {
			p.sendMessage(ChatColor.RED + "You do not have permission to do this!");
			return;
		}
	
		//Purpose of this sub command is for developing use only.
		
		//Currently the sub command is a force command to win a war. Differs from the end command because it uses the win function.
	
		if(args.length != 2) return;
		
		Nation nation = TownyUtil.getNation(args[1]);

		if (nation == null) {
			p.sendMessage(ChatColor.RED + "You have not specified a valid nation.");
			return;
		}

		if (!WarUtil.inWar(nation)) {
			p.sendMessage(ChatColor.RED + "That nation is not in a war!");
			return;
		}

		War war = WarUtil.getWar(nation);
		Nation loser = (nation.getName().equalsIgnoreCase(war.getDeclaredNation())) ?  TownyUtil.getNation(war.getDeclaringNation()) : TownyUtil.getNation(war.getDeclaredNation());
		WarUtil.win(nation, loser, KingdomWars.getFinishAmount());
  }
}
