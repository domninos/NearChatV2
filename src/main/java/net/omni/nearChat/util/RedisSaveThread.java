package net.omni.nearChat.util;

import io.lettuce.core.RedisClient;
import net.omni.nearChat.database.RedisDatabase;
import net.omni.nearChat.database.adapters.RedisAdapter;
import net.omni.nearChat.handlers.DatabaseHandler;

public class RedisSaveThread extends Thread {

    @Override
    public void run() {
        if (DatabaseHandler.ADAPTER == null)
            return;

        RedisAdapter redis = RedisAdapter.adapt();

        if (redis == null)
            return;

        redis.saveSyncDB();

        if (redis.getDatabase() == null)
            return;

        RedisClient client = ((RedisDatabase) redis.getDatabase()).getClient();

        if (client != null)
            client.shutdown();
    }
}
