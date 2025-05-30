package net.omni.nearChat.database;

public interface ISQLDatabase {
    void checkTable();

    // TODO not exists save

    void saveNonExists(String playerName, Boolean value);
}
