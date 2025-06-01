package net.omni.nearChat.database;

import java.util.Map;

public interface DatabaseAdapter {

    void initDatabase();

    boolean connect();

    boolean isEnabled();

    boolean existsInDatabase(String playerName);

    void saveMap(Map<String, Boolean> enabledPlayers);

    void savePlayer(String playerName, Boolean value);

    void lastSaveMap();

    void closeDatabase();

    void setToCache(String playerName);

    boolean getValue(String playerName);

    NearChatDatabase getDatabase();

    String toString();
}
