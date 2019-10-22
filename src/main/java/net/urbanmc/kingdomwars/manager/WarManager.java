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
            end(war);
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

    public synchronized void win(Nation winner, Nation loser, double amount) {
        War war = getWar(winner);

        WarEndEvent event = new WarEndEvent(war);
        Bukkit.getPluginManager().callEvent(event);

        if (event.isCancelled())
            return;

        currentWars.remove(war);
        saveCurrentWars();

        for (Player p : Bukkit.getServer().getOnlinePlayers()) {
            Nation nation = TownyUtil.getNation(p);

            if (nation == null)
                continue;

            if (nation.equals(winner) || nation.equals(loser)) {
                if (p.getScoreboard().equals(war.getScoreBoard())) {
                    p.setScoreboard(Bukkit.getScoreboardManager().getNewScoreboard());
                }
            }
        }

        TownyUtil.sendNationMessage(winner, "Your nation has won the war against " + loser.getName() + "!");
        TownyUtil.sendNationMessage(loser, "Your nation has lost the war against " + winner.getName() + "!");

        LastWar lastWar =
                new LastWar(winner.getName(), loser.getName(), war.isDeclaringNation(winner.getName()),
                        amount == ConfigManager.getTruceAmount(),
                        System.currentTimeMillis() + ConfigManager.getLastTime(),
                        System.currentTimeMillis() + ConfigManager.getLastTimeRevenge());

        // Add last war
        plugin.getLastWarManager().addLast(lastWar);

        try {
            double balance = winner.getHoldingBalance() + amount;

            if (war.getDeclaringNation().equals(winner.getName())) {
                balance += ConfigManager.getStartAmount();
            }

            winner.setBalance(balance, "War win against " + loser.getName());
        } catch (EconomyException ex) {
            ex.printStackTrace();
        }

        double balance = 0;

        try {
            balance = loser.getHoldingBalance();
        } catch (EconomyException ex) {
            ex.printStackTrace();
        }

        if (balance < amount) {
            TownyUtil.sendNationMessage(loser, "Your nation could not pay the war loss fee and has fallen!");
            TownyUtil.deleteNation(loser);
            plugin.getLeaderboard().deleteNationFromLeaderboard(loser.getName());
        } else {
            try {
                loser.setBalance(balance - amount, "War loss");
            } catch (EconomyException ex) {
                ex.printStackTrace();
            }
        }

        if (loser.getExtraBlocks() > ConfigManager.getNegTownBlockMin() && TownySettings.getNationBonusBlocks(loser) >= ConfigManager.getTownBlockLoss()) {
            TownyUtil.addNationBonusBlocks(winner, ConfigManager.getTownBlockBonus());
            TownyUtil.addNationBonusBlocks(loser, -1 * ConfigManager.getTownBlockLoss());
            TownyAPI.getInstance().getDataSource().saveNation(winner);
            TownyAPI.getInstance().getDataSource().saveNation(loser);
        }

        // Update leaderboard
        plugin.getLeaderboard().addWinToLeaderBoard(winner.getName());
        plugin.getLeaderboard().addLossToLeaderBoard(loser.getName());
        plugin.getLeaderboard().sortLeaderboard();
        plugin.getLeaderboard().saveLeaderboard();
    }

    public void end(War war) {
        Nation nation1 = TownyUtil.getNation(war.getDeclaringNation());
        Nation nation2 = TownyUtil.getNation(war.getDeclaredNation());

        //isPrimaryThread doesn't neccesarily mean that it's async but it's one of the better options
        WarEndEvent event = new WarEndEvent(war);
        Bukkit.getPluginManager().callEvent(event);

        if (event.isCancelled()) return;

        currentWars.remove(war);
        saveCurrentWars();

        for (Player p : Bukkit.getServer().getOnlinePlayers()) {
            Nation nation = TownyUtil.getNation(p);

            if (nation == null)
                continue;

            if (nation.equals(nation1) || nation.equals(nation2)) {
                if (p.getScoreboard().equals(war.getScoreBoard())) {
                    p.setScoreboard(Bukkit.getScoreboardManager().getNewScoreboard());
                }
            }
        }

        String nation1Name = nation1 == null ? "null" : nation1.getName(),
                nation2Name = nation2 == null ? "null" : nation2.getName();

        LastWar lastWar = new LastWar(nation1Name,
                nation2Name,
                true,
                false,
                System.currentTimeMillis() + ConfigManager.getLastTime(),
                System.currentTimeMillis() + ConfigManager.getLastTimeRevenge());

        // Add last war
        plugin.getLastWarManager().addLast(lastWar);
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
