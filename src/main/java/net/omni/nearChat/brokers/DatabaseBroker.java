package net.omni.nearChat.brokers;

import net.omni.nearChat.NearChatPlugin;
import org.bukkit.Bukkit;
import org.bukkit.scheduler.BukkitTask;

public class DatabaseBroker extends NCBroker {
    public DatabaseBroker(NearChatPlugin plugin) {
        super(plugin, BrokerType.DATABASE, true);
    }

    @Override
    public void brokerRun() {
        if (isRestartable() && !isRunning()) {
            cancelBroker(true);
            return;
        }

        plugin.sendConsole(plugin.getMessageHandler().getDBTrySave());

        if (plugin.getDatabaseHandler().isEnabled())
            plugin.getPlayerManager().saveMap(true);
        else {
            plugin.sendConsole(plugin.getMessageHandler().getBrokerStop(getBrokerName()));
            cancelBroker(true);
        }
    }

    @Override
    public boolean checkEmpty() {
        // there are no players that are using nearchat currently.
        return plugin.getPlayerManager().getEnabledPlayers().isEmpty();
    }

    @Override
    public void init() {
        if (isRestartable() && isRunning())
            cancelBroker();

        try {
            BukkitTask task = Bukkit.getScheduler().runTaskTimerAsynchronously(plugin, this::brokerRun,
                    20L, plugin.getConfigHandler().getDatabaseSaveDelay()); // needs to be async
            setTaskId(task.getTaskId());

            starting();
        } catch (Exception e) {
            cancelBrokerError(true, e);
        }
    }
}
