package net.urbanmc.kingdomwars.manager;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.palmergames.bukkit.towny.object.Nation;
import net.urbanmc.kingdomwars.KingdomWars;
import net.urbanmc.kingdomwars.data.LastWar;
import net.urbanmc.kingdomwars.data.WarResult;
import net.urbanmc.kingdomwars.util.TownyUtil;

import java.io.File;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;

public class WarArchiveManager {

    @FunctionalInterface
    private interface SQLConsumer<T> {
        void accept(T obj) throws SQLException;
    }

    private final String CREATE_NATIONS_TABLE = "CREATE TABLE IF NOT EXISTS NATION_NAMES (NATION_UUID TEXT PRIMARY KEY, NATION_NAME TEXT);";

    private final String CREATE_ARCHIVE_STMT =  "CREATE TABLE IF NOT EXISTS PAST_WARS " +
                                        "(ID INTEGER PRIMARY KEY AUTOINCREMENT," +
                                        "DECLARING TEXT REFERENCES NATION_NAMES(NATION_UUID)," +
                                        " DECLARED TEXT REFERENCES NATION_NAMES(NATION_UUID)," +
                                        " DECLARING_POINTS INT, DECLARED_POINTS INT," +
                                        " START_TIME LONG, END_TIME LONG," +
                                        " MONEY_WON DOUBLE, MONEY_LOSS DOUBLE," +
                                        " TOWNBLOCKS_WON INT, TOWNBLOCKS_LOST INT," +
                                        " RESULT TEXT);";

    private final String CREATE_DECLARING_ALLIES_STMT = "CREATE TABLE IF NOT EXISTS PAST_WAR_ALLIES (" +
            "WAR_ID INT REFERENCES PAST_WARS(ID) ON DELETE CASCADE, " +
            "ALLY TEXT REFERENCES NATION_NAMES(NATION_UUID), " +
            "DECLARING BOOLEAN );";


    private final String INSERT_WAR_STMT = "INSERT INTO PAST_WARS " +
            "(DECLARING, DECLARED, DECLARING_POINTS, DECLARED_POINTS, START_TIME, END_TIME, MONEY_WON, MONEY_LOSS, TOWNBLOCKS_WON, TOWNBLOCKS_LOST, RESULT)" +
            "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ? ,?, ?)";

    private final String INSERT_ALLY_STMT = "INSERT INTO PAST_WAR_ALLIES (WAR_ID, ALLY, DECLARING) VALUES (?, ?, ?)";

    private final String SELECT_PAST_WAR = "SELECT ID, dcn.NATION_NAME DECLARING_NAME, " +
            "dn.NATION_NAME DECLARED_NAME, " +
            "DECLARING_POINTS, DECLARED_POINTS, START_TIME, END_TIME, MONEY_WON, MONEY_LOSS, TOWNBLOCKS_WON, TOWNBLOCKS_LOST, RESULT " +
            "FROM PAST_WARS pw " +
            "LEFT JOIN NATION_NAMES dcn " +
            "ON pw.DECLARING = dcn.NATION_UUID " +
            "LEFT JOIN NATION_NAMES dn " +
            "ON pw.DECLARED = dn.NATION_UUID";

    private final String SELECT_PAST_WAR_NATION_UUID = SELECT_PAST_WAR + " WHERE dcn.NATION_UUID = ? OR dn.NATION_UUID = ? ORDER BY END_TIME ASC;";

    private final String SELECT_PAST_WAR_ID = SELECT_PAST_WAR + " WHERE ID = ?";

    private final String SELECT_PAST_WAR_RECENT = SELECT_PAST_WAR + " WHERE END_TIME > ? ORDER BY END_TIME DESC;";

    private final String GET_WAR_ALLIES_STMT = "SELECT nn.NATION_NAME ALLY_NAME, DECLARING " +
                                                "FROM PAST_WAR_ALLIES pwa " +
                                                "LEFT JOIN NATION_NAMES nn ON pwa.ALLY = nn.NATION_UUID WHERE WAR_ID = ?";

    private final String UPDATE_NATION_NAME = "UPDATE NATION_NAMES SET NATION_NAME = ? WHERE NATION_UUID = ?;";

    private final String INSERT_NATION = "INSERT INTO NATION_NAMES (NATION_UUID, NATION_NAME) VALUES (?, ?);";

    private final String DELETE_WAR_STMT = "DELETE FROM PAST_WARS WHERE ID = ?;";

    private final File file;

    private final ExecutorService executor = Executors.newSingleThreadExecutor();

    private Cache<String, List<LastWar>> warCache = CacheBuilder.newBuilder()
                                                    .expireAfterAccess(3, TimeUnit.MINUTES)
                                                    .build();

    // Recent last wars
    private List<LastWar> recentWars;


    public WarArchiveManager(File pluginDir) {
        this.file = new File(pluginDir, "wars_archive.db");
    }

