package net.omni.nearChat.database.redis;

import io.lettuce.core.RedisException;
import io.lettuce.core.RedisFuture;
import io.lettuce.core.TransactionResult;
import io.lettuce.core.api.async.RedisAsyncCommands;
import io.lettuce.core.api.sync.RedisCommands;
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

        // make sure redis is loaded in MainUtil
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
        try {
            if (!enabledPlayers.isEmpty()) {
                RedisAsyncCommands<String, String> async = redis.getAsync();

                async.multi();

                for (Map.Entry<String, Boolean> entry : enabledPlayers.entrySet()) {
                    String name = entry.getKey();
                    Boolean value = entry.getValue();

                    redis.asyncHashSetNoSave(async, name, value.toString());
                }

                RedisFuture<TransactionResult> exec = async.exec();

                exec.whenComplete((result, throwable) -> {
                    if (throwable != null) {
                        plugin.error("Could not complete execution: ", throwable);
                        return;
                    }

                    async.save();
                    plugin.sendConsole(plugin.getMessageHandler().getDatabaseSaved());
                });
            }

            plugin.sendConsole(plugin.getMessageHandler().getDatabaseSaved());
        } catch (RedisException e) {
            plugin.error("Could not save async properly");
        }
    }

    @Override
    public void lastSaveMap() {
        // needs to be sync
        try {
            Map<String, Boolean> enabledPlayers = plugin.getPlayerManager().getEnabledPlayers();

            if (!enabledPlayers.isEmpty()) {
                RedisCommands<String, String> sync = redis.getSync();

                try {
                    sync.multi();

                    for (Map.Entry<String, Boolean> entry : enabledPlayers.entrySet()) {
                        String name = entry.getKey();
                        Boolean value = entry.getValue();

                        redis.syncHashSet(sync, name, value.toString());
                    }

                    sync.exec();
                    sync.save();
                } catch (RedisException e) {
                    plugin.error("Could not exec properly");
                }
            }

            plugin.sendConsole(plugin.getMessageHandler().getDatabaseSaved());
        } catch (RedisException e) {
            plugin.error("Could not save properly");
        }
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
    public String toString() {
        return "REDIS";
    }
}