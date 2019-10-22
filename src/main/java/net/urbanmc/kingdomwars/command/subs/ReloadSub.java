package net.urbanmc.kingdomwars.command.subs;

import net.urbanmc.kingdomwars.KingdomWars;
import net.urbanmc.kingdomwars.manager.ConfigManager;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

public class ReloadSub {

    public ReloadSub(Player p, final KingdomWars plugin) {
        if (!p.hasPermission("kingdomwars.reload")) {
            p.sendMessage(ChatColor.RED + "You do not have permission to do this!");
            return;
        }

        new ConfigManager();
        plugin.getWarManager().loadCurrentWars();
        plugin.getLeaderboard().loadLeaderboard();
        plugin.getLastWarManager().loadLastWars();
        p.sendMessage(ChatColor.GREEN + "KingdomWars has been reloaded!");
    }

}
