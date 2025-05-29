package net.omni.nearChat.util;

import io.lettuce.core.RedisClient;
import net.omni.nearChat.database.redis.RedisDatabase;
import net.omni.nearChat.database.redis.RedisAdapter;
import net.omni.nearChat.handlers.DatabaseHandler;

public class RedisSaveThread extends Thread {

    @Override
    public void run() {
        try {
            if (DatabaseHandler.ADAPTER == null)
                return;

            RedisAdapter redis = RedisAdapter.adapt(); // todo: java.lang.IllegalStateException: zip file close

            if (redis == null)
                return;

            redis.saveSyncDB();

            if (redis.getDatabase() == null)
                return;

            RedisClient client = ((RedisDatabase) redis.getDatabase()).getClient();

            if (client != null)
                redis.closeDatabase();
        } catch (Exception ignore) {
        }
    }
}
