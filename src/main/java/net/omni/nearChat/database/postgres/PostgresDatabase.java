package net.omni.nearChat.database.postgres;

import net.omni.nearChat.NearChatPlugin;
import net.omni.nearChat.database.NearChatDatabase;
import net.omni.nearChat.util.MainUtil;
import org.postgresql.Driver;

import java.sql.*;


public class PostgresDatabase implements NearChatDatabase {
    private boolean enabled = false;

    private Connection connection;

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

    // TODO: USE HIKARICP (?)

    public boolean connect(String host, int port, String database_name, String user, String password) {
        if (isEnabled()) {
            plugin.error(plugin.getMessageHandler().getDBErrorConnectedAlready());
            return false;
        }

        try {
            Class.forName("org.postgresql.Driver");

            DriverManager.registerDriver(new Driver()); //register postgresql driver

            String url = String.format("jdbc:postgresql://%s:%d/%s", host, port, database_name);

            Connection connection = DriverManager.getConnection(url, user, password);

            if (connection != null) {
                this.connection = connection;

                plugin.sendConsole(plugin.getMessageHandler().getDBConnectedConsole(host));

                connection.close();
                this.enabled = true;
            } else {
                plugin.error(plugin.getMessageHandler().getDBErrorConnectUnsuccessful());
                return false;
            }
        } catch (Exception e) {
            plugin.error(plugin.getMessageHandler().getDBErrorConnectUnsuccessful(), e);
            return false;
        }

        plugin.tryBrokers();
        return true;
    }

    public boolean exists(String playerName) {
        // TODO async sync
        if (!isEnabled()) {
            plugin.error(plugin.getMessageHandler().getDBErrorConnectUnsuccessful());
            return false;
        }


//            stmt = connection.createStatement();
//            String sql = "CREATE TABLE COMPANY " +
//                    "(ID INT PRIMARY KEY     NOT NULL," +
//                    " NAME           TEXT    NOT NULL, " +
//                    " AGE            INT     NOT NULL, " +
//                    " ADDRESS        CHAR(50), " +
//                    " SALARY         REAL)";
//            stmt.executeUpdate(sql);
//            stmt.close();

                String query = "SELECT COUNT(1) FROM " + TABLE_NAME + " WHERE playerName=?";

        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setString(1, playerName);

            ResultSet resultSet = stmt.executeQuery();

            resultSet.close();

        } catch (SQLException e) {

        }
        return false;
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

    @Override
    public void close() {
        try {
            if (!isEnabled())
                return;

            connection.close();

            this.connection = null;

            this.enabled = false;
        } catch (Exception e) {
            plugin.error("Something went wrong closing database: ", e);
        }
    }

    @Override
    public boolean isEnabled() {
        return enabled && connection != null;
    }
}
