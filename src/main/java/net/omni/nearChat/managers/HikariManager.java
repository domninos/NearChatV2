package net.omni.nearChat.managers;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import net.omni.nearChat.NearChatPlugin;

import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Properties;


public class HikariManager {

    private HikariDataSource hikari;

    private final NearChatPlugin plugin;

    public HikariManager(NearChatPlugin plugin) {
        this.plugin = plugin;
    }

    // TODO only for jdbc sql
    public void initPool(String host, int port, String database_name, String username, String password) {
        plugin.sendConsole("&aInitializing Hikari Connection Pool."); // TODO messages.yml

        // REF: https://github.com/brettwooldridge/HikariCP#configuration-knobs-baby
        Properties properties = new Properties();

        /*
        DataSources:
          SQLITE: org.sqlite.SQLiteDataSource
          MySQL: don't use properties. Use HikariConfig#setJdbcUrl    || config.setJdbcUrl("jdbc:mysql://localhost:3306/simpsons");
         */

        // TODO for mysql
        properties.setProperty("dataSourceClassName", "org.postgresql.ds.PGSimpleDataSource");
        properties.setProperty("dataSource.user", username);
        properties.setProperty("dataSource.password", password);
        properties.setProperty("dataSource.databaseName", database_name);
        properties.put("dataSource.logWriter", new PrintWriter(System.out));

        HikariConfig config = new HikariConfig(properties);
        this.hikari = new HikariDataSource(config);

        // TODO sql postgresql mysql
    }

    public void close() {
        if (hikari != null)
            hikari.close();
    }

    public Connection getConnection() throws SQLException {
        return hikari.getConnection();
    }

    public HikariDataSource getHikari() {
        return hikari;
    }
}
