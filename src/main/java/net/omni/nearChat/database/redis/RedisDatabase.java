package net.omni.nearChat.database.redis;

import io.lettuce.core.RedisClient;
import io.lettuce.core.RedisFuture;
import io.lettuce.core.RedisURI;
import io.lettuce.core.api.StatefulRedisConnection;
import io.lettuce.core.api.async.RedisAsyncCommands;
import io.lettuce.core.api.sync.RedisCommands;
import net.omni.nearChat.NearChatPlugin;
import net.omni.nearChat.database.NearChatDatabase;
import net.omni.nearChat.util.MainUtil;

import java.util.Map;

public class RedisDatabase implements NearChatDatabase {

    private final NearChatPlugin plugin;

    protected static final String KEY = "enabled";

    private RedisClient client;
    private StatefulRedisConnection<String, String> connection;

    private boolean enabled = false;

    public RedisDatabase(NearChatPlugin plugin) {
        this.plugin = plugin;
    }

    public boolean connect(String host, int port, String user, char[] password) {
        if (isEnabled()) {
            plugin.error(plugin.getMessageHandler().getDBErrorConnectedAlready());
            return false;
        }

        try {
            RedisURI redisUri = RedisURI.Builder.redis(host, port)
                    .withAuthentication(user, password)
                    .build();

            client = RedisClient.create(redisUri);
            connection = client.connect();

            plugin.sendConsole(plugin.getMessageHandler().getDBConnectedConsole(host));

            connection.async().clientCaching(true); // TODO research

            this.enabled = true;
        } catch (Exception e) {
            plugin.error(plugin.getMessageHandler().getDBErrorConnectUnsuccessful(), e);
            return false;
        }

        plugin.tryBrokers();
        return true;
    }

    public boolean connectConfig() {
        String host, user, password;
        int port;

        if (plugin.getConfigHandler().checkDev()) {
            host = "redis-13615.crce178.ap-east-1-1.ec2.redns.redis-cloud.com";
            port = 13615;
            user = "default";
            password = "UMnqdMOz9GpF3LktR4hqKAO6rbJslpmS"; // TODO: REMOVE AFTER FINISHING PLUGIN

            plugin.sendConsole("&b[DEV] &aEnabled.");
        } else {
            host = plugin.getConfigHandler().getHost();
            port = plugin.getConfigHandler().getPort();
            user = plugin.getConfigHandler().getUser();
            password = plugin.getConfigHandler().getPassword();

            if (MainUtil.isNullOrBlank(host, user, password)) {
                plugin.error(plugin.getMessageHandler().getDBErrorCredentialsNotFound());
                return false;
            }
        }

        return connect(host, port, user, password.toCharArray());
    }

    public boolean hashExists(String field) {
        return getSync().hexists(KEY, field) != null;
    }

    public String syncGet(String key) {
        return isEnabled() ? getSync().get(key) : "NULL";
    }

    public String syncHashGet(String key, String field) {
        return isEnabled() ? getSync().hget(key, field) : "NULL";
    }

    public void syncSet(RedisCommands<String, String> sync, String key, String value) {
        if (!isEnabled()) {
            plugin.sendConsole(plugin.getMessageHandler().getDBErrorConnectDisabled());
            return;
        }

        sync.set(key, value);
    }

    public void syncSet(String key, String value) {
        syncSet(getSync(), key, value);
    }

    public void syncHashSet(RedisCommands<String, String> sync, String key, String field, String value) {
        if (!isEnabled()) {
            plugin.sendConsole(plugin.getMessageHandler().getDBErrorConnectDisabled());
            return;
        }

        sync.hset(key, field, value);
    }

    public void syncHashSet(RedisCommands<String, String> sync, String field, String value) {
        syncHashSet(sync, KEY, field, value);
    }

    public void syncHashSet(String key, String field, String value) {
        syncHashSet(getSync(), key, field, value);
    }

    public RedisFuture<String> asyncGet(String key) {
        return isEnabled() ? getAsync().get(key) : null;
    }

    public RedisFuture<String> asyncHashGet(String field) {
        return isEnabled() ? getAsync().hget(KEY, field) : null;
    }

    public RedisFuture<Map<String, String>> asyncHashGetAll(String key) {
        return isEnabled() ? getAsync().hgetall(key) : null;
    }

    public RedisFuture<String> asyncSet(RedisAsyncCommands<String, String> async, String value) {
        RedisFuture<String> future = async.set(KEY, value);
        future.thenRun(async::save);

        return future;
    }

    public RedisFuture<String> asyncSet(String value) {
        return asyncSet(getAsync(), value);
    }

    public void asyncHashSet(RedisAsyncCommands<String, String> async, String field, String value) {
        try {
            RedisFuture<Boolean> future = async.hset(KEY, field, value);

            future.whenComplete((action, throwable) -> async.save());
        } catch (Exception e) {
            plugin.error("Something went wrong using asyncHashSet: ", e);
        }
    }

    public void asyncHashSetNoSave(RedisAsyncCommands<String, String> async, String field, String value) {
        try {
            async.hset(KEY, field, value);
        } catch (Exception e) {
            plugin.error("Something went wrong using asyncHashSet: ", e);
        }
    }

    public void asyncHashSet(String field, String value) {
        asyncHashSet(getAsync(), field, value);
    }

    public RedisAsyncCommands<String, String> getAsync() {
        return connection.async();
    }

    public RedisCommands<String, String> getSync() {
        return connection.sync();
    }

    @Override
    public boolean isEnabled() {
        return this.enabled && connection != null;
    }

    public RedisClient getClient() {
        return client;
    }

    public StatefulRedisConnection<String, String> getConnection() {
        return connection;
    }

    @Override
    public void close() {
        try {
            if (!isEnabled())
                return;

            connection.close();
            client.shutdown();

            this.enabled = false;
        } catch (Exception e) {
            plugin.error("Something went wrong closing database: ", e);
        }
    }
}
