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
        // empty; ignore
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
            plugin.error("Could not save database properly", e);
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
        try {
            Map<String, Boolean> enabledPlayers = plugin.getPlayerManager().getEnabledPlayers();

            if (!enabledPlayers.isEmpty())
                this.database.saveMap(enabledPlayers, false);
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
    public boolean getValue(String playerName) {
        return this.database.fetchEnabled(playerName);
    }

    @Override
    public NearChatDatabase getDatabase() {
        return this.database;
    }

    @Override
    public String toString() {
        return "PostgreSQL";
    }
}
