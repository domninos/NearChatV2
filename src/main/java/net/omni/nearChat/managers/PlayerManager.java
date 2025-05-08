package net.omni.nearChat.managers;

import net.omni.nearChat.NearChatPlugin;
import net.omni.nearChat.util.PlayerUtil;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class PlayerManager {
    private final Map<String, Boolean> enabled = new HashMap<>();
    private final Map<Player, Set<Player>> nearby = new HashMap<>();

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

        // if not in database
        if (!plugin.getDatabaseHandler().checkExistsDB(playerName))
            plugin.getDatabaseHandler().setToDatabase(playerName, "false");

        // not in cache
        if (!has(playerName))
            plugin.getDatabaseHandler().setToCache(player);

        if (isEnabled(player.getName()))
            setNearby(player);
    }

    public void saveToDatabase(String playerName) {
        if (!plugin.getDatabaseHandler().isEnabled()) {
            plugin.sendConsole(plugin.getMessageHandler().getDBErrorConnectDisabled());
            return;
        }

        if (has(playerName)) {
            Boolean val = enabled.get(playerName);

            plugin.getDatabaseHandler().saveToDatabase(playerName, val);
        }
    }

    public void saveToDatabase() {
        if (!plugin.getDatabaseHandler().isEnabled()) {
            plugin.sendConsole(plugin.getMessageHandler().getDBErrorConnectDisabled());
            return;
        }

        plugin.getDatabaseHandler().saveToDatabase(this.enabled);
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

    public Set<Player> getNearby(Player player) {
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
        Set<Player> nearbyPlayers = PlayerUtil
                .getNearbyPlayers(this, player.getLocation(), plugin.getConfigHandler().getNearBlockRadius());

        this.nearby.put(player, nearbyPlayers);
    }

    public Map<Player, Set<Player>> getNearbyPlayers() {
        return nearby;
    }

    public Map<String, Boolean> getEnabledPlayers() {
        return enabled;
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