    public boolean testAccess() {
        return createFile() && getConnection() != null  && createTables();
    }

    private boolean createFile() {
        // Make plugin directory if not exists
        if (!file.getParentFile().exists()) {
            file.getParentFile().mkdir();
        }

        if (!file.exists()) {
            try {
                file.createNewFile();
            } catch (IOException e) {
                KingdomWars.logger().warning("Error creating wars_archive.db!");
                return false;
            }
        }

        return true;
    }

    private boolean createTables() {
        String tableCreation = CREATE_NATIONS_TABLE + " " + CREATE_ARCHIVE_STMT + CREATE_DECLARING_ALLIES_STMT;
        return executeUpdate(tableCreation , "Error creating PAST_WARS table for the Archive Manager!");
    }

    private Connection getConnection() {
        try {
            Class.forName("org.sqlite.JDBC");
            return DriverManager.getConnection("jdbc:sqlite:" + file.getPath());
        } catch (SQLException ex) {
            KingdomWars.logger().log(Level.SEVERE, "SQLite exception on initialize", ex);
        } catch (ClassNotFoundException ex) {
            KingdomWars.logger().log(Level.SEVERE, "You need the SQLite JBDC library. Google it. Put it in /lib folder.");
        }

        return null;
    }

    private boolean connectionConsumer(String errorMsg, SQLConsumer<Connection> consumer) {
        try (Connection con = getConnection()) {
            if (con == null)
                return false;

            consumer.accept(con);
        } catch (SQLException ex) {
            if (errorMsg != null) {
                KingdomWars.logger().log(Level.SEVERE, errorMsg, ex);
                return false;
            }
        }

        return true;
    }

    private void connectionConsumerAsync(final String errorMsg, final SQLConsumer<Connection> consumer) {
        run(() -> connectionConsumer(errorMsg, consumer));
    }

    private boolean executeUpdate(String updateStmt, String errorMsg) {
        return connectionConsumer(errorMsg, (con) -> {
            try (Statement stmt = con.createStatement()) {
                stmt.executeUpdate(updateStmt);
            }
        });
    }

    private boolean executePreparedUpdate(String preparedStmt, String errorMsg, SQLConsumer<PreparedStatement> preparedConsumer) {
        return connectionConsumer(errorMsg, con -> {
            try (PreparedStatement stmt = con.prepareStatement(preparedStmt)) {
                preparedConsumer.accept(stmt);
                stmt.executeUpdate();
            }
        });
    }

    private void executePreparedUpdateAsync(String preparedStmt, String errorMsg, SQLConsumer<PreparedStatement> preparedConsumer) {
        run(() -> executePreparedUpdate(preparedStmt, errorMsg, preparedConsumer));
    }

    public void createNation(final UUID nationUUID, final String nationName) {
        executePreparedUpdateAsync(INSERT_NATION, "Error adding a nation into the archive db!", stmt -> {
           stmt.setString(1, nationUUID.toString());
           stmt.setString(2, nationName);
        });
    }

    public void updateNation(UUID nationUUID, String oldName, String nationName) {
        updateRecentWars(oldName, nationName);
        executePreparedUpdateAsync(UPDATE_NATION_NAME, "Error changing nation name in the archive db!", stmt -> {
            stmt.setString(1, nationName);
            stmt.setString(2, nationUUID.toString());
        });
    }

