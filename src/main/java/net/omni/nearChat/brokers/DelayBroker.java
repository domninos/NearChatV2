package net.omni.nearChat.brokers;

import net.omni.nearChat.NearChatPlugin;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.Map;

public class DelayBroker extends NCBroker {
    public DelayBroker(NearChatPlugin plugin) {
        super(plugin, BrokerType.DELAY, false);
    }

    @Override
    public void brokerRun() {
        for (Map.Entry<Player, Integer> delays : plugin.getPlayerManager().getDelays().entrySet()) {
            Player player = delays.getKey();

            if (player == null || !plugin.getPlayerManager().isEnabled(player.getName())) {
                plugin.getPlayerManager().removeDelay(player);
                return;
            }

            int val = delays.getValue();

            plugin.sendConsole(player.getName() + ": " + val);

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
        if (isRunning())
            return;

        try {
            int taskId = Bukkit.getScheduler().scheduleSyncRepeatingTask(plugin, this::brokerRun, 20L, 20L); // sync
            setTaskId(taskId);

            starting();
        } catch (Exception e) {
            cancelBrokerError(true, e);
        }
    }
}
