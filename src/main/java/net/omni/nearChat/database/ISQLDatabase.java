package net.omni.nearChat.database;

import java.util.Map;
import java.util.concurrent.CompletableFuture;

public interface ISQLDatabase {

    // CONSTANT
    String TABLE_NAME = "nearchat_enabled";

    String getHost();

    boolean connectConfig();

    void checkTable();

    CompletableFuture<Boolean> get(String playerName);

    void insert(String playerName, Boolean value);

    CompletableFuture<Boolean> exists(String playerName);

    void saveMap(Map<String, Boolean> enabledPlayers, boolean async);

    void saveCallbackMap(Map<String, Boolean> enabledPlayers);

    void savePlayer(String playerName, Boolean value, boolean async);

    void saveCallback(String playerName, Boolean value);

    void handleExists(String playerName);

    void saveNonExists(String playerName, Boolean value);

    boolean fetchExists(String playerName);

    boolean fetchEnabled(String playerName);
}
