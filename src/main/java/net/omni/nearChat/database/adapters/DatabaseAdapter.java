package net.omni.nearChat.database.adapters;

import org.bukkit.entity.Player;

import java.util.Map;

public interface DatabaseAdapter {

    void initDatabase();

    boolean connect();

    boolean isEnabled();

    void saveToDatabase(Map<String, Boolean> enabledPlayers);

    void saveToDatabase(Player player, Boolean value);

    void closeDatabase();
}
