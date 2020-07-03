package net.urbanmc.kingdomwars.util;

import net.md_5.bungee.api.ChatColor;
import net.urbanmc.kingdomwars.data.LastWar;
import net.urbanmc.kingdomwars.data.Leaderboard;

import java.text.SimpleDateFormat;
import java.util.Collection;
import java.util.Date;

public class ArchiveFormatter {

    public static void quickDisplay(JSONMessageBuilder msgBuilder, LastWar lastWar) {
        // Hyperion vs Winterhold (04/12/20 - 04/15/20) Declaring vs Declared

        // Click to view war
        msgBuilder.persistClick(msgBuilder.clickCommand("/twars archive id " + lastWar.getArchiveID()));
        msgBuilder.persistTooltip(
                JSONMessageBuilder.tooltipHover(JSONMessageBuilder.create("Click to view this war!").color(ChatColor.GOLD))
        );

        msgBuilder.then(lastWar.getDeclaringNation()).color(getColorForNation(lastWar, true));
        msgBuilder.then(" vs ").color(ChatColor.GOLD);
        msgBuilder.then(lastWar.getDeclaredNation()).color(getColorForNation(lastWar, false));
        String dateRange = " (" + getDateFromMillis(lastWar.getStartTime()) + " - "
                + getDateFromMillis(lastWar.getEndTime()) + ")";
        msgBuilder.then(dateRange).color(ChatColor.AQUA);
        // Flush and clear
        flushAndClear(msgBuilder);
    }

    private static String getDateFromMillis(long millis) {
        Date date = new Date(millis);
        SimpleDateFormat dateFormat = new SimpleDateFormat("MM/dd/yy"); // the format of your date
        return dateFormat.format(date);
    }

    private static String getDateTimeFromMillis(long millis) {
        Date date = new Date(millis);
        SimpleDateFormat dateFormat = new SimpleDateFormat("MM/dd/yy hh:mm"); // the format of your date
        return dateFormat.format(date);
    }

    private static ChatColor getColorForNation(LastWar war, boolean isDeclaring) {
        switch (war.getResult()) {
            case VICTORY:
            case DELETION:
                return isDeclaring ? ChatColor.GREEN : ChatColor.RED;
            case TRUCE:
                return ChatColor.BLUE;
            case END:
            case FORCE_END:
                return ChatColor.LIGHT_PURPLE;
            default:
                return ChatColor.GOLD;
        }
    }

    /*
            Hyperion vs Winterhold:
            Archive ID: 10
            Started: 04/12/20 04:35
            Ended: 04/15/20 12:35
            Declaring: Hyperion
            Hyperion's Kills: 1
            Winterhold's Kills: 2
            Allies:
            - Irken
            - Corsica

            Rewards:
            - Townblocks: +15 -10
            - Money: +12000 -15000
     */

    public static void fullDisplay(JSONMessageBuilder msgBuilder, LastWar lastWar) {
        // Hyperion vs Winterhold
        msgBuilder.then(lastWar.getDeclaringNation()).color(getColorForNation(lastWar, true));
        msgBuilder.then(" vs ").color(ChatColor.GOLD);
        msgBuilder.then(lastWar.getDeclaredNation()).color(getColorForNation(lastWar, false));
        msgBuilder.then(":").color(ChatColor.GOLD).then("\n");

        // Archive ID: 1
        msgBuilder.then("Archive ID: ").color(ChatColor.GRAY)
                    .then(lastWar.getArchiveID() + "").color(ChatColor.GOLD).then("\n");

        // Started: 04/12/20 04:35
        msgBuilder.then("Started: ").color(ChatColor.GRAY)
                  .then(getDateTimeFromMillis(lastWar.getStartTime())).color(ChatColor.GOLD).then("\n");

        // Ended: 04/12/20 04:35
        msgBuilder.then("Ended: ").color(ChatColor.GRAY)
                .then(getDateTimeFromMillis(lastWar.getEndTime())).color(ChatColor.GOLD).then("\n");

        // Declaring: Hyperion
        msgBuilder.then("Declaring: ").color(ChatColor.GRAY)
                    .then(lastWar.getDeclaringNation()).color(getColorForNation(lastWar, true)).then("\n");

        // Hyperion Kills: 5
        msgBuilder.then(lastWar.getDeclaringNation() + " Kills: ").color(getColorForNation(lastWar, true))
                .then(String.valueOf(lastWar.getDeclaringPoints())).color(ChatColor.GOLD).then("\n");

        // Winterhold Kills: 5
        msgBuilder.then(lastWar.getDeclaredNation() + " Kills: ").color(getColorForNation(lastWar, false))
                .then(String.valueOf(lastWar.getDeclaringPoints())).color(ChatColor.GOLD).then("\n");

        // Allies
        if (lastWar.hasAllies()) {
            msgBuilder.then("Allies:").color(ChatColor.GRAY).then("\n");
            buildAllies(msgBuilder, lastWar.getDeclaringAllies(), ChatColor.GOLD);
            buildAllies(msgBuilder, lastWar.getDeclaredAllies(), ChatColor.GOLD);
            msgBuilder.then("\n");
        }

        // Rewards:
        msgBuilder.then("Rewards: ").color(ChatColor.GRAY).then("\n");

        // Townblocks
        msgBuilder.then("- Townblocks: ").color(ChatColor.GOLD)
                .then("+" + lastWar.getTownblocksWon()).color(ChatColor.GREEN)
                .then(" -" + lastWar.getTownblocksLost()).color(ChatColor.RED).then("\n");

        // Money
        msgBuilder.then("- Money: ").color(ChatColor.GOLD)
                .then("+ $" + lastWar.getMoneyWon()).color(ChatColor.GREEN)
                .then(" - $" + lastWar.getMoneyLost()).color(ChatColor.RED);
    }

    private static void buildAllies(JSONMessageBuilder builder, Collection<String> allyNames, ChatColor color) {
        for (String allyName : allyNames) {
            builder.then("- " + allyName).color(color).then("\n");
        }
    }

    public static void buildLeaderBoard(JSONMessageBuilder builder, Leaderboard lb) {
        builder.persistTooltip(
                JSONMessageBuilder.tooltipHover(
                        JSONMessageBuilder.create("Click to see the wars for " + lb.getNation()).color(ChatColor.GOLD)
                )
        );
        builder.persistClick(builder.clickCommand("/twars archive nation " + lb.getNation()));
        builder.then(lb.getNation()).color(ChatColor.GREEN).then(" ");
        builder.then(String.valueOf(lb.getWins())).color(ChatColor.AQUA).then(" ");
        builder.then(String.valueOf(lb.getLosses())).color(ChatColor.RED);
        flushAndClear(builder);
    }

    private static void flushAndClear(JSONMessageBuilder builder) {
        // Flush components
        builder.flush();
        builder.persistClick(null);
        builder.persistTooltip(null);
    }


}
