package net.urbanmc.kingdomwars.manager;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.palmergames.bukkit.towny.object.Nation;
import net.urbanmc.kingdomwars.data.Leaderboard;
import net.urbanmc.kingdomwars.util.TownyUtil;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Scanner;

public class LeaderboardManager {

    private final File FILE;
    private List<Leaderboard> leaderboardList = new ArrayList<>();

    public LeaderboardManager(File dataDirectory) {
        this.FILE = new File(dataDirectory, "leaderboard.json");
    }

    public void loadLeaderboard() {
        if (!FILE.exists()) {
            try {
                FILE.createNewFile();
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        }
        else {
            try (Scanner scanner = new Scanner(FILE)) {

                Gson gson = new Gson();

                Type leaderboardListType = new TypeToken<List<Leaderboard>>() {}.getType();

                leaderboardList.addAll(gson.fromJson(scanner.nextLine(), leaderboardListType));
            } catch (Exception ignored) {
            }
        }

        filterLeaderboard();
        sortLeaderboard();
    }

    public void saveLeaderboard() {
        try(PrintWriter writer =
                    new PrintWriter(FILE)) {
            writer.write(new Gson().toJson(leaderboardList));
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    public void sortLeaderboard() {
        if (!leaderboardList.isEmpty())
            Collections.sort(leaderboardList);
    }

    public void deleteNationFromLeaderboard(String nation) {
        int index = -1;

        for(int i = 0; i < leaderboardList.size(); i++) {
            final Leaderboard lb = leaderboardList.get(i);

            if (lb.getNation().equals(nation)) {
                index = i;
                break;
            }
        }

        if (index != -1) {
            leaderboardList.remove(index);
            sortLeaderboard();
            saveLeaderboard();
        }
    }

    public void addWinToLeaderBoard(String nation) {
        Leaderboard lb = getLeaderboardForNation(nation);

        if (lb == null) {
            lb = new Leaderboard(nation);
            leaderboardList.add(lb);
        }

        lb.setWins(lb.getWins() + 1);
    }

    public void addLossToLeaderBoard(String nation) {
        Leaderboard lb = getLeaderboardForNation(nation);

        if (lb == null) {
            lb = new Leaderboard(nation);
            leaderboardList.add(lb);
        }

        lb.setLosses(lb.getLosses() + 1);
    }

    private Leaderboard getLeaderboardForNation(String nation) {
        for (Leaderboard lb : leaderboardList) {
            if (lb.getNation().equals(nation))
                return lb;
        }

        return null;
    }

    public List<Leaderboard> getLeaderboard() { return leaderboardList; }

    public void renameNation(String oldName, String newName) {
        Leaderboard leaderboard = getLeaderboardForNation(oldName);

        if (leaderboard != null) {
            leaderboard.setNation(newName);
            saveLeaderboard();
        }
    }

    public void filterLeaderboard() {
        leaderboardList.removeIf(lw -> TownyUtil.getNation(lw.getNation()) == null);
        saveLeaderboard();
    }


}
