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
import net.omni.nearChat.database.sqlite.SQLiteAdapter;
import net.omni.nearChat.database.sqlite.SQLiteDatabase;
import org.bukkit.entity.Player;

import java.util.Map;
import java.util.concurrent.ExecutionException;

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

    public boolean isSQLite() {
        return ADAPTER != null
                && plugin.getConfigHandler().getDatabaseType() == NearChatDatabase.Type.SQLITE
                && ADAPTER instanceof SQLiteAdapter;
    }

    // TODO other databases
    public NearChatDatabase.Type initDatabase() {
        if (ADAPTER != null) { // close previous database connection
            ADAPTER.closeDatabase();
            NearChatDatabase db = ADAPTER.getDatabase();

            if (db != null)
                db.close();

            ADAPTER = null;
        }

        NearChatDatabase.Type type = plugin.getConfigHandler().getDatabaseType();

        if (!type.isLoaded(plugin)) {
            try {
                plugin.getLibraryHandler().loadLibraries(type);
            } catch (ExecutionException | InterruptedException e) {
                plugin.error("Something went wrong loading libraries of " + type.getLabel(), e);
                return type;
            }
        }

        plugin.getLibraryHandler().submitExec(() -> {
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
                case SQLITE:
                    ADAPTER = new SQLiteAdapter(plugin, new SQLiteDatabase(plugin));
                    break;
            }

            if (ADAPTER == null) {
                plugin.sendConsole(plugin.getMessageHandler().getDBErrorConnectUnsuccessful());
                return false;
            }

            plugin.sendConsole(plugin.getMessageHandler().getDBInit());
            ADAPTER.initDatabase();
            return true;
        });

        return type;
    }

    public boolean connect() {
        if (plugin.getPlayerManager() != null)
            plugin.getPlayerManager().flush();

        NearChatDatabase.Type type = initDatabase();

        if (ADAPTER == null) {
            // retry loading
            try {
                // run on the same thread
                return plugin.getLibraryHandler().submitExec(() -> ADAPTER.connect()).get();
            } catch (InterruptedException | ExecutionException e) {
                plugin.error("Something went wrong connecting to " + type.getLabel(), e);
                return false;
            }
        }

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

    public boolean isEnabled() { // TODO make it not check for isLibLoaded for PlayerManager#loadEnabled (?)
        return ADAPTER != null && plugin.getLibraryHandler().isLibLoaded(ADAPTER.getType()) && ADAPTER.isEnabled();
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
