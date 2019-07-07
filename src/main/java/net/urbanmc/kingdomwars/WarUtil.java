package net.urbanmc.kingdomwars;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.palmergames.bukkit.towny.exceptions.EconomyException;
import com.palmergames.bukkit.towny.object.Nation;
import net.urbanmc.kingdomwars.data.PreWar;
import net.urbanmc.kingdomwars.data.last.LastWar;
import net.urbanmc.kingdomwars.data.last.LastWarList;
import net.urbanmc.kingdomwars.data.leaderboard.Leaderboard;
import net.urbanmc.kingdomwars.data.leaderboard.LeaderboardList;
import net.urbanmc.kingdomwars.data.war.War;
import net.urbanmc.kingdomwars.data.war.WarList;
import net.urbanmc.kingdomwars.data.war.WarListSerializer;
import net.urbanmc.kingdomwars.event.WarEndEvent;
import net.urbanmc.kingdomwars.util.TownyUtil;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.*;

public class WarUtil {

    private static Gson gson;

    private static List<War> wars;
    private static List<PreWar> scheduledWars = new ArrayList<>();
    private static List<LastWar> last;
    private static List<Leaderboard> leaderboardList;

    static {
        gson = new GsonBuilder().registerTypeAdapter(WarList.class, new WarListSerializer()).create();
        createFiles();
        loadWars();
        loadLast();
        loadLeaderboard();
    }

