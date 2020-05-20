package net.urbanmc.kingdomwars.command.subs;

import com.palmergames.bukkit.towny.object.Nation;
import net.urbanmc.kingdomwars.KingdomWars;
import net.urbanmc.kingdomwars.util.TownyUtil;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

public class AcceptDenySub {

    public AcceptDenySub(Player p, String arg, final KingdomWars plugin) {
        if (!p.hasPermission("kingdomwars.truce")) {
            p.sendMessage(ChatColor.RED + "You do not have permission to do this!");
            return;
        }

        Nation nation = TownyUtil.getNation(p);

        if (nation == null) {
            p.sendMessage(ChatColor.RED + "You are not in a nation!");
            return;
        }

        if (!plugin.getQuestionUtil().hasTruceRequest(nation.getUuid())) {
            p.sendMessage(ChatColor.RED + "Your nation does not have a truce request!");
            return;
        }

        plugin.getQuestionUtil().runRunnable(nation.getUuid(), arg.equalsIgnoreCase("accept"));

    }

}
