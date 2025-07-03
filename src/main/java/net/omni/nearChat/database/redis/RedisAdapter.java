package net.omni.nearChat.database.redis;

import net.omc.database.redis.OMCRedisAdapter;
import net.omni.nearChat.NearChatPlugin;

import java.util.Map;

public class RedisAdapter extends OMCRedisAdapter {

    private final NearChatPlugin nearChatPlugin;

    public RedisAdapter(NearChatPlugin plugin, RedisDatabase redis) {
        super(plugin, redis);

        this.nearChatPlugin = plugin;
    }

    @Override
    public void saveMap(Map<String, Boolean> enabledPlayers) {
        if (!enabledPlayers.isEmpty())
            redis.multipleAsync(enabledPlayers);
        else
            plugin.sendConsole(plugin.getDBMessageHandler().getDatabaseSaved());
    }

    @Override
    public void lastSaveMap() {
        if (!redis.isEnabled())
            return;

        // needs to be sync
        Map<String, Boolean> enabledPlayers = nearChatPlugin.getPlayerManager().getEnabledPlayers();

        if (!enabledPlayers.isEmpty())
            redis.multiple(enabledPlayers);

        plugin.sendConsole(plugin.getDBMessageHandler().getDatabaseSaved());
    }

    @Override
    public void setToCache(String playerName) {
        redis.asyncHashGet(playerName).thenAcceptAsync((string)
                -> nearChatPlugin.getPlayerManager().set(playerName, Boolean.parseBoolean(string)));
    }
}