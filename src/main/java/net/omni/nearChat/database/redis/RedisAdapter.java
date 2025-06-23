package net.omni.nearChat.database.redis;

import net.omni.nearChat.NearChatPlugin;
import net.omni.nearChat.database.DatabaseAdapter;
import net.omni.nearChat.database.NearChatDatabase;
import net.omni.nearChat.handlers.DatabaseHandler;

import java.util.Map;

public class RedisAdapter implements DatabaseAdapter {

    private final NearChatPlugin plugin;

    private final RedisDatabase redis;

    public RedisAdapter(NearChatPlugin plugin, RedisDatabase redis) {
        this.plugin = plugin;
        this.redis = redis;
    }

    public static RedisAdapter from(DatabaseAdapter adapter) {
        return adapter instanceof RedisAdapter ? ((RedisAdapter) adapter) : null;
    }

    public static RedisAdapter adapt() {
        return from(DatabaseHandler.ADAPTER);
    }

    @Override
    public void initDatabase() {
        // REF: https://redis.io/docs/latest/develop/clients/lettuce/connect/
    }

    @Override
    public boolean connect() {
        return redis.connectConfig();
    }

    @Override
    public boolean isEnabled() {
        return redis != null && redis.isEnabled();
    }

    @Override
    public boolean existsInDatabase(String playerName) {
        return redis.hashExists(playerName);
    }

    @Override
    public void saveMap(Map<String, Boolean> enabledPlayers) {
        if (!enabledPlayers.isEmpty())
            redis.multipleAsync(enabledPlayers);
        else
            plugin.sendConsole(plugin.getMessageHandler().getDatabaseSaved());
    }

    @Override
    public void lastSaveMap() {
        if (!redis.isEnabled())
            return;

        // needs to be sync
        Map<String, Boolean> enabledPlayers = plugin.getPlayerManager().getEnabledPlayers();

        if (!enabledPlayers.isEmpty())
            redis.multiple(enabledPlayers);

        plugin.sendConsole(plugin.getMessageHandler().getDatabaseSaved());
    }

    @Override
    public void savePlayer(String playerName, Boolean value) {
        redis.asyncHashSet(playerName, value.toString());
    }

    @Override
    public void closeDatabase() {
        try {
            if (isEnabled()) {
                redis.close();
                plugin.sendConsole(plugin.getMessageHandler().getDBDisconnected());
            }
        } catch (Exception e) {
            plugin.error("Something went wrong closing database connection: ", e);
        }
    }

    @Override
    public void setToCache(String playerName) {
        redis.asyncHashGet(playerName).thenAcceptAsync((string)
                -> plugin.getPlayerManager().set(playerName, Boolean.parseBoolean(string)));
    }

    @Override
    public boolean getValue(String playerName) {
        return Boolean.parseBoolean(redis.syncGet(playerName));
    }

    @Override
    public NearChatDatabase getDatabase() {
        return this.redis;
    }

    @Override
    public NearChatDatabase.Type getType() {
        return NearChatDatabase.Type.REDIS;
    }

    @Override
    public String toString() {
        return "REDIS";
    }
}