    public void insertLastWar(LastWar war) {
        if (war == null)
            return;

        addRecentWar(war);
        connectionConsumerAsync("Error inserting a war into the war archive DB!", con -> {
            try (PreparedStatement stmt = con.prepareStatement(INSERT_WAR_STMT)) {
                // Declaring UUID
                stmt.setString(1, war.getDeclaringUUID().toString());
                // Declared UUID
                stmt.setString(2, war.getDeclaredUUID().toString());
                // Declaring Points
                stmt.setInt(3, war.getDeclaringPoints());
                // Declared Points
                stmt.setInt(4, war.getDeclaredPoints());
                // Start Time
                stmt.setLong(5, war.getStartTime());
                // End Time
                stmt.setLong(6, war.getEndTime());
                // Money Won
                stmt.setDouble(7, war.getMoneyWon());
                // Money Loss
                stmt.setDouble(8, war.getMoneyLost());
                // Townblocks won
                stmt.setInt(9, war.getTownblocksWon());
                // Townblocks Lost
                stmt.setInt(10, war.getTownblocksLost());
                // Result
                stmt.setString(11, war.getResult().name());

                stmt.executeUpdate();
            }

            int archiveID = 0;
            try (Statement fetchIDStmt = con.createStatement()) {
                ResultSet rs = fetchIDStmt.executeQuery("SELECT last_insert_rowid()");
                archiveID = rs.getInt(1);
                if (archiveID != 0) {
                    war.setArchiveID(archiveID);
                } else {
                    KingdomWars.logger().severe("Error getting archive ID for a recently inserted war!");
                }
            }

            if (archiveID != 0 && war.hasAllies()) {
                try (PreparedStatement stmt = con.prepareStatement(INSERT_ALLY_STMT)) {
                    // "INSERT INTO PAST_WAR_ALLIES (WAR_ID, ALLY, DECLARING) VALUES (?, ?, ?)"
                    for (String ally : war.getDeclaredAllies()) {
                        UUID allyUUID = TownyUtil.getNationUUID(ally);
                        if (allyUUID != null) {
                            stmt.setInt(1, archiveID);
                            stmt.setString(2, allyUUID.toString());
                            stmt.setBoolean(3, true);
                            stmt.addBatch();
                        }
                    }

                    for (String ally : war.getDeclaringAllies()) {
                        UUID allyUUID = TownyUtil.getNationUUID(ally);
                        if (allyUUID != null) {
                            stmt.setInt(1, archiveID);
                            stmt.setString(2, allyUUID.toString());
                            stmt.setBoolean(3, false);
                            stmt.addBatch();
                        }
                    }

                    stmt.executeBatch();
                }
            }
        });
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

    private List<LastWar> getArchivedWarsFromQuery(Connection con, String stmt, SQLConsumer<PreparedStatement> stmtConsumer) throws SQLException {
        Map<Integer, LastWar> warIDMap = new HashMap<>();

        try (PreparedStatement queryStmt = con.prepareStatement(stmt)) {
            if (stmtConsumer != null)
                stmtConsumer.accept(queryStmt);

            try (ResultSet rs = queryStmt.executeQuery()) {
                while (rs.next()) {
                    LastWar lastWar = buildLastWarFromQuery(rs);
                    warIDMap.put(lastWar.getArchiveID(), lastWar);
                }
            }
        }

        try (PreparedStatement preparedStmt = con.prepareStatement(GET_WAR_ALLIES_STMT)) {
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
        run(() -> {
            final String cacheKey = "war_" + id;
            List<LastWar> lastWars = warCache.getIfPresent(cacheKey);
            if (lastWars != null) {
                warFuture.complete(lastWars.isEmpty() ? null : lastWars.get(0));
            }
            else {
                connectionConsumer("Error fetching war with id " + id, con -> {
                    List<LastWar> queryResult = getArchivedWarsFromQuery(con, SELECT_PAST_WAR_ID, p -> p.setInt(1, id));
                    if (queryResult.isEmpty()) {
                        warCache.put(cacheKey, Collections.emptyList());
                    }
                    else {
                        warCache.put(cacheKey, Collections.singletonList(queryResult.get(0)));
                    }
                    warFuture.complete(queryResult.isEmpty() ? null : queryResult.get(0));
                });
            }
        });

        return warFuture;
    }

    public CompletableFuture<List<LastWar>> getWarFromNation(UUID nation) {
        final CompletableFuture<List<LastWar>> warFuture = new CompletableFuture<>();
        run(() -> {
            final String cacheKey = "wars_" + nation;
            List<LastWar> lastWars = warCache.getIfPresent(cacheKey);
            if (lastWars != null) {
                warFuture.complete(lastWars);
            }
            else {
                connectionConsumer("Error fetching war with uuid " + nation, con -> {
                    List<LastWar> queryResult = getArchivedWarsFromQuery(con, SELECT_PAST_WAR_NATION_UUID, p ->{
                        String uuidString = nation.toString();
                        p.setString(1, uuidString);
                        p.setString(2, uuidString);
                    });

                    warCache.put(cacheKey, queryResult.isEmpty() ? Collections.emptyList() : queryResult);
                    warFuture.complete(queryResult);
                });
            }
        });

        return warFuture;
    }

    public void removeCache(int warID) {
        warCache.invalidate("war_" + warID);
    }

    public void removeCache(UUID nationID) {
        warCache.invalidate("wars_" + nationID);
    }


    private void run(Runnable run) {
        executor.submit(run);
    }

    public void shutdown() {
        executor.shutdown();
        try {
            executor.awaitTermination( 2, TimeUnit.MINUTES);
        } catch (InterruptedException e) {
            System.out.println("[KingdomWars] Archive Executor was interrupted during shutdown!");
        }
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
        connectionConsumer("Error fetching recent wars!", con -> {
           List<LastWar> queryResult = getArchivedWarsFromQuery(con, SELECT_PAST_WAR_RECENT,
                                        ps -> ps.setLong(1, warsAfter));
           recentWarsQuery.addAll(queryResult);
        });

        for (LastWar lastWar : recentWarsQuery) {
            addRecentWar(lastWar);
        }
    }

    public void removeWar(int warID) {
        // Remove from DB
        executePreparedUpdate(DELETE_WAR_STMT, "Error deleting war id " + warID, ps -> ps.setInt(1, warID));
        // Remove from cache
        warCache.invalidate("war_" + warID);

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
