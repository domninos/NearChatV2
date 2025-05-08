package net.omni.nearChat.database.adapters;

import net.omni.nearChat.database.NearChatDatabase;

import java.util.Map;

public interface DatabaseAdapter {

    void initDatabase();

    boolean connect();

    boolean isEnabled();

    boolean existsInDatabase(String playerName);

    void saveToDatabase(Map<String, Boolean> enabledPlayers);

    void saveToDatabase(String playerName, Boolean value);

    void closeDatabase();

    NearChatDatabase getDatabase();
}
