package net.urbanmc.kingdomwars.manager;

import net.urbanmc.kingdomwars.KingdomWars;
import net.urbanmc.kingdomwars.data.Leaderboard;
import net.urbanmc.kingdomwars.util.TownyUtil;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public class LeaderboardManager {

    private SQLManager sqlManager;

    private final String CACHE_KEY = "leaderboard";

    public LeaderboardManager(SQLManager manager) {
       this.sqlManager = manager;
       createTable();
    }

    private void createTable() {
        sqlManager.executeUpdate(false, "create-leaderboard-table",
                "Error creating leaderboard table!");
    }

    public void deleteNationFromLeaderboard(String nation) {
        UUID nationUUID = TownyUtil.getNationUUID(nation);

        if (nationUUID == null) {
            KingdomWars.logger().severe("Cannot delete nation " + nation + " from leaderboard because nation not found!");
            return;
        }

        sqlManager.executeUpdate(true, "delete-nation-leaderboard",
                "Error deleting nation " + nation + " from leaderboard!",
                nationUUID.toString());
    }

    public void addWinToLeaderBoard(UUID nationUUID) {
        sqlManager.executeUpdate(true, "add-leaderboard-win",
                "Error adding leaderboard win for nation " + nationUUID,
                nationUUID.toString());
        sqlManager.invalidateCache(CACHE_KEY);
    }

    public void addLossToLeaderBoard(UUID nationUUID) {
        sqlManager.executeUpdate(true, "add-leaderboard-loss",
                "Error adding leaderboard loss for nation " + nationUUID,
                nationUUID.toString());
        sqlManager.invalidateCache(CACHE_KEY);
    }

    public CompletableFuture<List<Leaderboard>> getLeaderboard() {
        CompletableFuture<List<Leaderboard>> future = new CompletableFuture<>();

        sqlManager.useFromCacheOrFetch(true, CACHE_KEY,
                o -> future.complete((List<Leaderboard>) o),
                con -> {
                    final List<Leaderboard> leaderBoardList = new ArrayList<>();
                    sqlManager.executePreparedQuery(con, "select-leaderboard", null,
                            rs -> {
                                while (rs.next()) {
                                    leaderBoardList.add(buildLeaderboard(rs));
                                }
                            });
                    return leaderBoardList.isEmpty() ? Collections.emptyList() : leaderBoardList;
                });

        return future;
    }

    private Leaderboard buildLeaderboard(ResultSet rs) throws SQLException {
        return new Leaderboard(rs.getString("NAME"),
                rs.getInt("WINS"), rs.getInt("LOSSES"));
    }

    public void invalidateLeaderboardCache() {
        sqlManager.invalidateCache(CACHE_KEY);
    }

}
