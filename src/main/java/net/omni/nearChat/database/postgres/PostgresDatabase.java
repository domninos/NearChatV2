package net.omni.nearChat.database.postgres;

import com.zaxxer.hikari.HikariDataSource;
import net.omni.nearChat.NearChatPlugin;
import net.omni.nearChat.database.ISQLDatabase;
import net.omni.nearChat.database.NearChatDatabase;
import net.omni.nearChat.util.MainUtil;

import java.sql.*;
import java.util.Map;


public class PostgresDatabase implements NearChatDatabase, ISQLDatabase {
    private boolean enabled = false;

    private final NearChatPlugin plugin;

    private static final String TABLE_NAME = "nearchat_enabled";

    /*
     * POSTGRESQL
     * host: localhost
     * username: postgres
     * password: admin
     * database_name: postgres
     * port: 5432
     */

    public PostgresDatabase(NearChatPlugin plugin) {
        this.plugin = plugin;
    }

    public boolean connect(String host, int port, String database_name, String user, String password) {
        if (isEnabled()) {
            plugin.error(plugin.getMessageHandler().getDBErrorConnectedAlready());
            return false;
        }

        try {
            plugin.getHikariManager().initPool(host, port, database_name, user, password);

            Connection connection = plugin.getHikariManager().getConnection();

            if (connection != null) {
                plugin.sendConsole(plugin.getMessageHandler().getDBConnectedConsole(host));

                checkTable();

                connection.close();
                this.enabled = true;
            }
        } catch (Exception e) {
            plugin.error(plugin.getMessageHandler().getDBErrorConnectUnsuccessful(), e);
            return false;
        }

        plugin.tryBrokers();
        return true;
    }

    public boolean connectConfig() {
        String host, user, password, database_name;
        int port;

        if (plugin.getConfigHandler().checkDev()) {
            host = "localhost";
            port = 5432;
            database_name = "postgres";
            user = "postgres";
            password = "admin"; // TODO: REMOVE AFTER FINISHING PLUGIN

            plugin.sendConsole("&b[DEV] &aEnabled.");
        } else {
            host = plugin.getConfigHandler().getHost();
            port = plugin.getConfigHandler().getPort();
            database_name = plugin.getConfigHandler().getDatabaseName();
            user = plugin.getConfigHandler().getUser();
            password = plugin.getConfigHandler().getPassword();

            if (MainUtil.isNullOrBlank(host, user, password)) {
                plugin.error(plugin.getMessageHandler().getDBErrorCredentialsNotFound());
                return false;
            }
        }

        return connect(host, port, database_name, user, password);
    }

    // TODO async / sync functions
    public boolean exists(String playerName) {
        if (!isEnabled()) {
            plugin.error(plugin.getMessageHandler().getDBErrorConnectUnsuccessful());
            return false;
        }

        String query = "SELECT EXISTS (SELECT 1 FROM " + TABLE_NAME + " WHERE playerName = ?);";

        try (Connection connection = plugin.getHikariManager().getConnection();
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
    }

    public void save(Map<String, Boolean> enabledPlayers) {
        String query = "UPDATE " + TABLE_NAME + " SET enabled=? WHERE playerName=?";

        try (Connection connection = plugin.getHikariManager().getConnection();
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
        } catch (SQLException e) {
            plugin.error("Something went wrong saving database.", e);
        }
    }

    public void save(String playerName, Boolean value) {
        String query = "UPDATE " + TABLE_NAME + " SET enabled=? WHERE playerName=?";

        try (Connection connection = plugin.getHikariManager().getConnection();
             PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setBoolean(1, value);
            stmt.setString(2, playerName);

            stmt.executeUpdate();
        } catch (SQLException e) {
            plugin.error("Something went wrong saving database.", e);
        }
    }

    public void insert(String playerName, Boolean value) {
        String query = "INSERT INTO " + TABLE_NAME + "(playerName,values) VALUES(?,?);";

        try (Connection connection = plugin.getHikariManager().getConnection();
             PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setString(1, playerName);
            stmt.setBoolean(2, value);

            stmt.executeUpdate();
        } catch (SQLException e) {
            plugin.error("Something went wrong saving database.", e);
        }
    }

    @Override
    public void checkTable() {
        try (Connection connection = plugin.getHikariManager().getConnection()) {

            DatabaseMetaData metaData = connection.getMetaData();
            try (ResultSet resultSet = metaData.getTables(null, null, TABLE_NAME, new String[]{"TABLE"})) {
                if (!resultSet.next()) {

                    String create = "CREATE TABLE IF NOT EXISTS " + TABLE_NAME + "(playerName varchar(36), enabled BOOLEAN)";
                    try (PreparedStatement stmt = connection.prepareStatement(create)) {
                        stmt.executeUpdate();
                        plugin.sendConsole("&aSuccessfully created " + TABLE_NAME); // TODO: messages.yml
                    }
                }
            }
        } catch (SQLException e) {
            plugin.error("Something went wrong checking database table.", e);
        }
    }

    @Override
    public void saveNonExists(String playerName, Boolean value) {
        insert(playerName, value);
    }

    @Override
    public void close() {
        try {
            if (!isEnabled())
                return;

            HikariDataSource hikari = plugin.getHikariManager().getHikari();

            if (hikari != null)
                hikari.close();

            this.enabled = false;
        } catch (Exception e) {
            plugin.error("Something went wrong closing database: ", e);
        }
    }

    @Override
    public boolean isEnabled() {
        return enabled;
    }
}
