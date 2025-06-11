package net.omni.nearChat.database.sqlite;

import net.omni.nearChat.NearChatPlugin;
import net.omni.nearChat.database.NearChatDatabase;

import java.io.File;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class SQLiteDatabase implements NearChatDatabase {
    // TODO
    private final NearChatPlugin plugin;

    private final File db_file;
    private final String urlString;

    private Connection connection;

    public SQLiteDatabase(NearChatPlugin plugin) {
        this.plugin = plugin;
        this.db_file = new File(plugin.getDataFolder(), "nearchat.db");
        this.urlString = "jdbc:sqlite:" + db_file.getAbsolutePath();
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

        try (Connection conn = DriverManager.getConnection(urlString)) {
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
}
