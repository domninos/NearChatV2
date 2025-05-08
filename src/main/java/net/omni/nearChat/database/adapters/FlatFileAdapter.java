package net.omni.nearChat.database.adapters;

import net.omni.nearChat.NearChatPlugin;
import net.omni.nearChat.database.FlatFileDatabase;
import net.omni.nearChat.database.NearChatDatabase;
import net.omni.nearChat.handlers.DatabaseHandler;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
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

    @Override
    public void saveToDatabase(Map<String, Boolean> enabledPlayers) {
        Map<String, Boolean> savedPlayers = database.readFile();
        List<String> newPlayers = new ArrayList<>();

        for (Map.Entry<String, Boolean> entry : enabledPlayers.entrySet()) {
            String name = entry.getKey();
            Boolean value = entry.getValue();

            if (savedPlayers.containsKey(name)) {
                // in database already, replace
                savedPlayers.replace(name, value);
                continue;
            }

            // new entry/player
            newPlayers.add(name + ": " + value.toString());
            plugin.sendConsole("[DEBUG] Added " + name + ": " + value);
        }

        // now add everything
        for (Map.Entry<String, Boolean> entry : savedPlayers.entrySet()) {
            String name = entry.getKey();
            Boolean value = entry.getValue();

            database.writeToFile(name + ": " + value.toString());
        }

        newPlayers.forEach(database::writeToFile); // since newPlayers consists of-  name: boolean

        savedPlayers.clear();
        newPlayers.clear();
        plugin.sendConsole("&7[FlatFile] &aSaved to database.");
    }

    @Override
    public void saveToDatabase(String playerName, Boolean value) {
        database.put(playerName, value);
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
    public NearChatDatabase getDatabase() {
        return this.database;
    }

    @Override
    public String toString() {
        return "FLAT-FILE";
    }
}
