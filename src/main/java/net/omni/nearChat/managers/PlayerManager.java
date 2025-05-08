package net.omni.nearChat.managers;

import io.lettuce.core.RedisFuture;
import io.lettuce.core.TransactionResult;
import io.lettuce.core.api.async.RedisAsyncCommands;
import net.omni.nearChat.NearChatPlugin;
import net.omni.nearChat.database.adapters.FlatFileAdapter;
import net.omni.nearChat.database.adapters.RedisAdapter;
import net.omni.nearChat.util.PlayerUtil;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class PlayerManager {
    private final Map<String, Boolean> enabled = new HashMap<>();
    private final Map<Player, List<Player>> nearby = new HashMap<>();

    private static final String KEY = "enabled";

    private final NearChatPlugin plugin;

    public PlayerManager(NearChatPlugin plugin) {
        this.plugin = plugin;
    }

    public void loadEnabled(Player player) {
        if (!plugin.getDatabaseHandler().isEnabled()) {
            plugin.sendConsole(plugin.getMessageHandler().getDBErrorConnectDisabled());
            return;
        }

        // TODO: database have hashset structure
        //  * KEY,uuid,owner,value (NearChat 2.0)

        String playerName = player.getName();

        if (plugin.getDatabaseHandler().isFlatFile()) {
            FlatFileAdapter flatFile = FlatFileAdapter.adapt();

            // TODO
        } else {
            RedisAdapter redis = RedisAdapter.adapt();

            if (!redis.hashExists(KEY, playerName))
                redis.asyncHashSet(KEY, playerName, "false");
        }

        if (!has(playerName)) {
            // put into cache

            if (plugin.getDatabaseHandler().isFlatFile()) {
                FlatFileAdapter flatFile = FlatFileAdapter.adapt();

                // TODO

            } else {
                RedisAdapter redis = RedisAdapter.adapt();

                redis.asyncHashGet(KEY, playerName).thenAcceptAsync((string) -> {
                    enabled.put(playerName, Boolean.valueOf(string));
                    plugin.sendConsole("[DEBUG] Set " + playerName + " | " + Boolean.valueOf(string));

                    setNearby(player);
                });
            }

        } else if (isEnabled(player.getName()))
            setNearby(player);
    }

    public void saveToDatabase(Player player) {
        // TODO: for flat-file
        if (!plugin.getDatabaseHandler().isEnabled()) {
            plugin.sendConsole(plugin.getMessageHandler().getDBErrorConnectDisabled());
            return;
        }

        if (has(player.getName())) {
            Boolean val = enabled.get(player.getName());

            if (plugin.getDatabaseHandler().isFlatFile()) {
                // TODO: save to .txt file
            } else {
                RedisAdapter redis = RedisAdapter.adapt();

                redis.asyncHashSet(KEY, player.getName(), val.toString());
            }
        }
    }

    public void saveToDatabase() {
        // TODO: for flat-file
        if (!plugin.getDatabaseHandler().isEnabled()) {
            plugin.sendConsole(plugin.getMessageHandler().getDBErrorConnectDisabled());
            return;
        }

        if (plugin.getDatabaseHandler().isFlatFile()) {
            // TODO

        } else {
            RedisAdapter redis = RedisAdapter.adapt();

            RedisAsyncCommands<String, String> async = redis.getAsync();

            async.multi();

            enabled.forEach(((name, value) ->
                    redis.asyncHashSet(async, KEY, name, value.toString())));

            RedisFuture<TransactionResult> exec = async.exec();

            exec.whenComplete((result, throwable) -> {
                if (throwable != null) {
                    plugin.error(throwable.getMessage());
                    return;
                }

                if (!result.isEmpty())
                    result.forEach(o -> plugin.sendConsole("[DEBUG] " + o.toString()));

                plugin.sendConsole("&aSaved database.");
            });
        }
    }

    public void toggle(Player player) {
        String name = player.getName();

        if (has(name))
            this.enabled.replace(name, !isEnabled(name));
        else
            this.enabled.put(name, true);

        if (isEnabled(name))
            setNearby(player);
        else
            removeNearby(player);
    }

    public List<Player> getNearby(Player player) {
        return nearby.getOrDefault(player, null);
    }

    public boolean hasNearby(Player player) {
        return nearby.containsKey(player);
    }

    public void removeNearby(Player player) {
        if (!hasNearby(player))
            return;

        getNearby(player).clear();

        nearby.remove(player);
    }

    public void setNearby(Player player) {
        List<Player> nearbyPlayers = PlayerUtil.getNearbyPlayers(player.getLocation(), plugin.getConfigHandler().getNearBlockRadius());

        this.nearby.put(player, nearbyPlayers);
    }

    public Map<Player, List<Player>> getNearbyPlayers() {
        return nearby;
    }

    public boolean isEnabled(String name) {
        return enabled.getOrDefault(name, false);
    }

    public boolean has(String name) {
        return enabled.containsKey(name);
    }

    public void flush() {
        enabled.clear();

        if (!nearby.isEmpty())
            nearby.forEach((name, list) -> list.clear());

        nearby.clear();
    }
}
