package net.omni.nearChat.handlers;

import net.omni.nearChat.NearChatPlugin;
import net.omni.nearChat.database.DatabaseAdapter;
import net.omni.nearChat.database.NearChatDatabase;
import net.omni.nearChat.database.flatfile.FlatFileAdapter;
import net.omni.nearChat.database.flatfile.FlatFileDatabase;
import net.omni.nearChat.database.postgres.PostgresAdapter;
import net.omni.nearChat.database.postgres.PostgresDatabase;
import net.omni.nearChat.database.redis.RedisAdapter;
import net.omni.nearChat.database.redis.RedisDatabase;
import org.bukkit.entity.Player;

import java.util.Map;

public class DatabaseHandler {
    private final NearChatPlugin plugin;

    public static DatabaseAdapter ADAPTER;

    public DatabaseHandler(NearChatPlugin plugin) {
        this.plugin = plugin;
    }

    public boolean isFlatFile() {
        return ADAPTER != null && plugin.getConfigHandler().getDatabaseType() == NearChatDatabase.Type.FLAT_FILE && ADAPTER instanceof FlatFileAdapter;
    }

    public boolean isRedis() {
        return ADAPTER != null && plugin.getConfigHandler().getDatabaseType() == NearChatDatabase.Type.REDIS && ADAPTER instanceof RedisAdapter;
    }

    public boolean isPostgreSQL() {
        return ADAPTER != null && plugin.getConfigHandler().getDatabaseType() == NearChatDatabase.Type.POSTGRESQL && ADAPTER instanceof PostgresAdapter;
    }

    public void initDatabase() {
        // REF: https://redis.io/docs/latest/develop/clients/lettuce/connect/

        if (ADAPTER != null) {
            ADAPTER.closeDatabase();
            NearChatDatabase db = ADAPTER.getDatabase();

            if (db != null)
                db.close();
        }

        switch (plugin.getConfigHandler().getDatabaseType()) {
            case REDIS:
                ADAPTER = new RedisAdapter(plugin, new RedisDatabase(plugin));
                break;
            case POSTGRESQL:
                ADAPTER = new PostgresAdapter(plugin, new PostgresDatabase(plugin));
                break;
            case FLAT_FILE:
                ADAPTER = new FlatFileAdapter(plugin, new FlatFileDatabase(plugin));
                break;
        }

        // TODO other databases
        ADAPTER.initDatabase();
        plugin.sendConsole(plugin.getMessageHandler().getDBInit());
    }

    public boolean connect() {
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

        // TODO other DB
        DatabaseAdapter adapter = switch (plugin.getConfigHandler().getDatabaseType()) {
            case REDIS -> RedisAdapter.adapt();
            case POSTGRESQL -> PostgresAdapter.adapt();
            case FLAT_FILE -> FlatFileAdapter.adapt();
        };

        if (adapter == null) {
            plugin.sendConsole(plugin.getMessageHandler().getDBErrorConnectUnsuccessful());
            return;
        }

        adapter.saveToDatabase(playerName, Boolean.valueOf(value));
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

        // TODO postgres mongo nosql
        DatabaseAdapter adapter = switch (plugin.getConfigHandler().getDatabaseType()) {
            case REDIS -> RedisAdapter.adapt();
            case POSTGRESQL -> PostgresAdapter.adapt();
            case FLAT_FILE -> FlatFileAdapter.adapt();
        };

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
