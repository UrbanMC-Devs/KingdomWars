package net.urbanmc.kingdomwars.command.subs;

import net.md_5.bungee.api.chat.TextComponent;
import net.urbanmc.kingdomwars.KingdomWars;
import net.urbanmc.kingdomwars.data.war.War;
import net.urbanmc.kingdomwars.util.JSONMessageBuilder;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;

public class Wars {

	public Wars(Player p, final KingdomWars plugin) {
		if (!p.hasPermission("kingdomwars.wars")) {
			p.sendMessage(ChatColor.RED + "You do not have permission to do this!");
			return;
		}

		plugin.getWarManager().checkForceEndAll();

		p.sendMessage(ChatColor.GREEN + " === Current Wars ===");

		if (!plugin.getWarManager().getCurrentWars().isEmpty()) {
			JSONMessageBuilder builder = JSONMessageBuilder.create();

			final TextComponent vsComp = new TextComponent(" vs ");
			vsComp.setItalic(true);
			vsComp.setColor(net.md_5.bungee.api.ChatColor.WHITE);
			vsComp.setHoverEvent(null);

			List<War> wars = new ArrayList<>(plugin.getWarManager().getCurrentWars());

			int length = wars.size();

			for (int i = 0; i < length; i++) {
				War war = wars.get(i);

				builder.then(war.getDeclaringNation()).color(ChatColor.RED).style(ChatColor.BOLD).tooltip(""+war.getDeclaringPoints())
						.then(vsComp)
						.then(war.getDeclaredNation()).color(ChatColor.AQUA).style(ChatColor.BOLD).tooltip("" + war.getDeclaredPoints());

				if (i != (length - 1)) {
					builder.then("\n");
				}
			}

			builder.send(p);
		} else {
			p.sendMessage(ChatColor.ITALIC + "No Current Wars!");
		}

		p.sendMessage(ChatColor.GREEN + "=======");
	}
}
