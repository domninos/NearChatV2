package net.omni.nearChat.managers;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

import java.sql.Connection;
import java.sql.SQLException;


public class HikariManager {

    private HikariDataSource hikari;

    // TODO only for jdbc sql
    public void initPool(String database_url, String username, String password) {
        HikariConfig config = new HikariConfig();

        config.setJdbcUrl(database_url);
        config.setUsername(username);
        config.setPassword(password);

        this.hikari = new HikariDataSource(config);

        // TODO sql postgresql mysql
    }

    public Connection getConnection() throws SQLException {
        return hikari.getConnection();
    }
}
