package net.omni.nearChat.handlers;

import io.lettuce.core.RedisClient;
import io.lettuce.core.RedisFuture;
import io.lettuce.core.RedisURI;
import net.omni.nearChat.NearChatPlugin;

import java.util.Arrays;

public class DatabaseHandler {
    private final NearChatPlugin plugin;

    private boolean enabled = false;

    private RedisClient client;

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
            String password = "UMnqdMOz9GpF3LktR4hqKAO6rbJslpmS"; // TODO: REMOVE AFTER FINISHING PLUGINO

            connect(host, portInt, user, password.toCharArray());
            plugin.sendConsole("&b[DEV] &aEnabled.");
        }
    }

    public boolean connectConfig() {
        String host = plugin.getNearConfig().getString("host");
        String port = plugin.getNearConfig().getString("port");
        String user = plugin.getNearConfig().getString("user");
        String password = plugin.getNearConfig().getString("password");

        if (isNullOrBlank(host, port, user, password) || port.isBlank()) {
            plugin.error("Database information not found in config.yml. Will not use database...");
            return false;
        }

        int portInt;

        try {
            portInt = Integer.parseInt(port);
        } catch (NumberFormatException e) {
            plugin.error("port: " + e);
            return false;
        }

        connect(host, portInt, user, password.toCharArray());
        return true;
    }

    public void connect(String host, int portInt, String user, char[] password) {
        if (isEnabled()) {
            plugin.error("&cYou cannot connect to the database whilst enabled.");
            return;
        }

        try {
            RedisURI redisUri = RedisURI.Builder.redis(host, portInt)
                    .withAuthentication(user, password).build();

            client = RedisClient.create(redisUri);

            plugin.sendConsole("&aSuccessfully connected to: &3" + host);

            this.enabled = true;
        } catch (Exception e) {
            plugin.error(e);
        }
    }

    public boolean isEnabled() {
        return this.enabled && client != null;
    }

    public String syncGet(String key) {
        return client.connect().sync().get(key);
    }

    public void syncSet(String key, String value) {
        client.connect().sync().set(key, value);
    }

    public RedisFuture<String> asyncGet(String key) {
        return client.connect().async().get(key);
    }

    public RedisFuture<String> asyncSet(String k, String v) {
        return client.connect().async().set(k, v);
    }

    private boolean isNullOrBlank(String... strings) {
        return Arrays.stream(strings).anyMatch(string -> string == null || toString().isBlank());
    }

    public void closeDatabase() {
        if (isEnabled()) {
            client.shutdown();
            plugin.sendConsole("&aDatabase disconnected.");
        }
    }
}
