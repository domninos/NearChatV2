package net.omni.nearChat.handlers;

import io.lettuce.core.RedisClient;
import io.lettuce.core.RedisURI;
import io.lettuce.core.api.StatefulRedisConnection;
import io.lettuce.core.api.async.RedisAsyncCommands;
import net.omni.nearChat.NearChatPlugin;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutionException;

public class DatabaseHandler {
    private final NearChatPlugin plugin;

    public DatabaseHandler(NearChatPlugin plugin) {
        this.plugin = plugin;
    }

    public void initDatabase() {
        String devS = plugin.getNearConfig().getString("dev");

        boolean dev;

        if (devS == null || devS.isBlank())
            dev = false;
        else
            dev = plugin.getNearConfig().getBool("dev");

        String host, port, password;
        int portInt;

        if (!dev) {
            host = plugin.getNearConfig().getString("host");
            port = plugin.getNearConfig().getString("port");
            password = plugin.getNearConfig().getString("password");

            if (host == null || port == null || password == null) {
                plugin.error("Database information not found in config.yml. Will not use database...");
                return;
            }

            try {
                portInt = Integer.parseInt(port);
            } catch (NumberFormatException e) {
                plugin.error(e);
                return;
            }
        } else {
            host = "redis-13615.crce178.ap-east-1-1.ec2.redns.redis-cloud.com";
            portInt = 13615;
            password = "UMnqdMOz9GpF3LktR4hqKAO6rbJslpmS";

            plugin.sendConsole("&b[DEV] &aEnabled.");
        }

        RedisURI uri = RedisURI.Builder.redis(host, portInt)
                .withSsl(true)
                .withPassword(password.toCharArray())
                .build();

        try (RedisClient client = RedisClient.create(uri)) {
            try (StatefulRedisConnection<String, String> connection = client.connect()) {
                RedisAsyncCommands<String, String> commands = connection.async();

                // Asynchronously store & retrieve a simple string
                commands.set("foo", "bar").get();
                System.out.println(commands.get("foo").get()); // prints bar

                // Asynchronously store key-value pairs in a hash directly
                Map<String, String> hash = new HashMap<>();
                hash.put("name", "John");
                hash.put("surname", "Smith");
                hash.put("company", "Redis");
                hash.put("age", "29");
                commands.hset("user-session:123", hash).get();

                System.out.println(commands.hgetall("user-session:123").get());
                // Prints: {name=John, surname=Smith, company=Redis, age=29}
                plugin.sendConsole("&aSuccessfully connected to: &3" + host);
            } catch (ExecutionException | InterruptedException e) {
                throw new RuntimeException(e);
            } finally {
                client.shutdown();
            }
        }

        // REF: https://redis.io/docs/latest/develop/clients/lettuce/connect/
    }

    /*
     getters setters
     everything must run asynchronously

    */

    public void closeDatabase() {
    }
}
