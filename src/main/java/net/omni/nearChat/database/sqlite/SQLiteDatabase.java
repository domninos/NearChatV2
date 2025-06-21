package net.omni.nearChat.database.sqlite;

import net.omni.nearChat.NearChatPlugin;
import net.omni.nearChat.database.ISQLDatabase;
import net.omni.nearChat.database.NearChatDatabase;
import net.omni.nearChat.database.SQLCredentialLess;
import org.bukkit.Bukkit;

import java.io.File;
import java.io.IOException;
import java.sql.*;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

public class SQLiteDatabase implements NearChatDatabase, ISQLDatabase, SQLCredentialLess {
    private final NearChatPlugin plugin;

    private final File db_file;
    private final String urlString;

    private boolean enabled = false;

    private final String host = "nearchat.db";

    public SQLiteDatabase(NearChatPlugin plugin) {
        this.plugin = plugin;
        this.db_file = new File(plugin.getDataFolder(), this.host);
        this.urlString = "jdbc:sqlite:" + db_file.getAbsolutePath();
    }


    @Override
    public boolean connect(String host) {
        if (!db_file.exists()) {
            try {
                if (db_file.createNewFile())
                    plugin.sendConsole(plugin.getMessageHandler().getCreatedFile(db_file.getName()));
            } catch (IOException e) {
                plugin.error("Something went wrong creating " + getHost(), e);
                return false;
            }
        }

        try {
            checkTable();

            this.enabled = true;

            plugin.sendConsole(plugin.getMessageHandler().getDBConnectedConsole(host));

            return true;
        } catch (Exception e) {
            plugin.error("Something went wrong connecting to SQLite database.", e);
            return false;
        }
    }

    @Override
    public String getHost() {
        return this.host;
    }

    @Override
    public boolean connectConfig() {
        return connect(getHost());
    }

    @Override
    public void checkTable() {
        String create = "CREATE TABLE IF NOT EXISTS " + TABLE_NAME + "(player_name VARCHAR(36), enabled BOOLEAN)";

        try (Connection connection = DriverManager.getConnection(this.urlString);
             PreparedStatement stmt = connection.prepareStatement(create)) {

            stmt.executeUpdate();
        } catch (SQLException e) {
            plugin.error("Something went wrong checking database table.", e);
        }
    }

    @Override
    public CompletableFuture<Boolean> get(String playerName) {
        if (!isEnabled()) {
            plugin.error(plugin.getMessageHandler().getDBErrorConnectDisabled());
            return null;
        }

        CompletableFuture<Boolean> future = new CompletableFuture<>();

        future.completeAsync(() -> {
            String query = "SELECT enabled FROM " + TABLE_NAME + " WHERE player_name=?;";

            try (Connection connection = DriverManager.getConnection(this.urlString);
                 PreparedStatement stmt = connection.prepareStatement(query)) {
                stmt.setString(1, playerName);

                ResultSet resultSet = stmt.executeQuery();

                if (resultSet.next())
                    return resultSet.getBoolean("enabled");

                return false;
            } catch (SQLException e) {
                plugin.error("Something went wrong fetching from database.", e);
                return false;
            }
        });

        return future;
    }

    @Override
    public void insert(String playerName, Boolean value) {
        if (!isEnabled()) {
            plugin.error(plugin.getMessageHandler().getDBErrorConnectDisabled());
            return;
        }

        String query = "INSERT INTO " + TABLE_NAME + "(player_name,enabled) VALUES(?,?);";

        try (Connection connection = DriverManager.getConnection(this.urlString);
             PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setString(1, playerName);
            stmt.setBoolean(2, value);

            stmt.executeUpdate();
        } catch (SQLException e) {
            plugin.error("Something went wrong inserting to database.", e);
        }
    }

    @Override
    public CompletableFuture<Boolean> exists(String playerName) {
        if (!isEnabled()) {
            plugin.error(plugin.getMessageHandler().getDBErrorConnectUnsuccessful());
            return null;
        }

        CompletableFuture<Boolean> future = new CompletableFuture<>();

        future.completeAsync(() -> {
            String query = "SELECT 1 FROM " + TABLE_NAME + " WHERE player_name=? LIMIT 1;";

            try (Connection connection = DriverManager.getConnection(this.urlString);
                 PreparedStatement stmt = connection.prepareStatement(query)) {
                stmt.setString(1, playerName);

                // EXISTS
                try (ResultSet resultSet = stmt.executeQuery()) {
                    return resultSet.next();
                }
            } catch (SQLException e) {
                plugin.error("Something went wrong checking for existence.", e);
                return false;
            }

        });

        return future;
    }

