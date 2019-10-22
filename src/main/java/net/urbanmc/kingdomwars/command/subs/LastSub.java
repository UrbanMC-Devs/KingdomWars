package net.urbanmc.kingdomwars.command.subs;

import com.palmergames.bukkit.towny.object.Nation;
import net.urbanmc.kingdomwars.KingdomWars;
import net.urbanmc.kingdomwars.data.LastWar;
import net.urbanmc.kingdomwars.manager.ConfigManager;
import net.urbanmc.kingdomwars.util.TownyUtil;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

public class LastSub {

    //Admin command for viewing/modifying war cooldowns

    public LastSub(Player p, String[] args, final KingdomWars plugin) {
        if (!p.hasPermission("kingdomwars.last")) {
            p.sendMessage(ChatColor.RED + "You do not have permission to do this!");
            return;
        }

        if (args.length == 1) {
            sendColor(p, "&cPlease enter one of the following arguments: &bview&f, &bremove");
            return;
        }

        if (args.length != 3) {
            sendColor(p, "&cUsage: /twars last [remove/view] [nation]");
            return;
        }

        Nation targetNation = TownyUtil.getNation(args[2]);

        if (targetNation == null) {
            p.sendMessage(ChatColor.RED + "You have not specified a valid nation.");
            return;
        }

        LastWar lastWar = plugin.getLastWarManager().getLastWar(targetNation);

        if (lastWar == null) {
            sendColor(p, "&4No last war for this nation found!");
            return;
        }

        switch (args[1].toLowerCase()) {
            case "view":
                viewLast(p, lastWar, targetNation.getName());
                break;
            case "remove":
                removeLast(plugin, p, lastWar);
                break;
            default:
                sendColor(p, "&cPlease enter a valid argument: &bview&f, &bremove");
                break;
        }
    }

    private void viewLast(Player p, LastWar lastWar, String nationName) {
        String time = ConfigManager.formatTime((System.currentTimeMillis() - (lastWar.getMillisTillNextWar() - ConfigManager.getLastTime())) / 1000);

        sendColor(p,
                "&2--- &cRecent War for &f" + nationName + " &2---\n" +
                        "&bAgainst: &e" + (lastWar.getDeclaringNation().equalsIgnoreCase(nationName) ? lastWar.getDeclaredNation() : lastWar.getDeclaringNation()) +
                        "\n&a-- Was Declaring Nation: &6" + lastWar.getDeclaringNation().equalsIgnoreCase(nationName) +
                        "\n&a-- Won? &6" + !lastWar.isLosingNation(nationName) +
                                "\n&a-- Truce? &f" + lastWar.wasTruce() +
                                "\n&a-- Fought: &d" + time + " ago!"
        );
    }

    private void removeLast(KingdomWars plugin, Player p, LastWar lastWar) {
        plugin.getLastWarManager().removeLast(lastWar);
        sendColor(p, "&aThe most recent last war data for that nation was removed!");
    }



    private void sendColor(Player p, String message) {
        p.sendMessage(ChatColor.translateAlternateColorCodes('&', message));
    }

}
