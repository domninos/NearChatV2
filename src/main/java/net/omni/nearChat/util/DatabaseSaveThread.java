package net.omni.nearChat.util;

import io.lettuce.core.RedisClient;
import net.omni.nearChat.database.flatfile.FlatFileAdapter;
import net.omni.nearChat.database.postgres.PostgresAdapter;
import net.omni.nearChat.database.redis.RedisAdapter;
import net.omni.nearChat.database.redis.RedisDatabase;
import net.omni.nearChat.handlers.DatabaseHandler;

public class DatabaseSaveThread extends Thread {

    @Override
    public void run() {
        try {
            if (DatabaseHandler.ADAPTER == null)
                return;

            // TODO other databases

            if (DatabaseHandler.ADAPTER instanceof RedisAdapter) {
                RedisAdapter redis = RedisAdapter.adapt();

                if (redis == null)
                    return;

                redis.save();

                if (redis.getDatabase() == null)
                    return;

                RedisClient client = ((RedisDatabase) redis.getDatabase()).getClient();

                if (client != null)
                    redis.closeDatabase();
            } else if (DatabaseHandler.ADAPTER instanceof FlatFileAdapter) {
                FlatFileAdapter flat_file = FlatFileAdapter.adapt();

                if (flat_file != null)
                    flat_file.save();
            } else if (DatabaseHandler.ADAPTER instanceof PostgresAdapter) {
                PostgresAdapter postgres = PostgresAdapter.adapt();

                if (postgres != null)
                    postgres.save(); // TODO
            }
        } catch (Exception ignore) {
        }
    }
}
