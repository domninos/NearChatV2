package net.omni.nearChat.handlers;

import io.lettuce.core.RedisClient;
import io.lettuce.core.RedisFuture;
import io.lettuce.core.RedisURI;
import io.lettuce.core.TransactionResult;
import io.lettuce.core.api.StatefulRedisConnection;
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
        String devS = plugin.getNearConfig().getString("dev");

        boolean dev = devS != null && !devS.isBlank() && plugin.getNearConfig().getBool("dev");

        if (!dev) {
            connectConfig();
        } else {
            String host = "redis-13615.crce178.ap-east-1-1.ec2.redns.redis-cloud.com";
            int portInt = 13615;
            String user = "default";
            String password = "UMnqdMOz9GpF3LktR4hqKAO6rbJslpmS"; // TODO: REMOVE AFTER FINISHING PLUGIN

            connect(host, portInt, user, password.toCharArray());
            plugin.sendConsole("&b[DEV] &aEnabled.");
        }
    }

    public boolean connectConfig() {
        if (isEnabled()) {
            plugin.error("&cYou cannot connect to the database while it is already enabled.");
            return false;
        }

        String host = plugin.getConfigHandler().getHost();
        int port = plugin.getConfigHandler().getPort();
        String user = plugin.getNearConfig().getString("user");
        String password = plugin.getNearConfig().getString("password");

        if (isNullOrBlank(host, user, password)) {
            plugin.error("Database information not found in config.yml. Will not use database...");
            return false;
        }

        connect(host, port, user, password.toCharArray());
        return true;
    }

    public void connect(String host, int portInt, String user, char[] password) {
        if (isEnabled()) {
            plugin.error("&cYou cannot connect to the database while it is already enabled.");
            return;
        }

        try {
            RedisURI redisUri = RedisURI.Builder.redis(host, portInt)
                    .withTimeout(Duration.ofSeconds(10))
                    .withAuthentication(user, password).build();

            client = RedisClient.create(redisUri);
            connection = client.connect();

            plugin.sendConsole("&aSuccessfully connected to: &3" + host);

            this.enabled = true;
        } catch (Exception e) {
            plugin.error(e);
        }

        connection.async().multi();
        // TODO: test db

        System.out.println(syncGet("key"));
        asyncGet("key").thenRun(System.out::println);

        syncSet("keys", "testkey");
        System.out.println(syncGet("keys"));

        asyncGet("keys").thenRun(System.out::println);
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

        connection.sync().set(key, value);
        connection.sync().save();
    }

    public void syncHashSet(String key, String field, String value) {
        if (!isEnabled()) {
            plugin.sendConsole("Could not process sync hashset, database is disabled.");
            return;
        }

        connection.sync().hset(key, field, value);
        connection.sync().save();
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
        RedisFuture<String> future = connection.async().set(key, value);
        future.thenRun(() -> connection.async().save());

        return future;
    }

    public void asyncHashSet(String key, String field, String value) {
        RedisFuture<Boolean> future = connection.async().hset(key, field, value);
        future.thenRun(() -> connection.async().save());
    }

    private boolean isNullOrBlank(String... strings) {
        return Arrays.stream(strings).anyMatch(string -> string == null || toString().isBlank());
    }

    public void closeDatabase() {
        if (isEnabled()) {
            connection.sync().shutdown(true);
            client.shutdown();
            plugin.sendConsole("&aDatabase disconnected.");
        }
    }
}
