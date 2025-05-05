package net.omni.nearChat.managers;

import io.lettuce.core.RedisFuture;
import io.lettuce.core.TransactionResult;
import net.omni.nearChat.NearChatPlugin;
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
            plugin.sendConsole("Could not load player because database is disabled.");
            return;
        }

        // TODO: database have hashset structure]
        //  * KEY,uuid,owner,value (NearChat 2.0)

        String playerName = player.getName();
//
//        if (!has(playerName)) { // not in cache
//            plugin.getDatabaseHandler().asyncHashGet(KEY, playerName).thenAcceptAsync((string) -> {
//                enabled.put(playerName, Boolean.valueOf(string));
//                plugin.sendConsole("[DEBUG] Added " + playerName + " | " + Boolean.valueOf(string));
//            });
//        } else {
//        }


        if (!has(playerName)) {
            plugin.getDatabaseHandler().asyncHashGet(KEY, playerName).thenAcceptAsync((string) -> {
                enabled.put(playerName, Boolean.valueOf(string));
                plugin.sendConsole("[DEBUG] Added " + playerName + " | " + Boolean.valueOf(string));
            });
        }
        // TODO check if in cache = reset to database

        // TODO check if user is present in database

    }

    public void saveToDatabase(Player player) {
        if (!plugin.getDatabaseHandler().isEnabled()) {
            plugin.sendConsole("Could not save to database because database is disabled.");
            return;
        }

        if (has(player.getName())) {
            Boolean val = enabled.get(player.getName());

            plugin.getDatabaseHandler().asyncHashSet(KEY, player.getName(), val.toString());
        }
    }

    public void saveToDatabase() {
        if (!plugin.getDatabaseHandler().isEnabled()) {
            plugin.sendConsole("Could not save to database because database is disabled.");
            return;
        }

        RedisFuture<String> multi = plugin.getDatabaseHandler().asyncMulti();

        enabled.forEach(((name, value) ->
                plugin.getDatabaseHandler().asyncHashSet(KEY, name, value.toString())));

        RedisFuture<TransactionResult> exec = plugin.getDatabaseHandler().getAsyncExec();

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

    public void toggle(Player player) {
        String name = player.getName();

        if (!has(name)) return;

        this.enabled.replace(name, !isEnabled(name));

        saveToDatabase(player);
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
