package net.omni.nearChat.database.flatfile;

import net.omni.nearChat.NearChatPlugin;
import net.omni.nearChat.database.DatabaseAdapter;
import net.omni.nearChat.database.NearChatDatabase;
import net.omni.nearChat.handlers.DatabaseHandler;

import java.util.Map;

public class FlatFileAdapter implements DatabaseAdapter {

    private final NearChatPlugin plugin;
    private final FlatFileDatabase database;

    public FlatFileAdapter(NearChatPlugin plugin, FlatFileDatabase database) {
        this.plugin = plugin;
        this.database = database;
    }

    public static FlatFileAdapter from(DatabaseAdapter adapter) {
        return adapter instanceof FlatFileAdapter ? ((FlatFileAdapter) adapter) : null;
    }

    public static FlatFileAdapter adapt() {
        return from(DatabaseHandler.ADAPTER);
    }

    @Override
    public void initDatabase() {
        database.checkFile();
        connect();
    }

    @Override
    public boolean connect() {
        return database.connect();
    }

    @Override
    public boolean isEnabled() {
        return database != null && database.isEnabled();
    }

    @Override
    public boolean existsInDatabase(String playerName) {
        return database.has(playerName);
    }

    @Override
    public void saveMap(Map<String, Boolean> enabledPlayers) {
        database.saveMap(enabledPlayers);
        plugin.sendConsole(plugin.getMessageHandler().getDatabaseSaved());
    }

    @Override
    public void savePlayer(String playerName, Boolean value) {
        database.savePlayer(playerName, value);
        // SAVE TO FILE
    }

    @Override
    public void lastSaveMap() {
        if (!database.isEnabled()) {
            plugin.error(plugin.getMessageHandler().getDBErrorConnectDisabled());
            return;
        }

        Map<String, Boolean> enabledPlayers = plugin.getPlayerManager().getEnabledPlayers();

        if (!enabledPlayers.isEmpty())
            saveMap(enabledPlayers);
    }

    @Override
    public void closeDatabase() {
        try {
            if (isEnabled()) {
                database.close();
                plugin.sendConsole(plugin.getMessageHandler().getDBDisconnected());
            }
        } catch (Exception e) {
            plugin.error("Something went wrong closing database: ", e);
        }
    }

    @Override
    public void setToCache(String playerName) {
        plugin.getPlayerManager().set(playerName, database.getValue(playerName));
    }

    @Override
    public boolean getValue(String playerName) {
        return this.database.getValue(playerName);
    }

    @Override
    public NearChatDatabase getDatabase() {
        return this.database;
    }

    @Override
    public NearChatDatabase.Type getType() {
        return NearChatDatabase.Type.FLAT_FILE;
    }

    @Override
    public String toString() {
        return "FLAT-FILE";
    }
}
