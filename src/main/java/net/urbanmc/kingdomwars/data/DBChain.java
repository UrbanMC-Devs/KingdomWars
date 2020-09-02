package net.urbanmc.kingdomwars.data;

import net.urbanmc.kingdomwars.KingdomWars;
import net.urbanmc.kingdomwars.manager.SQLManager;
import net.urbanmc.kingdomwars.manager.SQLManager.SQLConsumer;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayDeque;
import java.util.Queue;
import java.util.function.Consumer;
import java.util.logging.Level;

public class DBChain {

    private boolean async;
    private SQLManager manager;
    private Queue<Consumer<Connection>> callQueue = new ArrayDeque<>();

    public DBChain(SQLManager manager, boolean async) {
        this.manager = manager;
        this.async = async;
    }

    private void append(String errorMsg, SQLConsumer<Connection> conConsumer) {
        callQueue.add(con -> {
            try {
                conConsumer.accept(con);
            } catch (SQLException e) {
                KingdomWars.logger().log(Level.SEVERE, errorMsg, e);
            }
        });
    }

    public DBChain preparedUpdate(String stmtKey, SQLConsumer<PreparedStatement> stmtConsumer) {
        append("Error executing update for " + stmtKey, con -> {
            try (PreparedStatement stmt = con.prepareStatement(getSQLStmt(stmtKey))) {
                stmtConsumer.accept(stmt);
                stmt.executeUpdate();
            }
        });
        return this;
    }

    public DBChain preparedUpdate(String stmtKey, Object... preparedObjects) {
        if (preparedObjects != null && preparedObjects.length > 0) {
            append("Error executing update for " + stmtKey, con -> {
                try (PreparedStatement stmt = con.prepareStatement(getSQLStmt(stmtKey))) {
                    for (int i = 0; i < preparedObjects.length; i++) {
                        stmt.setObject(i + 1, preparedObjects[i]);
                    }
                    stmt.executeUpdate();
                }
            });
        }
        return this;
    }

    public DBChain preparedBatch(String stmtKey, SQLConsumer<PreparedStatement> stmtConsumer) {
        append("Error executing batch for " + stmtKey, con -> {
            try (PreparedStatement stmt = con.prepareStatement(getSQLStmt(stmtKey))) {
                stmtConsumer.accept(stmt);
                stmt.executeBatch();
            }
        });
        return this;
    }

    public DBChain query(String queryKey, SQLConsumer<ResultSet> rsConsumer) {
        append("Error executing query " + queryKey, con -> {
            try (Statement stmt = con.createStatement()) {
                ResultSet rs = stmt.executeQuery(getSQLStmt(queryKey));
                rsConsumer.accept(rs);
            }
        });
        return this;
    }

    public void execute() {
        String errorMsg = "Error getting connection for connection chain!";
        SQLConsumer<Connection> conConsumer = con -> {
            while (!callQueue.isEmpty()) {
                callQueue.poll().accept(con);
            }
        };

        manager.connectionConsumer(async, errorMsg, conConsumer);
    }

    private String getSQLStmt(String key) {
        return manager.getSQLStmt(key);
    }

}
