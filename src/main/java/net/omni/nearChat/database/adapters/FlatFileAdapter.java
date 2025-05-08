package net.omni.nearChat.database.adapters;

import net.omni.nearChat.NearChatPlugin;
import net.omni.nearChat.database.DatabaseHandler;
import org.bukkit.entity.Player;

import java.util.Map;

public class FlatFileAdapter implements DatabaseAdapter {
    private final boolean enabled = false;

    private final NearChatPlugin plugin;

    public FlatFileAdapter(NearChatPlugin plugin) {
        this.plugin = plugin;
    }

    public static FlatFileAdapter from(DatabaseAdapter adapter) {
        return adapter instanceof FlatFileAdapter ? ((FlatFileAdapter) adapter) : null;
    }

    public static FlatFileAdapter adapt() {
        return from(DatabaseHandler.ADAPTER);
    }

    @Override
    public void initDatabase() {

    }

    @Override
    public boolean connect() {
        return false;
    }

    @Override
    public boolean isEnabled() {
        return this.enabled;
    }

    @Override
    public void saveToDatabase(Map<String, Boolean> enabledPlayers) {

    }

    @Override
    public void saveToDatabase(Player player, Boolean value) {

    }

    @Override
    public void closeDatabase() {

    }
}
