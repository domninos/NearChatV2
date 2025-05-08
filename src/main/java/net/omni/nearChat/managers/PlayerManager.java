package net.omni.nearChat.managers;

import net.omni.nearChat.NearChatPlugin;
import net.omni.nearChat.util.PlayerUtil;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class PlayerManager {
    private final Map<String, Boolean> enabled = new HashMap<>();
    private final Map<Player, List<Player>> nearby = new HashMap<>();

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
        if (!plugin.getDatabaseHandler().checkExists(playerName))
            plugin.getDatabaseHandler().set(playerName, "false");

        // not in cache
        if (!has(playerName))
            plugin.getDatabaseHandler().putToCache(player);

        if (isEnabled(player.getName()))
            setNearby(player);
    }

    public void saveToDatabase(Player player) {
        if (!plugin.getDatabaseHandler().isEnabled()) {
            plugin.sendConsole(plugin.getMessageHandler().getDBErrorConnectDisabled());
            return;
        }

        if (has(player.getName())) {
            Boolean val = enabled.get(player.getName());

            plugin.getDatabaseHandler().saveToDatabase(player, val);
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