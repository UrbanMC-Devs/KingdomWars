package net.urbanmc.kingdomwars.command.subs;

import com.palmergames.bukkit.towny.object.Nation;
import net.urbanmc.kingdomwars.KingdomWars;
import net.urbanmc.kingdomwars.util.TownyUtil;
import net.urbanmc.kingdomwars.data.war.War;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

import java.util.logging.Level;

public class End {

	public End(Player p, final KingdomWars plugin) {
		if (!p.hasPermission("kingdomwars.end")) {
			p.sendMessage(ChatColor.RED + "You do not have permission to do this!");
			return;
		}

		Nation nation1 = TownyUtil.getNation(p);

		if (nation1 == null) {
			p.sendMessage(ChatColor.RED + "You are not in a nation!");
			return;
		}

		if (!plugin.getWarManager().inWar(nation1)) {
			p.sendMessage(ChatColor.RED + "Your nation is not in a war!");
			return;
		}

		War war = plugin.getWarManager().getWar(nation1);

		String nationName = nation1.getName();

		int declaringPoints = war.getDeclaringPoints(), declaredPoints = war.getDeclaredPoints();

		if (war.isDeclaringNation(nationName) && declaredPoints > declaringPoints ||
				!war.isDeclaringNation(nationName) && declaringPoints > declaredPoints) {
			p.sendMessage(ChatColor.RED + "Your nation cannot end the war because your nation is losing!");
			return;
		} else if (declaringPoints == declaredPoints && !war.isDeclaringNation(nationName)) {
			p.sendMessage(ChatColor.RED + "Your nation cannot end the war because your nation did not start it!");
			return;
		}

		plugin.getWarManager().end(war);

		Nation nation2 = war.getOtherNation(nation1);

		if (nation2 == null) {
			Bukkit.getLogger()
					.log(Level.WARNING, "[KingdomWars] Error while getting nation " + war.getDeclaredNation());
			return;
		}

		TownyUtil.sendNationMessage(nation1, "Your nation has ended the war against " + nation2.getName() + ".");
		TownyUtil.sendNationMessage(nation2, nation1.getName() + " has ended the war against your nation.");
	}
}
