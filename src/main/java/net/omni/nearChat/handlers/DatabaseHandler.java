package net.omni.nearChat.handlers;

import net.omni.nearChat.NearChatPlugin;
import net.omni.nearChat.database.DatabaseAdapter;
import net.omni.nearChat.database.ISQLDatabase;
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

    private int updates = 0;

    public static DatabaseAdapter ADAPTER;

    public DatabaseHandler(NearChatPlugin plugin) {
        this.plugin = plugin;
    }

    public boolean isFlatFile() {
        return ADAPTER != null
                && plugin.getConfigHandler().getDatabaseType() == NearChatDatabase.Type.FLAT_FILE
                && ADAPTER instanceof FlatFileAdapter;
    }

    public boolean isRedis() {
        return ADAPTER != null
                && plugin.getConfigHandler().getDatabaseType() == NearChatDatabase.Type.REDIS
                && ADAPTER instanceof RedisAdapter;
    }

    public boolean isPostgreSQL() {
        return ADAPTER != null
                && plugin.getConfigHandler().getDatabaseType() == NearChatDatabase.Type.POSTGRESQL
                && ADAPTER instanceof PostgresAdapter;
    }

    // TODO other databases
    public void initDatabase() {
        // REF: https://redis.io/docs/latest/develop/clients/lettuce/connect/

        if (ADAPTER != null) {
            ADAPTER.closeDatabase();
            NearChatDatabase db = ADAPTER.getDatabase();

            if (db != null)
                db.close();
        }

        NearChatDatabase.Type type = plugin.getConfigHandler().getDatabaseType();

        switch (type) {
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

        if (ADAPTER == null) {
            plugin.sendConsole(plugin.getMessageHandler().getDBErrorConnectUnsuccessful());
            return;
        }

        ADAPTER.initDatabase();
        plugin.sendConsole(plugin.getMessageHandler().getDBInit());
    }

    public boolean connect() {
        if (plugin.getPlayerManager() != null)
            plugin.getPlayerManager().flush();

        initDatabase();

        return ADAPTER.connect();
    }

    public void setToCache(Player player) {
        if (!isEnabled()) {
            plugin.sendConsole(plugin.getMessageHandler().getDBErrorConnectDisabled());
            return;
        }

        ADAPTER.setToCache(player.getName());
        plugin.getPlayerManager().setNearby(player);

        updateChecks();
    }

    public void updateChecks() {
        this.updates++;
    }

    public void resetCheckUpdates() {
        this.updates = 0;
    }

    public int getUpdates() {
        return updates;
    }

    public void saveMap(Map<String, Boolean> enabledPlayers, boolean async) {
        if (!isEnabled()) {
            plugin.sendConsole(plugin.getMessageHandler().getDBErrorConnectDisabled());
            return;
        }
        if (ADAPTER == null) {
            plugin.sendConsole(plugin.getMessageHandler().getDBErrorConnectUnsuccessful());
            return;
        }

        // sql async
        if (isSQL()) {
            ISQLDatabase sqlDb = (ISQLDatabase) getAdapter().getDatabase();
            sqlDb.saveMap(enabledPlayers, async);
        } else
            ADAPTER.saveMap(enabledPlayers);

        updateChecks();
    }

    public void savePlayer(String playerName, Boolean value, boolean async) {
        if (!isEnabled()) {
            plugin.sendConsole(plugin.getMessageHandler().getDBErrorConnectDisabled());
            return;
        }
        if (ADAPTER == null) {
            plugin.sendConsole(plugin.getMessageHandler().getDBErrorConnectUnsuccessful());
            return;
        }

        // sql async
        if (isSQL()) {
            ISQLDatabase sqlDb = (ISQLDatabase) getAdapter().getDatabase();
            sqlDb.savePlayer(playerName, value, async);
        } else
            ADAPTER.savePlayer(playerName, value);

        updateChecks();
    }

    public void savePlayer(String playerName, String value) {
        savePlayer(playerName, Boolean.getBoolean(value), true);
    }

    public boolean isEnabled() {
        return ADAPTER != null && ADAPTER.isEnabled();
    }

    public boolean checkExistsDB(String playerName) {
        return isEnabled() && ADAPTER.existsInDatabase(playerName);
    }

    public DatabaseAdapter getAdapter() {
        return ADAPTER;
    }

    public boolean isSQL() {
        return ADAPTER != null && ADAPTER.getDatabase() instanceof ISQLDatabase;
    }

    public void closeDatabase() {
        if (isEnabled())
            ADAPTER.closeDatabase();
    }
}
