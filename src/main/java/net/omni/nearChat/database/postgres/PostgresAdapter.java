package net.omni.nearChat.database.postgres;

import net.omni.nearChat.NearChatPlugin;
import net.omni.nearChat.database.DatabaseAdapter;
import net.omni.nearChat.database.NearChatDatabase;
import net.omni.nearChat.handlers.DatabaseHandler;

import java.util.Map;

public class PostgresAdapter implements DatabaseAdapter {

    private final NearChatPlugin plugin;
    private final PostgresDatabase database;

    public PostgresAdapter(NearChatPlugin plugin, PostgresDatabase database) {
        this.plugin = plugin;
        this.database = database;

        // make sure postgre is loaded in MainUtil
    }

    public static PostgresAdapter from(DatabaseAdapter adapter) {
        return adapter instanceof PostgresAdapter ? ((PostgresAdapter) adapter) : null;
    }

    public static PostgresAdapter adapt() {
        return from(DatabaseHandler.ADAPTER);
    }

    @Override
    public void initDatabase() {
        try {
            Class.forName("org.postgresql.Driver");
        } catch (ClassNotFoundException e) {
            plugin.error("Something went wrong initializing PostgreSQL.", e);
        }
    }

    @Override
    public boolean connect() {
        return this.database.connectConfig();
    }

    @Override
    public boolean isEnabled() {
        return database != null && database.isEnabled();
    }

    @Override
    public boolean existsInDatabase(String playerName) {
        return false;
    }

    @Override
    public void saveToDatabase(Map<String, Boolean> enabledPlayers) {

    }

    @Override
    public void saveToDatabase(String playerName, Boolean value) {

    }

    @Override
    public void closeDatabase() {

    }

    @Override
    public NearChatDatabase getDatabase() {
        return null;
    }
}
