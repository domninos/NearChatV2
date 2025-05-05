package net.omni.nearChat.handlers;

import io.lettuce.core.RedisClient;
import io.lettuce.core.RedisFuture;
import io.lettuce.core.RedisURI;
import io.lettuce.core.TransactionResult;
import io.lettuce.core.api.StatefulRedisConnection;
import io.lettuce.core.api.async.RedisAsyncCommands;
import io.lettuce.core.api.sync.RedisCommands;
import net.omni.nearChat.NearChatPlugin;

import java.time.Duration;
import java.util.Arrays;
import java.util.Map;

public class DatabaseHandler {
    private final NearChatPlugin plugin;

    private boolean enabled = false;

    private RedisClient client;

    private StatefulRedisConnection<String, String> connection;

    public DatabaseHandler(NearChatPlugin plugin) {
        this.plugin = plugin;
    }

    public void initDatabase() {
        // REF: https://redis.io/docs/latest/develop/clients/lettuce/connect/

        if (isEnabled()) {
            plugin.error("&cYou cannot connect to the database while it is already enabled.");
            return;
        }

        connectConfig();
    }

    private boolean checkDev() {
        String devS = plugin.getNearConfig().getString("dev");

        return devS != null && !devS.isBlank() && plugin.getNearConfig().getBool("dev");
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

            if (isNullOrBlank(host, user, password)) {
                plugin.error("Database information not found in config.yml. Will not use database...");
                return false;
            }
        }

        return connect(host, port, user, password.toCharArray());
    }

    public boolean connect(String host, int portInt, String user, char[] password) {
        if (isEnabled()) {
            plugin.error("&cYou cannot connect to the database while it is already enabled."); // TODO messages.yml
            return false;
        }

        try {
            RedisURI redisUri = RedisURI.Builder.redis(host, portInt)
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

    public boolean isEnabled() {
        return this.enabled && client != null;
    }

    public RedisFuture<String> asyncMulti() {
        return connection.async().multi();
    }

    public RedisFuture<TransactionResult> getAsyncExec() {
        return connection.async().exec();
    }

    public String syncGet(String key) {
        return isEnabled() ? connection.sync().get(key) : "NULL";
    }

    public String syncHashGet(String key, String field) {
        return isEnabled() ? connection.sync().hget(key, field) : "NULL";
    }

    public void syncSet(String key, String value) {
        if (!isEnabled()) {
            plugin.sendConsole("Could not process sync set, database is disabled.");
            return;
        }

        RedisCommands<String, String> sync = connection.sync();
        sync.set(key, value);
//        sync.save();
    }

    public void syncHashSet(String key, String field, String value) {
        if (!isEnabled()) {
            plugin.sendConsole("Could not process sync hashset, database is disabled.");
            return;
        }

        RedisCommands<String, String> sync = connection.sync();
        sync.hset(key, field, value);
//        sync.save();
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

    public RedisFuture<String> asyncSet(String key, String value) {
        RedisAsyncCommands<String, String> async = connection.async();

        RedisFuture<String> future = async.set(key, value);
        future.thenRun(async::save);

        return future;
    }

    public void asyncHashSet(String key, String field, String value) {
        RedisAsyncCommands<String, String> async = connection.async();

        RedisFuture<Boolean> future = async.hset(key, field, value);
        future.thenRun(() -> {
            async.save();
            plugin.sendConsole("[DEBUG] Saved Async HashSet");
        });
    }

    private boolean isNullOrBlank(String... strings) {
        return Arrays.stream(strings).anyMatch(string -> string == null || string.isBlank());
    }

    public void closeDatabase() {
        if (isEnabled()) {
            connection.sync().shutdown(true);
            client.shutdown();
            plugin.sendConsole("&aDatabase disconnected.");
        }
    }
}
