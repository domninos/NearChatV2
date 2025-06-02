package net.omni.nearChat.brokers;

import net.omni.nearChat.NearChatPlugin;
import net.omni.nearChat.util.PlayerUtil;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitTask;

import java.util.Map;
import java.util.Set;

public class NearbyBroker extends NCBroker {
    public NearbyBroker(NearChatPlugin plugin) {
        super(plugin, BrokerType.NEARBY);
    }

    @Override
    public void brokerRun() {
        for (Map.Entry<Player, Set<Player>> entry : plugin.getPlayerManager().getNearbyPlayers().entrySet()) {
            Player key = entry.getKey();
            if (key == null || !plugin.getPlayerManager().isEnabled(key.getName())) continue;

            int block_radius = plugin.getConfigHandler().getNearBlockRadius();

            Set<Player> nearbyPlayers = PlayerUtil.getNearbyPlayers(plugin.getPlayerManager(), key, block_radius);

            entry.setValue(nearbyPlayers);
        }
    }

    @Override
    public boolean checkEmpty() {
        return plugin.getPlayerManager().getNearbyPlayers().isEmpty();
    }

    @Override
    public void init() {
        try {
            BukkitTask task = Bukkit.getScheduler().runTaskTimer(plugin, this::brokerRun, 0L, plugin.getConfigHandler().getNearbyGetDelay());
            setTaskId(task.getTaskId());

            starting();
        } catch (Exception e) {
            cancelBrokerError(true, e);
        }
    }
}