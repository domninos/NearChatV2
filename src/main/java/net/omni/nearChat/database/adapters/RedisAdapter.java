package net.omni.nearChat.database.adapters;

import io.lettuce.core.RedisClient;
import io.lettuce.core.RedisFuture;
import io.lettuce.core.RedisURI;
import io.lettuce.core.TransactionResult;
import io.lettuce.core.api.StatefulRedisConnection;
import io.lettuce.core.api.async.RedisAsyncCommands;
import io.lettuce.core.api.sync.RedisCommands;
import net.omni.nearChat.NearChatPlugin;
import net.omni.nearChat.database.DatabaseHandler;
import net.omni.nearChat.util.MainUtil;
import org.bukkit.ChatColor;

import java.time.Duration;
import java.util.Map;

public class RedisAdapter implements DatabaseAdapter {
    private final NearChatPlugin plugin;

    private RedisClient client;

    private StatefulRedisConnection<String, String> connection;

    private boolean enabled = false;

    public RedisAdapter(NearChatPlugin plugin) {
        this.plugin = plugin;
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

        if (isEnabled()) {
            plugin.error("&cYou cannot connect to the database while it is already enabled.");
            return;
        }

        connectConfig();
    }

    @Override
    public boolean connect() {
        if (isEnabled()) {
            plugin.error(ChatColor.stripColor(plugin.getMessageHandler().getDBErrorConnectDisabled()));
            return false;
        }

        connectConfig();
        return true;
    }

    public boolean connect(String host, int port, String user, char[] password) {
        if (isEnabled()) {
            plugin.error(ChatColor.stripColor(plugin.getMessageHandler().getDBErrorConnectDisabled()));
            return false;
        }

        try {
            RedisURI redisUri = RedisURI.Builder.redis(host, port)
                    .withTimeout(Duration.ofSeconds(10))
                    .withAuthentication(user, password).build();

            client = RedisClient.create(redisUri);
            connection = client.connect();

            plugin.sendConsole("&aSuccessfully connected to: &3" + host); // TODO messages.yml

            connection.async().clientCaching(true); // TODO research

            this.enabled = true;
        } catch (Exception e) {
            plugin.error(e);
            return false;
        }

        plugin.tryBrokers();

        // TODO: test db

        System.out.println(syncGet("key"));
        asyncGet("key").thenRun(System.out::println);

        syncSet("keys", "testkey");
        System.out.println(syncGet("keys"));

        asyncGet("keys").thenRun(System.out::println);
        return true;
    }

    public boolean connectConfig() {
        String host, user, password;
        int port;

        if (checkDev()) {
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
                plugin.error("Database information not found in config.yml. Will not use database...");
                return false;
            }
        }

        return connect(host, port, user, password.toCharArray());
    }

    @Override
    public boolean isEnabled() {
        return this.enabled;
    }

    @Override
    public void closeDatabase() {
        try {
            if (isEnabled()) {
                connection.sync().shutdown(true);
                client.shutdown();
                plugin.sendConsole("&aDatabase disconnected."); // TODO: messages.yml
            }
        } catch (Exception e) {
            plugin.error("Something went wrong closing database: " + e.getMessage());
        }
    }

    public boolean hashExists(String key, String field) {
        return getSync().hexists(key, field);
    }

    public RedisFuture<String> asyncMulti() {
        return getAsync().multi();
    }

    public RedisFuture<TransactionResult> getAsyncExec() {
        return getAsync().exec();
    }

    public String syncGet(String key) {
        return isEnabled() ? getSync().get(key) : "NULL";
    }

    public String syncHashGet(String key, String field) {
        return isEnabled() ? connection.sync().hget(key, field) : "NULL";
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

    public void syncHashSet(String key, String field, String value) {
        syncHashSet(getSync(), key, field, value);
    }

    public RedisFuture<String> asyncGet(String key) {
        return isEnabled() ? connection.async().get(key) : null;
    }

    public RedisFuture<String> asyncHashGet(String key, String field) {
        return isEnabled() ? connection.async().hget(key, field) : null;
    }

    public RedisFuture<Map<String, String>> asyncHashGetAll(String key) {
        return isEnabled() ? connection.async().hgetall(key) : null;
    }

    public RedisFuture<String> asyncSet(RedisAsyncCommands<String, String> async, String key, String value) {
        RedisFuture<String> future = async.set(key, value);
        future.thenRun(async::save);

        return future;
    }

    public RedisFuture<String> asyncSet(String key, String value) {
        return asyncSet(getAsync(), key, value);
    }

    public void asyncHashSet(RedisAsyncCommands<String, String> async, String key, String field, String value) {
        try {
            RedisFuture<Boolean> future = async.hset(key, field, value);

            future.whenComplete((action, throwable) -> async.save());
        } catch (Exception e) {
            e.fillInStackTrace();
            plugin.error(e);
        }
    }

    public void asyncHashSet(String key, String field, String value) {
        asyncHashSet(getAsync(), key, field, value);
    }

    public RedisAsyncCommands<String, String> getAsync() {
        return connection.async();
    }

    public RedisCommands<String, String> getSync() {
        return connection.sync();
    }

    private boolean checkDev() {
        String devS = plugin.getNearConfig().getString("dev");

        return devS != null && !devS.isBlank() && plugin.getNearConfig().getBool("dev");
    }
}
