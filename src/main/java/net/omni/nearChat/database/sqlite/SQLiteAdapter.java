package net.omni.nearChat.database.sqlite;

import net.omni.nearChat.database.DatabaseAdapter;
import net.omni.nearChat.database.NearChatDatabase;

import java.util.Map;

public class SQLiteAdapter implements DatabaseAdapter {
    @Override
    public void initDatabase() {

    }

    @Override
    public boolean connect() {
        return false;
    }

    @Override
    public boolean isEnabled() {
        return false;
    }

    @Override
    public boolean existsInDatabase(String playerName) {
        return false;
    }

    @Override
    public void saveMap(Map<String, Boolean> enabledPlayers) {

    }

    @Override
    public void savePlayer(String playerName, Boolean value) {

    }

    @Override
    public void lastSaveMap() {

    }

    @Override
    public void closeDatabase() {

    }

    @Override
    public void setToCache(String playerName) {

    }

    @Override
    public boolean getValue(String playerName) {
        return false;
    }

    @Override
    public NearChatDatabase getDatabase() {
        return null;
    }
    // TODO
}