    @Override
    public void saveMap(Map<String, Boolean> enabledPlayers, boolean async) {
        if (!isEnabled()) {
            plugin.error(plugin.getMessageHandler().getDBErrorConnectDisabled());
            return;
        }

        if (async)
            Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> saveCallbackMap(enabledPlayers));
        else
            saveCallbackMap(enabledPlayers);
    }

    @Override
    public void saveCallbackMap(Map<String, Boolean> enabledPlayers) {
        if (!isEnabled()) {
            plugin.error(plugin.getMessageHandler().getDBErrorConnectDisabled());
            return;
        }

        final String query = "UPDATE " + TABLE_NAME + " SET enabled=? WHERE player_name=?;";

        try (Connection connection = DriverManager.getConnection(this.urlString);
             PreparedStatement stmt = connection.prepareStatement(query)) {
            connection.setAutoCommit(false);

            for (Map.Entry<String, Boolean> entry : enabledPlayers.entrySet()) {
                String name = entry.getKey();
                Boolean value = entry.getValue();

                stmt.setBoolean(1, value);
                stmt.setString(2, name);
                stmt.addBatch();
            }

            stmt.executeBatch();
            connection.commit();

            plugin.sendConsole(plugin.getMessageHandler().getDatabaseSaved());
        } catch (SQLException e) {
            plugin.error("Something went wrong saving database.", e);
        }
    }

    @Override
    public void savePlayer(String playerName, Boolean value, boolean async) {
        if (!isEnabled()) {
            plugin.error(plugin.getMessageHandler().getDBErrorConnectDisabled());
            return;
        }

        if (async)
            Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> saveCallback(playerName, value));
        else
            saveCallback(playerName, value);
    }

    @Override
    public void saveCallback(String playerName, Boolean value) {
        if (!isEnabled()) {
            plugin.error(plugin.getMessageHandler().getDBErrorConnectDisabled());
            return;
        }

        String query = "UPDATE " + TABLE_NAME + " SET enabled=? WHERE player_name=?;";

        try (Connection connection = DriverManager.getConnection(this.urlString);
             PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setBoolean(1, value);
            stmt.setString(2, playerName);

            stmt.executeUpdate();
        } catch (SQLException e) {
            plugin.error("Something went wrong saving database.", e);
        }
    }

    @Override
    public void handleExists(String playerName) {
        if (!isEnabled()) {
            plugin.error(plugin.getMessageHandler().getDBErrorConnectDisabled());
            return;
        }

        exists(playerName).whenComplete((value, throwable) -> {
            if (throwable != null) {
                plugin.error("Something went wrong handling SQL exists.", throwable);
                return;
            }

            if (!value)
                saveNonExists(playerName, false);
        });
    }

    @Override
    public void saveNonExists(String playerName, Boolean value) {
        if (!isEnabled()) {
            plugin.error(plugin.getMessageHandler().getDBErrorConnectDisabled());
            return;
        }

        insert(playerName, value);

        plugin.getPlayerManager().setInitial(playerName, value);
    }

    @Override
    public boolean fetchExists(String playerName) {
        if (!isEnabled()) {
            plugin.error(plugin.getMessageHandler().getDBErrorConnectDisabled());
            return false;
        }

        try {
            return Objects.requireNonNull(exists(playerName)).get();
        } catch (InterruptedException | ExecutionException e) {
            plugin.error("Something went wrong fetching exists.", e);
            return false;
        }
    }

    @Override
    public boolean fetchEnabled(String playerName) {
        if (!isEnabled()) {
            plugin.error(plugin.getMessageHandler().getDBErrorConnectDisabled());
            return false;
        }

        try {
            return Objects.requireNonNull(get(playerName)).get();
        } catch (InterruptedException | ExecutionException e) {
            plugin.error("Something went wrong fetching enabled.", e);
            return false;
        }
    }

    @Override
    public void close() {
        if (!isEnabled())
            return;

        this.enabled = false;
    }

    @Override
    public boolean isEnabled() {
        return this.enabled;
    }
}