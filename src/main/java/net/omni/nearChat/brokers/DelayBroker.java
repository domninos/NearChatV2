package net.omni.nearChat.brokers;

import net.omni.nearChat.NearChatPlugin;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitTask;

import java.util.Map;

public class DelayBroker extends NCBroker {
    public DelayBroker(NearChatPlugin plugin) {
        super(plugin, BrokerType.DELAY);
    }

    @Override
    public void brokerRun() {
        for (Map.Entry<Player, Integer> delays : plugin.getPlayerManager().getDelays().entrySet()) {
            Player player = delays.getKey();

            if (player != null && !plugin.getPlayerManager().isEnabled(player.getName())) {
                plugin.getPlayerManager().removeDelay(player);
                return;
            }

            int val = delays.getValue();

            if (val == 0)
                plugin.getPlayerManager().removeDelay(player);
            else
                delays.setValue(val - 1);
        }
    }

    @Override
    public boolean checkEmpty() {
        return plugin.getPlayerManager().getDelays().isEmpty();
    }

    @Override
    public void init() {
        try {
            BukkitTask task = Bukkit.getScheduler().runTaskTimer(plugin, this::brokerRun, 0L, 20L); // sync
            setTaskId(task.getTaskId());

            starting();
        } catch (Exception e) {
            cancelBrokerError(true, e);
        }
    }
}
