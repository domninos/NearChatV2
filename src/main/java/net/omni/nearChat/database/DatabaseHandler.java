package net.omni.nearChat.database;

import net.omni.nearChat.NearChatPlugin;
import net.omni.nearChat.database.adapters.DatabaseAdapter;
import net.omni.nearChat.database.adapters.FlatFileAdapter;
import net.omni.nearChat.database.adapters.RedisAdapter;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

import java.util.Map;

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

    public boolean checkExists(String playerName) {
        // TODO
        if (isFlatFile()) {

        } else {
            RedisAdapter redis = RedisAdapter.adapt();

            return redis.hashExists(RedisAdapter.KEY, playerName);
        }

        return false;
    }

    public void set(String playerName, String value) {
        // TODO
        if (isFlatFile()) {

        } else {
            RedisAdapter redis = RedisAdapter.adapt();

            if (!checkExists(playerName))
                redis.asyncHashSet(RedisAdapter.KEY, playerName, value);
        }
    }

    public void putToCache(Player player) {
        String playerName = player.getName();

        if (isFlatFile()) {
            // TODO
        } else {
            RedisAdapter redis = RedisAdapter.adapt();

            redis.asyncHashGet(RedisAdapter.KEY, playerName).thenAcceptAsync((string) -> {
                plugin.getPlayerManager().getEnabledPlayers().put(playerName, Boolean.valueOf(string));
                plugin.sendConsole("[DEBUG] Set " + playerName + " | " + Boolean.valueOf(string));

                plugin.getPlayerManager().setNearby(player);
            });
        }
    }

    public void saveToDatabase(Map<String, Boolean> enabledPlayers) {
        if (isFlatFile()) FlatFileAdapter.adapt().saveToDatabase(enabledPlayers);
        else RedisAdapter.adapt().saveToDatabase(enabledPlayers);
    }

    public void saveToDatabase(Player player, Boolean value) {
        if (isFlatFile()) FlatFileAdapter.adapt().saveToDatabase(player, value);
        else RedisAdapter.adapt().saveToDatabase(player, value);
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
