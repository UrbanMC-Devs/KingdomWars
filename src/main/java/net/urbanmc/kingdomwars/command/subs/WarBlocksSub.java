package net.urbanmc.kingdomwars.command.subs;

import com.palmergames.bukkit.towny.object.Nation;
import net.urbanmc.kingdomwars.util.TownyUtil;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

public class WarBlocksSub {

    public WarBlocksSub(Player p, String[] args) {
        if (!p.hasPermission("kingdomwars.warblocks")) {
            p.sendMessage(ChatColor.RED + "You do not have permission to run this command!");
            return;
        }

        if (args.length == 1) {
            p.sendMessage(ChatColor.RED + "Please specify a nation!");
            return;
        }

        Nation targetNation = TownyUtil.getNation(args[1]);

        if (targetNation == null) {
            p.sendMessage(ChatColor.RED + "Invalid nation specified!");
            return;
        }

        int warBlocks = TownyUtil.getNationWarBlocks(targetNation);

        if (args.length == 2) {
            p.sendMessage(ChatColor.GREEN + "Current War Townblocks: " + warBlocks);
        }
        else if (args.length == 3) {
            // Set the amount of war blocks
            int amount;

            try {
                amount = Integer.parseInt(args[2]);
            } catch (NumberFormatException ex) {
                p.sendMessage(ChatColor.RED + "Please enter a valid integer!");
                return;
            }

            TownyUtil.addNationWarBlocks(targetNation, amount - warBlocks);
            p.sendMessage(ChatColor.GREEN + "Changed nation's war townblocks to " + amount + " from " + warBlocks);
        }
    }

}
