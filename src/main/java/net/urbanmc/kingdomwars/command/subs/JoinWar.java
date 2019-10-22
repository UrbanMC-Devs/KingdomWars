package net.urbanmc.kingdomwars.command.subs;

import com.palmergames.bukkit.towny.object.Nation;
import net.urbanmc.kingdomwars.KingdomWars;
import net.urbanmc.kingdomwars.data.PreWar;
import net.urbanmc.kingdomwars.util.TownyUtil;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

public class JoinWar {

    public JoinWar(Player p, String[] args, final KingdomWars plugin) {
        if (!p.hasPermission("kingdomwars.joinwar")) {
            p.sendMessage(ChatColor.RED + "You do not have permission to do this!");
            return;
        }

        Nation nation1 = TownyUtil.getNation(p);

        if (nation1 == null) {
            p.sendMessage(ChatColor.RED + "You are not in a nation!");
            return;
        }

        if (plugin.getWarManager().alreadyScheduledForWar(nation1.getName())) {
            p.sendMessage(ChatColor.RED + "You are already starting a war soon!");
            return;
        }

        if (args.length == 1) {
            p.sendMessage(ChatColor.RED + "Please specify a nation to ally with!");
            return;
        }

        Nation nation2 = TownyUtil.getNation(args[1]);

        if (nation2 == null) {
            p.sendMessage(ChatColor.RED + "You have not specified a valid nation.");
            return;
        }

        if (plugin.getWarManager().inWar(nation1)) {
            p.sendMessage(ChatColor.RED + "You are already in a war!");
            return;
        }

        if (plugin.getWarManager().inWar(nation2)) {
            p.sendMessage(ChatColor.RED + "That nation is already in a war! It is too late to join them!");
            return;
        }

        PreWar preWar = plugin.getWarManager().getPreWar(nation2.getName());

        if (preWar == null) {
            p.sendMessage(ChatColor.RED + "That nation is not stating a war soon!");
            return;
        }

        if (!preWar.isMainNation(nation2.getName())) {
            p.sendMessage(ChatColor.RED + "That nation is allying in another nation's war!");
            return;
        }

        if (!nation1.getAllies().contains(nation2) || !nation2.getAllies().contains(nation1)) {
            p.sendMessage(ChatColor.RED + "Your nation must be allied with the other nation! That nation must also be allied with you!");
            return;
        }

        preWar.addAlly(nation2.getName().equalsIgnoreCase(preWar.getDeclaringNation()), nation1.getName());

        TownyUtil.sendNationMessage(nation2, nation1.getName() + " has now joined your side in the upcoming war!");
        TownyUtil.sendNationMessage(nation1, "Your nation is now participating in the war between "  + preWar.getDeclaringNation() + " vs " + preWar.getDeclaredNation() + "!");
    }

}
