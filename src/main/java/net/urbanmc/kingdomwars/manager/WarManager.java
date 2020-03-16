package net.urbanmc.kingdomwars.manager;

import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import com.palmergames.bukkit.towny.TownyMessaging;
import com.palmergames.bukkit.towny.TownySettings;
import com.palmergames.bukkit.towny.exceptions.EconomyException;
import com.palmergames.bukkit.towny.object.Nation;
import net.urbanmc.kingdomwars.KingdomWars;
import net.urbanmc.kingdomwars.WarBoard;
import net.urbanmc.kingdomwars.data.GraceNation;
import net.urbanmc.kingdomwars.data.LastWar;
import net.urbanmc.kingdomwars.data.PreWar;
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
            win(winner, loser);
        }
    }

    // Called when nation in a war gets deleted
    public void winByDeletion(War war, String nationDeleted) {
        String winningNation = war.getDeclaringNation().equals(nationDeleted) ? war.getDeclaredNation() : war.getDeclaringNation();

        Nation winNation = TownyUtil.getNation(winningNation);

        if (winNation == null) return;

        endWar(winNation, null,
                ConfigManager.getWinAmount(), ConfigManager.getLoseAmount(),
                true, true,
                true, true);
    }

    // Legacy Compat Method
    public void win(Nation winner, Nation loser, double amount) {
        endWar(winner, loser,
                amount, amount,
                true, true,
                true, true);
    }

    // Normal win method when nation war wins by killing participants
    public void win(Nation winner, Nation loser) {
        endWar(winner, loser,
                ConfigManager.getWinAmount(), ConfigManager.getLoseAmount(),
                false, true,
                true, true);
    }

    // Called when the war is over
    private void wonByTime(War war) {
        Nation nation1 = TownyUtil.getNation(war.getDeclaringNation());
        Nation nation2 = TownyUtil.getNation(war.getDeclaredNation());

        if (nation1 == null || nation2 == null) return;

        Nation winner = war.getDeclaringPoints() > war.getDeclaredPoints() ? nation1 : nation2;

        endWar(winner, winner == nation1 ? nation2 : nation1,
                ConfigManager.getWinAmount(), ConfigManager.getLoseAmount(),
                true, true,
                true, true);
    }

    // Called when a truce is reached
    public void truceWar(Nation declaring, Nation declared) {
        endWar(declaring, declared,
                ConfigManager.getTruceAmount(), ConfigManager.getTruceAmount(),
                false, false,
                false, true);
    }

    // Called when an admin force ends the war
    public void forceEnd(War war) {
        declaringEndWar(war);
    }

    // Called when the declaring nation ends the war
    public void declaringEndWar(War war) {
        Nation nation1 = TownyUtil.getNation(war.getDeclaringNation());
        Nation nation2 = TownyUtil.getNation(war.getDeclaredNation());

        endWar(nation1, nation2,
                0, 0,
                false, false,
                false, false);
    }

    public synchronized void endWar(Nation winner, Nation loser,
                                    double winAmount, double loseAmount,
                                    boolean halfReward, boolean rewardTownBlocks,
                                    boolean adjustLeaderBoard, boolean sendNationMessages) {
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

        boolean isTruce = winAmount == ConfigManager.getTruceAmount();

        String winnerName = winner != null ? winner.getName() : "an unknown nation";
        String loserName = loser != null ? loser.getName() : "an unknown nation";

        if (sendNationMessages) {
            if (winner != null)
                TownyUtil.sendNationMessage(winner, isTruce ? "Your nation has truced with " + loserName + "!" :
                        "Your nation has won the war against " + loserName + "!");

            if (loser != null)
                TownyUtil.sendNationMessage(loser, isTruce ? "Your nation has truced with " + winnerName + "!" :
                        "Your nation has lost the war against " + winnerName + "!");
        }

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

        if (halfReward) {
            winAmount /= 2;
            loseAmount /= 2;
        }

        if (winAmount > 0)
            rewardMoney(winner, loser,
                    !isTruce && winner.getName().equals(war.getDeclaringNation()),
                    winAmount, loseAmount);

        if (rewardTownBlocks)
            rewardTownBlocks(winner, loser, halfReward);

        if (adjustLeaderBoard) {
            if (winner != null)
                plugin.getLeaderboard().addWinToLeaderBoard(winnerName);

            if (loser != null)
                plugin.getLeaderboard().addLossToLeaderBoard(loserName);

            plugin.getLeaderboard().sortLeaderboard();
            plugin.getLeaderboard().saveLeaderboard();
        }
    }

    private void rewardMoney(Nation winner, Nation loser, boolean returnStartingCost, double winAmount, double loseAmount) {
        if (winner != null) {
            double balance = TownyUtil.getNationBalance(winner) + winAmount;

            if (returnStartingCost) {
                balance += ConfigManager.getStartAmount();
            }

            TownyUtil.setNationBalance(winner, balance, "War win against " + (loser != null ? loser.getName() : "unknown!"));
        }

        if (loser != null) {
            double balance = 0;

            try {
                balance = loser.getHoldingBalance();
            } catch (EconomyException ex) {
                ex.printStackTrace();
            }

            if (balance < loseAmount) {
                TownyUtil.sendNationMessage(loser, "Your nation could not pay the war loss fee and has fallen!");
                TownyUtil.deleteNation(loser);
                plugin.getLeaderboard().deleteNationFromLeaderboard(loser.getName());
            } else {
                TownyUtil.setNationBalance(loser, balance - loseAmount, "War loss");
            }
        }
    }

    private void rewardTownBlocks(Nation winner, Nation loser, boolean halfReward) {
        // Townblock Bonus
        // Behavioural Restraints:
        // If a losing nation has less than the town block minimum, then don't give the winner town blocks
        // If a losing nation can't give all the town blocks, make the winning townblocks proportional to the ones lost
        // Winning townblock cap, give townblocks up to the cap.
        // Half-war rewards

        int negTownBlockMin = ConfigManager.getNegTownBlockMin();
        int townBlockLoss = ConfigManager.getTownBlockLoss();
        int townBlockWinBonus = ConfigManager.getTownBlockBonus();

        if (halfReward) {
            negTownBlockMin /= 2;
            townBlockLoss /= 2;
            townBlockWinBonus /= 2;
        }

        // transferTownBlock boolean checks whether we should award/take townblocks from the winning/losing nations.
        // Check if the losing nation bonus is greater than than the minimum
        boolean transferTownBlocks = loser.getExtraBlocks() > negTownBlockMin;

        // BonusDifference is positive if losing nation cannot afford to lose any more townblocks
        int bonusDifference = townBlockLoss - TownySettings.getNationBonusBlocks(loser);

        // If the bound difference is 0 that means the nation has 0 nation bonus and we don't want to transfer townblocks.
        transferTownBlocks &= bonusDifference != townBlockWinBonus;

        // If the bonus difference is positive then that means they can't afford to give all the town blocks.
        if (bonusDifference > 0) {
            // Reduce the win bonus proportionally to the ratio between the win bonus and the take amount.
            // 30 : 10 * x : 3
            // (townBlockWinBonus * bonusDifference) / townBlockLoss
            townBlockWinBonus = (townBlockWinBonus * bonusDifference) / townBlockLoss;
            townBlockLoss = bonusDifference;
        }

        if (transferTownBlocks) {
            if (winner != null) {
                int maxTownBlocks = ConfigManager.getMaxTownBlocksWin();

                if ((maxTownBlocks  - winner.getExtraBlocks()) < townBlockWinBonus) {
                    int townBlockDifference = maxTownBlocks - winner.getExtraBlocks();

                    townBlockWinBonus = Math.max(townBlockDifference, 0);
                }

                if (townBlockWinBonus > 0) {
                    TownyUtil.addNationBonusBlocks(winner, townBlockWinBonus);
                    TownyUtil.saveNation(winner);
                    TownyMessaging.sendNationMessage(winner,"The nation won " + townBlockWinBonus + " townblocks!");
                }
            }

            TownyUtil.addNationBonusBlocks(loser, -townBlockLoss);
            TownyUtil.saveNation(loser);
            TownyMessaging.sendNationMessage(loser, "The nation has lost " + townBlockLoss + " townblocks!");
        }
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
                graceNations.remove(graceNation), 20 * 60 * ConfigManager.getGracePeriod());
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
