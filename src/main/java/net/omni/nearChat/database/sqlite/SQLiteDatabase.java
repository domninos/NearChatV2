package net.omni.nearChat.database.sqlite;

import net.omni.nearChat.NearChatPlugin;
import net.omni.nearChat.database.ISQLDatabase;
import net.omni.nearChat.database.NearChatDatabase;

import java.io.File;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

public class SQLiteDatabase implements NearChatDatabase, ISQLDatabase {
    // TODO

    private final NearChatPlugin plugin;

    private final File db_file;
    private final String urlString;

    public SQLiteDatabase(NearChatPlugin plugin) {
        this.plugin = plugin;
        this.db_file = new File(plugin.getDataFolder(), "nearchat.db");
        this.urlString = "jdbc:sqlite:" + db_file.getAbsolutePath();
    }

    protected Connection getConn() throws SQLException {
        return DriverManager.getConnection(this.urlString);
    }

    public void connect() {
        // TODO async

        if (!db_file.exists()) {
            try {
                db_file.createNewFile(); // TODO
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }

        try (Connection conn = getConn()) {
            System.out.println("Connection to SQLite has been established.");
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
    }

    @Override
    public void close() {

    }

    @Override
    public boolean isEnabled() {
        return false;
    }

    @Override
    public boolean connect(String host, int port, String database_name, String user, String password) {
        return false;
    }

    @Override
    public String getHost() {
        return "nearchat.db";
    }

    @Override
    public boolean connectConfig() {
        return false;
    }

    @Override
    public void checkTable() {
        String create = "CREATE TABLE IF NOT EXISTS " + TABLE_NAME + "(player_name VARCHAR(36), enabled BOOLEAN)";

        try (Connection conn = getConn();
             PreparedStatement stmt = conn.prepareStatement(create)) {

            stmt.executeUpdate();
        } catch (SQLException e) {
            plugin.error("Something went wrong checking database table.", e);
        }
    }

    @Override
    public CompletableFuture<Boolean> get(String playerName) {
        return null;
    }

    @Override
    public void insert(String playerName, Boolean value) {

    }

    @Override
    public CompletableFuture<Boolean> exists(String playerName) {
        return null;
    }

    @Override
    public void saveMap(Map<String, Boolean> enabledPlayers, boolean async) {

    }

    @Override
    public void saveCallbackMap(Map<String, Boolean> enabledPlayers) {

    }

    @Override
    public void savePlayer(String playerName, Boolean value, boolean async) {

    }

    @Override
    public void saveCallback(String playerName, Boolean value) {

    }

    @Override
    public void handleExists(String playerName) {

    }

    @Override
    public void saveNonExists(String playerName, Boolean value) {

    }

    @Override
    public boolean fetchExists(String playerName) {
        return false;
    }

    @Override
    public boolean fetchEnabled(String playerName) {
        return false;
    }
}
