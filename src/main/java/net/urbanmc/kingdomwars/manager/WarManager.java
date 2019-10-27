package net.urbanmc.kingdomwars.manager;

import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import com.palmergames.bukkit.towny.TownyAPI;
import com.palmergames.bukkit.towny.TownySettings;
import com.palmergames.bukkit.towny.exceptions.EconomyException;
import com.palmergames.bukkit.towny.object.Nation;
import net.urbanmc.kingdomwars.KingdomWars;
import net.urbanmc.kingdomwars.WarBoard;
import net.urbanmc.kingdomwars.data.GraceNation;
import net.urbanmc.kingdomwars.data.PreWar;
import net.urbanmc.kingdomwars.data.LastWar;
import net.urbanmc.kingdomwars.data.war.War;
import net.urbanmc.kingdomwars.data.war.WarSerializer;
import net.urbanmc.kingdomwars.event.WarEndEvent;
import net.urbanmc.kingdomwars.util.TownyUtil;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class WarManager {

    private List<War> currentWars;
    private List<PreWar> scheduledWars = new ArrayList<>();
    private List<GraceNation> graceNations = new ArrayList<>();

    private KingdomWars plugin;

    public WarManager(KingdomWars plugin) {
        this.plugin = plugin;
    }

    public void loadCurrentWars() {
        final File file = new File("plugins/KingdomWars/wars.json");

        if (!file.getParentFile().isDirectory()) {
            file.getParentFile().mkdir();
        }

        if (!file.exists()) {
            try {
                file.createNewFile();
                currentWars = new ArrayList<>();
            } catch (IOException e) {
                e.printStackTrace();
            }

            return;
        }

        if (currentWars != null) currentWars.clear();

        try(Scanner scanner = new Scanner(file)) {

            JsonElement element = new JsonParser().parse(scanner.nextLine());

            currentWars = WarSerializer.deserializeWars(element);
        } catch (Exception print) {
            print.printStackTrace();
        }
    }

    public void saveCurrentWars() {
        try(PrintWriter writer = new PrintWriter(
                new File("plugins/KingdomWars/wars.json")))
        {
            writer.write(WarSerializer.serialize(currentWars).toString());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public boolean inWar(Nation nation) {
        return inWar(nation.getName());
    }

    public boolean inWar(String nation) {
        for (War war : currentWars) {
            if (war.isInWar(nation)) return true;
        }

        return false;
    }

    public War getWar(Nation nation) {
        return getWar(nation.getName());
    }

    public War getWar(String nation) {
        for (War war : currentWars) {
            if (war.isInWar(nation)) return war;
        }

        return null;
    }

    public List<War> getCurrentWars() {
        return currentWars;
    }

    public void startWar(War war) {
        // Generate the amount of kills needed to win
        int kills = ConfigManager.getWinningKills();

        kills += war.getAllies(true).size() * ConfigManager.getAllyKills();
        kills += war.getAllies(false).size() * ConfigManager.getAllyKills();

        war.setKills(kills);

        // Set the war start time
        war.setStarted();

        // Add it to the arraylist
        currentWars.add(war);

        saveCurrentWars();

        WarBoard.createBoard(war);

        Nation declaring = TownyUtil.getNation(war.getDeclaringNation());

        TownyUtil.setNationBalance(declaring,
                TownyUtil.getNationBalance(declaring) - ConfigManager.getStartAmount(),
                "War start with " + war.getDeclaredNation());
    }

    public void updateWar(War war) {
        War oldWar = getWar(TownyUtil.getNation(war.getDeclaringNation()));

        if (oldWar != null) {
            currentWars.remove(oldWar);
        }

        currentWars.add(war);
        saveCurrentWars();
    }

    public synchronized boolean checkForceEnd(War war) {
        long millis = System.currentTimeMillis() - war.getStarted();

        if (millis >= ConfigManager.getEndTime()) {
            wonByTime(war);
            return true;
        } else
            return false;
    }

    public synchronized void checkForceEndAll() {
        for (War war : currentWars) {
            if (war == null) continue;
            checkForceEnd(war);
        }
    }

    public synchronized void checkWin(War war) {
        Nation winner = null, loser = null;

        if (war.getDeclaringPoints() >= war.getKillsToWin()) {
            winner = TownyUtil.getNation(war.getDeclaringNation());
            loser = TownyUtil.getNation(war.getDeclaredNation());
        } else if (war.getDeclaredPoints() >= war.getKillsToWin()) {
            winner = TownyUtil.getNation(war.getDeclaredNation());
            loser = TownyUtil.getNation(war.getDeclaringNation());
        }

        if (winner != null && loser != null) {
            win(winner, loser, ConfigManager.getFinishAmount());
        }
    }

    public void winByDeletation(War war, String nationDeleted) {
        String winningNation = war.getDeclaringNation().equals(nationDeleted) ? war.getDeclaredNation() : war.getDeclaringNation();

        Nation winNation = TownyUtil.getNation(winningNation);

        if (winNation == null) return;

        endWar(winNation, null, ConfigManager.getFinishAmount(), true, true);
    }

    public void win(Nation winner, Nation loser, double amount) {
        endWar(winner, loser, amount, false, true);
    }

    public synchronized void endWar(Nation winner, Nation loser, double monetaryAmount, boolean halfReward, boolean giveReward) {
        War war = getWar(winner);

        WarEndEvent event = new WarEndEvent(war);
        Bukkit.getPluginManager().callEvent(event);

        if (event.isCancelled()) return;

        currentWars.remove(war);
        saveCurrentWars();

        for (Player p : Bukkit.getServer().getOnlinePlayers()) {
            Nation nation = TownyUtil.getNation(p);

            if (nation == null) continue;

            if ((winner != null && nation.equals(winner)) || (loser != null && nation.equals(loser))) {
                if (p.getScoreboard().equals(war.getScoreBoard())) {
                    p.setScoreboard(Bukkit.getScoreboardManager().getNewScoreboard());
                }
            }
        }

        boolean isTruce = monetaryAmount == ConfigManager.getTruceAmount();

        String winnerName = winner != null ? winner.getName() : "an unknown nation";
        String loserName = loser != null ? loser.getName() : "an unknown nation";

        if (winner != null)
        TownyUtil.sendNationMessage(winner, isTruce ? "Your nation has truced with " + loserName + "!" :
                "Your nation has won the war against " + loserName + "!");

        if (loser != null)
        TownyUtil.sendNationMessage(loser, isTruce ? "Your nation has truced with " + winnerName + "!" :
                "Your nation has lost the war against " + winnerName + "!");

        LastWar lastWar =
                new LastWar(winnerName, loserName, war.isDeclaringNation(winner.getName()),
                         isTruce,
                        System.currentTimeMillis() + ConfigManager.getLastTime(),
                        System.currentTimeMillis() + ConfigManager.getLastTimeRevenge());

        // Add last war
        try {
            plugin.getLastWarManager().addLast(lastWar);
        } catch (Exception ignore) {
            Bukkit.getLogger().warning("[KingdomWars] Error saving last war for war between " + winner.getName() + " and " + loser.getName());
        }

        if (giveReward) {
            if (halfReward) monetaryAmount /= 2;

            if (winner != null) {
                try {
                    double balance = winner.getHoldingBalance() + monetaryAmount;

                    if (war.getDeclaringNation().equals(winner.getName())) {
                        balance += ConfigManager.getStartAmount();
                    }

                    winner.setBalance(balance, "War win against " + loserName);
                } catch (EconomyException ex) {
                    ex.printStackTrace();
                }
            }

            if (loser != null) {
                double balance = 0;

                try {
                    balance = loser.getHoldingBalance();
                } catch (EconomyException ex) {
                    ex.printStackTrace();
                }

                if (balance < monetaryAmount) {
                    TownyUtil.sendNationMessage(loser, "Your nation could not pay the war loss fee and has fallen!");
                    TownyUtil.deleteNation(loser);
                    plugin.getLeaderboard().deleteNationFromLeaderboard(loser.getName());
                } else {
                    try {
                        loser.setBalance(balance - monetaryAmount, "War loss");
                    } catch (EconomyException ex) {
                        ex.printStackTrace();
                    }
                }

                int townBlockMin = ConfigManager.getNegTownBlockMin();
                int townBlockLoss = ConfigManager.getTownBlockLoss();
                int townBlockBonus = ConfigManager.getTownBlockBonus();

                if (halfReward) {
                    townBlockMin /= 2;
                    townBlockLoss /= 2;
                    townBlockBonus /= 2;
                }

                if (loser.getExtraBlocks() > townBlockMin && TownySettings.getNationBonusBlocks(loser) >= townBlockLoss) {
                    if (winner != null) {
                        TownyUtil.addNationBonusBlocks(winner, townBlockBonus);
                        TownyAPI.getInstance().getDataSource().saveNation(winner);
                    }
                    TownyUtil.addNationBonusBlocks(loser, -townBlockLoss);
                    TownyAPI.getInstance().getDataSource().saveNation(loser);
                }
            }
        }


        if (winner != null)
            plugin.getLeaderboard().addWinToLeaderBoard(winnerName);

        if (loser != null)
            plugin.getLeaderboard().addLossToLeaderBoard(loserName);

        plugin.getLeaderboard().sortLeaderboard();
        plugin.getLeaderboard().saveLeaderboard();
    }

    private void wonByTime(War war) {
        Nation nation1 = TownyUtil.getNation(war.getDeclaringNation());
        Nation nation2 = TownyUtil.getNation(war.getDeclaredNation());

        if (nation1 == null || nation2 == null) return;

        Nation winner = war.getDeclaringPoints() > war.getDeclaredPoints() ? nation1 : nation2;

        endWar(winner, winner == nation1 ? nation2 : nation1, ConfigManager.getFinishAmount(), true, true);
    }

    public void end(War war) {
        Nation nation1 = TownyUtil.getNation(war.getDeclaringNation());
        Nation nation2 = TownyUtil.getNation(war.getDeclaredNation());

        endWar(nation1, nation2, 0, false, false);
    }

    public void renameWarNation(String oldName, String newName) {
        War war = getWar(oldName);

        boolean declaring = war.isDeclaringNation(oldName);

        if (declaring) {
            war.setDeclaringNation(newName);
        } else {
            war.setDeclaredNation(newName);
        }

        WarBoard.updateNationNames(war, oldName, declaring);

        saveCurrentWars();
    }

    public boolean alreadyScheduledForWar( String nation) {
        for (PreWar preWar : scheduledWars) {
            if (preWar.alreadyDeclared(nation)) return true;
        }

        return false;
    }

    public PreWar getPreWar( String nation) {
        for (PreWar preWar : scheduledWars) {
            if (preWar.alreadyDeclared(nation)) return preWar;
        }

        return null;
    }

    public void addPreWar(PreWar preWar) {
        scheduledWars.add(preWar);
    }

    public void removePreWar(PreWar preWar) {
        scheduledWars.remove(preWar);
    }

    public void createNation(String nationName) {
        if (isGraceNation(nationName)) return;

        GraceNation graceNation = new GraceNation(nationName);

        Bukkit.getScheduler().runTaskLaterAsynchronously(plugin, () ->
                graceNations.remove(graceNation), 20 * 60 * 10);
    }

    public boolean isGraceNation(String nationName) {
        for (GraceNation grace : graceNations) {
            if (grace.getNationName().equalsIgnoreCase(nationName)) return true;
        }

        return false;
    }

    public void renameGraceNation(String oldName, String newName) {
        for (GraceNation grace : graceNations) {
            if (grace.getNationName().equalsIgnoreCase(oldName))
                grace.setNationName(newName);
        }
    }


}
