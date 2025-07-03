package net.omni.nearChat.managers;

import net.omc.database.DatabaseAdapter;
import net.omc.database.ISQLDatabase;
import net.omc.util.Flushable;
import net.omc.util.PlayerUtil;
import net.omni.nearChat.NearChatPlugin;
import net.omni.nearChat.brokers.NCBroker;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public class PlayerManager implements Flushable {
    private final Map<String, Boolean> initial = new HashMap<>();
    private final Map<String, Boolean> enabled = new HashMap<>();
    private final Map<Player, Set<Player>> nearby = new HashMap<>();

    private final Map<Player, Integer> delay = new ConcurrentHashMap<>();

    private final Set<String> switching = new HashSet<>();


    private final NearChatPlugin plugin;

    public PlayerManager(NearChatPlugin plugin) {
        this.plugin = plugin;
    }

    // TODO: database have hashset (redis) structure
    //  * KEY,uuid,owner,value (NearChat 2.0)
    public void loadEnabled(Player player) {
        if (!plugin.getDatabaseHandler().isEnabled()) {
            plugin.sendConsole(plugin.getDBMessageHandler().getDBErrorConnectDisabled());
            return;
        }

        if (player == null) return;

        String playerName = player.getName();

        // if not in database
        if (plugin.getDatabaseHandler().isSQL()) {
            DatabaseAdapter adapter = plugin.getDatabaseHandler().getAdapter();
            ISQLDatabase sqlDb = (ISQLDatabase) adapter.getDatabase();

            sqlDb.handleExists(playerName);
        } else {
            if (!plugin.getDatabaseHandler().checkExistsDB(playerName))
                plugin.getDatabaseHandler().savePlayer(playerName, "false");

            setInitial(player.getName(), false);
        }

        // not in cache
        if (!has(playerName))
            plugin.getDatabaseHandler().setToCache(player);

        if (isEnabled(player.getName())) {
            setNearby(player);

            if (plugin.getConfigHandler().isDelay())
                setDelay(player);
        }
    }

    public void setInitial(String player, boolean value) {
        initial.put(player, value);
    }

    public boolean getInitial(String player) {
        return initial.getOrDefault(player, false);
    }

    public boolean hasChanged(String player) {
        return has(player) && getInitial(player) != enabled.getOrDefault(player, false);
    }

    public void removeInitial(String player) {
        initial.remove(player);
    }

    public Map<Player, Integer> getDelays() {
        return delay;
    }

    public void setDelay(Player player) {
        int iDelay = plugin.getConfigHandler().getDelayTime() + 1; // add 1 for accuracy (sync chatting is ran asynchronously)
        delay.put(player, iDelay);

        // make sure it is added
        if (!plugin.getBrokerManager().isDelayRunning())
            plugin.getBrokerManager().tryBroker(NCBroker.BrokerType.DELAY);
    }

    public int getDelay(Player player) {
        return delay.getOrDefault(player, 0);
    }

    public boolean hasDelay(Player player) {
        return delay.containsKey(player);
    }

    public void removeDelay(Player player) {
        if (player == null) return;

        delay.remove(player);

        if (delay.isEmpty())
            plugin.getBrokerManager().cancelBroker(NCBroker.BrokerType.DELAY);
    }

    public void save(String playerName) {
        if (!plugin.getDatabaseHandler().isEnabled()) {
            plugin.sendConsole(plugin.getDBMessageHandler().getDBErrorConnectDisabled());
            return;
        }

        if (has(playerName)) {
            Boolean val = enabled.get(playerName);
            plugin.getDatabaseHandler().savePlayer(playerName, val, true);
        }
    }

    public void saveMap(boolean async) {
        if (!plugin.getDatabaseHandler().isEnabled())
            plugin.sendConsole(plugin.getDBMessageHandler().getDBErrorConnectDisabled());
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

        initial.put(playerName, val);

        if (!plugin.getBrokerManager().isDatabaseRunning())
            plugin.getBrokerManager().tryBroker(NCBroker.BrokerType.DATABASE);
    }

    public Set<Player> getNearby(Player player) {
        return nearby.getOrDefault(player, null);
    }

    public boolean hasNearby(Player player) {
        return nearby.containsKey(player);
    }

    public void removeNearby(Player player) {
        if (player == null) return;

        if (!hasNearby(player))
            return;

        getNearby(player).clear();

        nearby.remove(player);

        if (nearby.isEmpty())
            plugin.getBrokerManager().cancelBroker(NCBroker.BrokerType.NEARBY);
    }

    public void setNearby(Player player) {
        Set<Player> nearbyPlayers = PlayerUtil
                .getNearbyPlayers(player, plugin.getConfigHandler().getNearBlockRadius(),
                        (p) -> plugin.getPlayerManager().isEnabled(p.getName()));

        this.nearby.put(player, nearbyPlayers);

        if (!plugin.getBrokerManager().isNearbyRunning())
            plugin.getBrokerManager().tryBroker(NCBroker.BrokerType.NEARBY);
    }

    public void setSwitching(String name) {
        switching.add(name);
    }

    public void removeSwitching(String name) {
        switching.remove(name);
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

    public boolean isSwitching(String name) {
        return switching.contains(name);
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
        initial.clear();

        switching.clear();
    }
}