package net.urbanmc.kingdomwars.command.subs;

import net.urbanmc.kingdomwars.KingdomWars;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

public class SaveSub {

    public SaveSub(Player p, final KingdomWars plugin) {
        if (!p.hasPermission("kingdomwars.save")) {
            p.sendMessage(ChatColor.RED + "You do not have permission to do this!");
            return;
        }

        plugin.getWarManager().saveCurrentWars();
        plugin.getLeaderboard().saveLeaderboard();
        plugin.getLastWarManager().saveLastWars();
        p.sendMessage(ChatColor.GREEN + "KingdomWars has been saved!");
    }

}
