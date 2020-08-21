package net.urbanmc.kingdomwars.manager;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import com.palmergames.bukkit.towny.object.Nation;
import net.urbanmc.kingdomwars.KingdomWars;
import net.urbanmc.kingdomwars.WarBoard;
import net.urbanmc.kingdomwars.data.LastWar;
import net.urbanmc.kingdomwars.data.PreWar;
import net.urbanmc.kingdomwars.data.WarAbstract;
import net.urbanmc.kingdomwars.data.WarResult;
import net.urbanmc.kingdomwars.data.WarReward;
import net.urbanmc.kingdomwars.data.WarStage;
import net.urbanmc.kingdomwars.data.war.War;
import net.urbanmc.kingdomwars.data.war.WarSerializer;
import net.urbanmc.kingdomwars.event.WarEndEvent;
import net.urbanmc.kingdomwars.event.WarStartEvent;
import net.urbanmc.kingdomwars.util.TownyUtil;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitTask;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Scanner;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.stream.Collectors;

public class WarManager {

    private List<WarAbstract> allWars = new ArrayList<>();

    private KingdomWars plugin;
    private final Gson gson;

    public WarManager(KingdomWars plugin) {
        this.plugin = plugin;
        gson = new GsonBuilder().create();
    }

    public void loadCurrentWars() {
        final File file = new File(plugin.getDataFolder(), "wars.json");

        if (!file.getParentFile().isDirectory()) {
            file.getParentFile().mkdir();
        }

        if (!file.exists()) {
            try {
                file.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }

            return;
        }

        if (!allWars.isEmpty())
            allWars.clear();

        try(Scanner scanner = new Scanner(file)) {
            if (scanner.hasNext()) {
                JsonElement element = new JsonParser().parse(scanner.nextLine());
                allWars.addAll(WarSerializer.deserializeWars(gson, element));
            }
        } catch (Exception ex) {
            plugin.getLogger().log(Level.WARNING, "Error loading wars.json!", ex);
        }

        restartPreWarTasks();
    }

    private void restartPreWarTasks() {
        long normalTaskTime = TimeUnit.MINUTES.toSeconds(5);
        long allyTaskTime = TimeUnit.MINUTES.toSeconds(10);

        for (WarAbstract allWar : allWars) {
            if (allWar.getWarStage() == WarStage.DECLARED) {
                PreWar preWar = (PreWar) allWar;
                long secondsTillWar = (preWar.hasAllies() ? allyTaskTime : normalTaskTime);
                BukkitTask task = Bukkit.getScheduler().runTaskLater(plugin, () -> startWar(preWar),
                        secondsTillWar * 20);
                preWar.setTask(task);
                // Log information
                KingdomWars.logger().info(preWar.getDeclaringNation() + " and " + preWar.getDeclaredNation() + " are going to war in "
                        + TimeUnit.SECONDS.toMinutes(secondsTillWar) + " minutes!");
            }
        }
    }

    public void saveCurrentWars() {
        try(PrintWriter writer = new PrintWriter(
                new File(plugin.getDataFolder(), "wars.json")))
        {
            writer.write(WarSerializer.serialize(gson, allWars).toString());
        } catch (IOException e) {
            plugin.getLogger().log(Level.SEVERE, "Error saving wars.json!", e);
        }
    }

    public boolean inWar(Nation nation) {
        return inWar(nation.getName());
    }

    public boolean inWar(String nation) {
        return getWar(nation) != null;
    }

    public War getWar(Nation nation) {
        return getWar(nation.getName());
    }

    public War getWar(String nation) {
        for (WarAbstract war : allWars) {
            if (war.getWarStage() == WarStage.FIGHTING && war.isInWar(nation))
                return (War) war;
        }

        return null;
    }

    public Collection<War> getCurrentWars() {
        return allWars.stream()
                .filter(wa -> wa != null && wa.getWarStage() == WarStage.FIGHTING)
                .map(wa -> (War) wa)
                .collect(Collectors.toList());
    }