    private static void createFiles() {
        File file = new File("plugins/KingdomWars/wars.json");

        if (!file.getParentFile().isDirectory()) {
            file.getParentFile().mkdir();
        }

        if (!file.exists()) {
            try {
                file.createNewFile();
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        }

        File last = new File("plugins/KingdomWars/last.json");

        if (!last.exists()) {
            try {
                last.createNewFile();
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        }

        File leaderboard = new File("plugins/KingdomWars/leaderboard.json");

        if (!leaderboard.exists()) {
            try {
                leaderboard.createNewFile();
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        }

    }

    private static void loadWars() {
        wars = new ArrayList<>();

        try {
            Scanner scanner = new Scanner(new File("plugins/KingdomWars/wars.json"));

            wars = gson.fromJson(scanner.nextLine(), WarList.class).getWars();

            scanner.close();
        } catch (Exception ignored) {
        }
    }

    private static void loadLast() {
        last = new ArrayList<>();

        try {
            Scanner scanner = new Scanner(new File("plugins/KingdomWars/last.json"));

            last = new Gson().fromJson(scanner.nextLine(), LastWarList.class).getLast();

            scanner.close();
        } catch (Exception ignored) {
        }

        reloadLast();
    }

    private static void loadLeaderboard() {
        leaderboardList = new ArrayList<>();

        try {
            Scanner scanner = new Scanner(new File("plugins/KingdomWars/leaderboard.json"));

            Gson gson = new Gson();

            leaderboardList = gson.fromJson(scanner.nextLine(), LeaderboardList.class).getLeaderboards();
            if (leaderboardList == null)
                System.out.println("[KingdomWars] Loading Leaderboard List = null");
            scanner.close();
        } catch (Exception ignored) {
        }

    }

    public static void filterLeaderboard() {
        Nation n;
        List<Leaderboard> newLeaderBoardList = new ArrayList<>();
        for (Leaderboard leaderboard : leaderboardList) {
            n = TownyUtil.getNation(leaderboard.getNation());

            if (n != null) newLeaderBoardList.add(leaderboard);
        }

        leaderboardList = newLeaderBoardList;
        saveLeaderboard();
    }

    private static void reloadLast() {
        long millis = System.currentTimeMillis();

        ArrayList<LastWar> remove = new ArrayList<>();

        last.stream().filter(t -> t.getMillis() <= millis).forEach(remove::add);

        last.removeAll(remove);

        saveLast();
    }

    public static void startWar(War war) {
        generateKillsToWin(war);

        war.setStarted();
        wars.add(war);
        saveWars();
        WarBoard.createBoard(war);

        Nation declaring = TownyUtil.getNation(war.getDeclaringNation());
        TownyUtil.setNationBalance(declaring,
                TownyUtil.getNationBalance(declaring) - KingdomWars.getStartAmount(),
                "War start with " + war.getDeclaredNation());
    }

    public static void endWar(War war) {
        wars.remove(war);
        saveWars();
        end(war);
    }

    public static void updateWar(War war) {
        War oldWar = getWar(TownyUtil.getNation(war.getDeclaringNation()));

        if (oldWar != null) {
            wars.remove(oldWar);
        }

        wars.add(war);
        saveWars();
    }

    public static boolean inWar(Nation nation) {
        return inWar(nation.getName());
    }

    public static boolean inWar(String nation) {
        for (War war : copyWarList()) {

            if (war.getDeclaringNation().equals(nation) || war.getDeclaredNation().equals(nation))
                return true;

            if (war.hasAllies() && war.isAllied(nation)) return true;
        }

        return false;
    }

    public static List<War> getWarList() {
        return wars;
    }

    public static War getWar(Nation nation) {
        return getWar(nation.getName());
    }

    public static War getWar(String nation) {
        for (War war : wars) {
            if (war.getDeclaringNation().equals(nation) || war.getDeclaredNation().equals(nation))
                return war;

            if (war.hasAllies() && war.isAllied(nation)) return war;
        }

        return null;
    }

    public synchronized static boolean checkForceEnd(War war) {
        long millis = System.currentTimeMillis() - war.getStarted();

        if (millis >= KingdomWars.getEndTime()) {
            endWar(war);
            return true;
        } else
            return false;
    }

    public synchronized static void checkForceEndAll() {
        for (War war : copyWarList()) {
            if (war == null) continue;
            checkForceEnd(war);
        }
    }

    public synchronized static void checkWin(War war) {
        Nation winner = null, loser = null;

        if (war.getDeclaringPoints() >= war.getKillsToWin()) {
            winner = TownyUtil.getNation(war.getDeclaringNation());
            loser = TownyUtil.getNation(war.getDeclaredNation());
        } else if (war.getDeclaredPoints() >= war.getKillsToWin()) {
            winner = TownyUtil.getNation(war.getDeclaredNation());
            loser = TownyUtil.getNation(war.getDeclaringNation());
        }

        if (winner != null && loser != null) {
            win(winner, loser, KingdomWars.getFinishAmount());
        }
    }

    public synchronized static void win(Nation winner, Nation loser, double amount) {
        War war = getWar(winner);

        WarEndEvent event = new WarEndEvent(war);
        Bukkit.getPluginManager().callEvent(event);

        if (event.isCancelled())
            return;

        wars.remove(war);
        saveWars();

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
                        amount == KingdomWars.getTruceAmount(),
                        System.currentTimeMillis() + KingdomWars.getLastTime(),
                        System.currentTimeMillis() + KingdomWars.getLastTimeRevenge());
        addLast(lastWar);

        try {
            double balance = winner.getHoldingBalance() + amount;

            if (war.getDeclaringNation().equals(winner.getName())) {
                balance += KingdomWars.getStartAmount();
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
        } else {
            try {
                loser.setBalance(balance - amount, "War loss");
            } catch (EconomyException ex) {
                ex.printStackTrace();
            }
        }

        addWinToLeaderBoard(winner.getName(), true);
        addWinToLeaderBoard(loser.getName(), false);
        updateWarInfoInLeaderboard(winner.getName(), loser.getName());
    }

    public static void end(War war) {
        Nation nation1 = TownyUtil.getNation(war.getDeclaringNation());
        Nation nation2 = TownyUtil.getNation(war.getDeclaredNation());

        WarEndEvent event = new WarEndEvent(war);
        Bukkit.getPluginManager().callEvent(event);

        wars.remove(war);
        saveWars();

        if (event.isCancelled())
            return;

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

        LastWar lastWar = new LastWar(nation1.getName(),
                nation2.getName(),
                true,
                false,
                System.currentTimeMillis() + KingdomWars.getLastTime(),
                System.currentTimeMillis() + KingdomWars.getLastTimeRevenge());
        addLast(lastWar);
    }

    public static boolean hasLast(String nation1, String nation2) {
        reloadLast();

        for (LastWar lastWar : last) {
            if (lastWar.getDeclaringNation().equals(nation1) && lastWar.getDeclaredNation().equals(nation2))
                return true;
            if (lastWar.getDeclaringNation().equals(nation2) && lastWar.getDeclaredNation().equals(nation1))
                return true;
        }

        return false;
    }

    private static void addLast(LastWar lastWar) {
        last.add(lastWar);
        reloadLast();
    }

    public static LastWar getLast(Nation nation1, Nation nation2) {
        reloadLast();

        for (LastWar lastWar : last) {
            if (lastWar.getDeclaringNation().equals(nation1.getName()) &&
                    lastWar.getDeclaredNation().equals(nation2.getName()) ||
                    lastWar.getDeclaringNation().equals(nation2.getName()) &&
                            lastWar.getDeclaredNation().equals(nation1.getName()))
                return lastWar;
        }

        return null;
    }

    public static LastWar getLastWar(Nation nation1) {
        reloadLast();

        List<LastWar> lastWars = new ArrayList<>(last);

        lastWars.removeIf((lw) -> !(lw.getDeclaredNation().equalsIgnoreCase(nation1.getName()) || lw.getDeclaringNation().equalsIgnoreCase(nation1.getName())));

        if (lastWars.isEmpty()) return null;

        if (lastWars.size() == 1) return lastWars.get(0);

        //Bigger millis means more recent. This sorts the list by the most recent lastwar.
        lastWars.sort(Comparator.comparingLong(LastWar::getMillis));

        return lastWars.get(0);
    }

    public static void removeAllLast(String nation) {
        List<LastWar> remove = new ArrayList<>();

        for (LastWar lastWar : last) {
            if (lastWar.getDeclaringNation().equals(nation) || lastWar.getDeclaredNation().equals(nation)) {
                remove.add(lastWar);
            }
        }

        last.removeAll(remove);
    }

    public static void removeLast(LastWar lastWar) {
        last.remove(lastWar);
        saveLast();
    }

    private static void saveWars() {
        try {
            PrintWriter writer = new PrintWriter(new File("plugins/KingdomWars/wars.json"));

            writer.write(gson.toJson(new WarList(wars)));

            writer.close();
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    private static void saveLast() {
        try {
            PrintWriter writer = new PrintWriter(new File("plugins/KingdomWars/last.json"));

            Gson gson = new Gson();

            writer.write(gson.toJson(new LastWarList(last)));

            writer.close();
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    public static boolean canRevenge(Nation nation1, Nation nation2) {
        LastWar lastWar = getLast(nation1, nation2);

        if (lastWar == null)
            return false;

        if (!(lastWar.isDeclaringWinner() && lastWar.isDeclaringNation(nation2.getName())))
            return false;

        return System.currentTimeMillis() <= lastWar.getRevengeMillis();
    }

    private static void saveLeaderboard() {
        try {
            PrintWriter writer = new PrintWriter(new File("plugins/KingdomWars/leaderboard.json"));

            writer.write(new Gson().toJson(new LeaderboardList(leaderboardList)));

            writer.close();
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    public static List<Leaderboard> getLeaderboards() {
        if (leaderboardList == null)
            System.out.println("Returning LList = Null");
        return leaderboardList;
    }

    private static void addWinToLeaderBoard(String nation, boolean won) {
        Leaderboard leaderboard = getLeaderboardForNation(nation);

        if (leaderboard == null) {
            leaderboard = new Leaderboard(nation);
            leaderboardList.add(leaderboard);
        }

        if (won) {
            leaderboard.setWins(leaderboard.getWins() + 1);
        } else {
            leaderboard.setLosses(leaderboard.getLosses() + 1);
        }

        saveLeaderboard();
    }

    private static void updateWarInfoInLeaderboard(String winner, String loser) {
        DateFormat df = new SimpleDateFormat("dd/MM/yy");

        String lastwarinfo = winner + ";" + loser + ";" + df.format(new Date());

        Leaderboard leaderboard = getLeaderboardForNation(winner);

        leaderboard.setLastWarInfo(lastwarinfo);
        saveLeaderboard();

        leaderboard = getLeaderboardForNation(loser);
        leaderboard.setLastWarInfo(lastwarinfo);
        saveLeaderboard();
    }

    public static void warNationRename(String oldName, String newName) {
        War war = getWar(oldName);

        boolean declaring = war.isDeclaringNation(oldName);

        if (declaring) {
            war.setDeclaringNation(newName);
        } else {
            war.setDeclaredNation(newName);
        }

        WarBoard.updateNationNames(war, oldName, declaring);

        saveWars();
    }

    public static void leaderBoardNationRename(String oldName, String newName) {
        Leaderboard leaderboard = getLeaderboardForNation(oldName);

        if (leaderboard != null) {
            leaderboard.setNation(newName);
            saveLeaderboard();
        }
    }

    public static void lastNationRename(String oldName, String newName) {
        for (LastWar lastWar : last) {
            if (!lastWar.getDeclaringNation().equals(oldName) || lastWar.getDeclaredNation().equals(oldName))
                continue;

            boolean declaring = lastWar.isDeclaringNation(oldName);

            if (declaring) {
                lastWar.setDeclaringNation(newName);
            } else {
                lastWar.setDeclaredNation(newName);
            }
        }

        saveLast();
    }

    public static void leaderBoardNationDelete(String nation) {
        Leaderboard leaderboard = getLeaderboardForNation(nation);

        if (leaderboard != null) {
            leaderboardList.remove(leaderboard);
            saveLeaderboard();
        }
    }

    private static Leaderboard getLeaderboardForNation(String nation) {
        if (leaderboardList.isEmpty())
            return null;

        for (Leaderboard leaderboard : leaderboardList) {
            if (leaderboard.getNation().equals(nation))
                return leaderboard;
        }

        return null;
    }

    private static void generateKillsToWin(War war) {
        int kills = KingdomWars.getWinningKills();

        kills += war.getAllies(true).size() * KingdomWars.getAllyKills();
        kills += war.getAllies(false).size() * KingdomWars.getAllyKills();

        war.setKills(kills);


    }

    public static boolean alreadyScheduledForWar( String nation) {
        for (PreWar preWar : copyPreWars()) {
            if (preWar.alreadyDeclared(nation)) return true;
        }

        return false;
    }

    public static PreWar getPreWar( String nation) {
        for (PreWar preWar : scheduledWars) {
            if (preWar.alreadyDeclared(nation)) return preWar;
        }

        return null;
    }

    public static void addPreWar(PreWar preWar) {
        scheduledWars.add(preWar);
    }

    public static void removePreWar(PreWar preWar) {
        scheduledWars.remove(preWar);
    }


    //This method is to avoid concurrent modification of the main war list
    private static List<War> copyWarList() {
        return new ArrayList<>(wars);
    }

    private static List<PreWar> copyPreWars() {
        return new ArrayList<>(scheduledWars);
    }
}
