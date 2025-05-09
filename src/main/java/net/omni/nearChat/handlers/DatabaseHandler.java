package net.omni.nearChat.handlers;

import net.omni.nearChat.NearChatPlugin;
import net.omni.nearChat.database.FlatFileDatabase;
import net.omni.nearChat.database.NearChatDatabase;
import net.omni.nearChat.database.RedisDatabase;
import net.omni.nearChat.database.adapters.DatabaseAdapter;
import net.omni.nearChat.database.adapters.FlatFileAdapter;
import net.omni.nearChat.database.adapters.RedisAdapter;
import org.bukkit.entity.Player;

import java.util.Map;

public class DatabaseHandler {
    private final NearChatPlugin plugin;

    public static DatabaseAdapter ADAPTER;

    public DatabaseHandler(NearChatPlugin plugin) {
        this.plugin = plugin;
    }

    public boolean isFlatFile() {
        return ADAPTER != null && plugin.getConfigHandler().isFlatFile() && ADAPTER instanceof FlatFileAdapter;
    }

    public void initDatabase() {
        // REF: https://redis.io/docs/latest/develop/clients/lettuce/connect/

//        if (isEnabled()) {
//            plugin.error(plugin.getMessageHandler().getDBErrorConnectedAlready());
//            return;
//        }

        if (ADAPTER != null) {
            ADAPTER.closeDatabase();
            NearChatDatabase db = ADAPTER.getDatabase();

            if (db != null)
                db.close();
        }

        ADAPTER = plugin.getConfigHandler().isFlatFile()
                ? new FlatFileAdapter(plugin, new FlatFileDatabase(plugin))
                : new RedisAdapter(plugin, new RedisDatabase(plugin));

        ADAPTER.initDatabase();
    }

    public boolean connect() {
//        if (isEnabled()) {
//            plugin.error(plugin.getMessageHandler().getDBErrorConnectedAlready());
//            return false;
//        }

        if (plugin.getPlayerManager() != null)
            plugin.getPlayerManager().flush();

        initDatabase();

        return ADAPTER.connect();
    }

    public void setToDatabase(String playerName, String value) {
        if (!isEnabled()) {
            plugin.sendConsole(plugin.getMessageHandler().getDBErrorConnectDisabled());
            return;
        }

        if (isFlatFile()) {
            FlatFileAdapter flatFIle = FlatFileAdapter.adapt();
            flatFIle.saveToDatabase(playerName, Boolean.valueOf(value));
        } else {
            RedisAdapter redis = RedisAdapter.adapt();
            redis.saveToDatabase(playerName, Boolean.valueOf(value));
        }
    }

    public void setToCache(Player player) {
        if (!isEnabled()) {
            plugin.sendConsole(plugin.getMessageHandler().getDBErrorConnectDisabled());
            return;
        }

        // TODO put to databaseadapter

        String playerName = player.getName();

        if (isFlatFile()) {
            FlatFileAdapter flatFile = FlatFileAdapter.adapt();
            FlatFileDatabase database = (FlatFileDatabase) flatFile.getDatabase();

            boolean fromDatabase = database.getValue(player.getName());

            plugin.getPlayerManager().getEnabledPlayers().put(playerName, fromDatabase);
            plugin.sendConsole("[DEBUG] Set " + playerName + " | " + fromDatabase);
        } else {
            RedisAdapter redis = RedisAdapter.adapt();
            RedisDatabase database = (RedisDatabase) redis.getDatabase();

            database.asyncHashGet(playerName).thenAcceptAsync((string) -> {
                plugin.getPlayerManager().getEnabledPlayers().put(playerName, Boolean.valueOf(string));
                plugin.sendConsole("[DEBUG] Set " + playerName + " | " + Boolean.valueOf(string));
            });
        }

        plugin.getPlayerManager().setNearby(player);
    }

    public void saveToDatabase(Map<String, Boolean> enabledPlayers) {
        if (!isEnabled()) {
            plugin.sendConsole(plugin.getMessageHandler().getDBErrorConnectDisabled());
            return;
        }

        DatabaseAdapter adapter = isFlatFile() ? FlatFileAdapter.adapt() : RedisAdapter.adapt();

        if (adapter == null) {
            plugin.sendConsole(plugin.getMessageHandler().getDBErrorConnectUnsuccessful());
            return;
        }

        adapter.saveToDatabase(enabledPlayers);
    }

    public void saveToDatabase(String playerName, Boolean value) {
        if (!isEnabled()) {
            plugin.sendConsole(plugin.getMessageHandler().getDBErrorConnectDisabled());
            return;
        }

        if (isFlatFile()) FlatFileAdapter.adapt().saveToDatabase(playerName, value);
        else RedisAdapter.adapt().saveToDatabase(playerName, value);
    }

    public boolean isEnabled() {
        return ADAPTER != null && ADAPTER.isEnabled();
    }

    public boolean checkExistsDB(String playerName) {
        return isEnabled() && ADAPTER.existsInDatabase(playerName);
    }

    public DatabaseAdapter getDatabase() {
        return ADAPTER;
    }

    public void closeDatabase() {
        if (isEnabled())
            ADAPTER.closeDatabase();
    }
}
