package net.omni.nearChat.database.redis;

import net.omc.database.redis.OMCRedisDatabase;
import net.omni.nearChat.NearChatPlugin;

public class RedisDatabase extends OMCRedisDatabase {

    public RedisDatabase(NearChatPlugin plugin) {
        super(plugin, "nearchat_enabled");
    }
}
