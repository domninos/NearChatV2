package net.omni.nearChat.managers;

import net.omni.nearChat.NearChatPlugin;
import net.omni.nearChat.database.DatabaseAdapter;
import net.omni.nearChat.database.ISQLDatabase;
import net.omni.nearChat.util.Flushable;
import net.omni.nearChat.util.PlayerUtil;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class PlayerManager implements Flushable {
    private final Map<String, Boolean> enabled = new HashMap<>();
    private final Map<Player, Set<Player>> nearby = new HashMap<>();

    private final Map<Player, Integer> delay = new HashMap<>();

    private final NearChatPlugin plugin;

    public PlayerManager(NearChatPlugin plugin) {
        this.plugin = plugin;
    }

    // TODO: database have hashset (redis) structure
    //  * KEY,uuid,owner,value (NearChat 2.0)
    public void loadEnabled(Player player) {
        if (!plugin.getDatabaseHandler().isEnabled()) {
            plugin.sendConsole(plugin.getMessageHandler().getDBErrorConnectDisabled());
            return;
        }

        String playerName = player.getName();

        // if not in database
        if (plugin.getDatabaseHandler().isSQL()) {
            DatabaseAdapter adapter = plugin.getDatabaseHandler().getAdapter();
            ISQLDatabase sqlDb = (ISQLDatabase) adapter.getDatabase();

            sqlDb.handleExists(playerName);
        } else {
            if (!plugin.getDatabaseHandler().checkExistsDB(playerName))
                plugin.getDatabaseHandler().savePlayer(playerName, "false");
        }

        // not in cache
        if (!has(playerName))
            plugin.getDatabaseHandler().setToCache(player);

        if (isEnabled(player.getName()))
            setNearby(player);

        int iDelay = plugin.getConfigHandler().getDelay();

        delay.put(player, iDelay);

        //  TODO DelayBroker();
        /*

        Bukkit.getScheduler().runTaskTimer(plugin, () -> {
            if (!hasDelay(player)) {
                cancelTask()
            }
            delay.replace(player.getName(), getDelay(player.getName()) - 1);
        }, 0, 20L);

         */
    }

    public Map<Player, Integer> getDelays() {
        return delay;
    }

    public int getDelay(Player player) {
        return delay.getOrDefault(player, 0);
    }

    public boolean hasDelay(Player player) {
        return delay.containsKey(player);
    }

    public void removeDelay(Player player) {
        delay.remove(player);
    }

    public void save(String playerName) {
        if (!plugin.getDatabaseHandler().isEnabled()) {
            plugin.sendConsole(plugin.getMessageHandler().getDBErrorConnectDisabled());
            return;
        }

        if (has(playerName)) {
            Boolean val = enabled.get(playerName);
            plugin.getDatabaseHandler().savePlayer(playerName, val, true);
        }
    }

    public void saveMap(boolean async) {
        if (!plugin.getDatabaseHandler().isEnabled())
            plugin.sendConsole(plugin.getMessageHandler().getDBErrorConnectDisabled());
        else
            plugin.getDatabaseHandler().saveMap(this.enabled, async);
    }

    public void toggle(Player player) {
        toggle(player, true);
    }

    public void toggle(Player player, boolean sendLog) {
        String name = player.getName();

        if (has(name))
            this.enabled.replace(name, !isEnabled(name));
        else
            this.enabled.put(name, true);

        if (isEnabled(name)) {
            setNearby(player);

            if (sendLog) plugin.sendMessage(player, plugin.getMessageHandler().getNearChatEnabled());
        } else {
            removeNearby(player);

            if (sendLog) plugin.sendMessage(player, plugin.getMessageHandler().getNearChatDisabled());
        }
    }

    public void set(String playerName, boolean val) {
        this.enabled.put(playerName, val);
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

    @Override
    public void flush() {
        enabled.clear();

        if (!nearby.isEmpty())
            nearby.forEach((name, list) -> list.clear());

        nearby.clear();
        delay.clear();
    }
}