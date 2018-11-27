package net.urbanmc.kingdomwars.command.subs;

import net.urbanmc.kingdomwars.data.last.LastWar;
import net.urbanmc.kingdomwars.event.WarDeclareEvent;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

import com.palmergames.bukkit.towny.object.Nation;

import net.urbanmc.kingdomwars.KingdomWars;
import net.urbanmc.kingdomwars.util.TownyUtil;
import net.urbanmc.kingdomwars.WarUtil;
import net.urbanmc.kingdomwars.data.war.War;
import net.urbanmc.kingdomwars.event.WarStartEvent;

public class Start {

    public Start(Player p, String[] args) {
        if (!p.hasPermission("kingdomwars.start")) {
            p.sendMessage(ChatColor.RED + "You do not have permission to do this!");
            return;
        }

        Nation nation1 = TownyUtil.getNation(p);

        if (nation1 == null) {
            p.sendMessage(ChatColor.RED + "You are not in a nation!");
            return;
        }

        if (args.length == 1) {
            p.sendMessage(ChatColor.RED + "Please specify a nation to declare war against.");
            return;
        }

        Nation nation2 = TownyUtil.getNation(args[1]);

        if (nation2 == null) {
            p.sendMessage(ChatColor.RED + "You have not specified a valid nation.");
            return;
        }

        if (WarUtil.inWar(nation1)) {
            p.sendMessage(ChatColor.RED + "You are already in a war!");
            return;
        }

        if (WarUtil.inWar(nation2)) {
            p.sendMessage(ChatColor.RED + "That nation is already in a war!");
            return;
        }

        if (nation1.isNeutral()) {
            p.sendMessage(ChatColor.RED + "Your nation is neutral!");
            return;
        }

        if (nation1.getName().equals(nation2.getName())) {
            p.sendMessage(ChatColor.RED + "This plug-in does not support civil wars!");
            return;
        }

        if (nation1.hasAlly(nation2)) {
            p.sendMessage(ChatColor.RED + "You cannot have a war with your ally!");
            return;
        }

        boolean revenge = false;

        if (WarUtil.hasLast(nation1.getName(), nation2.getName())) {
            if (WarUtil.canRevenge(nation1, nation2)) {
                revenge = true;
            } else {
                p.sendMessage(ChatColor.RED + "You cannot have another war with this nation until " + getLast
                        (nation1, nation2)
                        + " seconds from now!");
                return;
            }
        }

        if (!revenge && nation2.isNeutral()) {
            p.sendMessage(ChatColor.RED + "That nation is neutral!");
            return;
        }

        if (TownyUtil.getNationBalance(nation1) < KingdomWars.getStartAmount()) {
            p.sendMessage(ChatColor.RED + "Your nation balance does not have the required amount to start a war! "
                    + ChatColor.GREEN + "($" + KingdomWars.getStartAmount() + ")");
            return;
        }

        WarDeclareEvent declareEvent = new WarDeclareEvent(nation1.getName(), nation2.getName(), 5); //5 minutes is default time. Can be modified through event.
        Bukkit.getPluginManager().callEvent(declareEvent);

        if (declareEvent.isCancelled())
            return;

        TownyUtil.sendNationMessage(nation1, "Your nation has will be at war against " + nation2.getName() + " in " + declareEvent.getTimeTillWar() + " minutes!");
        TownyUtil.sendNationMessage(nation2, nation1.getName() + " has declared war against your nation! The war will begin in " + declareEvent.getTimeTillWar() + " minutes!");

        Bukkit.getScheduler().runTaskLater(KingdomWars.getInstance(), () ->
                startWar(nation1, nation2)
                , 20* 60 * declareEvent.getTimeTillWar()); //20 ticks per second * 60 seconds per minute * Time till War in minutes generates the amount of ticks.
    }

    private String getLast(Nation nation1, Nation nation2) {
        LastWar last = WarUtil.getLast(nation1, nation2);

        long time;

        if (last.isLosingNation(nation1.getName())) {
            time = last.getRevengeMillis();
        } else {
            time = last.getMillis();
        }

        time -= System.currentTimeMillis();

        return formatTime(time / 1000);
    }

    private static String formatTime(long time) {
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

    private void startWar(Nation nation1, Nation nation2) {
        War war = new War(nation1.getName(), nation2.getName());

        WarStartEvent event = new WarStartEvent(war);
        Bukkit.getPluginManager().callEvent(event);

        if (event.isCancelled())
            return;

        WarUtil.startWar(war);

        TownyUtil.sendNationMessage(nation1, "Your nation has started a war against " + nation2.getName() + "!");
        TownyUtil.sendNationMessage(nation2, nation1.getName() + " has began a war against your nation!");
    }
}
