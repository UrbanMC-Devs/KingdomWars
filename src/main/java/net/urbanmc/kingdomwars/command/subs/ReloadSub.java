package net.urbanmc.kingdomwars.command.subs;

import net.urbanmc.kingdomwars.util.ConfigManager;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

public class ReloadSub {

    public ReloadSub(Player p, String[] args) {
        if (!p.hasPermission("kingdomwars.reload")) {
            p.sendMessage(ChatColor.RED + "You do not have permission to do this!");
            return;
        }

        new ConfigManager();
        p.sendMessage(ChatColor.GREEN + "KingdomWars has been reloaded!");
    }

}
