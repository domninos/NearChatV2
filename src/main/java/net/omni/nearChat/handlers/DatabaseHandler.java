package net.omni.nearChat.handlers;

import net.omc.database.ISQLDatabase;
import net.omc.database.OMCDatabase;
import net.omc.handlers.OMCDatabaseHandler;
import net.omni.nearChat.NearChatPlugin;
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

public class DatabaseHandler extends OMCDatabaseHandler {

    private final NearChatPlugin nearChatPlugin;

    public DatabaseHandler(NearChatPlugin plugin) {
        super(plugin);

        this.nearChatPlugin = plugin;
    }

    // TODO other databases
    @Override
    public OMCDatabase.Type initDatabase() {
        if (ADAPTER != null) { // close previous database connection
            ADAPTER.closeDatabase();
            OMCDatabase db = ADAPTER.getDatabase();

            if (db != null)
                db.close();

            ADAPTER = null;
        }

        OMCDatabase.Type type = plugin.getDBConfigHandler().getDatabaseType();

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
                    ADAPTER = new RedisAdapter(nearChatPlugin, new RedisDatabase(nearChatPlugin));
                    break;
                case POSTGRESQL:
                    ADAPTER = new PostgresAdapter(nearChatPlugin, new PostgresDatabase(nearChatPlugin));
                    break;
                case FLAT_FILE:
                    ADAPTER = new FlatFileAdapter(nearChatPlugin, new FlatFileDatabase(nearChatPlugin));
                    break;
                case SQLITE:
                    ADAPTER = new SQLiteAdapter(nearChatPlugin, new SQLiteDatabase(nearChatPlugin));
                    break;
            }

            if (ADAPTER == null) {
                plugin.sendConsole(plugin.getDBMessageHandler().getDBErrorConnectUnsuccessful());
                return false;
            }

            plugin.sendConsole(plugin.getDBMessageHandler().getDBInit());
            ADAPTER.initDatabase();
            return true;
        });

        return type;
    }

    public boolean connect() {
        if (nearChatPlugin.getPlayerManager() != null)
            nearChatPlugin.getPlayerManager().flush();

        OMCDatabase.Type type = initDatabase();

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

    @Override
    public void setToCache(Player player) {
        if (!isEnabled()) {
            plugin.sendConsole(plugin.getDBMessageHandler().getDBErrorConnectDisabled());
            return;
        }

        ADAPTER.setToCache(player.getName());
        nearChatPlugin.getPlayerManager().setNearby(player);

        updateChecks();
    }

    @Override
    public void saveMap(Map<String, Boolean> enabledPlayers, boolean async) {
        if (!isEnabled()) {
            plugin.sendConsole(plugin.getDBMessageHandler().getDBErrorConnectDisabled());
            return;
        }
        if (ADAPTER == null) {
            plugin.sendConsole(plugin.getDBMessageHandler().getDBErrorConnectUnsuccessful());
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
}
