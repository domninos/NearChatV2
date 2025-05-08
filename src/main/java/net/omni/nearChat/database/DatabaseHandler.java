package net.omni.nearChat.database;

import net.omni.nearChat.NearChatPlugin;
import net.omni.nearChat.database.adapters.DatabaseAdapter;
import net.omni.nearChat.database.adapters.FlatFileAdapter;
import net.omni.nearChat.database.adapters.RedisAdapter;
import org.bukkit.ChatColor;

public class DatabaseHandler {
    private final NearChatPlugin plugin;

    public static DatabaseAdapter ADAPTER;

    public DatabaseHandler(NearChatPlugin plugin) {
        this.plugin = plugin;
    }

    public boolean isFlatFile() {
        return plugin.getConfigHandler().isFlatFile() && ADAPTER instanceof FlatFileAdapter;
    }

    public void initDatabase() {
        // REF: https://redis.io/docs/latest/develop/clients/lettuce/connect/

        if (isEnabled()) {
            plugin.error("&cYou cannot connect to the database while it is already enabled."); // TODO: mesages.yml
            return;
        }

        ADAPTER = plugin.getConfigHandler().isFlatFile()
                ? new FlatFileAdapter(plugin) : new RedisAdapter(plugin);

        ADAPTER.connect();
    }

    public boolean connect() {
        if (isEnabled()) {
            plugin.error(ChatColor.stripColor(plugin.getMessageHandler().getDBErrorConnectDisabled()));
            return false;
        }

        return ADAPTER.connect();
    }

    // TODO: getters and setters
    public boolean isEnabled() {
        return ADAPTER != null && ADAPTER.isEnabled();
    }

    public DatabaseAdapter getDatabase() {
        return ADAPTER;
    }

    public void closeDatabase() {
        if (isEnabled())
            ADAPTER.closeDatabase();
    }
}
