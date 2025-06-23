package net.omni.nearChat.database.sqlite;

import net.omni.nearChat.NearChatPlugin;
import net.omni.nearChat.database.DatabaseAdapter;
import net.omni.nearChat.database.NearChatDatabase;
import net.omni.nearChat.handlers.DatabaseHandler;

import java.util.Map;

public class SQLiteAdapter implements DatabaseAdapter {

    private final NearChatPlugin plugin;

    private final SQLiteDatabase database;

    public SQLiteAdapter(NearChatPlugin plugin, SQLiteDatabase database) {
        this.plugin = plugin;
        this.database = database;
    }

    public static SQLiteAdapter from(DatabaseAdapter adapter) {
        return adapter instanceof SQLiteAdapter ? ((SQLiteAdapter) adapter) : null;
    }

    public static SQLiteAdapter adapt() {
        return from(DatabaseHandler.ADAPTER);
    }

    @Override
    public void initDatabase() {
        try {
            Class.forName("org.sqlite.JDBC"); // check for driver
        } catch (ClassNotFoundException e) {
            throw new RuntimeException(e);
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
        return this.database.fetchExists(playerName);
    }

    @Override
    public void saveMap(Map<String, Boolean> enabledPlayers) {
        try {
            if (!enabledPlayers.isEmpty())
                this.database.saveMap(enabledPlayers, true);

            plugin.sendConsole(plugin.getMessageHandler().getDatabaseSaved());
        } catch (Exception e) {
            plugin.error("Could not save database properly.", e);
        }
    }

    @Override
    public void savePlayer(String playerName, Boolean value) {
        try {
            this.database.savePlayer(playerName, value, true);
        } catch (Exception e) {
            plugin.error("Could not save database properly", e);
        }
    }

    @Override
    public void lastSaveMap() {
        if (!database.isEnabled())
            return;

        try {
            Map<String, Boolean> enabledPlayers = plugin.getPlayerManager().getEnabledPlayers();

            if (!enabledPlayers.isEmpty())
                this.database.saveMap(enabledPlayers, false);

            closeDatabase();
        } catch (Exception e) {
            plugin.error("Could not save database properly", e);
        }
    }

    @Override
    public void closeDatabase() {
        try {
            if (isEnabled()) {
                database.close();
                plugin.sendConsole(plugin.getMessageHandler().getDBDisconnected());
            }
        } catch (Exception e) {
            plugin.error("Something went wrong closing database connection: ", e);
        }
    }

    @Override
    public void setToCache(String playerName) {
        boolean value = getValue(playerName);

        plugin.getPlayerManager().set(playerName, value);
    }

    @Override
    public boolean getValue(String playerName) {
        return this.database.fetchEnabled(playerName);
    }

    @Override
    public NearChatDatabase getDatabase() {
        return this.database;
    }

    @Override
    public NearChatDatabase.Type getType() {
        return NearChatDatabase.Type.SQLITE;
    }

    @Override
    public String toString() {
        return "SQLite";
    }
}