    public void startWar(PreWar preWar) {
        War war = preWar.toFullWar();

        allWars.remove(preWar);

        WarStartEvent event = new WarStartEvent(war);
        Bukkit.getPluginManager().callEvent(event);

        if (event.isCancelled())
            return;

        Nation declaringNation = TownyUtil.getNation(war.getDeclaringNation());
        Nation declaredNation = TownyUtil.getNation(war.getDeclaredNation());

        if (declaringNation == null || declaredNation == null) {
            Bukkit.getLogger().warning("[KingdomWars] Error starting war between " + war.getDeclaringNation() + " and " +
                    war.getDeclaredNation() + " because could not fetch one of those nations from their names!");
            return;
        }

        // Generate the amount of kills needed to win
        int kills = ConfigManager.getWinningKills();

        kills += war.getAllies().size() * ConfigManager.getAllyKills();

        war.setKills(kills);

        // Set the war start time
        war.setStartTime(System.currentTimeMillis());

        // Add it to the arraylist, and remove the pre-war
        allWars.add(war);

        saveCurrentWars();

        TownyUtil.sendNationMessage(declaringNation, "Your nation has started a war against " + declaredNation.getName() + "!");
        TownyUtil.sendNationMessage(declaredNation, declaringNation.getName() + " has began a war against your nation!");

        for (String ally : war.getAllies()) {
            Nation tempNat = TownyUtil.getNation(ally);

            if(tempNat != null)
                TownyUtil.sendNationMessage(tempNat, "The war between " + declaringNation.getName() + " and " + declaredNation.getName() +
                        " has started! Join the fight!");
        }

        WarBoard.createBoard(war);

        Nation declaring = TownyUtil.getNation(war.getDeclaringNation());

        TownyUtil.addMoneyToNation(declaring, -ConfigManager.getStartAmount(),
                            "War start with " + war.getDeclaredNation());
    }

    public synchronized boolean checkForceEnd(War war) {
        long millis = System.currentTimeMillis() - war.getStartTIme();

        if (millis >= ConfigManager.getEndTime()) {
            wonByTime(war);
            return true;
        }

        return false;
    }

    public synchronized void checkForceEndAll() {
        getCurrentWars().forEach(this::checkForceEnd);
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

        if (winNation != null) {
            WarReward warReward = new WarReward().setRewardFactor(.5);
            LastWar lastWar = endWar(winNation, TownyUtil.getNation(nationDeleted), false, warReward,
                    true, true);

            setResultAndSave(lastWar, WarResult.DELETION);
        }
        else {
            KingdomWars.logger().severe("Error ending war. The winning nation " + winningNation + " cannot be found!");
        }
    }

    // Normal win method when nation war wins by killing participants
    public void win(Nation winner, Nation loser) {
        WarReward warReward = new WarReward();

        LastWar lastWar = endWar(winner, loser, false, warReward,
                true, true);

        setResultAndSave(lastWar, WarResult.VICTORY);
    }

    // Called when the war is over
    private void wonByTime(War war) {
        Nation nation1 = TownyUtil.getNation(war.getDeclaringNation());
        Nation nation2 = TownyUtil.getNation(war.getDeclaredNation());

        if (nation1 == null || nation2 == null) return;

        Nation winner = war.getDeclaringPoints() > war.getDeclaredPoints() ? nation1 : nation2;

        WarReward warReward = new WarReward().setRewardFactor(.5);

        LastWar lastWar = endWar(winner, war.getOtherNation(winner), false,
                warReward, true, true);
        setResultAndSave(lastWar, WarResult.VICTORY);
    }

    // Called when a truce is reached
    public void truceWar(Nation declaring, Nation declared) {
        WarReward warReward = new WarReward()
                            .setTruce(true)
                            .rewardTownBlocks(false);

        LastWar lastWar = endWar(declaring, declared, true,
                warReward, false, true);

        setResultAndSave(lastWar, WarResult.TRUCE);
    }

    // Called when an admin force ends the war
    public void forceEnd(War war) {
        declaringEndWar(war);
    }

    // Called when the declaring nation ends the war
    public void declaringEndWar(War war) {
        Nation nation1 = TownyUtil.getNation(war.getDeclaringNation());
        Nation nation2 = TownyUtil.getNation(war.getDeclaredNation());

        WarReward warReward = new WarReward().rewardTownBlocks(false).rewardMoney(false);

        LastWar lastWar = endWar(nation1, nation2,
                false, warReward,
                false, false);

        setResultAndSave(lastWar, WarResult.END);
    }

