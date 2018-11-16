package net.urbanmc.kingdomwars.command.subs;

import com.palmergames.bukkit.towny.object.Nation;
import net.urbanmc.kingdomwars.KingdomWars;
import net.urbanmc.kingdomwars.util.TownyUtil;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

public class AcceptDenySub {

    public AcceptDenySub(Player p, String arg) {
        arg = arg.toLowerCase();
        if (!p.hasPermission("twars." + arg)) {
            p.sendMessage(ChatColor.RED + "You do not have permission to do this!");
            return;
        }

        Nation nation = TownyUtil.getNation(p);

        if (nation == null) {
            p.sendMessage(ChatColor.RED + "You are not in a nation!");
            return;
        }

        if (!KingdomWars.getQuestionUtil().hasTruceRequest(nation.getUuid())) {
            p.sendMessage(ChatColor.RED + "Your nation does not have a truce request!");
            return;
        }

        KingdomWars.getQuestionUtil().runRunnable(nation.getUuid(), arg.equalsIgnoreCase("accept") ? true : false);

    }

}
