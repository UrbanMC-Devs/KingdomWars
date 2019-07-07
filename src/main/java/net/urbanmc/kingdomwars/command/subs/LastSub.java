package net.urbanmc.kingdomwars.command.subs;

import com.palmergames.bukkit.towny.object.Nation;
import net.urbanmc.kingdomwars.WarUtil;
import net.urbanmc.kingdomwars.data.last.LastWar;
import net.urbanmc.kingdomwars.util.TownyUtil;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

public class LastSub {

    //Admin command for viewing/modifying war cooldowns

    public LastSub(Player p, String[] args) {
        if (!p.hasPermission("kingdomwars.last")) {
            p.sendMessage(ChatColor.RED + "You do not have permission to do this!");
            return;
        }

        if (args.length == 1) {
            sendColor(p, "&cPlease enter one of the following arguments: &bview&f, &bremove");
            return;
        }

        if (args.length != 3) {
            sendColor(p, "&cPlease enter a valid nation!");
            return;
        }

        Nation targetNation = TownyUtil.getNation(args[2]);

        if (targetNation == null) {
            p.sendMessage(ChatColor.RED + "You have not specified a valid nation.");
            return;
        }

        LastWar lastWar = WarUtil.getLastWar(targetNation);

        if (lastWar == null) {
            sendColor(p, "&4No last war for this nation found!");
            return;
        }

        switch (args[1].toLowerCase()) {
            case "view":
                viewLast(p, lastWar, targetNation.getName());
                break;
            case "remove":
                removeLast(p, lastWar);
                break;
            default:
                sendColor(p, "&cPlease enter a valid argument: &bview&f, &bremove");
                break;
        }




    }

    private void viewLast(Player p, LastWar lastWar, String nationName) {
        String time = formatTime((System.currentTimeMillis() - lastWar.getMillis()) / 1000);

        sendColor(p,
                "&2--- &cRecent War for &f" + nationName + " &2---\n" +
                        "&bAgainst: &e" + (lastWar.getDeclaringNation().equalsIgnoreCase(nationName) ? lastWar.getDeclaredNation() : lastWar.getDeclaringNation()) +
                        "\n&a-- Was Declaring Nation: &6" + lastWar.getDeclaringNation().equalsIgnoreCase(nationName) +
                        "\n&a-- Won? &6" + !lastWar.isLosingNation(nationName) +
                                "\n&a-- Truce? &f" + lastWar.wasTruce() +
                                "\n&a-- Fought: &d" + time
        );
    }

    private void removeLast(Player p, LastWar lastWar) {
        WarUtil.removeLast(lastWar);

        sendColor(p, "&aThe most recent last war data for that nation was removed!");
    }



    private void sendColor(Player p, String message) {
        p.sendMessage(ChatColor.translateAlternateColorCodes('&', message));
    }

    private String formatTime(long time) {
        int days = 0, hours = 0, minutes = 0, seconds;

        while (time >= 86400) {
            days++;
            time -= 86400;
        }

        while (time >= 3600) {
            hours++;
            time -= 3600;
        }

        while (time >= 60) {
            minutes++;
            time -= 60;
        }

        seconds = Long.valueOf(time).intValue();

        if (seconds == 60) {
            minutes++;
            seconds = 0;
        }

        String output = "";

        if (days > 1) {
            output += days + " days, ";
        } else {
            output += days + " day, ";
        }

        if (hours > 1) {
            output += hours + " hours, ";
        } else if (hours == 1) {
            output += hours + " hour, ";
        }

        if (minutes > 1) {
            output += minutes + " minutes, ";
        } else if (minutes == 1) {
            output += minutes + " minute, ";
        }

        if (seconds > 1) {
            output += seconds + " seconds";
        } else if (seconds == 1) {
            output += seconds + " second";
        }

        output = output.trim();

        if (output.endsWith(",")) {
            output = output.substring(0, output.length() - 1);
        }

        return output;
    }

}