    public synchronized LastWar endWar(Nation winner, Nation loser,
                                    boolean truce,
                                    WarReward warReward,
                                    boolean adjustLeaderBoard, boolean sendNationMessages) {

        // Winner can never be null. Just not how it works
        Objects.requireNonNull(winner);
        Objects.requireNonNull(loser);

        War war = getWar(winner);

        WarEndEvent event = new WarEndEvent(war);
        Bukkit.getPluginManager().callEvent(event);

        if (event.isCancelled())
            return null;

        allWars.remove(war);
        saveCurrentWars();

        for (Player p : Bukkit.getServer().getOnlinePlayers()) {
            Nation nation = TownyUtil.getNation(p);

            if (nation != null &&
                    (nation.equals(winner) || (nation.equals(loser)))) {
                if (p.getScoreboard().equals(war.getScoreBoard())) {
                    p.setScoreboard(Bukkit.getScoreboardManager().getNewScoreboard());
                }
            }
        }

        String winnerName = winner.getName();
        String loserName = loser.getName();

        if (sendNationMessages) {
            TownyUtil.sendNationMessage(winner, truce ? "Your nation has truced with " + loserName + "!" :
                    "Your nation has won the war against " + loserName + "!");

            TownyUtil.sendNationMessage(loser, truce ? "Your nation has truced with " + winnerName + "!" :
                    "Your nation has lost the war against " + winnerName + "!");
        }


        warReward.returnStartingCost(ConfigManager.receiveStartAmountBack());
        // Process War Reward
        warReward.process(winner, loser);

        // Take money from loser
        if (warReward.getMonetaryLoss() > 0) {
            double losingBalance = TownyUtil.getNationBalance(loser);

            if (losingBalance < warReward.getMonetaryLoss()) {
                TownyUtil.sendNationMessage(loser, "Your nation could not pay the war loss fee and has fallen!");
                TownyUtil.deleteNation(loser);
            } else {
                TownyUtil.addMoneyToNation(loser,  -warReward.getMonetaryLoss(), "War loss");
            }
        }

        // Give money to winner
        if (warReward.getMonetaryReward() > 0) {
            TownyUtil.addMoneyToNation(winner, warReward.getMonetaryReward(),
                    "War win against " + loser.getName());
        }

        // Give townblocks to winner
        if (warReward.getTownblockWon() > 0) {
            int townBlockWinBonus = warReward.getTownblockWon();
            TownyUtil.addNationWarBlocks(winner, townBlockWinBonus);
            TownyUtil.sendNationMessage(winner,"The nation won " + townBlockWinBonus + " townblocks!");
        }

        if (warReward.getTownblocksLoss() > 0) {
            int townBlockLoss = warReward.getTownblocksLoss();
            TownyUtil.addNationWarBlocks(loser, -townBlockLoss);
            TownyUtil.sendNationMessage(loser, "The nation has lost " + townBlockLoss + " townblocks!");
        }


        UUID declaringUUID = TownyUtil.getNationUUID(war.isDeclaringNation(winnerName) ? winner : loser);
        UUID declaredUUID = TownyUtil.getNationUUID(war.isDeclaredNation(winnerName) ? winner : loser);

        LastWar lastWar = new LastWar(war, warReward, declaringUUID, declaredUUID);
        lastWar.setResult(WarResult.VICTORY);

        if (adjustLeaderBoard) {
            plugin.getLeaderboard().addWinToLeaderBoard(TownyUtil.getNationUUID(winner));
            plugin.getLeaderboard().addLossToLeaderBoard(TownyUtil.getNationUUID(loser));
        }

        return lastWar;
    }

    private void setResultAndSave(LastWar war, WarResult result) {
        if (war != null) {
            war.setResult(result);
            // Add last war
            try {
                plugin.getArchiveManager().insertLastWar(war);
            } catch (Exception ex) {
                KingdomWars.logger().warning("Error saving last war for war between " + war.getDeclaringNation() + " and " + war.getDeclaredNation());
                ex.printStackTrace();
            }
        }
    }

    public void renameWarNation(String oldName, String newName) {
        boolean renamedNation = false;

        for (WarAbstract war : allWars) {
            if (war.isInWar(oldName)) {
                renamedNation = true;
                war.renameNation(oldName, newName);

                if (war.getWarStage() == WarStage.FIGHTING) {
                    WarBoard.updateNationNames((War) war, oldName, war.isDeclaringNation(newName));
                }
            }
        }

        if (renamedNation)
            saveCurrentWars();
    }

    public Collection<PreWar> getScheduledWars() {
        return Collections.unmodifiableCollection(
                allWars.stream().filter(war -> war.getWarStage() == WarStage.DECLARED)
                      .map(war -> (PreWar) war)
                      .collect(Collectors.toList())
        );
    }


    public boolean alreadyScheduledForWar(String nation) {
        return getPreWar(nation) != null;
    }

    public PreWar getPreWar(String nation) {
        for (WarAbstract preWar : allWars) {
            if (preWar.getWarStage() == WarStage.DECLARED
                    && preWar.isInWar(nation))
                return (PreWar) preWar;
        }

        return null;
    }

    public void declareWar(PreWar preWar) {
        allWars.add(preWar);
    }

    public void cancelDeclaredWar(PreWar preWar) {
        allWars.remove(preWar);
    }

    public boolean isGraceNation(Nation nation) {
        long creationTime = TownyUtil.getNationCreationTime(nation);

        long dif = System.currentTimeMillis() - creationTime;
        long graceTimeMillis = TimeUnit.MINUTES.toMillis(ConfigManager.getGracePeriod());

        return graceTimeMillis > dif;
    }
}
