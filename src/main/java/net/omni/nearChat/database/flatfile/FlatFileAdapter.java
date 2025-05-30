package net.omni.nearChat.database.flatfile;

import net.omni.nearChat.NearChatPlugin;
import net.omni.nearChat.database.DatabaseAdapter;
import net.omni.nearChat.database.NearChatDatabase;
import net.omni.nearChat.handlers.DatabaseHandler;

import java.io.IOException;
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
        try {
            database.createFile();
        } catch (IOException e) {
            plugin.error("Could not initialize database", e);
            return;
        }

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


    // TODO: test
    // TODO: possibly just use NearChatConfig for .yml
    @Override
    public void saveMap(Map<String, Boolean> enabledPlayers) {
        if (!enabledPlayers.isEmpty()) {
            StringBuilder toSave = new StringBuilder();

            for (Map.Entry<String, Boolean> entry : enabledPlayers.entrySet()) {
                String name = entry.getKey();
                Boolean value = entry.getValue();

                // new entry/player
                toSave.append(name).append(": ").append(value.toString()).append("\n");
                plugin.sendConsole("[DEBUG] Added " + name + ": " + value);
            }

            database.writeToFile(toSave.toString());
        }

        plugin.sendConsole(plugin.getMessageHandler().getDatabaseSaved());
    }

    @Override
    public void savePlayer(String playerName, Boolean value) {
        database.put(playerName, value);

        Map<String, Boolean> savedPlayers = database.readFile();

        // in database already, replace
        if (savedPlayers.containsKey(playerName)) {
            savedPlayers.replace(playerName, value);
            plugin.sendConsole("[DEBUG] Replaced " + playerName + ": " + value);
        } else { // new to database
            savedPlayers.put(playerName, value);
            plugin.sendConsole("[DEBUG] Added  " + playerName + ": " + value);
        }

        // TODO find line from file and replace


        database.writeToFile(playerName + ": " + value);

        savedPlayers.clear();
        // SAVE TO FILE
    }

    @Override
    public void lastSaveMap() {
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
    public boolean getValue(String playerName) {
        return this.database.getValue(playerName);
    }

    @Override
    public NearChatDatabase getDatabase() {
        return this.database;
    }

    @Override
    public String toString() {
        return "FLAT-FILE";
    }
}
