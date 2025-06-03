package net.omni.nearChat.brokers;

import net.omni.nearChat.NearChatPlugin;
import net.omni.nearChat.util.PlayerUtil;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitTask;

import java.util.ConcurrentModificationException;
import java.util.Map;
import java.util.Set;

public class NearbyBroker extends NCBroker {
    public NearbyBroker(NearChatPlugin plugin) {
        super(plugin, BrokerType.NEARBY, true);
    }

    @Override
    public void brokerRun() {
        try {
            for (Map.Entry<Player, Set<Player>> entry : plugin.getPlayerManager().getNearbyPlayers().entrySet()) {
                Player key = entry.getKey();
                if (key == null || !plugin.getPlayerManager().isEnabled(key.getName())) {
                    plugin.getPlayerManager().removeNearby(key);
                    continue;
                }

                int block_radius = plugin.getConfigHandler().getNearBlockRadius();

                Set<Player> nearbyPlayers = PlayerUtil.getNearbyPlayers(plugin.getPlayerManager(), key, block_radius);

                entry.setValue(nearbyPlayers);
            }
        } catch (ConcurrentModificationException e) {
            plugin.error("Something went wrong during runtime of " + getBrokerName(), e);
        }
    }

    @Override
    public boolean checkEmpty() {
        return plugin.getPlayerManager().getNearbyPlayers().isEmpty();
    }

    @Override
    public void init() {
        if (isRunning()) {
            plugin.sendConsole("running nearby");
            return;
        }

        try {
            BukkitTask task = Bukkit.getScheduler().runTaskTimer(plugin, this::brokerRun, 0L, plugin.getConfigHandler().getNearbyGetDelay());
            setTaskId(task.getTaskId());

            starting();
        } catch (Exception e) {
            cancelBrokerError(true, e);
        }
    }
}