package net.urbanmc.kingdomwars.manager;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import net.urbanmc.kingdomwars.KingdomWars;
import net.urbanmc.kingdomwars.data.DBChain;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.PropertyResourceBundle;
import java.util.ResourceBundle;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;
import java.util.logging.Level;

public class SQLManager {

    @FunctionalInterface
    public interface SQLConsumer<T> {
        void accept(T obj) throws SQLException;
    }

    @FunctionalInterface
    public interface SQLFunctionr<T, R> {
        R accept(T obj) throws SQLException;
    }

    private final File file;

    private final ExecutorService executor = Executors.newSingleThreadExecutor();

    private Cache<String, Object> dbCache = CacheBuilder.newBuilder()
            .expireAfterAccess(3, TimeUnit.MINUTES)
            .build();

    private ResourceBundle bundle;

    public SQLManager(File pluginDir) {
        this.file = new File(pluginDir, "wars.db");
        createFile();
        loadBundle();
    }

    private void createFile() {
        if (!file.exists()) {
            if (!file.getParentFile().exists())
                file.getParentFile().mkdir();

            try {
                file.createNewFile();
            } catch (IOException e) {
                KingdomWars.logger().log(Level.SEVERE, "Error creating wars.db!", e);
            }
        }
    }

    public void shutdown() {
        executor.shutdown();
        try {
            executor.awaitTermination(3, TimeUnit.MINUTES);
        } catch (InterruptedException e) {
            System.out.println("[KingdomWars] Error shutting down SQL executor!");
        }
    }

    // ---- SQL Methods ----

    public boolean testConnection() {
        return getConnection() != null;
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

    public void connectionConsumer(boolean async, String errorMsg, SQLConsumer<Connection> consumer) {
        Runnable runnable = () -> connectionConsumer(errorMsg, consumer);

        if (async)
            run(runnable);
        else
            runnable.run();
    }

    public boolean connectionConsumer(String errorMsg, SQLConsumer<Connection> consumer) {
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

    public boolean executeUpdateRaw(String updateStmt, String errorMsg) {
        return connectionConsumer(errorMsg, (con) -> {
            try (Statement stmt = con.createStatement()) {
                stmt.executeUpdate(updateStmt);
            }
        });
    }

    public boolean executeUpdates(String errorMsg, String... updateKeys) {
        if (updateKeys == null || updateKeys.length == 0)
            return false;


        return connectionConsumer(errorMsg, (con) -> {
            try (Statement stmt = con.createStatement()) {
                for (String updateKey : updateKeys) {
                    stmt.execute(getValue(updateKey));
                }
            }
        });
    }

    public void executeUpdate(boolean async, String stmtKey, String errorMsg, Object... parameters) {
        final String sqlSTMT = getValue(stmtKey);
        errorMsg += "\nStatement: " + sqlSTMT;

        connectionConsumer(async, errorMsg, con -> {
            try (PreparedStatement stmt = con.prepareStatement(sqlSTMT)) {
                if (parameters != null && parameters.length > 0) {
                    for (int i = 0; i < parameters.length; i++) {
                        stmt.setObject(i + 1, parameters[i]);
                    }
                }
                stmt.executeUpdate();
            }
        });
    }

    public void executePreparedQuery(Connection con, String stmtKey, SQLConsumer<PreparedStatement> preparedConsumer,
                                     SQLConsumer<ResultSet> resultConsumer) throws SQLException {
        try (PreparedStatement stmt = con.prepareStatement(getValue(stmtKey))) {
            if (preparedConsumer != null)
                preparedConsumer.accept(stmt);
            ResultSet rs = stmt.executeQuery();
            resultConsumer.accept(rs);
        }
    }

    private void run(Runnable run) {
        executor.submit(run);
    }

    private void loadBundle() {
        try {
            InputStream input = getClass().getClassLoader().getResourceAsStream("sql.properties");
            Reader reader = new InputStreamReader(input, "UTF-8");

            bundle = new PropertyResourceBundle(reader);

            reader.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private String getValue(String resourceKey) {
        // Allows keys to be used to get joint
        if (resourceKey.contains(".")) {
            StringBuilder builder = new StringBuilder();
            String[] split = resourceKey.split("\\.");
            if (split.length == 2) {
                builder.append(bundle.getString(split[0]))
                        .append(" ")
                        .append(bundle.getString(resourceKey));
                return builder.toString();
            }
        }

        return bundle.getString(resourceKey);
    }

    public String getSQLStmt(String key) {
        return getValue(key);
    }

    public DBChain newChain(boolean async) {
        return new DBChain(this, async);
    }

    public void useFromCacheOrFetch(boolean async, String cacheKey, Consumer<Object> cacheConsumer,
                                    SQLFunctionr<Connection, Object> fetchConsumer) {

        Object o = dbCache.getIfPresent(cacheKey);

        if (o != null)
            cacheConsumer.accept(o);
        else {
            String errorMessage = "Error fetching cache for key: " + cacheKey;
            SQLConsumer<Connection> conConsumer = con -> {
                Object retrieved = fetchConsumer.accept(con);
                if (retrieved != null) {
                    dbCache.put(cacheKey, retrieved);
                }
                cacheConsumer.accept(retrieved);
            };

            connectionConsumer(async, errorMessage, conConsumer);
        }
    }

    public void invalidateCache(String cacheKey) {
        dbCache.invalidate(cacheKey);
    }


}
