package net.urbanmc.kingdomwars.manager;

import com.palmergames.bukkit.towny.object.Nation;
import net.urbanmc.kingdomwars.KingdomWars;
import net.urbanmc.kingdomwars.data.DBChain;
import net.urbanmc.kingdomwars.data.LastWar;
import net.urbanmc.kingdomwars.data.WarResult;
import net.urbanmc.kingdomwars.util.TownyUtil;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public class WarArchiveManager {

    private SQLManager sqlManager;

    // Recent last wars
    private List<LastWar> recentWars;

    // A dummy war
    private final LastWar DUMMY_WAR = new LastWar();

    public WarArchiveManager(SQLManager manager) {
        this.sqlManager = manager;
        createTables();
        loadRecentWars();
    }
    private boolean createTables() {
        return sqlManager.executeUpdates("Error creating tables for the Archive Manager!",
                "create-nations-table", "create-past-wars-table", "create-past-war-allies");
    }

    public void createNation(final UUID nationUUID, final String nationName) {
        sqlManager.executeUpdate(true,"insert-nation",
                "Error adding a nation into the archive db!",
                nationUUID.toString(), nationName);
    }

    public void updateNation(UUID nationUUID, String oldName, String nationName) {
        updateRecentWars(oldName, nationName);
        sqlManager.executeUpdate(true,"update-nation-name",
                "Error changing nation name in the archive db!",
                nationName, nationUUID.toString());
    }

    public void insertLastWar(LastWar war) {
        if (war == null)
            return;

        addRecentWar(war);

        DBChain chain = sqlManager.newChain(true);

        chain.preparedUpdate("insert-war",
                war.getDeclaringUUID().toString(),
                war.getDeclaredUUID().toString(),
                war.getDeclaringPoints(),
                war.getDeclaredPoints(),
                war.getStartTime(),
                war.getEndTime(),
                war.getMoneyWon(),
                war.getMoneyLost(),
                war.getTownblocksWon(),
                war.getTownblocksLost(),
                war.getResult().name());

        chain.query("recent-id", rs -> {
            int archiveID = rs.getInt(1);
            if (archiveID != 0) {
                war.setArchiveID(archiveID);
            } else {
                KingdomWars.logger().severe("Error getting archive ID for a recently inserted war!");
            }
        });

        chain.preparedBatch("insert-ally", stmt -> {
            // "INSERT INTO PAST_WAR_ALLIES (WAR_ID, ALLY, DECLARING) VALUES (?, ?, ?)"
            if (war.getArchiveID() == 0) {
                KingdomWars.logger().severe("Archive ID is invalid!");
                return;
            }

            stmt.setInt(1, war.getArchiveID());
            for (String ally : war.getDeclaredAllies()) {
                UUID allyUUID = TownyUtil.getNationUUID(ally);
                if (allyUUID != null) {
                    stmt.setString(2, allyUUID.toString());
                    stmt.setBoolean(3, true);
                    stmt.addBatch();
                }
            }

            for (String ally : war.getDeclaringAllies()) {
                UUID allyUUID = TownyUtil.getNationUUID(ally);
                if (allyUUID != null) {
                    stmt.setString(2, allyUUID.toString());
                    stmt.setBoolean(3, false);
                    stmt.addBatch();
                }
            }
        });

        chain.execute();
    }

    private LastWar buildLastWarFromQuery(ResultSet rs) throws SQLException {
        // DECLARING_NAME, DECLARED_NAME,
        // DECLARING_POINTS, DECLARED_POINTS, START_TIME, END_TIME, MONEY_WON, MONEY_LOSS, TOWNBLOCKS_WON, TOWNBLOCKS_LOST, RESULT
        return new LastWar(
                rs.getInt("id"),
                rs.getString("DECLARING_NAME"), rs.getString("DECLARED_NAME"),
                rs.getInt("DECLARING_POINTS"), rs.getInt("DECLARED_POINTS"),
                rs.getLong("START_TIME"), rs.getLong("END_TIME"),
                rs.getDouble("MONEY_WON"), rs.getDouble("MONEY_LOSS"),
                rs.getInt("TOWNBLOCKS_WON"), rs.getInt("TOWNBLOCKS_LOST"),
                WarResult.valueOf(rs.getString("RESULT"))
        );
    }

    private List<LastWar> getArchivedWarsFromQuery(Connection con, String stmt, SQLManager.SQLConsumer<PreparedStatement> stmtConsumer)
            throws SQLException {
        Map<Integer, LastWar> warIDMap = new HashMap<>();

        sqlManager.executePreparedQuery(con, stmt,
                stmtConsumer, rs -> {
                    while (rs.next()) {
                        LastWar lastWar = buildLastWarFromQuery(rs);
                        warIDMap.put(lastWar.getArchiveID(), lastWar);
                    }
                });

        try (PreparedStatement preparedStmt = con.prepareStatement(sqlManager.getSQLStmt("select-war-allies"))) {
            for (Map.Entry<Integer, LastWar> entry : warIDMap.entrySet()) {
                preparedStmt.setInt(1, entry.getKey());
                try (ResultSet rs = preparedStmt.executeQuery()) {
                    while (rs.next()) {
                        String allyName = rs.getString("ALLY_NAME");
                        if (allyName != null) {
                            if (rs.getBoolean("DECLARING")) {
                                entry.getValue().addDeclaringAlly(allyName);
                            } else {
                                entry.getValue().addDeclaredAlly(allyName);
                            }
                        }
                    }
                }
            }
        }
        return new ArrayList<>(warIDMap.values());
    }

    public CompletableFuture<LastWar> getWarFromID(int id) {
        final CompletableFuture<LastWar> warFuture = new CompletableFuture<>();
        final String cacheKey = "war_" + id;
        sqlManager.useFromCacheOrFetch(true, cacheKey,
                o -> warFuture.complete(getLastWarFromCache(o)),
                con -> {
                    List<LastWar> queryResult = getArchivedWarsFromQuery(con, "select-past-war.war-id",
                            p -> p.setInt(1, id));

                    return queryResult.isEmpty() ? DUMMY_WAR : queryResult.get(0);

                });

        return warFuture;
    }

    public CompletableFuture<List<LastWar>> getWarFromNation(UUID nation) {
        final CompletableFuture<List<LastWar>> warFuture = new CompletableFuture<>();
        final String cacheKey = "wars_" + nation;
        sqlManager.useFromCacheOrFetch(true, cacheKey,
                o -> warFuture.complete(getLastWarsFroMCache(o)),
                con -> {
                    List<LastWar> nationWars = getArchivedWarsFromQuery(con, "select-past-war.nation-uuid", p -> {
                        String uuidString = nation.toString();
                        p.setString(1, uuidString);
                        p.setString(2, uuidString);
                    });

                    return nationWars.isEmpty() ? Collections.emptyList() : nationWars;
                });

        return warFuture;
    }

    private List<LastWar> getLastWarsFroMCache(Object object) {
        if (!(object instanceof List))
            return Collections.emptyList();

        List<?> wildList = (List<?>) object;

        if(wildList.isEmpty() || !(wildList.get(0) instanceof LastWar))
            return Collections.emptyList();

        return (List<LastWar>) object;
    }

    private LastWar getLastWarFromCache(Object o) {
        if (!(o instanceof LastWar))
            return null;

        LastWar lastWar = (LastWar) o;

        if (lastWar == DUMMY_WAR)
            return null;

        return lastWar;
    }

    public void removeNationCache(UUID nationID) {
        sqlManager.invalidateCache("wars_" + nationID);
    }

    private List<LastWar> getRecentWars() {
        if (recentWars == null)
            return Collections.emptyList();

        return Collections.unmodifiableList(recentWars);
    }

    public boolean hasRecentWar(String nation1, String nation2) {
        List<LastWar> lastWars = getRecentWars();

        for (LastWar lastWar : lastWars) {
            if (lastWar.foughtWar(nation1, nation2)) {
                return true;
            }
        }

        return false;
    }

    private void addRecentWar(LastWar lastWar) {
        // Make sure recent wars only contain the most recent war between the same two nations
        if (recentWars != null) {
            recentWars.removeIf(lw ->
                    lw.foughtWar(lastWar.getDeclaringNation(), lastWar.getDeclaredNation()));
        }
        else {
            recentWars = new ArrayList<>();
        }

        recentWars.add(lastWar);
    }

    public LastWar getRecentWar(Nation nation1, Nation nation2) {
        for (LastWar lastWar : getRecentWars()) {
            if (lastWar.foughtWar(nation1.getName(), nation2.getName()))
                return lastWar;
        }

        return null;
    }

    public boolean canRevenge(Nation declaringNation, Nation declaredNation) {
        LastWar lastWar = getRecentWar(declaringNation, declaredNation);

        if (lastWar == null)
            return false;

        // Revenge Criteria:
        // - Declaring nation must have won the war
        // - Declaring nation cannot revenge

        if (lastWar.isDeclaringWinner() && !lastWar.isDeclaringNation(declaringNation.getName())) {
            long revengeDate = lastWar.getEndTime() + ConfigManager.getLastTimeRevenge();
            return System.currentTimeMillis() >= revengeDate;
        }

        return false;
    }

    private void updateRecentWars(String oldName, String newName) {
        for (LastWar lastWar : getRecentWars()) {
            if (lastWar.foughtWar(oldName)) {
                if (lastWar.isDeclaringNation(oldName)) {
                    lastWar.setDeclaringNation(newName);
                } else {
                    lastWar.setDeclaredNation(newName);
                }
            }
        }
    }

    public void removeRecentWars(String nation) {
        if (recentWars != null) {
            recentWars.removeIf(lastWar -> lastWar.foughtWar(nation));
        }
    }

    public void loadRecentWars() {
        // Get whether the revenge or losing time is long
        long lastTime = Math.max(ConfigManager.getLastTime(), ConfigManager.getLastTimeRevenge());
        long warsAfter = System.currentTimeMillis() - lastTime;
        if (recentWars != null)
            recentWars.clear();

        List<LastWar> recentWarsQuery = new ArrayList<>();
        sqlManager.connectionConsumer("Error fetching recent wars!", con -> {
           List<LastWar> queryResult = getArchivedWarsFromQuery(con, "select-past-war.recent",
                                        ps -> ps.setLong(1, warsAfter));
           recentWarsQuery.addAll(queryResult);
        });

        for (LastWar lastWar : recentWarsQuery) {
            addRecentWar(lastWar);
        }
    }

    public void removeWar(int warID) {
        // Remove from DB
        sqlManager.executeUpdate(true, "delete-war", "Error deleting war id " + warID, warID);
        // Remove from cache
        sqlManager.invalidateCache("war_" + warID);

        // Remove from recent wars
        if (recentWars != null) {
            int indexToDelete = -1;
            for (int i = 0; i < recentWars.size(); i++) {
                if (recentWars.get(i).getArchiveID() == warID) {
                    indexToDelete = i;
                    break;
                }
            }
            recentWars.remove(indexToDelete);
        }
    }

}
